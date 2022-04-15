package com.pseuco.np21.shared;

import com.pseuco.np21.Factory;
import com.pseuco.np21.Simulator;
import com.pseuco.np21.shared.Trail.Pheromone;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;

import java.util.List;

import static org.mockito.Mockito.*;

class AntRecorderTest {
    private Clearing<?, ?> anthill, food;
    private Trail<?, ?> trail, trailReverse;
    private Ant ant;

    private Simulator simulator;
    private Recorder recorder;

    @BeforeEach
    void setUp() {
        final var factory = new Factory();

        final var anthill = factory.createClearing("anthill");
        this.anthill = anthill;
        factory.setAnthill(anthill);
        final var food = factory.createClearing("food", 2);
        this.food = food;

        trail = factory.createTrail(anthill, food);
        trailReverse = trail.reverse();

        ant = factory.createAnt("Anthony", 1, 1000);

        final var world = factory.finishWorld("TestLine", -1);
        recorder = mock(Recorder.class);

        simulator = new Simulator(world, recorder);
    }


    @Test
    @Timeout(value = 1)
    void run() {
        simulator.run();

        final var inOrder = inOrder(recorder);

        inOrder.verify(recorder).start();

        inOrder.verify(recorder).spawn(eq(ant));
        inOrder.verify(recorder).enter(eq(ant), same(anthill));

        inOrder.verify(recorder).startFoodSearch(eq(ant));

        inOrder.verify(recorder).startExploration(eq(ant));
        inOrder.verify(recorder).select(eq(ant), same(trail), eq(List.of(trail)), same(Recorder.SelectionReason.EXPLORATION));
        inOrder.verify(recorder).enter(eq(ant), same(trail));
        inOrder.verify(recorder).leave(eq(ant), same(anthill));
        inOrder.verify(recorder).enter(eq(ant), same(food));
        inOrder.verify(recorder).leave(eq(ant), same(trail));
        inOrder.verify(recorder).updateAnthill(eq(ant), same(trailReverse), eq(Pheromone.get(1)));

        inOrder.verify(recorder).pickupFood(eq(ant), same(food));
        inOrder.verify(recorder).startFoodReturn(eq(ant));

        inOrder.verify(recorder).select(eq(ant), same(trailReverse), any(), same(Recorder.SelectionReason.RETURN_IN_SEQUENCE));
        inOrder.verify(recorder).enter(eq(ant), same(trailReverse));
        inOrder.verify(recorder).leave(eq(ant), same(food));
        inOrder.verify(recorder).enter(eq(ant), same(anthill));
        inOrder.verify(recorder).leave(eq(ant), same(trailReverse));
        inOrder.verify(recorder).updateFood(eq(ant), same(trail), eq(Pheromone.get(1)));

        inOrder.verify(recorder).returnedFood(eq(ant));

        inOrder.verify(recorder).startFoodSearch(eq(ant));

        inOrder.verify(recorder).select(eq(ant), same(trail), eq(List.of(trail)), same(Recorder.SelectionReason.FOOD_SEARCH));
        inOrder.verify(recorder).enter(eq(ant), same(trail));
        inOrder.verify(recorder).leave(eq(ant), same(anthill));
        inOrder.verify(recorder).enter(eq(ant), same(food));
        inOrder.verify(recorder).leave(eq(ant), same(trail));
        inOrder.verify(recorder).updateAnthill(eq(ant), same(trailReverse), eq(Pheromone.get(1)));

        inOrder.verify(recorder).pickupFood(eq(ant), same(food));
        inOrder.verify(recorder).startFoodReturn(eq(ant));

        inOrder.verify(recorder).select(eq(ant), same(trailReverse), any(), same(Recorder.SelectionReason.RETURN_FOOD));
        inOrder.verify(recorder).enter(eq(ant), same(trailReverse));
        inOrder.verify(recorder).leave(eq(ant), same(food));
        inOrder.verify(recorder).enter(eq(ant), same(anthill));
        inOrder.verify(recorder).leave(eq(ant), same(trailReverse));

        inOrder.verify(recorder).returnedFood(eq(ant));

        inOrder.verify(recorder).leave(eq(ant), same(anthill));
        inOrder.verify(recorder).despawn(eq(ant), same(Recorder.DespawnReason.ENOUGH_FOOD_COLLECTED));

        inOrder.verify(recorder).stop();

        verifyNoMoreInteractions(recorder);
    }
}