// testsuite by nils.husung, https://np21.pseuco.com/t/testsuite-fuer-ant-terminierung/784
package com.pseuco.np21;

import static org.mockito.Mockito.any;
import static org.mockito.Mockito.atMost;
import static org.mockito.Mockito.atMostOnce;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.same;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.AdditionalMatchers.or;

import com.pseuco.np21.shared.Ant;
import com.pseuco.np21.shared.Parser;
import com.pseuco.np21.shared.Recorder;
import com.pseuco.np21.shared.World;
import com.pseuco.np21.shared.Recorder.DespawnReason;
import com.pseuco.np21.shared.Recorder.SelectionReason;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;
import org.mockito.InOrder;

import java.io.IOException;
import java.lang.reflect.Field;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.stream.Collectors;

public class TerminationTests {

    private static String map;
    private static String ants;

    private static Field clearingAntsField;
    private static Field clearingFoodField;

    private Factory factory;
    private World<Clearing, Trail> world;
    private Clearing anthill;
    private Map<String, Clearing> clearings;
    private Ant ant;

    private Recorder recorder;

    @BeforeAll
    static void loadMap() throws IOException, URISyntaxException, NoSuchFieldException {
        final var classLoader = TerminationTests.class.getClassLoader();
        final var mapFile = classLoader.getResource("maps/line.map").toURI();
        final var antsFile = classLoader.getResource("ants/herbert.ants").toURI();

        map = Files.readString(Path.of(mapFile));
        ants = Files.readString(Path.of(antsFile));

        // In antEatenWhileEnoughFood() we’ll be modifying the ant count in clearing A
        // to let the ant die because it was eaten.
        clearingAntsField = Clearing.class.getDeclaredField("ants");
        clearingAntsField.setAccessible(true);

        // In fasterAnts() we’ll be modifying the amount of available food in
        // clearing C.
        clearingFoodField = Clearing.class.getDeclaredField("food");
        clearingFoodField.setAccessible(true);
    }

    @BeforeEach
    void setUp() {
        factory = new Factory();
        recorder = mock(Recorder.class);
    }

    private void createWorld(final String name, final int foodThreshold) {
        final var mapName = Parser.parse(map, ants, factory);
        world = factory.finishWorld(name + ": " + mapName, foodThreshold);
        anthill = world.anthill();
        ant = world.ants().get(0);

        clearings = world.clearings().stream()
                .collect(Collectors.toMap(com.pseuco.np21.shared.Clearing::name, clearing -> clearing));
    }

    private void verifyWalk(final InOrder inOrder, final Recorder recorder, final SelectionReason reason,
                            final String... path) {
        assert path.length >= 2; // first clearing is the current clearing

        var tmp = clearings.get(path[0]);
        for (int i = 1; i < path.length; i++) {
            final var from = tmp;
            final var to = clearings.get(path[i]);
            final var trail = from.connectsTo().stream().filter(t -> t.to() == to).findAny().get();

            inOrder.verify(recorder).select(eq(ant), same(trail), any(), same(reason));
            inOrder.verify(recorder).enter(eq(ant), same(trail));
            inOrder.verify(recorder).leave(eq(ant), same(from));
            inOrder.verify(recorder).enter(eq(ant), same(to));
            inOrder.verify(recorder).leave(eq(ant), same(trail));

            tmp = to;
        }
    }

    @Test
    @Timeout(value = 1)
    void enoughFoodImmediateTermination() {
        createWorld("enoughFoodImmediateTermination", 0);
        new Simulator(world, recorder).run();

        final var inOrder = inOrder(recorder);

        inOrder.verify(recorder).start();

        inOrder.verify(recorder).spawn(eq(ant));
        inOrder.verify(recorder).enter(eq(ant), same(anthill));

        inOrder.verify(recorder).leave(eq(ant), same(anthill));
        inOrder.verify(recorder).despawn(eq(ant), same(DespawnReason.ENOUGH_FOOD_COLLECTED));

        inOrder.verify(recorder).stop();

        verifyNoMoreInteractions(recorder);

        // Whether this behavior is correct is discussed in
        // https://np21.pseuco.com/t/terminierung-durch-isfoodleft/783
    }

    @Test
    @Timeout(value = 1)
    void enoughFoodSimpleThresholdOne() {
        createWorld("enoughFoodSimpleThresholdOne", 1);
        new Simulator(world, recorder).run();

        // don’t care a lot about pheromones
        verify(recorder, times(3)).updateAnthill(eq(ant), any(), any());
        verify(recorder, times(3)).updateFood(eq(ant), any(), any());

        final var inOrder = inOrder(recorder);

        inOrder.verify(recorder).start();

        inOrder.verify(recorder).spawn(eq(ant));
        inOrder.verify(recorder).enter(eq(ant), same(anthill));

        inOrder.verify(recorder).startFoodSearch(eq(ant));
        inOrder.verify(recorder).startExploration(eq(ant));

        verifyWalk(inOrder, recorder, SelectionReason.EXPLORATION, "Hill", "A", "B", "C");

        inOrder.verify(recorder).pickupFood(eq(ant), same(clearings.get("C")));
        inOrder.verify(recorder).startFoodReturn(eq(ant));

        verifyWalk(inOrder, recorder, SelectionReason.RETURN_IN_SEQUENCE, "C", "B", "A", "Hill");

        inOrder.verify(recorder).returnedFood(eq(ant));

        inOrder.verify(recorder).leave(eq(ant), same(anthill));
        inOrder.verify(recorder).despawn(eq(ant), same(DespawnReason.ENOUGH_FOOD_COLLECTED));

        inOrder.verify(recorder).stop();

        inOrder.verifyNoMoreInteractions();
    }

    @Test
    @Timeout(value = 1)
    void enoughFoodAtReturn() {
        createWorld("enoughFoodAtReturn", 2);

        // stub recorder.returnedFood() such that a second food unit is returned
        doAnswer(invocation -> {
            world.foodCollected();
            return null;
        }).when(recorder).returnedFood(any());

        new Simulator(world, recorder).run();

        // don’t care a lot about pheromones
        verify(recorder, times(3)).updateAnthill(eq(ant), any(), any());
        verify(recorder, times(3)).updateFood(eq(ant), any(), any());

        final var inOrder = inOrder(recorder);

        inOrder.verify(recorder).start();

        inOrder.verify(recorder).spawn(eq(ant));
        inOrder.verify(recorder).enter(eq(ant), same(anthill));

        inOrder.verify(recorder).startFoodSearch(eq(ant));
        inOrder.verify(recorder).startExploration(eq(ant));

        verifyWalk(inOrder, recorder, SelectionReason.EXPLORATION, "Hill", "A", "B", "C");

        inOrder.verify(recorder).pickupFood(eq(ant), same(clearings.get("C")));
        inOrder.verify(recorder).startFoodReturn(eq(ant));

        verifyWalk(inOrder, recorder, SelectionReason.RETURN_IN_SEQUENCE, "C", "B", "A", "Hill");

        inOrder.verify(recorder).returnedFood(eq(ant));
        // at this moment a second food unit came in
        // -> the ant should now terminate with ENOUGH_FOOD_COLLECTED

        inOrder.verify(recorder).leave(eq(ant), same(anthill));
        inOrder.verify(recorder).despawn(eq(ant), same(DespawnReason.ENOUGH_FOOD_COLLECTED));

        inOrder.verify(recorder).stop();

        verifyNoMoreInteractions(recorder);
    }

    @Test
    @Timeout(value = 1)
    void enoughFoodAtPickup() {
        createWorld("enoughFoodAtPickup", 1);

        // stub recorder.pickupFood() such that a second food unit is returned
        doAnswer(invocation -> {
            world.foodCollected();
            return null;
        }).when(recorder).pickupFood(any(), any());

        new Simulator(world, recorder).run();

        // don’t care a lot about pheromones
        verify(recorder, times(3)).updateAnthill(eq(ant), any(), any());
        verify(recorder, atMost(3)).updateFood(eq(ant), any(), any());

        final var inOrder = inOrder(recorder);

        inOrder.verify(recorder).start();

        inOrder.verify(recorder).spawn(eq(ant));
        inOrder.verify(recorder).enter(eq(ant), same(anthill));

        inOrder.verify(recorder).startFoodSearch(eq(ant));
        inOrder.verify(recorder).startExploration(eq(ant));

        verifyWalk(inOrder, recorder, SelectionReason.EXPLORATION, "Hill", "A", "B", "C");

        inOrder.verify(recorder).pickupFood(eq(ant), same(clearings.get("C")));

        // Now, the food threshold is reached and the ant should evenutally terminate
        // with ENOUGH_FOOD_COLLECTED.

        verify(recorder, atMostOnce()).startFoodReturn(eq(ant)); // atMost() does not work with inOrder

        // ignore the walk – note: atMost() takes the total numbers
        verify(recorder, atMost(3)).select(eq(ant), any(), any(), same(SelectionReason.RETURN_IN_SEQUENCE));
        verify(recorder, atMost(6)).enter(eq(ant), any(Trail.class));
        verify(recorder, atMost(6)).leave(eq(ant), any(Trail.class));
        verify(recorder, atMost(7)).enter(eq(ant), any(Clearing.class));
        verify(recorder, atMost(7)).leave(eq(ant), any(Clearing.class));

        verify(recorder, atMostOnce()).returnedFood(eq(ant)); // atMost() does not work with inOrder

        inOrder.verify(recorder).despawn(eq(ant), same(DespawnReason.ENOUGH_FOOD_COLLECTED));

        inOrder.verify(recorder).stop();

        verifyNoMoreInteractions(recorder);
    }

    @Test
    @Timeout(value = 1)
    void enoughFoodAndInterruptAtPickup() {
        createWorld("enoughFoodAndInterruptAtPickup", 1);

        final var clearingC = clearings.get("C");

        // stub recorder.pickupFood() such that a second food unit is returned and the
        // ant’s thread is interrupted
        doAnswer(invocation -> {
            world.foodCollected();
            Thread.currentThread().interrupt();
            return null;
        }).when(recorder).pickupFood(any(), any());

        new Simulator(world, recorder).run();

        // don’t care a lot about pheromones
        verify(recorder, times(3)).updateAnthill(eq(ant), any(), any());
        // no food pheromone updates because of termination!

        final var inOrder = inOrder(recorder);

        inOrder.verify(recorder).start();

        inOrder.verify(recorder).spawn(eq(ant));
        inOrder.verify(recorder).enter(eq(ant), same(anthill));

        inOrder.verify(recorder).startFoodSearch(eq(ant));
        inOrder.verify(recorder).startExploration(eq(ant));

        verifyWalk(inOrder, recorder, SelectionReason.EXPLORATION, "Hill", "A", "B", "C");

        inOrder.verify(recorder).pickupFood(eq(ant), same(clearingC));

        // now, the food threshold is reached and the ant should terminate
        // with either TERMINATED or ENOUGH_FOOD_COLLECTED

        // permit startFoodReturn and select (no inOrder since it does not work with
        // atMost())
        verify(recorder, atMostOnce()).startFoodReturn(eq(ant));
        verify(recorder, atMostOnce()).select(eq(ant), same(clearingC.connectsTo().get(0)), any(),
                same(SelectionReason.RETURN_IN_SEQUENCE));

        // The ant must leave clearing C …
        inOrder.verify(recorder).leave(eq(ant), same(clearingC));
        // … however we assume that it will notice its interrupt status when it tries to
        // enter the trail and thus terminates next. Note that this is stricter than the
        // specification but I’d consider this to be good practice.
        inOrder.verify(recorder).despawn(eq(ant),
                or(same(DespawnReason.ENOUGH_FOOD_COLLECTED), same(DespawnReason.TERMINATED)));
        // Whether both ENOUGH_FOOD_COLLECTED and TERMINATED are allowed is discussed in
        // https://np21.pseuco.com/t/terminierung-aus-mehreren-gruenden/781

        inOrder.verify(recorder).stop();

        verifyNoMoreInteractions(recorder);
    }

    @Test
    @Timeout(value = 2)
    void antEatenWhileEnoughFood() {
        createWorld("antEatenWhileEnoughFood", 1);

        final var clearingA = clearings.get("A");
        final var trail = anthill.connectsTo().get(0);

        // stub recorder.enter(Trail) such that the ant cannot enter clearing A
        // beacuse it is full and the foodThreshold is reached while the ant is waiting
        doAnswer(invocation -> {
            clearingAntsField.set(clearingA, 1);
            new Timer().schedule(new TimerTask() {
                @Override
                public void run() {
                    world.foodCollected();
                }
            }, 500 /* ms */); // Herbert has disguise 1000
            return null;
        }).when(recorder).enter(any(), any(Trail.class));

        new Simulator(world, recorder).run();

        final var inOrder = inOrder(recorder);

        inOrder.verify(recorder).start();

        inOrder.verify(recorder).spawn(eq(ant));
        inOrder.verify(recorder).enter(eq(ant), same(anthill));

        inOrder.verify(recorder).startFoodSearch(eq(ant));
        inOrder.verify(recorder).startExploration(eq(ant));

        inOrder.verify(recorder).select(eq(ant), same(trail), any(), same(SelectionReason.EXPLORATION));
        inOrder.verify(recorder).enter(eq(ant), same(trail));
        inOrder.verify(recorder).leave(eq(ant), same(anthill));

        // Now the ant gets discovered and eaten – sad story.
        inOrder.verify(recorder).attractAttention(eq(ant));
        inOrder.verify(recorder).leave(eq(ant), same(trail));
        // I’m not that sure if the order is as above (attractAttention() and then
        // leave()) or the other way round or whether it does not matter.

        inOrder.verify(recorder).despawn(eq(ant), same(DespawnReason.DISCOVERED_AND_EATEN));
        // Whether the despawn reason is correct is discussed in
        // https://np21.pseuco.com/t/terminierung-aus-mehreren-gruenden/781

        inOrder.verify(recorder).stop();

        verifyNoMoreInteractions(recorder);
    }

    @Test
    @Timeout(value = 1)
    void fasterAnts() {
        createWorld("fasterAnts", 1);

        final var clearingC = clearings.get("C");

        // Stub recorder.pickupFood() such we simulate a faster ant: At the moment when
        // Herbert enters clearing C, the food disappers and the food threshold is
        // reached.
        doAnswer(invocation -> {
            clearingFoodField.setInt(clearingC, 0);
            world.foodCollected();
            return null;
        }).when(recorder).enter(any(), eq(clearingC));

        new Simulator(world, recorder).run();

        // don’t care a lot about pheromones
        verify(recorder, times(3)).updateAnthill(eq(ant), any(), any());
        verify(recorder, atMost(3)).updateFood(eq(ant), any(), any());

        final var inOrder = inOrder(recorder);

        inOrder.verify(recorder).start();

        inOrder.verify(recorder).spawn(eq(ant));
        inOrder.verify(recorder).enter(eq(ant), same(anthill));

        inOrder.verify(recorder).startFoodSearch(eq(ant));
        inOrder.verify(recorder).startExploration(eq(ant));

        verifyWalk(inOrder, recorder, SelectionReason.EXPLORATION, "Hill", "A", "B", "C");

        // Now, the food threshold is reached and no food is available anymore. Hence,
        // the ant should evenutally terminate with either ENOUGH_FOOD_COLLECTED or
        // TERMINATED. According to
        // https://np21.pseuco.com/t/terminierung-aus-mehreren-gruenden/781/2, both
        // reasons should be correct.

        // ignore the walk – note: atMost() takes the total numbers
        verify(recorder, atMost(3)).select(eq(ant), any(), any(), same(SelectionReason.NO_FOOD_RETURN));
        verify(recorder, atMost(6)).enter(eq(ant), any(Trail.class));
        verify(recorder, atMost(6)).leave(eq(ant), any(Trail.class));
        verify(recorder, atMost(7)).enter(eq(ant), any(Clearing.class));
        verify(recorder, atMost(7)).leave(eq(ant), any(Clearing.class));

        inOrder.verify(recorder).despawn(eq(ant),
                or(same(DespawnReason.ENOUGH_FOOD_COLLECTED), same(DespawnReason.TERMINATED)));

        inOrder.verify(recorder).stop();

        verifyNoMoreInteractions(recorder);
    }
}