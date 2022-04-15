package com.pseuco.np21;

import com.pseuco.np21.shared.Recorder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;

import static org.mockito.Mockito.*;

public class InterruptionTests {

    private final int TIME_THRESHOLD = 100;
    private final String ANT_NAME = "testAnt";

    private Clearing food;
    private Factory factory;

    private Simulator simulator;

    @BeforeEach
    /*
    Setup for all tests is:
    One anthill + one clearing with "unlimited" food, so the ants can't reach their goal. Both are connected with one trail.
     */
    void setUp() {
        factory = new Factory();

        final var anthill = factory.createClearing("anthill");
        factory.setAnthill(anthill);
        food = factory.createClearing("food", Integer.MAX_VALUE);

        factory.createTrail(anthill, food);
    }

    void interruptAnts(final int antCount) throws InterruptedException {
        for (int i = 0; i < antCount; i++) {
            factory.createAnt(ANT_NAME, 0, Integer.MAX_VALUE); // spawn antCount ants
        }

        final var world = factory.finishWorld("world", -1);
        Recorder recorder = mock(Recorder.class);
        simulator = new Simulator(world, recorder);

        doAnswer(i -> {
            for (final var t : Thread.getAllStackTraces().keySet()) {
                if (t.getName().equals(ANT_NAME)) {
                    t.interrupt();
                }
            }
            return null;
        }).when(recorder).enter(any(), eq(food));

        // run simulation in extra thread
        final var thread = new Thread(() -> {
            simulator.run();
        });
        thread.start();
        thread.join();

        var inOrder = inOrder(recorder);
        inOrder.verify(recorder, times(antCount)).despawn(any(), same(Recorder.DespawnReason.TERMINATED));
        inOrder.verify(recorder).stop();
    }

    @Test
    @Timeout(value = 3)
    /*
    Goal of collecting all food can't be reached before timeout.
    Simulation gets interrupted. All ants should end their work before timeout.
    */
    void interruptManyAnts() throws InterruptedException {
        interruptAnts(100);
    }

    @Test
    @Timeout(value = 3)
    /*
    Same as interruptManyAnts with only one ant.
    */
    void interruptOneAnt() throws InterruptedException {
        interruptAnts(1);
    }

    @Test
    @Timeout(value = 3)
    /*
    Same as interruptOneAnt but we check if the ant terminates in TIME_THRESHOLD ms.
    This is an optional test as the project specification says nothing about how fast the ants should react to an interrupt.
    Additionally, in the worst case this test could even fail on a perfect implementation if our simulation gets no cpu time.
    */
    void interruptOneAntInTime() throws InterruptedException {
        final var startTime = System.currentTimeMillis();
        interruptOneAnt();
        assert startTime + TIME_THRESHOLD >= System.currentTimeMillis();
    }
}