package com.pseuco.np21.shared;

import java.util.List;

/**
 * Interface for recording simulation actions. Your implementation must call
 * these methods appropriately in order to pass the functional correctness
 * tests. The methods document when they shall be called.
 * <p>
 * You may assume that every instance with type {@link Recorder} is free of data
 * races. However, you may not assume that calls to its methods synchronize,
 * i.e. it is perfectly fine for an instance with type {@link Recorder} to have
 * its methods executed in parallel.
 * <p>
 * While calling the recorder interface you must not hold any locks!
 */
public interface Recorder {
    /**
     * Reasons for an ant to despawn (terminate).
     */
    enum DespawnReason {
        /**
         * Enough food was collected and returned, i.e. the food collection threshold was reached.
         */
        ENOUGH_FOOD_COLLECTED,
        /**
         * The ant was discovered on a trail due to its lack of disguise :)
         */
        DISCOVERED_AND_EATEN,
        /**
         * The ant terminated due to some other reason, e.g. it received an {@link InterruptedException}.
         */
        TERMINATED,
    }

    /**
     * Reasons for an ant to select a trail to follow.
     */
    enum SelectionReason {
        /**
         * Trail was selected for its low food pheromone level during food search.
         */
        FOOD_SEARCH,
        /**
         * Ant selects a trail due to its lack of patience or explored options.
         */
        EXPLORATION,
        /**
         * Used when an ant backtracks from an already visited clearing.
         */
        IMMEDIATE_RETURN,
        /**
         * Used when an ant backtracks from a clearing for which it has no options to reach food other than turning back.
         */
        NO_FOOD_RETURN,
        /**
         * Trail was selected for its low anthill pheromone level during food return.
         */
        RETURN_FOOD,
        /**
         * Used when an ant backtracks its steps after it found food while it was exploring.
         */
        RETURN_IN_SEQUENCE,
    }

    /**
     * Call this method <strong>once</strong> before starting any ants.
     * This marks the beginning of the simulation.
     */
    void start();

    /**
     * Call this method <strong>once</strong> after every ant terminated.
     * This marks the end of the simulation.
     */
    void stop();

    /**
     * Call this method <strong>once</strong> for every ant before it does anything else.
     * From here on the ant is ready to collect food until it calls {@link Recorder#despawn}.
     *
     * @param ant that spawned
     */
    void spawn(Ant ant);

    /**
     * Call this method <strong>once</strong> for every ant before it is about to terminate.
     *
     * @param ant    that will despawn
     * @param reason for the ants termination
     */
    void despawn(Ant ant, DespawnReason reason);

    /**
     * Call this method <strong>after</strong> an ant entered a clearing.
     *
     * @param ant      that entered
     * @param clearing that was entered
     */
    void enter(Ant ant, Clearing<?, ?> clearing);

    /**
     * Call this method <strong>before</strong> an ant leaves a clearing.
     *
     * @param ant      that leaves
     * @param clearing that will be left
     */
    void leave(Ant ant, Clearing<?, ?> clearing);

    /**
     * Call this method <strong>after</strong> an ant entered a trail.
     *
     * @param ant   that entered
     * @param trail that was entered
     */
    void enter(Ant ant, Trail<?, ?> trail);

    /**
     * Call this method <strong>before</strong> an ant leaves a trail.
     *
     * @param ant   that leaves
     * @param trail that will be left
     */
    void leave(Ant ant, Trail<?, ?> trail);

    /**
     * Call this method <strong>once</strong> for every trail an ant is about to enter,
     * i.e. <strong>before</strong> it does enter.
     * <p>
     * With this the ant announces its next step. It may not change its selection!
     * <p>
     * In situations where there may be more than one trail to consider the ant must provide
     * all candidates that it considered. This can occur whenever multiple trails share the
     * same pheromone level. In all other situations the candidates may be null.
     *
     * @param ant        that is selecting
     * @param trail      that was selected
     * @param candidates trails from which the ant chose (or null if not applicable)
     * @param reason     for the selection
     * @param <T>        the type of trails the implementation uses
     */
    <T extends Trail<?, ?>> void select(Ant ant, T trail, List<T> candidates, SelectionReason reason);

    /**
     * Call this method everytime <strong>before</strong> the ant starts its search for the next piece of food.
     *
     * @param ant that is starting its search for food
     */
    void startFoodSearch(Ant ant);

    /**
     * Call this method <strong>at most once</strong> for every new search for food.
     * <p>
     * A new search starts with the ant's announcement for {@link Recorder#startFoodSearch}.
     * <p>
     * Call this method before the ant selects any particular trail to explore.
     *
     * @param ant that starts its exploration
     */
    void startExploration(Ant ant);

    /**
     * Call this method <strong>after</strong> the ant picked up some food and <strong>before</strong>
     * it selects a trail to return the food.
     *
     * @param ant that starts its food return
     */
    void startFoodReturn(Ant ant);

    /**
     * Call this method everytime the ant returned some food to the anthill.
     *
     * @param ant that returned food
     */
    void returnedFood(Ant ant);

    /**
     * Call this method when the ant picks up food at a clearing that has some.
     *
     * @param ant      that picks up food
     * @param clearing where the ant picks up the food
     */
    void pickupFood(Ant ant, Clearing<?, ?> clearing);

    /**
     * Call this method everytime the ant updates the food pheromone level at a trail.
     *
     * @param ant   that updates the pheromone level
     * @param trail that gets updated
     * @param value the pheromone level the ant wants to set
     */
    void updateFood(Ant ant, Trail<?, ?> trail, Trail.Pheromone value);

    /**
     * Call this method everytime the ant updates the anthill pheromone level at a trail.
     *
     * @param ant   that updates the pheromone level
     * @param trail that gets updated
     * @param value the pheromone level the ant wants to set
     */
    void updateAnthill(Ant ant, Trail<?, ?> trail, Trail.Pheromone value);

    /**
     * Call this method <strong>once after</strong> the ant was discovered on a trail
     * and <strong>before</strong> it terminates.
     * <p>
     * An ant will be discovered on a trail if its disguise is exceeded.
     *
     * @param ant that was discovered
     */
    void attractAttention(Ant ant);
}
