package com.pseuco.np21;

import com.pseuco.np21.shared.Ant;
import com.pseuco.np21.shared.Recorder;
import com.pseuco.np21.shared.Trail.Pheromone;
import com.pseuco.np21.shared.World;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;

import java.util.List;

import static org.mockito.Mockito.*;

class SimpleNaPTest1 {
    private World<Clearing, Trail> world;
    private Clearing anthill, food, clearing;
    private Trail anthill_food, anthill_food_reverse, anthill_clearing, anthill_clearing_reverse;
    private Ant ant;

    private Simulator simulator;
    private Recorder recorder;

    private void overwriteFoodPheromone(Trail trail, Pheromone value) {
        try {
            var food = trail.getClass().getDeclaredField("food");
            food.setAccessible(true);
            food.set(trail, value);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            e.printStackTrace();
        }
    }

    @BeforeEach
    void setUp() {
        final var factory = new Factory();

        anthill = factory.createClearing("anthill");
        factory.setAnthill(anthill);

        food = factory.createClearing("food", 2);
        clearing = factory.createClearing("clearing", 0);

        anthill_food = factory.createTrail(anthill, food);
        anthill_food_reverse = anthill_food.reverse();

        anthill_clearing = factory.createTrail(anthill, clearing);
        anthill_clearing_reverse = anthill_clearing.reverse();

        ant = factory.createAnt("Anthony", 1, 1000);

        world = factory.finishWorld("TestLine", -1);
        recorder = mock(Recorder.class, withSettings().verboseLogging());

        simulator = new Simulator(world, recorder);

        overwriteFoodPheromone(anthill_food, Pheromone.get(2));
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

        inOrder.verify(recorder).select(eq(ant), same(anthill_clearing), eq(List.of(anthill_clearing)), same(Recorder.SelectionReason.EXPLORATION));
        inOrder.verify(recorder).enter(eq(ant), same(anthill_clearing));
        inOrder.verify(recorder).leave(eq(ant), same(anthill));
        inOrder.verify(recorder).enter(eq(ant), same(clearing));
        inOrder.verify(recorder).leave(eq(ant), same(anthill_clearing));
        inOrder.verify(recorder).updateAnthill(eq(ant), same(anthill_clearing.reverse()), eq(Pheromone.get(1)));

        inOrder.verify(recorder).select(eq(ant), same(anthill_clearing_reverse), any(), same(Recorder.SelectionReason.NO_FOOD_RETURN));
        inOrder.verify(recorder).enter(eq(ant), same(anthill_clearing_reverse));
        inOrder.verify(recorder).leave(eq(ant), same(clearing));
        inOrder.verify(recorder).enter(eq(ant), same(anthill));
        inOrder.verify(recorder).leave(eq(ant), same(anthill_clearing_reverse));
        inOrder.verify(recorder).updateFood(eq(ant), same(anthill_clearing), eq(Pheromone.INFINITE));

        inOrder.verify(recorder).select(eq(ant), same(anthill_food), eq(List.of(anthill_food)), same(Recorder.SelectionReason.FOOD_SEARCH));
        inOrder.verify(recorder).enter(eq(ant), same(anthill_food));
        inOrder.verify(recorder).leave(eq(ant), same(anthill));
        inOrder.verify(recorder).enter(eq(ant), same(food));
        inOrder.verify(recorder).leave(eq(ant), same(anthill_food));
        inOrder.verify(recorder).updateAnthill(eq(ant), same(anthill_food_reverse), eq(Pheromone.get(1)));

        inOrder.verify(recorder).pickupFood(eq(ant), same(food));
        inOrder.verify(recorder).startFoodReturn(eq(ant));

        inOrder.verify(recorder).select(eq(ant), same(anthill_food_reverse), any(), same(Recorder.SelectionReason.RETURN_IN_SEQUENCE));
        inOrder.verify(recorder).enter(eq(ant), same(anthill_food_reverse));
        inOrder.verify(recorder).leave(eq(ant), same(food));
        inOrder.verify(recorder).enter(eq(ant), same(anthill));
        inOrder.verify(recorder).leave(eq(ant), same(anthill_food_reverse));
        inOrder.verify(recorder).updateFood(eq(ant), same(anthill_food), eq(Pheromone.get(1)));

        inOrder.verify(recorder).returnedFood(eq(ant));

        inOrder.verify(recorder).startFoodSearch(eq(ant));

        inOrder.verify(recorder).select(eq(ant), same(anthill_food), eq(List.of(anthill_food)), same(Recorder.SelectionReason.FOOD_SEARCH));
        inOrder.verify(recorder).enter(eq(ant), same(anthill_food));
        inOrder.verify(recorder).leave(eq(ant), same(anthill));
        inOrder.verify(recorder).enter(eq(ant), same(food));
        inOrder.verify(recorder).leave(eq(ant), same(anthill_food));
        inOrder.verify(recorder).updateAnthill(eq(ant), same(anthill_food_reverse), eq(Pheromone.get(1)));

        inOrder.verify(recorder).pickupFood(eq(ant), same(food));
        inOrder.verify(recorder).startFoodReturn(eq(ant));

        inOrder.verify(recorder).select(eq(ant), same(anthill_food_reverse), any(), same(Recorder.SelectionReason.RETURN_FOOD));
        inOrder.verify(recorder).enter(eq(ant), same(anthill_food_reverse));
        inOrder.verify(recorder).leave(eq(ant), same(food));
        inOrder.verify(recorder).enter(eq(ant), same(anthill));
        inOrder.verify(recorder).leave(eq(ant), same(anthill_food_reverse));

        inOrder.verify(recorder).returnedFood(eq(ant));

        inOrder.verify(recorder).leave(eq(ant), same(anthill));
        inOrder.verify(recorder).despawn(eq(ant), same(Recorder.DespawnReason.ENOUGH_FOOD_COLLECTED));

        inOrder.verify(recorder).stop();

        verifyNoMoreInteractions(recorder);
    }
}