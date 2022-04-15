package com.pseuco.np21.shared;

import java.util.List;

/**
 * Recorder implementation that concatenates multiple recorders.
 * <p>
 * Given a list of recorders the {@link CatRecorder} forwards all calls to the recorders contained within that list.
 */
public class CatRecorder<R extends Recorder> implements Recorder {
    protected final List<R> recorders;

    /**
     * Constructs a new concatenating recorder.
     *
     * @param recorders list of recorders to forward the calls to
     */
    public CatRecorder(final List<R> recorders) {
        this.recorders = recorders;
    }

    @Override
    public void start() {
        recorders.forEach(Recorder::start);
    }

    @Override
    public void stop() {
        recorders.forEach(Recorder::stop);
    }

    @Override
    public void spawn(Ant ant) {
        recorders.forEach(r -> r.spawn(ant));
    }

    @Override
    public void despawn(Ant ant, DespawnReason reason) {
        recorders.forEach(r -> r.despawn(ant, reason));
    }

    @Override
    public void enter(Ant ant, Clearing<?, ?> clearing) {
        recorders.forEach(r -> r.enter(ant, clearing));
    }

    @Override
    public void leave(Ant ant, Clearing<?, ?> clearing) {
        recorders.forEach(r -> r.leave(ant, clearing));
    }

    @Override
    public void enter(Ant ant, Trail<?, ?> trail) {
        recorders.forEach(r -> r.enter(ant, trail));
    }

    @Override
    public void leave(Ant ant, Trail<?, ?> trail) {
        recorders.forEach(r -> r.leave(ant, trail));
    }

    @Override
    public <T extends Trail<?, ?>> void select(Ant ant, T trail, List<T> candidates, SelectionReason reason) {
        recorders.forEach(r -> r.select(ant, trail, candidates, reason));
    }

    @Override
    public void startFoodSearch(Ant ant) {
        recorders.forEach(r -> r.startFoodSearch(ant));
    }

    @Override
    public void startExploration(Ant ant) {
        recorders.forEach(r -> r.startExploration(ant));
    }

    @Override
    public void startFoodReturn(Ant ant) {
        recorders.forEach(r -> r.startFoodReturn(ant));
    }

    @Override
    public void returnedFood(Ant ant) {
        recorders.forEach(r -> r.returnedFood(ant));
    }

    @Override
    public void pickupFood(Ant ant, Clearing<?, ?> clearing) {
        recorders.forEach(r -> r.pickupFood(ant, clearing));
    }

    @Override
    public void updateFood(Ant ant, Trail<?, ?> trail, Trail.Pheromone value) {
        recorders.forEach(r -> r.updateFood(ant, trail, value));
    }

    @Override
    public void updateAnthill(Ant ant, Trail<?, ?> trail, Trail.Pheromone value) {
        recorders.forEach(r -> r.updateAnthill(ant, trail, value));
    }

    @Override
    public void attractAttention(Ant ant) {
        recorders.forEach(r -> r.attractAttention(ant));
    }
}
