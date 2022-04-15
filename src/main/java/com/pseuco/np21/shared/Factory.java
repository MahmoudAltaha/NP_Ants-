package com.pseuco.np21.shared;

/**
 * Interface the parser and tests need to construct the {@link World}.
 *
 * @param <C> the type of clearings the implementation uses
 * @param <T> the type of trails the implementation uses
 */
public interface Factory<C extends Clearing<C, T>, T extends Trail<C, T>> {
    /**
     * Set the given clearing as anthill.
     * <p>
     * Must be called <strong>exactly once</strong>.
     *
     * @param clearing set to be the anthill
     */
    void setAnthill(C clearing);

    /**
     * Constructs a new clearing given a name.
     *
     * @param name of the clearing
     * @return constructed clearing
     */
    C createClearing(String name);

    /**
     * Constructs a new clearing given a name and initial food amount.
     *
     * @param name of the clearing
     * @param food amount of food initially found at the clearing
     * @return constructed clearing
     */
    C createClearing(String name, int food);

    /**
     * Constructs a new clearing given a name and capacity.
     *
     * @param name     of the clearing
     * @param capacity of the clearing
     * @return constructed clearing
     */
    C createClearing(String name, Position.Capacity capacity);

    /**
     * Constructs a new clearing given a name, capacity and initial food amount.
     *
     * @param name     of the clearing
     * @param food     amount of food initially found at the clearing
     * @param capacity of the clearing
     * @return constructed clearing
     */
    C createClearing(String name, int food, Position.Capacity capacity);

    /**
     * Constructs a new trail (and its reverse) given the clearings it connects.
     * <p>
     * Also updates the connected clearings, i.e. calls {@link Clearing#addTrail}.
     *
     * @param a first clearing
     * @param b second clearing
     * @return constructed trail
     */
    T createTrail(C a, C b);

    /**
     * Constructs a new ant given a name, impatience and disguise.
     * <p>
     * This will be a basic ant without behavior.
     * <p>
     * Ants with behavior will be constructed later given these basic ants.
     *
     * @param name       of the ant
     * @param impatience of the ant
     * @param disguise   of the ant
     * @return constructed ant
     */
    Ant createAnt(String name, int impatience, int disguise);

    /**
     * Constructs a new world given a name and food threshold the ants have to collect.
     * <p>
     * Must be called <strong>exactly once</strong>.
     * <p>
     * The constructed world will have the clearings, trails and ants that were constructed before in it.
     *
     * @param name          of the world
     * @param foodThreshold the ants have to collect
     * @return constructed world
     */
    World<C, T> finishWorld(String name, int foodThreshold);
}
