package com.pseuco.np21.shared;


import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.same;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.withSettings;
import com.pseuco.np21.Simulator;
import com.pseuco.np21.shared.Ant;
import com.pseuco.np21.shared.Parser;
import com.pseuco.np21.shared.Recorder;
import com.pseuco.np21.shared.Trail.Pheromone;
import com.pseuco.np21.shared.Trail.*;
import com.pseuco.np21.Trail;
import com.pseuco.np21.shared.World;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatchers;
import org.mockito.InOrder;
import org.mockito.Mockito;
import com.pseuco.np21.Factory;

public class SingleAntOnlyMaP {

  private com.pseuco.np21.shared.Clearing anthill;
  private Map<String, com.pseuco.np21.shared.Clearing> clearings;
  private Ant ant;

  private Simulator simulator;
  private Recorder recorder;

  private Trail getTrail(String from, String to) {
    List<Trail> t = clearings.get(from).connectsTo();
    return t.stream().filter(trail -> trail.to().name().equals(to)).findAny().get();
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
    World<com.pseuco.np21.Clearing, com.pseuco.np21.Trail> world = factory.finishWorld(world_name, -1);

    ant = world.ants().get(0);
    anthill = world.anthill();
    clearings = world.clearings().stream().collect(Collectors.toMap(
        com.pseuco.np21.shared.Clearing::name,
        clearing -> clearing
    ));

    simulator = new Simulator(world, recorder);
    setFoodPheromone("Hill", "C995", Pheromone.INFINITE);
    setFoodPheromone("Hill", "C994", Pheromone.INFINITE);
    setFoodPheromone("Hill", "C987", Pheromone.INFINITE);
  }


  @Test
  public void run(){

    simulator.run();

    InOrder inOrder = inOrder(recorder);

    inOrder.verify(recorder).start();

    inOrder.verify(recorder).spawn(ArgumentMatchers.eq(ant));
    inOrder.verify(recorder).enter(ArgumentMatchers.eq(ant), ArgumentMatchers.same(anthill));

    inOrder.verify(recorder).startFoodSearch(ArgumentMatchers.eq(ant));


    inOrder.verify(recorder).leave(ant, anthill);
    inOrder.verify(recorder).despawn(ant, Recorder.DespawnReason.TERMINATED);

    inOrder.verify(recorder).stop();

    Mockito.verifyNoMoreInteractions(recorder);
  }
}