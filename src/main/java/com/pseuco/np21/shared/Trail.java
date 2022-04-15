package com.pseuco.np21.shared;

import java.util.Comparator;
import java.util.NoSuchElementException;

/**
 * Representation of a basic trail.
 * <p>
 * Manages the clearings it connects and has a reference to the reverse trail.
 *
 * @param <C> the type of clearings the implementation uses
 * @param <T> the type of trails the implementation uses
 */
public class Trail<C extends Clearing<C, T>, T extends Trail<C, T>> extends Position {
    /**
     * Abstraction for representing pheromones, including infinite and absent ones.
     */
    public static class Pheromone {
        /**
         * The infinite pheromone (MaP). You may not assume its internal representation.
         */
        public static final Pheromone INFINITE = new Pheromone(-1);
        /**
         * The absent pheromone (NaP). You may not assume its internal representation.
         */
        public static final Pheromone NOT_A_PHEROMONE = new Pheromone(-2);

        private final int value;

        private Pheromone(final int value) {
            this.value = value;
        }

        /**
         * Constructs a pheromone given an integer value.
         *
         * @param value to construct the pheromone for
         * @return A new pheromone for a non-negative input or {@link #INFINITE} for {@code -1} or
         * {@link #NOT_A_PHEROMONE} for {@code -2}
         * @throws IllegalArgumentException if {@code value} is smaller than {@code -2}
         */
        public static Pheromone get(final int value) {
            if (value < -2)
                throw new IllegalArgumentException("Cannot construct pheromone for value " + value + "!");
            else if (value == -1)
                return INFINITE;
            else if (value == -2)
                return NOT_A_PHEROMONE;
            else
                return new Pheromone(value);
        }

        /**
         * Use this to determine whether or not the pheromone is infinite.
         *
         * @return {@code true} iff called on the infinite pheromone
         */
        public boolean isInfinite() {
            return this == INFINITE;
        }

        /**
         * Use this to determine whether or not the pheromone is present.
         *
         * @return {@code false} iff called on the absent pheromone
         */
        public boolean isAPheromone() {
            return this != NOT_A_PHEROMONE;
        }

        /**
         * Integer value of the pheromone. The infinite and absent pheromone do not have an integer
         * value!
         *
         * @return The integer value of the pheromone
         * @throws NoSuchElementException if called on the infinite pheromone
         */
        public int value() {
            if (isInfinite() || !isAPheromone())
                throw new NoSuchElementException();
            return value;
        }

        @Override
        public boolean equals(final Object other) {
            if (!(other instanceof final Pheromone pheromone)) {
                return false;
            }
            return this.value == pheromone.value;
        }

        @Override
        public String toString() {
            return isAPheromone() ? (isInfinite() ? "∞" : "" + value) : "NaP";
        }
    }

    /**
     * Comparator for pheromones.
     */
    public static class PheromoneComparator implements Comparator<Pheromone> {
        private PheromoneComparator() {
        }

        @Override
        public int compare(Pheromone o1, Pheromone o2) {
            if (o1 == o2)
                return 0;
            else if (o1.isInfinite())
                return 1;
            else if (o2.isInfinite())
                return -1;
            else if (!o1.isAPheromone())
                return 1;
            else if (!o2.isAPheromone())
                return -1;
            else
                return o1.value - o2.value;
        }
    }

    /**
     * The pheromone comparator.
     */
    public static final PheromoneComparator PHEROMONE_COMPARATOR = new PheromoneComparator();

    /**
     * Clearing it leads away from.
     */
    protected final C from;
    /**
     * Clearing it leads to.
     */
    protected final C to;
    /**
     * Trail in the opposite direction.
     */
    protected final T reverse;

    /**
     * Constructor interface to get the reverse trail with the appropriate static type.
     *
     * @param <C> the type of clearings the implementation uses
     * @param <T> the type of trails the implementation uses
     */
    @FunctionalInterface
    protected interface ReverseConstructor<C extends Clearing<C, T>, T extends Trail<C, T>> {
        /**
         * Construct a new trail in the opposite direction.
         *
         * @param reverse original trail
         * @return trail in the opposite direction
         */
        T construct(Trail<?, ?> reverse);
    }

    /**
     * Constructs a new (reversed) trail from a given trail.
     *
     * @param reverse trail in the opposite direction
     */
    protected Trail(final T reverse) {
        super(Capacity.get(1));

        this.from = reverse.to;
        this.to = reverse.from;
        this.reverse = reverse;
    }

    /**
     * Constructs a new trail given the connected clearings.
     *
     * @param from        clearing the trail leads away from
     * @param to          clearing the trail leads to
     * @param constructor reverse trail constructor
     */
    protected Trail(final C from, final C to, final ReverseConstructor<C, T> constructor) {
        super(Capacity.get(1));

        this.from = from;
        this.to = to;

        this.reverse = constructor.construct(this);
    }

    /**
     * Get the clearing the trail leads away from.
     *
     * @return clearing the trail leads away from
     */
    public C from() {
        return from;
    }

    /**
     * Get the clearing the trail leads to.
     *
     * @return clearing the trail leads to
     */
    public C to() {
        return to;
    }

    /**
     * Get the trail in the opposite direction.
     *
     * @return trail in the opposite direction
     */
    public T reverse() {
        return reverse;
    }

    @Override
    public String toString() {
        return "Trail{" +
                "from='" + from.name() + '\'' +
                ", to='" + to.name() + '\'' +
                '}';
    }
}
