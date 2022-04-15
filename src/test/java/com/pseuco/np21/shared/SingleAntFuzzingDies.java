package com.pseuco.np21.shared;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.same;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.withSettings;
import com.pseuco.np21.Factory;
import com.pseuco.np21.Simulator;
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
import org.mockito.Mockito;

public class SingleAntFuzzingDies {

  private com.pseuco.np21.Clearing anthill;
  private com.pseuco.np21.Trail deathTrap;
  private Map<String, com.pseuco.np21.Clearing> clearings;
  private Ant ant;

  private Simulator simulator;
  private Recorder recorder;

  private com.pseuco.np21.Trail getTrail(String from, String to) {
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
    com.pseuco.np21.Trail.class.getClassLoader().setClassAssertionStatus(com.pseuco.np21.Trail.class.getName(), false);
    final var factory = new Factory();
    String world_name = Parser.parse(
        """
            rand
            Hill;-1;0;(5,2)
            Deathtrap;0;1000;(-3,-5)
            Hill-Deathtrap
            """,
        "Anthony;2;1", factory);
    recorder = mock(Recorder.class, withSettings().verboseLogging());
    World<com.pseuco.np21.Clearing, com.pseuco.np21.Trail> world = factory.finishWorld(world_name, -1);

    ant = world.ants().get(0);
    anthill = world.anthill();
    clearings = world.clearings().stream().collect(Collectors.toMap(
        com.pseuco.np21.shared.Clearing::name,
        clearing -> clearing
    ));

    deathTrap = getTrail("Hill", "Deathtrap");

    simulator = new Simulator(world, recorder);
    setFoodPheromone("Hill", "Deathtrap", Pheromone.get(1));
  }


  @Test
  public void run() {

    simulator.run();
    InOrder inOrder = inOrder(recorder);

    //Ant is born
    inOrder.verify(recorder).start();
    inOrder.verify(recorder).spawn(eq(ant));
    inOrder.verify(recorder).enter(eq(ant), same(anthill));
    inOrder.verify(recorder).startFoodSearch(eq(ant));


    //Sets out into the world full of hopes and dreams
    inOrder.verify(recorder).select(eq(ant), same(deathTrap), eq(List.of(deathTrap)), same(SelectionReason.FOOD_SEARCH));
    inOrder.verify(recorder).enter(ant, deathTrap);
    inOrder.verify(recorder).leave(ant, anthill);

    //Dies
    inOrder.verify(recorder).attractAttention(ant);
    inOrder.verify(recorder).leave(ant, deathTrap);
    inOrder.verify(recorder).despawn(ant, DespawnReason.DISCOVERED_AND_EATEN);

    inOrder.verify(recorder).stop();
    Mockito.verifyNoMoreInteractions(recorder);
  }
}