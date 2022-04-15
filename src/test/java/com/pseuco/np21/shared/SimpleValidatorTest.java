package com.pseuco.np21.shared;

import com.pseuco.np21.Factory;
import com.pseuco.np21.Simulator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertTrue;

public class SimpleValidatorTest {
    private Simulator simulator;
    private CatValidator<?> validator;

    @BeforeEach
    void setUp() throws IOException, URISyntaxException {
        final var factory = new Factory();

        final var classLoader = getClass().getClassLoader();
        final var mapFile = classLoader.getResource("maps/line.map").toURI();
        final var antsFile = classLoader.getResource("ants/simpsons.ants").toURI();

        final var map = Files.readString(Path.of(mapFile));
        final var ants = Files.readString(Path.of(antsFile));
        final var name = Parser.parse(map, ants, factory);

        final var world = factory.finishWorld("SimpleValidatorTest: " + name, -1);
        validator = new CatValidator<>(List.of(
                new StartStopValidator(),
                new SpawnDespawnValidator()
        ));


        simulator = new Simulator(world, validator);
    }

    @Test
    @Timeout(value = 5)
    void run() {
        simulator.run();

        assertTrue(validator.isRecordingValid(), () -> String.join("\n", validator.errors()));
    }
}
