package com.pseuco.np21.shared;

/**
 * Representation of a basic ant without any behavior.
 * <p>
 * It manages the essential ant properties and implements a custom name-based equality.
 * <p>
 * Except for the ant's name, its impatience and disguise are stored here.
 * <p>
 * The ant's impatience is a measure of the ant's willingness to explore new parts of the world.
 * Food pheromone levels exceeding the ant's impatience (i.e. pheromone level > impatience) will cause it to explore the world.
 * The ant's disguise is the time (measured in milliseconds) it has to traverse a trail.
 * If the ant takes longer than its disguise to traverse a trail, it will be found by a predator which causes the ant to die.
 * Any food the ant might be carrying will be lost.
 */
public class Ant extends Entity {
    /**
     * Name of the ant.
     */
    protected final String name;
    /**
     * Impatience of the ant.
     */
    protected final int impatience;
    /**
     * Disguise of the ant.
     */
    protected final int disguise;

    /**
     * Constructs a new ant.
     *
     * @param ant template from which to construct
     */
    protected Ant(final Ant ant) {
        this.name = ant.name;
        this.impatience = ant.impatience;
        this.disguise = ant.disguise;
    }

    /**
     * Constructs a new ant.
     *
     * @param name       of the ant
     * @param impatience of the ant
     * @param disguise   of the ant
     */
    public Ant(final String name, final int impatience, final int disguise) {
        this.name = name;
        this.impatience = impatience;
        this.disguise = disguise;
    }

    /**
     * Get the name of the ant.
     *
     * @return name of the ant
     */
    public String name() {
        return name;
    }

    /**
     * Get the impatience of the ant.
     *
     * @return impatience of the ant
     */
    public int impatience() {
        return impatience;
    }

    /**
     * Get the disguise of the ant.
     *
     * @return disguise of the ant
     */
    public int disguise() {
        return disguise;
    }

    @Override
    public boolean equals(final Object other) {
        if (!(other instanceof final Ant ant)) {
            return false;
        }
        return this.name.equals(ant.name);
    }

    @Override
    public String toString() {
        return "Ant{" +
                "name='" + name + '\'' +
                ", impatience=" + impatience +
                ", disguise=" + disguise +
                '}';
    }
}
