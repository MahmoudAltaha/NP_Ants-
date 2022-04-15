package com.pseuco.np21;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.same;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.withSettings;

import com.pseuco.np21.shared.Ant;
import com.pseuco.np21.shared.Parser;
import com.pseuco.np21.shared.Recorder;
import com.pseuco.np21.shared.Recorder.DespawnReason;
import com.pseuco.np21.shared.Recorder.SelectionReason;
import com.pseuco.np21.shared.Trail.Pheromone;
import com.pseuco.np21.shared.World;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InOrder;

class RemoveFromSequenceTest {

    private Clearing anthill;
    private Map<String, Clearing> clearings;
    private Ant ant;


    private Simulator simulator;
    private Recorder recorder;
    private InOrder inOrder;

    Trail SpawnOne;
    Trail OneTwo;
    Trail OneThree;
    Trail OneFour;
    Trail TwoThree;

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
        final var factory = new Factory();
        String world_description = Parser.parse(
                """
                    test
                    spawn;-1;0;(0,0)
                    one;-1;0;(0,0)
                    two;2;0;(0,0)
                    three;2;2;(0,0)
                    four;2;2;(0,0)
                    spawn-one
                    one-two
                    two-three
                    one-three
                    one-four
                    """,
                "Anthony;2;100", factory);
        recorder = mock(Recorder.class, withSettings().verboseLogging());
        World<Clearing, Trail> world = factory.finishWorld(world_description, -1);
        ant = world.ants().get(0);
        anthill = world.anthill();
        clearings = world.clearings().stream().collect(Collectors.toMap(
                com.pseuco.np21.shared.Clearing::name,
                clearing -> clearing
        ));

        SpawnOne = getTrail("spawn", "one");
        OneTwo = getTrail("one", "two");
        OneThree = getTrail("one", "three");
        OneFour = getTrail("one", "four");
        TwoThree = getTrail("two", "three");

        simulator = new Simulator(world, recorder);
        setFoodPheromone("one", "two", Pheromone.get(42));
        setFoodPheromone("one", "three", Pheromone.INFINITE);
        setFoodPheromone("one", "four", Pheromone.get(69));
    }

    @Test
    void run() {
        simulator.run();

        inOrder = inOrder(recorder);

        inOrder.verify(recorder).start();

        inOrder.verify(recorder).spawn(eq(ant));
        inOrder.verify(recorder).enter(eq(ant), same(anthill));

        inOrder.verify(recorder).startFoodSearch(eq(ant));

        inOrder.verify(recorder).startExploration(eq(ant));

        // Finde Futter auf three zum ersten Mal
        inOrder.verify(recorder).select(ant, SpawnOne, List.of(SpawnOne), SelectionReason.EXPLORATION);
        takeSingleTrail(SpawnOne);
        inOrder.verify(recorder).updateAnthill(ant, SpawnOne.reverse(), Pheromone.get(1));

        inOrder.verify(recorder).select(ant, OneTwo, List.of(OneTwo), SelectionReason.FOOD_SEARCH);
        takeSingleTrail(OneTwo);
        inOrder.verify(recorder).updateAnthill(ant, OneTwo.reverse(), Pheromone.get(2));

        inOrder.verify(recorder).select(ant, TwoThree, List.of(TwoThree), SelectionReason.EXPLORATION);
        takeSingleTrail(TwoThree);
        inOrder.verify(recorder).updateAnthill(ant, TwoThree.reverse(), Pheromone.get(3));

        // Hebe Futter auf
        inOrder.verify(recorder).pickupFood(ant, clearings.get("three"));
        inOrder.verify(recorder).startFoodReturn(ant);

        // Erster Rückweg
        returnToSpawnFromThree(SelectionReason.RETURN_IN_SEQUENCE, false);

        inOrder.verify(recorder).returnedFood(ant);
        inOrder.verify(recorder).startFoodSearch(ant);


        // Hole letztes Futter von three
        inOrder.verify(recorder).select(ant, SpawnOne, List.of(SpawnOne), SelectionReason.FOOD_SEARCH);
        takeSingleTrail(SpawnOne);
        inOrder.verify(recorder).updateAnthill(ant, SpawnOne.reverse(), Pheromone.get(1));

        inOrder.verify(recorder).select(ant, OneTwo, List.of(OneTwo), SelectionReason.FOOD_SEARCH);
        takeSingleTrail(OneTwo);
        inOrder.verify(recorder).updateAnthill(ant, OneTwo.reverse(), Pheromone.get(2));

        inOrder.verify(recorder).select(ant, TwoThree, List.of(TwoThree), SelectionReason.FOOD_SEARCH);
        takeSingleTrail(TwoThree);
        inOrder.verify(recorder).updateAnthill(ant, TwoThree.reverse(), Pheromone.get(3));

        // Hebe Futter auf
        inOrder.verify(recorder).pickupFood(ant, clearings.get("three"));
        inOrder.verify(recorder).startFoodReturn(ant);

        // Zweiter Rückweg
        System.out.println("Rückweg 2");
        returnToSpawnFromThree(SelectionReason.RETURN_FOOD, true);

        inOrder.verify(recorder).returnedFood(ant);
        inOrder.verify(recorder).startFoodSearch(ant);

        // Suche erneut nach Futter
        inOrder.verify(recorder).select(ant, SpawnOne, List.of(SpawnOne), SelectionReason.FOOD_SEARCH);
        takeSingleTrail(SpawnOne);
        inOrder.verify(recorder).updateAnthill(ant, SpawnOne.reverse(), Pheromone.get(1));

        inOrder.verify(recorder).select(ant, OneTwo, List.of(OneTwo), SelectionReason.FOOD_SEARCH);
        takeSingleTrail(OneTwo);
        inOrder.verify(recorder).updateAnthill(ant, OneTwo.reverse(), Pheromone.get(2));

        inOrder.verify(recorder).select(ant, TwoThree, List.of(TwoThree), SelectionReason.FOOD_SEARCH);
        takeSingleTrail(TwoThree);
        inOrder.verify(recorder).updateAnthill(ant, TwoThree.reverse(), Pheromone.get(3));

        inOrder.verify(recorder).startExploration(eq(ant));

        inOrder.verify(recorder).select(ant, OneThree.reverse(), List.of(OneThree.reverse()), SelectionReason.EXPLORATION);
        takeSingleTrail(OneThree.reverse());

        inOrder.verify(recorder).select(eq(ant), same(OneThree), any(), eq(SelectionReason.IMMEDIATE_RETURN));
        takeSingleTrail(OneThree);

        inOrder.verify(recorder).select(eq(ant), same(TwoThree.reverse()), any(), eq(SelectionReason.NO_FOOD_RETURN));
        takeSingleTrail(TwoThree.reverse());
        inOrder.verify(recorder).updateFood(ant, TwoThree, Pheromone.INFINITE);

        inOrder.verify(recorder).select(eq(ant), same(OneTwo.reverse()), any(), eq(SelectionReason.NO_FOOD_RETURN));
        takeSingleTrail(OneTwo.reverse());
        inOrder.verify(recorder).updateFood(ant, OneTwo, Pheromone.INFINITE);

        inOrder.verify(recorder).select(ant, OneFour, List.of(OneFour), SelectionReason.FOOD_SEARCH);
        takeSingleTrail(OneFour);
        inOrder.verify(recorder).updateAnthill(ant, OneFour.reverse(), Pheromone.get(2));

        // Hebe Futter auf
        inOrder.verify(recorder).pickupFood(ant, clearings.get("four"));
        inOrder.verify(recorder).startFoodReturn(ant);

        //Rückweg
        inOrder.verify(recorder).select(eq(ant), same(OneFour.reverse()), any(), eq(SelectionReason.RETURN_IN_SEQUENCE));
        takeSingleTrail(OneFour.reverse());
        inOrder.verify(recorder).updateFood(ant, OneFour, Pheromone.get(1));

        inOrder.verify(recorder).select(eq(ant), same(SpawnOne.reverse()), any(), eq(SelectionReason.RETURN_IN_SEQUENCE));
        takeSingleTrail(SpawnOne.reverse());
        inOrder.verify(recorder).updateFood(ant, SpawnOne, Pheromone.get(2));

        inOrder.verify(recorder).returnedFood(eq(ant));
        inOrder.verify(recorder).startFoodSearch(eq(ant));

        // Gehe erneut zu four


        inOrder.verify(recorder).select(eq(ant), same(SpawnOne), any(), eq(SelectionReason.FOOD_SEARCH));
        takeSingleTrail(SpawnOne);
        inOrder.verify(recorder).updateAnthill(ant, SpawnOne.reverse(), Pheromone.get(1));

        inOrder.verify(recorder).select(eq(ant), same(OneFour), any(), eq(SelectionReason.FOOD_SEARCH));
        takeSingleTrail(OneFour);
        inOrder.verify(recorder).updateAnthill(ant, OneFour.reverse(), Pheromone.get(2));

        // Hebe Futter auf
        inOrder.verify(recorder).pickupFood(ant, clearings.get("four"));
        inOrder.verify(recorder).startFoodReturn(ant);

        // Rückweg
        inOrder.verify(recorder).select(eq(ant), same(OneFour.reverse()), any(), eq(SelectionReason.RETURN_FOOD));
        takeSingleTrail(OneFour.reverse());

        inOrder.verify(recorder).select(eq(ant), same(SpawnOne.reverse()), any(), eq(SelectionReason.RETURN_FOOD));
        takeSingleTrail(SpawnOne.reverse());

        inOrder.verify(recorder).returnedFood(eq(ant));

        inOrder.verify(recorder).leave(ant, anthill);
        inOrder.verify(recorder).despawn(ant, DespawnReason.ENOUGH_FOOD_COLLECTED);

        inOrder.verify(recorder).stop();

        verifyNoMoreInteractions(recorder);
    }

    private void returnToSpawnFromThree(SelectionReason selectionReason, Boolean lastOne) {
        inOrder.verify(recorder).select(eq(ant), same(TwoThree.reverse()), any(), eq(selectionReason));
        takeSingleTrail(TwoThree.reverse());
        if (!lastOne) {
            inOrder.verify(recorder).updateFood(ant, TwoThree, Pheromone.get(1));
        }

        inOrder.verify(recorder).select(eq(ant), same(OneTwo.reverse()), any(), eq(selectionReason));
        takeSingleTrail(OneTwo.reverse());
        if (!lastOne) {
            inOrder.verify(recorder).updateFood(ant, OneTwo, Pheromone.get(2));
        }

        inOrder.verify(recorder).select(eq(ant), same(SpawnOne.reverse()), any(), eq(selectionReason));
        takeSingleTrail(SpawnOne.reverse());
        if (!lastOne) {
            inOrder.verify(recorder).updateFood(ant, SpawnOne, Pheromone.get(3));
        }
    }

    private void takeSingleTrail(Trail trail) {
        inOrder.verify(recorder).enter(ant, trail);
        inOrder.verify(recorder).leave(ant, trail.from());
        inOrder.verify(recorder).enter(ant, trail.to());
        inOrder.verify(recorder).leave(ant, trail);
    }

}
