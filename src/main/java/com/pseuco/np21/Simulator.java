package com.pseuco.np21;

import com.pseuco.np21.shared.Parser;
import com.pseuco.np21.shared.PrintRecorder;
import com.pseuco.np21.shared.Recorder;
import com.pseuco.np21.shared.World;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Entrypoint of the program and simulation manager.
 * <p>
 * You may change the code however you see fit except for the marked portions.
 */
public class Simulator {
    private final World<Clearing, Trail> world;
    private final Recorder recorder;

    /**
     * Constructs a new simulator.
     * <p>
     * You may change this except for the signature.
     *
     * @param world    to simulate
     * @param recorder to record actions against
     */
    public Simulator(final World<Clearing, Trail> world, final Recorder recorder) {
        this.world = world;
        this.recorder = recorder;
    }

    /**
     * The simulation.
     * <p>
     * You may change this except for the signature.
     */
    public void run() {
        final var ants = world.ants().stream()
                .map(a -> new Ant(a, world, recorder))
                .collect(Collectors.toList());

        this.recorder.start();

            Thread[] all = new Thread[ants.size()];
            for (int i = 0; i < ants.size(); i++) {
                all[i] = new Thread(ants.get(i),ants.get(i).name());
            }

            for (int i = 0; i < ants.size(); i++) {
                all[i].start();
            }
            try {
                for (int i = 0; i < ants.size(); i++) {
                        all[i].join();
                }
            }
            catch (InterruptedException ignored) {

            }
            finally {
               for (Ant ant : ants){
                    if(!ant.despawnd){
                        recorder.despawn(ant, Recorder.DespawnReason.TERMINATED);
                        ant.setDespawndTrue();
                    }
                }

                recorder.stop();
                Thread.currentThread().interrupt();
            }

    }

    /**
     * Entrypoint of the program.
     * <p>
     * DO NOT TOUCH!
     *
     * @param args command line arguments
     * @throws IOException if the specified files cannot be read
     */
    public static void main(final String[] args) throws IOException {
        final var usage = "Usage: <command> map=<map file> ants=<ants file> [food=<food>] [timeout=<timeout>]";
        final var arguments = parseArguments(args);
        if (arguments == null) {
            System.out.println(usage);
            System.exit(1);
        }

        final var map = getOrDefaultArgument(arguments, "map");
        final var ants = getOrDefaultArgument(arguments, "ants");
        final var foodThreshold = Integer.parseInt(getOrDefaultArgument(arguments, "food"));
        final var timeout = Integer.parseInt(getOrDefaultArgument(arguments, "timeout"));

        final var factory = new Factory();
        final var name = Parser.parse(map, ants, factory);
        final var world = factory.finishWorld(name, foodThreshold);

        final var recorder = new PrintRecorder();
        final var simulator = new Simulator(world, recorder);

        final var runner = new Thread(simulator::run);
        runner.start();
        try {
            runner.join(timeout);
            final var threads = Thread.getAllStackTraces().keySet();
            threads.forEach(Thread::interrupt);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    /**
     * DO NOT TOUCH!
     */
    private static Map<String, String> parseArguments(final String[] args) {
        final var options = Set.of("map", "ants", "food", "timeout");
        final var result = new HashMap<String, String>();

        for (final var arg : args) {
            final var parts = arg.split("=", 2);
            if (parts.length != 2 || !options.contains(parts[0])) {
                return null;
            }
            result.put(parts[0], parts[1]);
        }

        return result;
    }

    /**
     * DO NOT TOUCH!
     */
    private static String getOrDefaultArgument(final Map<String, String> arguments, final String argument) throws IOException {
        return switch (argument) {
            case "map" -> {
                if (arguments.containsKey("map")) {
                    yield Files.readString(Path.of(arguments.get("map")));
                } else {
                    yield """
                            test
                            spawn;-1;0;(0,0)
                            one;-1;0;(0,0)
                            two;2;0;(0,0)
                            three;2;3;(0,0)
                            four;2;3;(0,0)
                            spawn-one
                            one-two
                            two-three
                            one-three
                            one-four
                            """;
                }
            }
            case "ants" -> {
                if (arguments.containsKey("ants")) {
                    yield Files.readString(Path.of(arguments.get("ants")));
                } else {
                    yield """
                            Anthony;2;1000
                            """;
                }
            }
            case "food" -> arguments.getOrDefault("food", "-1");
            case "timeout" -> arguments.getOrDefault("timeout", "10000");
            default -> throw new IllegalArgumentException();
        };
    }
}
