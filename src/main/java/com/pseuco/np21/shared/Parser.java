package com.pseuco.np21.shared;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Pattern;

/**
 * Parser for map and ant files.
 * <p>
 * Map file structure:
 * <pre>
 * {@code
 * exampleName
 * Anthill;-1;0;(0,0)
 * InBetween;2;0;(1,0)
 * Food;-1;2;(1,1)
 * Anthill-InBetween
 * InBetween-Food
 * }
 * </pre>
 * <p>
 * Ant file structure:
 * <pre>
 * {@code
 * Anthony;2;1000
 * }
 * </pre>
 * <p>
 * This will result in a world with three clearings ('Anthill', 'InBetween' and 'Food'),
 * two trails ('Anthill' - 'InBetween' and 'InBetween' - 'Food') and one ant ('Anthony').
 * The first clearing will be considered the anthill.
 * For clearing declarations the structure is always: 'name;capacity;food;(x,y)', where a capacity of -1 indicates ∞.
 * For ant declarations the structure is always: 'name;impatience;disguise'
 */
public class Parser {
    /**
     * Default constructor is not needed and thus inaccessible.
     */
    private Parser() {
    }

    /**
     * Regex for matching names.
     */
    private static final String REGEX_NAME = "\\w+";
    /**
     * Regex for matching integers.
     */
    private static final String REGEX_NUMBER = "[-+]?\\d+";

    /**
     * Parses a new {@link World} given the map, ants and a factory.
     *
     * @param mapFile  contents of a map file
     * @param antsFile contents of a ants file
     * @param factory  used to construct the world
     * @return name of the constructed world
     */
    public static String parse(final String mapFile, final String antsFile, final Factory<?, ?> factory) {
        final var name = parseMap(mapFile, factory);
        parseAnts(antsFile, factory);
        return name;
    }

    private static <C extends Clearing<C, T>, T extends Trail<C, T>>
    String parseMap(final String mapFile, final Factory<C, T> factory) {
        final var lines = mapFile.split("\\r?\\n");
        if (lines.length < 2) {
            throw new IllegalArgumentException("Map files must contain at least 2 lines!");
        }

        final var name = lines[0];
        if (!name.matches(String.format("^%s$", REGEX_NAME))) {
            throw new IllegalArgumentException(
                    String.format("Map file must start with a name! \"%s\" is not a valid name!", name));
        }

        int i = 1;
        final var clearings = new HashMap<String, C>();
        Optional<C> clearing = parseClearing(lines[i], factory);
        factory.setAnthill(clearing.orElseThrow(() -> new IllegalArgumentException("Map file needs at least one clearing!")));
        while (i + 1 < lines.length && clearing.isPresent()) {
            final var c = clearing.get();

            if (clearings.containsKey(c.name())) {
                throw new IllegalArgumentException("Clearing names must be unique!");
            }

            clearings.put(c.name(), c);

            clearing = parseClearing(lines[++i], factory);
        }

        while (i < lines.length && parseTrail(lines[i], clearings, factory)) {
            i++;
        }

        return name;
    }

    private static <C extends Clearing<C, T>, T extends Trail<C, T>>
    Optional<C> parseClearing(final String line, final Factory<C, T> factory) {
        final String GROUP_NAME = "name";
        final String GROUP_CAPACITY = "capacity";
        final String GROUP_FOOD = "food";
        final String GROUP_X = "x";
        final String GROUP_Y = "y";

        final var pattern = Pattern.compile(
                String.format("^(?<%s>%s);(?<%s>%s);(?<%s>%s);\\((?<%s>%s),(?<%s>%s)\\)$",
                        GROUP_NAME, REGEX_NAME,
                        GROUP_CAPACITY, REGEX_NUMBER,
                        GROUP_FOOD, REGEX_NUMBER,
                        GROUP_X, REGEX_NUMBER,
                        GROUP_Y, REGEX_NUMBER)
        );
        final var matcher = pattern.matcher(line);
        if (!matcher.matches()) {
            return Optional.empty();
        }

        final String name = matcher.group(GROUP_NAME);
        final int capacity = Integer.parseInt(matcher.group(GROUP_CAPACITY));
        final int food = Integer.parseInt(matcher.group(GROUP_FOOD));
        final int x = Integer.parseInt(matcher.group(GROUP_X));
        final int y = Integer.parseInt(matcher.group(GROUP_Y));

        final var clearing = factory.createClearing(name, food, Position.Capacity.get(capacity));

        return Optional.of(clearing);
    }

    private static <C extends Clearing<C, T>, T extends Trail<C, T>>
    boolean parseTrail(final String line, final Map<String, C> clearings, final Factory<C, T> factory) {
        final String GROUP_NAME_A = "nameA";
        final String GROUP_NAME_B = "nameB";

        final var pattern = Pattern.compile(
                String.format("^(?<%s>%s)-(?<%s>%s)$",
                        GROUP_NAME_A, REGEX_NAME,
                        GROUP_NAME_B, REGEX_NAME));
        final var matcher = pattern.matcher(line);
        if (!matcher.matches())
            return false;

        final String nameA = matcher.group(GROUP_NAME_A);
        final String nameB = matcher.group(GROUP_NAME_B);

        final var clearingA = clearings.get(nameA);
        final var clearingB = clearings.get(nameB);

        if (clearingA == null || clearingB == null)
            throw new IllegalArgumentException(
                    String.format("Could not find clearing \"%s\" or \"%s\"!", nameA, nameB));

        factory.createTrail(clearingA, clearingB);

        return true;
    }

    private static void parseAnts(final String antsFile, final Factory<?, ?> factory) {
        final var lines = antsFile.split(System.getProperty("line.separator"));
        if (lines.length < 1) {
            throw new IllegalArgumentException("Ants files must contain at least 1 line!");
        }

        Arrays.stream(lines).forEach(l -> parseAnt(l, factory));
    }

    private static void parseAnt(final String line, final Factory<?, ?> factory) {
        final String GROUP_NAME = "name";
        final String GROUP_IMPATIENCE = "impatience";
        final String GROUP_DISGUISE = "disguise";

        final var pattern = Pattern.compile(
                String.format("^(?<%s>%s);(?<%s>%s);(?<%s>%s)$",
                        GROUP_NAME, REGEX_NAME,
                        GROUP_IMPATIENCE, REGEX_NUMBER,
                        GROUP_DISGUISE, REGEX_NUMBER)
        );
        final var matcher = pattern.matcher(line);
        if (!matcher.matches()) {
            return;
        }

        final String name = matcher.group(GROUP_NAME);
        final int impatience = Integer.parseInt(matcher.group(GROUP_IMPATIENCE));
        final int disguise = Integer.parseInt(matcher.group(GROUP_DISGUISE));

        factory.createAnt(name, impatience, disguise);
    }
}
