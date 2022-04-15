package com.pseuco.np21.shared;



import com.pseuco.np21.Factory;
import com.pseuco.np21.Simulator;
import com.pseuco.np21.shared.Ant;
import com.pseuco.np21.shared.Recorder;
import com.pseuco.np21.shared.Trail.Pheromone;
import com.pseuco.np21.shared.World;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;

import java.util.List;

import static org.mockito.Mockito.*;



public class SimpleUpdateAntHillTest {
    //test by tristanh, https://np21.pseuco.com/t/simpleupdateanthilltest-test-fuer-recorder-updateanthill-aufruf/796


        private World world;
        private Clearing anthill, deadend;
        private com.pseuco.np21.Trail trail, trailReverse;
        private Ant ant;

        private Simulator simulator;
        private Recorder recorder;

        private void overWriteAnthillPheromone(Trail trail, Pheromone value) {
            try {
                var food = trail.getClass().getDeclaredField("anthill");
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
            factory.setAnthill((com.pseuco.np21.Clearing) anthill);
            deadend = factory.createClearing("deadend", 1);

            trail = factory.createTrail((com.pseuco.np21.Clearing) anthill,(com.pseuco.np21.Clearing) deadend);
            trailReverse = trail.reverse();

            ant = factory.createAnt("Anthony", 1000, 1000);

            world = factory.finishWorld("TestLine", -1);
            recorder = mock(Recorder.class);

            simulator = new Simulator(world, recorder);

            overWriteAnthillPheromone(trailReverse, Pheromone.get(0));
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
            inOrder.verify(recorder).enter(eq(ant), same(deadend));
            inOrder.verify(recorder).leave(eq(ant), same(trail));
            inOrder.verify(recorder).updateAnthill(eq(ant), same(trailReverse), eq(Pheromone.get(0)));
        }


    }

