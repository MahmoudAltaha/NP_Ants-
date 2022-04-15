package com.pseuco.np21.shared;

import java.util.List;
import java.util.Queue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Verifies the following:
 * 1. spawn gets called for every ant before the ant does anything else
 * 2. spawn is not called more than once
 * 3. despawn gets called for every ant after the ant is finished
 * 4. despawn is not called more than once
 */
public class SpawnDespawnValidator implements Validator {
    private static final int MAX_ERRORS = 5;

    private final Queue<String> errors = new ConcurrentLinkedQueue<>();
    private final ConcurrentMap<Integer, Ant> ants = new ConcurrentHashMap<>();
    private final ConcurrentMap<Integer, AtomicInteger> spawnCalls = new ConcurrentHashMap<>();
    private final ConcurrentMap<Integer, AtomicInteger> despawnCalls = new ConcurrentHashMap<>();

    private void addError(final String message) {
        if (errors.size() < MAX_ERRORS) {
            errors.add(StartStopValidator.class.getName() + ": " + message);
        }
    }

    private boolean checkSpawnWasCalled(final Ant ant, final String methodName) {
        if (!this.spawnCalls.containsKey(ant.id())) {
            addError(methodName + " must not be called before spawn is called");
            return false;
        }

        return true;
    }

    private boolean checkDespawnWasNotCalled(final Ant ant, final String methodName) {
        if (!this.despawnCalls.containsKey(ant.id())) {
            return false;
        }

        final var despawnCalls = this.despawnCalls.get(ant.id());
        final var despawnCount = despawnCalls.get();

        if (despawnCount > 0) {
            addError(methodName + " must not be called after despawn is called");
        }

        return true;
    }

    @Override
    public void start() {
        // nop
    }

    @Override
    public void stop() {
        // nop
    }

    @Override
    public void spawn(Ant ant) {
        synchronized (this.ants) {
            if (!this.ants.containsKey(ant.id())) {
                this.ants.put(ant.id(), ant);
                this.spawnCalls.put(ant.id(), new AtomicInteger());
                this.despawnCalls.put(ant.id(), new AtomicInteger());
            }
        }

        final var spawnCalls = this.spawnCalls.get(ant.id());
        final var spawnCount = spawnCalls.getAndIncrement();

        if (spawnCount > 0) {
            addError("spawn must not be called more than once on " + ant);
        }
    }

    @Override
    public void despawn(Ant ant, DespawnReason reason) {
        if (!checkSpawnWasCalled(ant, "despawn")) {
            return;
        }

        final var despawnCalls = this.despawnCalls.get(ant.id());
        final var despawnCount = despawnCalls.getAndIncrement();

        if (despawnCount > 0) {
            addError("despawn must not be called more than once on " + ant);
        }

    }

    @Override
    public void enter(Ant ant, Clearing<?, ?> clearing) {
        checkSpawnWasCalled(ant, "enter<Clearing>");
        checkDespawnWasNotCalled(ant, "enter<Clearing>");
    }

    @Override
    public void leave(Ant ant, Clearing<?, ?> clearing) {
        checkSpawnWasCalled(ant, "leave<Clearing>");
        checkDespawnWasNotCalled(ant, "leave<Clearing>");
    }

    @Override
    public void enter(Ant ant, Trail<?, ?> trail) {
        checkSpawnWasCalled(ant, "enter<Trail>");
        checkDespawnWasNotCalled(ant, "enter<Trail>");
    }

    @Override
    public void leave(Ant ant, Trail<?, ?> trail) {
        checkSpawnWasCalled(ant, "leave<Trail>");
        checkDespawnWasNotCalled(ant, "leave<Trail>");
    }

    @Override
    public <T extends Trail<?, ?>> void select(Ant ant, T trail, List<T> candidates, SelectionReason reason) {
        checkSpawnWasCalled(ant, "select");
        checkDespawnWasNotCalled(ant, "select");
    }

    @Override
    public void startFoodSearch(Ant ant) {
        checkSpawnWasCalled(ant, "startFoodSearch");
        checkDespawnWasNotCalled(ant, "startFoodSearch");
    }

    @Override
    public void startExploration(Ant ant) {
        checkSpawnWasCalled(ant, "startExploration");
        checkDespawnWasNotCalled(ant, "startExploration");
    }

    @Override
    public void startFoodReturn(Ant ant) {
        checkSpawnWasCalled(ant, "startFoodReturn");
        checkDespawnWasNotCalled(ant, "startFoodReturn");
    }

    @Override
    public void returnedFood(Ant ant) {
        checkSpawnWasCalled(ant, "returnedFood");
        checkDespawnWasNotCalled(ant, "returnedFood");
    }

    @Override
    public void pickupFood(Ant ant, Clearing<?, ?> clearing) {
        checkSpawnWasCalled(ant, "pickupFood");
        checkDespawnWasNotCalled(ant, "pickupFood");
    }

    @Override
    public void updateFood(Ant ant, Trail<?, ?> trail, Trail.Pheromone value) {
        checkSpawnWasCalled(ant, "updateFood");
        checkDespawnWasNotCalled(ant, "updateFood");
    }

    @Override
    public void updateAnthill(Ant ant, Trail<?, ?> trail, Trail.Pheromone value) {
        checkSpawnWasCalled(ant, "updateAnthill");
        checkDespawnWasNotCalled(ant, "updateAnthill");
    }

    @Override
    public void attractAttention(Ant ant) {
        checkSpawnWasCalled(ant, "attractAttention");
        checkDespawnWasNotCalled(ant, "attractAttention");
    }

    @Override
    public boolean isRecordingValid() {
        boolean allAntsCalledDespawn = true;

        for (final var entry : ants.entrySet()) {
            final var despawnCalls = this.despawnCalls.get(entry.getKey());
            final var despawnCount = despawnCalls.get();

            if (despawnCount < 1) {
                allAntsCalledDespawn = false;
                addError("despawn must be called before " + entry.getValue() + " terminates");
            }
        }

        return allAntsCalledDespawn && errors.isEmpty();
    }

    @Override
    public List<String> errors() {
        return List.copyOf(errors);
    }
}
