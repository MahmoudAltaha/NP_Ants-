package com.pseuco.np21;

import com.pseuco.np21.shared.Ant;
import com.pseuco.np21.shared.Position;
import com.pseuco.np21.shared.World;

import java.util.ArrayList;
import java.util.List;

/**
 * Implementation of the {@link com.pseuco.np21.shared.Factory} interface.
 * <p>
 * You may change the code however you see fit.
 */
public class Factory implements com.pseuco.np21.shared.Factory<Clearing, Trail> {
    private final List<Clearing> clearings;
    private final List<Clearing> foodClearings;
    private final List<Ant> ants;

    private Clearing anthill;

    /**
     * Constructs a new factory.
     * <p>
     * You may change this except for the signature.
     */
    public Factory() {
        clearings = new ArrayList<>();
        foodClearings = new ArrayList<>();
        ants = new ArrayList<>();
    }

    @Override
    public void setAnthill(final Clearing clearing) {
        if (anthill != null) {
            throw new IllegalStateException("Cannot create spawn more than once!");
        }

        anthill = clearing;
    }

    @Override
    public Clearing createClearing(final String name) {
        return createClearing(name, 0, Position.Capacity.INFINITE);
    }

    @Override
    public Clearing createClearing(final String name, final int food) {
        return createClearing(name, food, Position.Capacity.INFINITE);
    }

    @Override
    public Clearing createClearing(final String name, final Position.Capacity capacity) {
        return createClearing(name, 0, capacity);
    }

    @Override
    public Clearing createClearing(final String name, final int food, final Position.Capacity capacity) {
        final var clearing = new com.pseuco.np21.Clearing(name, food, capacity);
        clearings.add(clearing);

        if (food > 0) {
            foodClearings.add(clearing);
        }

        return clearing;
    }

    @Override
    public Trail createTrail(final Clearing a, final Clearing b) {
        final var trail = new com.pseuco.np21.Trail(a, b);
        a.addTrail(trail);
        return trail;
    }

    @Override
    public Ant createAnt(final String name, final int impatience, final int disguise) {
        final var ant = new Ant(name, impatience, disguise);
        ants.add(ant);
        return ant;
    }

    @Override
    public World<Clearing, Trail> finishWorld(final String name, final int foodThreshold) {
        return new World<>(name, clearings, anthill, foodClearings, foodThreshold, ants);
    }
}
