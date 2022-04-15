package com.pseuco.np21.shared;

import com.pseuco.np21.AdvancedSimulator;
import com.pseuco.np21.Factory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assumptions.assumeTrue;
import static org.mockito.Mockito.mock;

class Exercise2Test {
    private AdvancedSimulator simulator;
/*
    @BeforeEach
    void setUp() {
        final var factory = new Factory();

        final var anthill = factory.createClearing("anthill");
        factory.setAnthill(anthill);
        final var food = factory.createClearing("food", 2);

        factory.createTrail(anthill, food);

        factory.createAnt("Anthony", 1, 1000);

        final var world = factory.finishWorld("TestLine", -1);
        final var recorder = mock(Recorder.class);

        simulator = new AdvancedSimulator(world, recorder, true);
    }

    @Test
    @Timeout(value = 1)
    void run() {
        assumeTrue(AdvancedSimulator.IS_IMPLEMENTED);

        assertDoesNotThrow(() -> simulator.run());
    }

 */
}