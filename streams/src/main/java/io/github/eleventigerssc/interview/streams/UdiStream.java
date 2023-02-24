package io.github.eleventigerssc.interview.streams;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Iterator;
import java.util.NoSuchElementException;

public class UdiStream<T> implements Stream<T> {

    Iterable<T> mainIterable =null;

    public UdiStream(Iterable<T> iterable) {
        this.mainIterable = iterable;
    }
    @Override
    public Iterator<T> iterator() {
        return mainIterable.iterator();
    }

    @Override
    public void forEach(Consumer<? super T> action) {
        Iterator<T> iter =  mainIterable.iterator();
        while (iter.hasNext()) {
            action.accept(iter.next());
        }
    }

    @Override
    public <R> Stream<R> map(Function<? super T, ? extends R> mapper) {
        Iterable<R> iterable = new Iterable<R>() {
            @NotNull
            @Override
            public Iterator<R> iterator() {
                return new Iterator<R>() {
                    final Iterator<T> mainIterator = mainIterable.iterator();

                    @Override
                    public boolean hasNext() {
                        return mainIterator.hasNext();
                    }

                    @Override
                    public R next() {
                        return mapper.call(mainIterator.next());
                    }
                };
            }
        };

        return new UdiStream<R>(iterable);
    }

    @Override
    public <R> Stream<R> flatMap(Function<? super T, ? extends Stream<? extends R>> mapper) {

        Iterable<R> iterable = new Iterable<R>() {

            @NotNull
            @Override
            public Iterator<R> iterator() {
                return new Iterator<R>() {
                    final Iterator<T> iterMain =  mainIterable.iterator();

                    // Iterator for the sub-elements of type R
                    @Nullable
                    Iterator<? extends R> currIter = null;

                    @Override
                    public boolean hasNext() {
                        if (currIter != null && currIter.hasNext()) {
                            return true;
                        }

                        currIter = getNextIterator();
                        return currIter != null && currIter.hasNext();
                    }

                    @Override
                    public R next() {
                        if(!hasNext()) {
                            throw new NoSuchElementException();
                        }

                        // currIter cannot be null if hasNext() is true
                        return currIter.next();
                    }

                    // Retrieving the next non-empty sub-stream.
                    @Nullable
                    private Iterator<? extends R> getNextIterator() {
                        Iterator<? extends R> mappedIter = null;
                        while (iterMain.hasNext()) {
                            T item = iterMain.next();
                            Stream<? extends R> stream = mapper.call(item);
                            mappedIter = stream.iterator();
                            if (mappedIter.hasNext()) {
                                break;
                            }
                        }
                        return mappedIter;

                    }
                };
            }
        };

        return new UdiStream<>(iterable);
    }

    @Override
    public Stream<T> filter(Predicate<? super T> predicate) {
        Iterable<T> totalIter = new Iterable<>() {

            @NotNull
            @Override
            public Iterator<T> iterator() {
                return new Iterator<>() {

                    // Caching the next valid item. Needed to properly implement hasNext()
                    T nextItem = null;

                    final Iterator<T> iterMain =  mainIterable.iterator();

                    @Override
                    public boolean hasNext() {
                        if (nextItem != null) {
                            return true;
                        }
                        while (iterMain.hasNext()) {
                            T nextInMain = iterMain.next();
                            if (predicate.test(nextInMain)) {
                                nextItem = nextInMain;
                                return true;
                            }
                        }
                        return false;
                    }

                    @Override
                    public T next() {
                        if (hasNext()) {
                            // hasNext() will initialize nextItem to the next valid item

                            T next = nextItem;
                            nextItem = null;
                            return next;
                        } else {
                            throw new NoSuchElementException();
                        }
                    }
                };
            }
        };

        return new UdiStream<>(totalIter);
    }
}
