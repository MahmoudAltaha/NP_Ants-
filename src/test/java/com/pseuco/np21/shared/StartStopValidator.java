package com.pseuco.np21.shared;

import java.util.List;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Verifies the following:
 * 1. start gets called at the beginning
 * 2. start is not called more than once
 * 3. stop gets called last
 * 4. stop is not called more than once
 */
public class StartStopValidator implements Validator {
    private static final int MAX_ERRORS = 5;

    private final Queue<String> errors = new ConcurrentLinkedQueue<>();
    private final AtomicInteger startCalls = new AtomicInteger();
    private final AtomicInteger stopCalls = new AtomicInteger();

    private void addError(final String message) {
        if (errors.size() < MAX_ERRORS) {
            errors.add(StartStopValidator.class.getName() + ": " + message);
        }
    }

    private boolean checkStartWasCalled(final String methodName) {
        final var startCount = startCalls.get();

        if (startCount < 1) {
            addError(methodName + " must not be called before start is called");
            return false;
        }

        return true;
    }

    private boolean checkStopWasNotCalled(final String methodName) {
        final var stopCount = stopCalls.get();

        if (stopCount > 0) {
            addError(methodName + " must not be called after stop is called");
            return false;
        }

        return true;
    }

    @Override
    public void start() {
        final var startCount = startCalls.getAndIncrement();

        if (startCount > 0) {
            addError("start must not be called more than once");
        }
    }

    @Override
    public void stop() {
        if (!checkStartWasCalled("stop")) {
            return;
        }

        final var stopCount = stopCalls.getAndIncrement();

        if (stopCount > 0) {
            addError("stop must not be called more than once");
        }
    }

    @Override
    public void spawn(Ant ant) {
        checkStartWasCalled("spawn");
        checkStopWasNotCalled("spawn");
    }

    @Override
    public void despawn(Ant ant, DespawnReason reason) {
        checkStartWasCalled("despawn");
        checkStopWasNotCalled("despawn");
    }

    @Override
    public void enter(Ant ant, Clearing<?, ?> clearing) {
        checkStartWasCalled("enter<Clearing>");
        checkStopWasNotCalled("enter<Clearing>");
    }

    @Override
    public void leave(Ant ant, Clearing<?, ?> clearing) {
        checkStartWasCalled("leave<Clearing>");
        checkStopWasNotCalled("leave<Clearing>");
    }

    @Override
    public void enter(Ant ant, Trail<?, ?> trail) {
        checkStartWasCalled("enter<Trail>");
        checkStopWasNotCalled("enter<Trail>");
    }

    @Override
    public void leave(Ant ant, Trail<?, ?> trail) {
        checkStartWasCalled("leave<Trail>");
        checkStopWasNotCalled("leave<Trail>");
    }

    @Override
    public <T extends Trail<?, ?>> void select(Ant ant, T trail, List<T> candidates, SelectionReason reason) {
        checkStartWasCalled("select");
        checkStopWasNotCalled("select");
    }

    @Override
    public void startFoodSearch(Ant ant) {
        checkStartWasCalled("startFoodSearch");
        checkStopWasNotCalled("startFoodSearch");
    }

    @Override
    public void startExploration(Ant ant) {
        checkStartWasCalled("startExploration");
        checkStopWasNotCalled("startExploration");
    }

    @Override
    public void startFoodReturn(Ant ant) {
        checkStartWasCalled("startFoodReturn");
        checkStopWasNotCalled("startFoodReturn");
    }

    @Override
    public void returnedFood(Ant ant) {
        checkStartWasCalled("returnedFood");
        checkStopWasNotCalled("returnedFood");
    }

    @Override
    public void pickupFood(Ant ant, Clearing<?, ?> clearing) {
        checkStartWasCalled("pickupFood");
        checkStopWasNotCalled("pickupFood");
    }

    @Override
    public void updateFood(Ant ant, Trail<?, ?> trail, Trail.Pheromone value) {
        checkStartWasCalled("updateFood");
        checkStopWasNotCalled("updateFood");
    }

    @Override
    public void updateAnthill(Ant ant, Trail<?, ?> trail, Trail.Pheromone value) {
        checkStartWasCalled("updateAnthill");
        checkStopWasNotCalled("updateAnthill");
    }

    @Override
    public void attractAttention(Ant ant) {
        checkStartWasCalled("attractAttention");
        checkStopWasNotCalled("attractAttention");
    }

    @Override
    public boolean isRecordingValid() {
        final var stopCount = stopCalls.get();

        if (stopCount < 1) {
            addError("stop must be called before the simulation ends");
        }

        return stopCount > 0 && errors.isEmpty();
    }

    @Override
    public List<String> errors() {
        return List.copyOf(errors);
    }
}
