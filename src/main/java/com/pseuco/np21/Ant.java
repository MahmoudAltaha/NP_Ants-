package com.pseuco.np21;

import com.pseuco.np21.shared.Position;
import com.pseuco.np21.shared.Recorder;
import com.pseuco.np21.shared.Recorder.DespawnReason;
import com.pseuco.np21.shared.Recorder.SelectionReason;
import com.pseuco.np21.shared.World;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

/**
 * Representation of an ant with behavior.
 * <p>
 * You may change the code however you see fit.
 */
public class Ant extends com.pseuco.np21.shared.Ant implements Runnable {

  private static class AntDiedException extends Throwable {

    private final boolean eaten;
    private final Position where;

    private AntDiedException(final boolean eaten, final Position where) {
      this.eaten = eaten;
      this.where = where;
    }

    private boolean wasEaten() {
      return eaten;
    }

    private Position where() {
      return where;
    }
  }

  private final World<Clearing, Trail> world;
  private final Recorder recorder;
  private Clearing position;
  private boolean died = false;
  private final List<Clearing> clearingSequence;
  public final List<Trail> TrailSequence;  // here we add the Trails Just by FoodSearch/Exploration Move
  private boolean adventurer;
  private boolean holdFood;
  public final HashMap<Integer, Trail> TrailsToVisitedClearings; // here we mark the Trail which leads to already visited clearings
  public final HashMap<Integer, Trail> alreadyEnteredTrails; // here we add all trails we have taken.
  public final SearchFoodPathCheck searchFood;
  public final AntRunHandler handler;
  private List<Trail> candidatesList;
  public boolean despawnd = false;

  public void setDespawndTrue(){
    despawnd = true;
  }

  /**
   * Constructs an ant given a basic ant, the world and a recorder.
   *
   * @param ant      the template ant
   * @param world    the ant has to live in
   * @param recorder to log all actions against
   */
  public Ant(final com.pseuco.np21.shared.Ant ant, final World<Clearing, Trail> world,
      final Recorder recorder) {
    super(ant);
    this.world = world;
    this.recorder = recorder;
    this.handler = new AntRunHandler(this);
    this.searchFood = new SearchFoodPathCheck(this);
    this.alreadyEnteredTrails = new HashMap<>();
    this.TrailsToVisitedClearings = new HashMap<>();
    this.clearingSequence = new LinkedList<>();
    this.holdFood = false;
    this.adventurer = false;
    this.TrailSequence = new LinkedList<>();
    this.candidatesList = new LinkedList<>();
  }


  public HashMap<Integer, Trail> getTrailsToVisitedClearings() { return TrailsToVisitedClearings; }
  public List<Trail> getTrailSequence() { return TrailSequence; }
  public HashMap<Integer, Trail> getAlreadyEnteredTrails() { return alreadyEnteredTrails; }
  public boolean isDied() { return died; }
  public void setDied(boolean died) { this.died = died; }
  public void setCandidatesList(List<Trail> candidatesList) { this.candidatesList = candidatesList; }
  public List<Trail> getCandidatesList() { return candidatesList; }
  public Recorder getRecorder() { return recorder; }
  public World<Clearing, Trail> getWorld() { return world; }

  /**
   * check if the Ant holds food.
   *
   * @return true if he Ant holds food.
   */
  public boolean hasFood() {
    return holdFood;
  }

  /**
   * Set holdFood to true when the Ant has picked up some food and to false when it drops the food.
   *
   * @param holdFood true or false.
   */
  public void setHoldFood(boolean holdFood) {
    this.holdFood = holdFood;
  }


  /**
   * check the state of the Ant
   *
   * @return true if the Ant is an adventurer.
   */
  public boolean isAdventurer() {
    return adventurer;
  }

  /**
   * change the current state of the Ant to Adventurer
   */
  public void setAntTOAdventure() {
    adventurer = true;
  }

  /**
   * return the state of the Ant to normal.
   */
  public void setAntTONormalState() {
    adventurer = false;
  }

  /**
   * this methode used to get the current Sequence.
   *
   * @return the sequence (visited Clearings).
   */
  public List<Clearing> getClearingSequence() {
    return clearingSequence;
  }

  /**
   * this methode used to add a new Clearing to the Sequence.
   *
   * @param c the Clearing to be added.
   */
  public void addClearingToSequence(Clearing c) {
    clearingSequence.add(c);
  }

  /**
   * this methode is used to check if the Clearing is already in the sequence.
   *
   * @param c Clearing
   * @return true if the Clearing is already in the sequence.
   */
  public boolean isInSequence(Clearing c) {
    for (int i = 0; i < getClearingSequence().size(); i++) {
      if (getClearingSequence().get(i).id() == c.id()) {
        return true;
      }
    }
    return false;
  }

  /**
   * this methode is the Brain of the Ants Simulation , it controls all classes and methods.
   *
   * @param currentPosition the Current Clearing where the Ant is now.
   * @throws InterruptedException InterruptedException  to terminate.
   */
  public void forwardMoving(Clearing currentPosition) throws InterruptedException {
    Clearing position = currentPosition;
    boolean foundATrail = searchFood.checkTrail(position); // check if there is a valid trail from this Clearing.
    // if the Clearing was the Hill and has no Trails terminate. (we need this just for the first round )
    if (position.id() == world.anthill().id() && !foundATrail) { // if this clearing is the hill && no Trails have been found
      handler.leaveAndInterruptAtHill(position); // terminate
    }// now the Clearing is not the Hill(when this is recursion round 2 or more) or maybe hill(first round or more) but has (for sure)some Trails.
    if (foundATrail) {  // found A Trail == true --> so we begin with Food Search moves.
      Trail targetTrail = searchFood.getTargetTrail(position); // get The Trail
      if (targetTrail.getOrUpdateFoodPheromone(false, null, false).isAPheromone()) {// if Trail has non-Nap pheromone
        recorder.select(this, targetTrail, this.candidatesList, SelectionReason.FOOD_SEARCH); // select FoodSEarch
      } else { // otherwise select Exploration , but first check if we need to select StartExploration also.
        if (!this.isAdventurer()) { // if the Ant not Adventurer then this is the first time it enter a Trail with Nap-Pheromone
          this.setAntTOAdventure(); // make it Adventurer .
          recorder.startExploration(this);
          recorder.select(this, targetTrail, this.candidatesList, SelectionReason.EXPLORATION);
        } else { // the Ant is Already Adventurer so we don't need to give(recorder signal "StartExploration").
          recorder.select(this, targetTrail, this.candidatesList, SelectionReason.EXPLORATION);
        }
      }
      boolean success = targetTrail.enterTrail(position, this, EntryReason.FOOD_SEARCH);  // enter the Trail
      handler.EnterTrailRecorderStuff(position, targetTrail, success); // do the recorder stuff
      Trail ourLastTrail = targetTrail;
      Clearing ourNextClearing = targetTrail.to();
      boolean clearingAlreadyInSequence = isInSequence(ourNextClearing); // check if the next Clearing is Already in sequence and save the info.
      handler.handleEnterClearingFoodSearch(ourNextClearing, ourLastTrail);// handle the Entering to this Clearing and the recorder stuff.
      position = ourNextClearing; // update the Position
      // now check if the Clearing is in the sequence:
      if (!clearingAlreadyInSequence) { // clearing is not already in sequence
        if (!this.hasFood()) {// if the Ant does not have food already try to get some
          if (position.TakeOnPieceOfFood(this)) { // if the Ant picked up some food start homeward. otherwise there is no Food or the Ant should for some reason terminate
            recorder.pickupFood(this, position);
            recorder.startFoodReturn(this);
            if (position.getOrSetFood(FoodInClearing.HAS_FOOD)) { // was that the last piece of food ??
              handler.homewardMoving(true, position, getClearingSequence());//start Homeward but don't update pheromone
              return;
            } else {
              handler.homewardMoving(false, position, getClearingSequence()); // start homeward and update Food pheromone.
              return;
            }
          } if(Thread.currentThread().isInterrupted()){
            recorder.despawn(this, DespawnReason.TERMINATED);
            this.setDespawndTrue();
            throw new InterruptedException();
          }
          if (! world.isFoodLeft()){
            recorder.despawn(this, DespawnReason.ENOUGH_FOOD_COLLECTED);
            this.setDespawndTrue();
            Thread.currentThread().interrupt();
            throw new InterruptedException();
          }
          else{// the Clearing has no food  so continue try to get new Trail and search for Food (do the same above --> recursion)
            forwardMoving(position);
          }
        }
      } // the Clearing is already in The sequence so we go one step by Immediate-return the keep going back by No FOOD Return if we don't find way.
       else {
        position = handler.goOneStepBackWithImmediatReturn(ourLastTrail, position); // go one step back with Immediate return.
        // now we stepped back one step ,,,we should see if the clearing here has other choices
        // as long as we don't find a Clearing with undiscovered Trail we go with no Food Return.
        while (!searchFood.checkTrail(position) && getClearingSequence().size() > 1) {
          position = handler.GoBackByNoFoodReturn(searchFood, position);
        }// now we are out the while-Loop that means we found a Clearing with some Trail Or we went so many Trails back
        // until we reached the Hill ,,so we need a check.
        if ((position.id() == world.anthill().id()) && !searchFood.checkTrail(
            position)) {  // if we are in the Hill and no more choices terminate) leaveAndInterrupt(this,position);
          handler.leaveAndInterruptAtHill(position);
        }// Or we are in a Trail which has some Other undiscovered Trails. so we need to continue FoodSearch --> recursion.
        else {
          forwardMoving(position);
        }
      }// if you are here then the Ant has reached by normal FoodSearch a Trails with no more(foundATrail in line(184) == false) new Choices: so NO-Food- Return.
    } else {
      while (!searchFood.checkTrail(position) && getClearingSequence().size() > 1) {//as long as we haven't found a Clearing with valid Trail and we still not in the Hill
        position = handler.GoBackByNoFoodReturn(searchFood, position); // one step Back with no-Food-Return
      }
      if ((position.id() == world.anthill().id() && !searchFood.checkTrail(position))) {  // if we are in the Hill and no more choices terminate) leaveAndInterrupt(this,position);
        handler.leaveAndInterruptAtHill(position);
      } else { // Or we are in a Trail which has some Other undiscovered Trails. so we need to continue FoodSearch --> recursion.
        forwardMoving(position);
      }
    }
  }

  /**
   * Primary ant behavior.
   */
  public void run() {
    try {
      position = world.anthill();
      position.enter();
      recorder.spawn(this);

      recorder.enter(this, position);

      this.addClearingToSequence(position);  // adding the antHill to the sequence

      while (world.isFoodLeft()) {
        recorder.startFoodSearch(this);
        forwardMoving(position);
      }

      position.leave();
      recorder.leave(this, position);
      recorder.despawn(this, DespawnReason.ENOUGH_FOOD_COLLECTED);
      this.setDespawndTrue();

      Thread.currentThread().interrupt();/////////////////////////////

    } catch (InterruptedException e) {
      e.printStackTrace();
    }
  }
}
