package com.pseuco.np21;

import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.AdditionalMatchers.or;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.same;
import static org.mockito.Mockito.withSettings;

import com.pseuco.np21.shared.Ant;
import com.pseuco.np21.shared.Parser;
import com.pseuco.np21.shared.Recorder;
import com.pseuco.np21.shared.Recorder.SelectionReason;
import com.pseuco.np21.shared.Trail.Pheromone;
import com.pseuco.np21.shared.World;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InOrder;
import org.mockito.Mockito;

class SingleAntNoFood {

    private Clearing anthill;
    private Map<String, Clearing> clearings;
    private Ant ant;

    private Simulator simulator;
    private Recorder recorder;
    private InOrder inOrder;

    private Trail getTrail(String from, String to) {
        return clearings.get(from).connectsTo().stream()
                .filter(trail -> trail.to().name().equals(to)).findAny().get();
    }

    public void setFoodPheromone(String from, String to, Pheromone value) {
        try {
            var food = getTrail(from, to).getClass().getDeclaredField("food");
            food.setAccessible(true);
            food.set(getTrail(from, to), value);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            e.printStackTrace();
        }
    }

    @BeforeEach
    void setUp() {
        Trail.class.getClassLoader().setClassAssertionStatus(Trail.class.getName(), false);
        final var factory = new Factory();
        String world_name = Parser.parse(
                """
                    rand
                    Hill;-1;0;(5,2)
                    C993;3;6;(-3,-5)
                    C994;5;0;(7,3)
                    C995;4;0;(4,0)
                    C996;4;10;(-2,1)
                    C997;2;0;(-4,3)
                    C998;5;0;(0,1)
                    C999;5;0;(-4,0)
                    C976;5;0;(8,6)
                    C979;3;0;(-5,-3)
                    C980;1;3;(0,-5)
                    C981;4;0;(-9,2)
                    C984;1;0;(10,3)
                    C985;3;1;(-4,-8)
                    C987;4;0;(2,3)
                    C988;3;0;(-6,1)
                    C990;3;0;(1,-4)
                    C991;2;0;(2,-1)
                    Hill-C987
                    Hill-C994
                    Hill-C995
                    C993-C979
                    C993-C980
                    C993-C985
                    C994-C976
                    C994-C984
                    C995-C991
                    C996-C997
                    C996-C998
                    C996-C999
                    C997-C988
                    C998-C987
                    C998-C991
                    C999-C979
                    C999-C988
                    C980-C990
                    C981-C988
                    C990-C991
                    """,
                "Anthony;2;1000", factory);
        recorder = mock(Recorder.class, withSettings().verboseLogging());
        World<Clearing, Trail> world = factory.finishWorld(world_name, -1);

        ant = world.ants().get(0);
        anthill = world.anthill();
        clearings = world.clearings().stream().collect(Collectors.toMap(
                com.pseuco.np21.shared.Clearing::name,
                clearing -> clearing
        ));

        simulator = new Simulator(world, recorder);
        setFoodPheromone("Hill", "C995", Pheromone.get(3));
        setFoodPheromone("C998", "C996", Pheromone.INFINITE);
        // updateFood("C998", "C991", Pheromone.INFINITE);
        setFoodPheromone("C991", "C990", Pheromone.INFINITE);
    }

    @RepeatedTest(5)
    //@Test
    void run() {
        simulator.run();

        inOrder = inOrder(recorder);

        inOrder.verify(recorder).start();

        inOrder.verify(recorder).spawn(eq(ant));
        inOrder.verify(recorder).enter(eq(ant), same(anthill));

        inOrder.verify(recorder).startFoodSearch(eq(ant));

        inOrder.verify(recorder).startExploration(eq(ant));

        ArgumentCaptor<Trail> tHill = ArgumentCaptor.forClass(Trail.class);
        inOrder.verify(recorder).select(eq(ant), tHill.capture(),
                or(eq(List.of(getTrail("Hill", "C987"), getTrail("Hill", "C994"))),
                        eq(List.of(getTrail("Hill", "C994"), getTrail("Hill", "C987")))),
                same(Recorder.SelectionReason.EXPLORATION));

        assertTrue(tHill.getValue() == getTrail("Hill", "C987")
                || tHill.getValue() == getTrail("Hill", "C994"));

        Function<Trail, Void> exploreFromHill = fromHill -> {
            inOrder.verify(recorder).enter(ant, fromHill);
            inOrder.verify(recorder).leave(ant, anthill);

            if (fromHill == getTrail("Hill", "C987")) {
                inOrder.verify(recorder).enter(ant, clearings.get("C987"));
                inOrder.verify(recorder).leave(ant, fromHill);

                inOrder.verify(recorder).updateAnthill(ant, fromHill.reverse(), Pheromone.get(1));

                exploreSingleTrail(getTrail("C987", "C998"));
                inOrder.verify(recorder)
                        .updateAnthill(ant, getTrail("C998", "C987"), Pheromone.get(2));

                exploreSingleTrail(getTrail("C998", "C991"));
                inOrder.verify(recorder)
                        .updateAnthill(ant, getTrail("C991", "C998"), Pheromone.get(3));

                exploreSingleTrail(getTrail("C991", "C995"));
                inOrder.verify(recorder)
                        .updateAnthill(ant, getTrail("C995", "C991"), Pheromone.get(4));

                exploreSingleTrail(getTrail("C995", "Hill"));

                // (d) IMMEDIATE_RETURN
                returnImmediate(getTrail("Hill", "C995"));

                returnTrail(getTrail("C995", "C991"));
                returnTrail(getTrail("C991", "C998"));
                returnTrail(getTrail("C998", "C987"));
                returnTrail(getTrail("C987", "Hill"));

            } else if (fromHill == getTrail("Hill", "C994")) {
                inOrder.verify(recorder).enter(ant, clearings.get("C994"));
                inOrder.verify(recorder).leave(ant, fromHill);
                inOrder.verify(recorder)
                        .updateAnthill(ant, fromHill.reverse(), Pheromone.get(1));

                var t = ArgumentCaptor.forClass(Trail.class);
                inOrder.verify(recorder).select(eq(ant), t.capture(),
                        or(eq(List.of(getTrail("C994", "C976"), getTrail("C994", "C984"))),
                                eq(List.of(getTrail("C994", "C984"), getTrail("C994", "C976")))),
                        same(Recorder.SelectionReason.EXPLORATION));

                assertTrue(t.getValue() == getTrail("C994", "C976")
                        || t.getValue() == getTrail("C994", "C984"));

                Function<Trail, Void> checkAndReturn = trail -> {
                    inOrder.verify(recorder).enter(ant, trail);
                    inOrder.verify(recorder).leave(ant, clearings.get("C994"));

                    inOrder.verify(recorder).enter(ant, trail.to());
                    inOrder.verify(recorder).leave(ant, trail);
                    inOrder.verify(recorder).updateAnthill(ant, trail.reverse(), Pheromone.get(2));

                    // (e)
                    returnTrail(trail.reverse());

                    return null;
                };

                checkAndReturn.apply(t.getValue());

                var next = t.getValue() == getTrail("C994", "C976") ? getTrail("C994", "C984")
                        : getTrail("C994", "C976");
                assertNotEquals(t.getValue(), next);
                inOrder.verify(recorder).select(eq(ant), eq(next), eq(List.of(next)),
                        same(Recorder.SelectionReason.EXPLORATION));
                checkAndReturn.apply(next);

                // return to hill (e)
                returnTrail(getTrail("C994", "Hill"));

            }
            return null;
        };

        assertTrue(tHill.getValue() == getTrail("Hill", "C987")
                || tHill.getValue() == getTrail("Hill", "C994"));

        exploreFromHill.apply(tHill.getValue());

        var otherFromHill = tHill.getValue() == getTrail("Hill", "C987") ? getTrail("Hill", "C994")
                : getTrail("Hill", "C987");
        assertTrue(otherFromHill == getTrail("Hill", "C987")
                || otherFromHill == getTrail("Hill", "C994"));
        assertNotEquals(otherFromHill, tHill.getValue());

        inOrder.verify(recorder).select(ant, otherFromHill, List.of(otherFromHill),
                Recorder.SelectionReason.EXPLORATION);

        exploreFromHill.apply(otherFromHill);

        inOrder.verify(recorder).leave(ant, anthill);
        inOrder.verify(recorder).despawn(ant, Recorder.DespawnReason.TERMINATED);

        inOrder.verify(recorder).stop();

        Mockito.verifyNoMoreInteractions(recorder);
    }

    private void returnImmediate(Trail trail) {
        inOrder.verify(recorder).select(eq(ant), eq(trail), any(), eq(SelectionReason.IMMEDIATE_RETURN));
        inOrder.verify(recorder).enter(ant, trail);
        inOrder.verify(recorder).leave(ant, trail.from());
        inOrder.verify(recorder).enter(ant, trail.to());
        inOrder.verify(recorder).leave(ant, trail);
    }

    private void exploreSingleTrail(Trail trail) {
        inOrder.verify(recorder).select(ant, trail, List.of(trail), SelectionReason.EXPLORATION);
        inOrder.verify(recorder).enter(ant, trail);
        inOrder.verify(recorder).leave(ant, trail.from());
        inOrder.verify(recorder).enter(ant, trail.to());
        inOrder.verify(recorder).leave(ant, trail);
    }

    private void returnTrail(Trail trail) {
        inOrder.verify(recorder).select(eq(ant), eq(trail), any(), eq(SelectionReason.NO_FOOD_RETURN));
        inOrder.verify(recorder).enter(ant, trail);
        inOrder.verify(recorder).leave(ant, trail.from());
        inOrder.verify(recorder).enter(ant, trail.to());
        inOrder.verify(recorder).leave(ant, trail);
        inOrder.verify(recorder).updateFood(ant, trail.reverse(), Pheromone.INFINITE);
    }
}