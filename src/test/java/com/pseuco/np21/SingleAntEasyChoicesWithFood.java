package com.pseuco.np21;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.same;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.withSettings;
import com.pseuco.np21.shared.Ant;
import com.pseuco.np21.shared.Parser;
import com.pseuco.np21.shared.Recorder;
import com.pseuco.np21.shared.Recorder.SelectionReason;
import com.pseuco.np21.shared.Trail.Pheromone;
import com.pseuco.np21.shared.World;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.api.Test;
import org.mockito.InOrder;
import org.mockito.Mockito;

public class SingleAntEasyChoicesWithFood {

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
                    C993;3;0;(-3,-5)
                    C994;5;0;(7,3)
                    C995;4;0;(4,0)
                    C996;4;0;(-2,1)
                    C997;2;0;(-4,3)
                    C998;5;0;(0,1)
                    C999;5;0;(-4,0)
                    C976;5;0;(8,6)
                    C979;3;0;(-5,-3)
                    C980;1;0;(0,-5)
                    C981;4;0;(-9,2)
                    C984;1;0;(10,3)
                    C985;3;0;(-4,-8)
                    C987;4;0;(2,3)
                    C988;3;0;(-6,1)
                    C990;3;1;(1,-4)
                    C991;2;200;(2,-1)
                    Hill-C995
                    Hill-C991
                    C995-C990
                    C995-C991
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
        setFoodPheromone("Hill", "C991", Pheromone.INFINITE);
        setFoodPheromone("C995", "C991", Pheromone.INFINITE);
        setFoodPheromone("C995", "C990", Pheromone.get(1));

    }

    @RepeatedTest(5)
   // @Test
    void run() {
        simulator.run();

        inOrder = inOrder(recorder);

        inOrder.verify(recorder).start();

        inOrder.verify(recorder).spawn(eq(ant));
        inOrder.verify(recorder).enter(eq(ant), same(anthill));

        inOrder.verify(recorder).startFoodSearch(eq(ant));

        inOrder.verify(recorder).startExploration(eq(ant));

        Trail trail = getTrail("Hill", "C995");
        exploreSingleTrail(trail);
        inOrder.verify(recorder).updateAnthill(ant, trail.reverse(), Pheromone.get(1));

        trail = getTrail("C995", "C990");
        inOrder.verify(recorder).select(ant, trail, List.of(trail), SelectionReason.FOOD_SEARCH);
        inOrder.verify(recorder).enter(ant, trail);
        inOrder.verify(recorder).leave(ant, trail.from());
        inOrder.verify(recorder).enter(ant, trail.to());
        inOrder.verify(recorder).leave(ant, trail);
        inOrder.verify(recorder).updateAnthill(ant, trail.reverse(), Pheromone.get(2));

        inOrder.verify(recorder).pickupFood(ant, clearings.get("C990"));
        inOrder.verify(recorder).startFoodReturn(ant);

        trail = getTrail("C990", "C995");

        inOrder.verify(recorder).select(eq(ant), same(trail), any(), eq(SelectionReason.RETURN_IN_SEQUENCE));
        inOrder.verify(recorder).enter(ant, trail);
        inOrder.verify(recorder).leave(ant, trail.from());
        inOrder.verify(recorder).enter(ant, trail.to());
        inOrder.verify(recorder).leave(ant, trail);

        trail = getTrail("C995", "Hill");

        inOrder.verify(recorder).select(eq(ant), same(trail), any(), eq(SelectionReason.RETURN_IN_SEQUENCE));
        inOrder.verify(recorder).enter(ant, trail);
        inOrder.verify(recorder).leave(ant, trail.from());
        inOrder.verify(recorder).enter(ant, trail.to());
        inOrder.verify(recorder).leave(ant, trail);

        inOrder.verify(recorder).returnedFood(ant);

        inOrder.verify(recorder).startFoodSearch(eq(ant));

        inOrder.verify(recorder).startExploration(eq(ant));

        trail = getTrail("Hill", "C995");
        exploreSingleTrail(trail);
        inOrder.verify(recorder).updateAnthill(ant, trail.reverse(), Pheromone.get(1));

        trail = getTrail("C995", "C990");
        inOrder.verify(recorder).select(ant, trail, List.of(trail), SelectionReason.FOOD_SEARCH);
        inOrder.verify(recorder).enter(ant, trail);
        inOrder.verify(recorder).leave(ant, trail.from());
        inOrder.verify(recorder).enter(ant, trail.to());
        inOrder.verify(recorder).leave(ant, trail);
        inOrder.verify(recorder).updateAnthill(ant, trail.reverse(), Pheromone.get(2));

        returnTrail(trail.reverse());
        returnTrail(getTrail("C995", "Hill"));

        inOrder.verify(recorder).leave(ant, anthill);
        inOrder.verify(recorder).despawn(ant, Recorder.DespawnReason.TERMINATED);

        inOrder.verify(recorder).stop();

        Mockito.verifyNoMoreInteractions(recorder);
    }

    private void exploreSingleTrail(Trail trail) {
        inOrder.verify(recorder).select(ant, trail, List.of(trail), SelectionReason.EXPLORATION);
        inOrder.verify(recorder).enter(ant, trail);
        inOrder.verify(recorder).leave(ant, trail.from());
        inOrder.verify(recorder).enter(ant, trail.to());
        inOrder.verify(recorder).leave(ant, trail);
    }

    private void returnTrail(Trail trail) {
        inOrder.verify(recorder).select(eq(ant), same(trail), any(), eq(SelectionReason.NO_FOOD_RETURN));
        inOrder.verify(recorder).enter(ant, trail);
        inOrder.verify(recorder).leave(ant, trail.from());
        inOrder.verify(recorder).enter(ant, trail.to());
        inOrder.verify(recorder).leave(ant, trail);
        inOrder.verify(recorder).updateFood(ant, trail.reverse(), Pheromone.INFINITE);
    }

}