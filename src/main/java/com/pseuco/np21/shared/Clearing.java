package com.pseuco.np21.shared;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Representation of a basic clearing.
 * <p>
 * Manages the clearing's name, capacity, food it starts with and any trails leading from/to it.
 *
 * @param <C> the type of clearings the implementation uses
 * @param <T> the type of trails the implementation uses
 */
public class Clearing<C extends Clearing<C, T>, T extends Trail<C, T>> extends Position {
    /**
     * Name of the clearing.
     */
    protected final String name;
    /**
     * Trails the clearing is connected to.
     */
    protected final List<T> trails;
    /**
     * Food amount the clearing starts with.
     */
    protected final int initialFood;

    /**
     * Constructs a new clearing.
     *
     * @param name     of the clearing
     * @param food     the clearing starts with
     * @param capacity the clearing has access to
     */
    protected Clearing(final String name, final int food, final Capacity capacity) {
        super(capacity);
        this.name = name;
        this.trails = new ArrayList<>();
        this.initialFood = food;
    }

    /**
     * Get the name of the clearing.
     *
     * @return name of the clearing
     */
    public String name() {
        return name;
    }

    /**
     * Get the amount of food the clearing started with.
     *
     * @return amount of food the clearing started with
     */
    public int initialFood() {
        return initialFood;
    }

    /**
     * Add a new trail to the clearing.
     * <p>
     * This will cause the reverse trail to be added to the trails target.
     *
     * @param trail connecting to the clearing
     */
    public void addTrail(final T trail) {
        trails.add(trail);
        trail.to().trails.add(trail.reverse());
    }

    /**
     * Get the trails the clearing is connected to.
     *
     * @return trails the clearing is connected to
     */
    public List<T> connectsTo() {
        return Collections.unmodifiableList(trails);
    }

    @Override
    public String toString() {
        return "Clearing{" +
                "name='" + name + '\'' +
                ", trails=" + trails.size() +
                ", initialFood=" + initialFood +
                ", capacity=" + capacity +
                '}';
    }
}
