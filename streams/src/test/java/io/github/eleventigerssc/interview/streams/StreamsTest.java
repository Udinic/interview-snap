package io.github.eleventigerssc.interview.streams;

import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.regex.Pattern;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.*;

public class StreamsTest {

    private static final String[] TEST_STRINGS = {"Hello", ",", "World", "!"};
    private static final String[] TEST_STRINGS2 = {"udinic", "", "", "snap"};
    private static final Character[] TEST_CHARACTERS = {'H', 'e', 'l', 'l', 'o', ',', 'W', 'o', 'r', 'l', 'd', '!'};

    private static final Function<String, String> UPPERCASE = String::toUpperCase;
    private static final Function<String, Integer> HASHCODE = String::hashCode;

    private static final Function<String, Stream<Character>> CHARACTERS = s -> {
        List<Character> characters = new ArrayList<>();
        char[] chars = s.toCharArray();
        for (char aChar : chars) {
            characters.add(aChar);
        }
        return Streams.from(characters);
    };

    private static final Predicate<Character> ONLY_LETTERS = new Predicate<Character>() {

        private final Pattern pattern = Pattern.compile("[a-zA-Z]+\\.?");

        @Override
        public boolean test(Character character) {
            return pattern.matcher(character.toString()).matches();
        }
    };

    private static final Predicate<Character> FAIL_ALL = new Predicate<Character>() {

        @Override
        public boolean test(Character character) {
            return false;
        }
    };

    private final List<String> strings = spy(new ArrayList<>(Arrays.asList(TEST_STRINGS)));
    private final List<String> strings2 = spy(new ArrayList<>(Arrays.asList(TEST_STRINGS2)));
    private final List<Character> characters = spy(new ArrayList<>(Arrays.asList(TEST_CHARACTERS)));

    private final List<Character> empty = spy(new ArrayList<>(Arrays.asList()));


    @Test
    public void from_newStream_doesNotManipulateIterable() {
        Stream<String> stream = Streams.from(strings);

        assertNotNull(stream);
        verifyZeroInteractions(strings);
    }

    @Test
    public void forEach_iterateOverOriginal() {
        Stream<String> stream = Streams.from(strings);

        List<String> expected = Arrays.asList(TEST_STRINGS);
        List<String> actual = new ArrayList<>();
        stream.forEach(actual::add);

        assertEquals(expected, actual);
    }

    @Test
    public void map_usesSuppliedMapper() {
        Stream<String> upperCaseStrings = Streams.from(strings).map(UPPERCASE);
        verifyZeroInteractions(strings);

        List<String> expected = Arrays.asList("HELLO", ",", "WORLD", "!");
        List<String> actual = new ArrayList<>();
        upperCaseStrings.forEach(actual::add);

        assertEquals(expected, actual);
    }

    @Test
    public void map_chained_usesSuppliedMappers() {
        Stream<Integer> hashCodes = Streams.from(strings).map(UPPERCASE).map(HASHCODE);
        verifyZeroInteractions(strings);

        List<Integer> expected = Arrays.asList(68624562, 44, 82781042, 33);
        List<Integer> actual = new ArrayList<>();

        Iterator<Integer> iterator = hashCodes.iterator();
        while (iterator.hasNext()) {
            actual.add(iterator.next());
        }

        assertEquals(expected, actual);
    }

    @Test
    public void flatMap_useSuppliedMapper() {
        Stream<Character> characterStream = Streams.from(strings).flatMap(CHARACTERS);
        verifyZeroInteractions(strings);

        List<Character> expected = Arrays.asList('H', 'e', 'l', 'l', 'o', ',', 'W', 'o', 'r', 'l', 'd', '!');
        List<Character> actual = new ArrayList<>();
        characterStream.forEach(actual::add);

        assertEquals(expected, actual);
    }
    @Test
    public void flatMap_useSuppliedMapper_emptyWords() {
        Stream<Character> characterStream = Streams.from(strings2).flatMap(CHARACTERS);
        verifyZeroInteractions(strings2);

        List<Character> expected = Arrays.asList('u', 'd', 'i', 'n', 'i', 'c', 's', 'n', 'a', 'p');
        List<Character> actual = new ArrayList<>();
        characterStream.forEach(actual::add);

        assertEquals(expected, actual);
    }

    @Test
    public void flatMap_useSuppliedMapper_emptySource() {
        Stream<Character> characterStream = Streams.from(Arrays.<String>asList()).flatMap(CHARACTERS);

        List<Character> expected = Arrays.asList();
        List<Character> actual = new ArrayList<>();
        characterStream.forEach(actual::add);

        assertEquals(expected, actual);
    }

    @Test
    public void filter_useSuppliedPredicate() {
        Stream<Character> characterStream = Streams.from(characters).filter(ONLY_LETTERS);
        verifyZeroInteractions(strings);

        List<Character> expected = Arrays.asList('H', 'e', 'l', 'l', 'o', 'W', 'o', 'r', 'l', 'd');
        List<Character> actual = new ArrayList<>();
        characterStream.forEach(actual::add);

        assertEquals(expected, actual);
    }

    @Test
    public void filter_useSuppliedPredicate_emptySource() {
        Stream<Character> characterStream = Streams.from(empty).filter(ONLY_LETTERS);

        List<Character> expected = Arrays.asList();
        List<Character> actual = new ArrayList<>();
        characterStream.forEach(actual::add);

        assertEquals(expected, actual);
    }

    @Test
    public void filter_useSuppliedPredicate_allFiltered() {
        Stream<Character> characterStream = Streams.from(characters).filter(FAIL_ALL);

        List<Character> expected = Arrays.asList();
        List<Character> actual = new ArrayList<>();
        characterStream.forEach(actual::add);

        assertEquals(expected, actual);
    }
}
