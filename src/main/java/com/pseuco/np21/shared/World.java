package com.pseuco.np21.shared;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Representation of the world.
 * <p>
 * Manages all clearings, trails and ants.
 *
 * @param <C> the type of clearings the implementation uses
 * @param <T> the type of trails the implementation uses
 */
public class World<C extends Clearing<C, T>, T extends Trail<C, T>> {
    private final String name;
    private final List<C> clearings;
    private final C anthill;
    private final int totalFood;
    private final int foodThreshold;
    private final List<Ant> ants;
    private final AtomicInteger foodCollected;

    /**
     * Constructs a new world given a name, clearings, ants and a food threshold.
     *
     * @param name          of the world
     * @param clearings     in this world
     * @param anthill       of this world
     * @param food          clearings that contain food
     * @param foodThreshold the ants have to reach
     * @param ants          in this world
     */
    public World(final String name, final List<C> clearings, final C anthill, final List<C> food, final int foodThreshold, final List<Ant> ants) {
        this.name = name;
        this.clearings = clearings;
        this.anthill = anthill;
        this.ants = ants;
        this.foodCollected = new AtomicInteger();

        this.totalFood = food.stream()
                .map(Clearing::initialFood)
                .reduce(0, Integer::sum);

        this.foodThreshold = foodThreshold < 0 ? this.totalFood : foodThreshold;

        if (this.totalFood < this.foodThreshold) {
            throw new IllegalArgumentException("foodThreshold must not be larger than the initial food capacity!");
        }
    }

    /**
     * Get the name of this world.
     *
     * @return name of this world
     */
    public String name() {
        return name;
    }

    /**
     * Get the anthill of this world.
     *
     * @return anthill of this world
     */
    public C anthill() {
        return anthill;
    }

    /**
     * Get a list of all clearings in this world.
     *
     * @return list of all clearings
     */
    public List<C> clearings() {
        return Collections.unmodifiableList(clearings);
    }

    /**
     * Get a list of all ants in this world.
     *
     * @return list of all ants
     */
    public List<Ant> ants() {
        return Collections.unmodifiableList(ants);
    }

    /**
     * Call this whenever an ant successfully returned food to the anthill.
     */
    public void foodCollected() {
        foodCollected.incrementAndGet();
    }

    /**
     * Check whether there is food left to collect.
     *
     * @return {@code true} iff there is food left to collect
     */
    public boolean isFoodLeft() {
        return foodCollected.get() < foodThreshold;
    }

    /**
     * Get the total amount of food in this world.
     *
     * @return total amount of food in this world
     */
    public int totalFood() {
        return totalFood;
    }
}
