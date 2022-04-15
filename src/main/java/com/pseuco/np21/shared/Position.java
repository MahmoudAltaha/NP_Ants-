package com.pseuco.np21.shared;

import java.util.NoSuchElementException;

/**
 * Representation of a position.
 * <p>
 * This is an abstraction over clearings and trails.
 * Both clearings and trails have a capacity, which is modeled here.
 */
public class Position extends Entity {
    /**
     * Abstraction for representing capacities, including infinite ones.
     */
    public static class Capacity {
        /**
         * The infinite capacity. You may not assume its internal representation.
         */
        public static final Capacity INFINITE = new Capacity(-1);

        private final int value;

        private Capacity(final int value) {
            this.value = value;
        }

        /**
         * Constructs a capacity given an integer value.
         *
         * @param value to construct the capacity for
         * @return A new capacity for a non-negative input or {@link #INFINITE} for {@code -1}
         * @throws IllegalArgumentException if {@code value} is smaller than {@code -1}
         */
        public static Capacity get(final int value) {
            if (value < -1)
                throw new IllegalArgumentException("Cannot construct capacity for value " + value + "!");
            else if (value == -1)
                return INFINITE;
            else
                return new Capacity(value);
        }

        /**
         * Use this to distinguish between the infinite capacity and finite ones.
         *
         * @return {@code true} iff called on the infinite capacity
         */
        public boolean isInfinite() {
            return this == INFINITE;
        }

        /**
         * Integer value of the capacity. The infinite capacity does not have an integer
         * value!
         *
         * @return The integer value of the capacity
         * @throws NoSuchElementException if called on the infinite capacity
         */
        public int value() {
            if (isInfinite())
                throw new NoSuchElementException();
            return value;
        }

        @Override
        public String toString() {
            return isInfinite() ? "∞" : "" + value;
        }
    }

    /**
     * The capacity of this position.
     */
    protected final Capacity capacity;

    /**
     * Constructs a new position given a capacity.
     *
     * @param capacity of the position
     */
    protected Position(final Capacity capacity) {
        this.capacity = capacity;
    }

    @Override
    public String toString() {
        return "Position{" +
                "capacity=" + capacity +
                '}';
    }
}
