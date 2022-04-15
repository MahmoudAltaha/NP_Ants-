package com.pseuco.np21.shared;

import java.util.List;

/**
 * Recorder that prints all received interactions.
 */
public class PrintRecorder implements Recorder {
    @Override
    public synchronized void start() {
        System.out.println("Simulation started.");
    }

    @Override
    public synchronized void stop() {
        System.out.println("Simulation stopped.");
    }

    private static String toString(final DespawnReason reason) {
        return switch (reason) {
            case ENOUGH_FOOD_COLLECTED -> "enough food was collected";
            case DISCOVERED_AND_EATEN -> "it was discovered and eaten";
            case TERMINATED -> "it was terminated externally";
        };
    }

    @Override
    public synchronized void spawn(final Ant ant) {
        System.out.printf("Ant \"%s\" spawned. %s%n", ant.name(), ant);
    }

    @Override
    public synchronized void despawn(final Ant ant, final DespawnReason reason) {
        System.out.printf("Ant \"%s\" despawned since %s.%n", ant.name(), toString(reason));
    }

    @Override
    public synchronized void enter(final Ant ant, final Clearing<?, ?> clearing) {
        System.out.printf("Ant \"%s\" enters %s.%n", ant.name(), clearing);
    }

    @Override
    public synchronized void leave(final Ant ant, final Clearing<?, ?> clearing) {
        System.out.printf("Ant \"%s\" leaves %s.%n", ant.name(), clearing);
    }

    @Override
    public synchronized void enter(final Ant ant, final Trail<?, ?> trail) {
        System.out.printf("Ant \"%s\" enters %s.%n", ant.name(), trail);
    }

    @Override
    public synchronized void leave(final Ant ant, final Trail<?, ?> trail) {
        System.out.printf("Ant \"%s\" leaves %s.%n", ant.name(), trail);
    }

    private static String toString(final SelectionReason reason) {
        return switch (reason) {
            case FOOD_SEARCH -> "it searches food";
            case EXPLORATION -> "it is exploring";
            case IMMEDIATE_RETURN -> "it returns immediately";
            case NO_FOOD_RETURN -> "it found no food and returns";
            case RETURN_FOOD -> "it returns food";
            case RETURN_IN_SEQUENCE -> "it returns the path it took to came here";
        };
    }

    @Override
    public synchronized <T extends Trail<?, ?>> void select(final Ant ant, final T trail, final List<T> candidates, final SelectionReason reason) {
        System.out.printf("Ant \"%s\" selects %s from the following candidates%n", ant.name(), trail);
        if (candidates != null) {
            candidates.forEach(System.out::println);
        }
        System.out.println("because " + toString(reason) + ".");
    }

    @Override
    public synchronized void startFoodSearch(final Ant ant) {
        System.out.printf("Ant \"%s\" starts food search.%n", ant.name());
    }

    @Override
    public synchronized void startExploration(final Ant ant) {
        System.out.printf("Ant \"%s\" starts exploration.%n", ant.name());
    }

    @Override
    public synchronized void startFoodReturn(final Ant ant) {
        System.out.printf("Ant \"%s\" starts to return food.%n", ant.name());
    }

    @Override
    public synchronized void returnedFood(final Ant ant) {
        System.out.printf("Ant \"%s\" returned food.%n", ant.name());
    }

    @Override
    public synchronized void pickupFood(final Ant ant, final Clearing<?, ?> clearing) {
        System.out.printf("Ant \"%s\" picks up food at %s.%n", ant.name(), clearing);
    }

    @Override
    public synchronized void updateFood(final Ant ant, final Trail<?, ?> trail, final Trail.Pheromone value) {
        System.out.printf("Ant \"%s\" updates food pheromone level at %s with %s.%n", ant.name(), trail, value);
    }

    @Override
    public synchronized void updateAnthill(final Ant ant, final Trail<?, ?> trail, final Trail.Pheromone value) {
        System.out.printf("Ant \"%s\" updates anthill pheromone level at %s with %s.%n", ant.name(), trail, value);
    }

    @Override
    public synchronized void attractAttention(final Ant ant) {
        System.out.printf("Ant \"%s\" attracts attention.%n", ant.name());
    }
}
