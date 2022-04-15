package com.pseuco.np21;

import com.pseuco.np21.shared.Recorder;
import com.pseuco.np21.shared.World;

import java.util.List;

/**
 * in this class we have some Methods to handle the Recorder stuff and the Homeward/Immediate_Return/No_Food_Return Moves.
 */
public class AntRunHandler {

        private final Ant  ant;

    public AntRunHandler(Ant ant) {
        this.ant = ant;
    }

    /**
     * this methode used (in this Class) to send the EnterClearing/LeaveTrail  Recorder Signals by all kind of Moves.
     * @param ourNextClearing  the Clearing we want to enter.
     * @param ourLastTrail the Trail we want to leave.
     */
    private void recorderStuffEnterClearingLeaveTrail(Clearing ourNextClearing, Trail ourLastTrail){
        ant.getRecorder().enter(ant, ourNextClearing);
        ant.getRecorder().leave(ant, ourLastTrail);
    }

    /**
     * this methode used (in this Class) to give recorder Signals when the Ant died on the Trail.
     * @param ourLastTrail the Trail where the Ant died.
     */
    private  void recorderStuffDeadDespawn(Trail ourLastTrail){
        ant.getRecorder().attractAttention(ant);
        ant.getRecorder().leave(ant, ourLastTrail);
        ant.getRecorder().despawn(ant, Recorder.DespawnReason.DISCOVERED_AND_EATEN);
        ant.setDespawndTrue();
    }

    /**
     * this methode used (in this Class) to send recorder signals when the ant should terminate now on some Trial.
     * @param trail the trail where the Ant notice that it should terminate.
     */
    private void recorderStuffTerminateNowOnLastTrail(Trail trail){
        ant.getRecorder().leave(ant, trail);

         if(Thread.currentThread().isInterrupted()) {
            ant.getRecorder().despawn(ant, Recorder.DespawnReason.TERMINATED);
             ant.setDespawndTrue();
        }
        else if(! ant.getWorld().isFoodLeft()) {
            ant.getRecorder().despawn(ant, Recorder.DespawnReason.ENOUGH_FOOD_COLLECTED);
             ant.setDespawndTrue();
             Thread.currentThread().interrupt();
        }
    }

    /**
     * this methode sends recorder signals when the ant want to enter a Trail
     * @param currentClearing  the Clearing where the Ant is now.
     * @param target  the Trail which the ant want to enter
     * @param success   flag if the Ant has entered the Trail Successfully to know which signal to send.
     *                  if this flag was false then the Ant was not able to enter the Trail cause it received a call to terminate.
     * @throws InterruptedException InterruptedException should be thrown if the ant should terminate now.
     */
    public void EnterTrailRecorderStuff(Clearing currentClearing , Trail target, boolean success) throws InterruptedException {
        if(success){
            ant.getRecorder().enter(ant,target);
            ant.getRecorder().leave(ant,currentClearing);

        }else {
            ant.getRecorder().leave(ant,currentClearing);

            if(Thread.currentThread().isInterrupted()) {
                ant.getRecorder().despawn(ant, Recorder.DespawnReason.TERMINATED);
                ant.setDespawndTrue();
            }
            else if(! ant.getWorld().isFoodLeft()) {
                ant.getRecorder().despawn(ant, Recorder.DespawnReason.ENOUGH_FOOD_COLLECTED);
                ant.setDespawndTrue();
                Thread.currentThread().interrupt();
            }

              throw new InterruptedException();
        }
    }

    /**
     * this methode handle the Entering of a Clearing including recorder stuff when she is on Search.
     * @param ourNextClearing the Clearing which the Ant want to enter.
     * @param ourLastTrail the Trail where the Ant is now.
     * @throws InterruptedException InterruptedException to terminate.
     */

    public void handleEnterClearingFoodSearch(Clearing ourNextClearing, Trail ourLastTrail) throws InterruptedException {
        boolean enteredSuccess = ourNextClearing.enterClearing(ourLastTrail, ant, EntryReason.FOOD_SEARCH); // enter the Clearing (trail.To)
        if (enteredSuccess) {
            recorderStuffEnterClearingLeaveTrail(ourNextClearing,ourLastTrail);
            com.pseuco.np21.shared.Trail.Pheromone hillPheromone = ourLastTrail.reverse().getOrUpdateHillPheromone(false, null);
            if(ourLastTrail.isShouldBeUpdated()) {
                ant.getRecorder().updateAnthill(ant, ourLastTrail.reverse(), hillPheromone);
            }
        } else {
            if (ant.isDied()) {
                recorderStuffDeadDespawn(ourLastTrail);
            } else {
                recorderStuffTerminateNowOnLastTrail(ourLastTrail);
            }
            throw new InterruptedException();
        }
    }

    /**
     * this methode handle the immediate return and the recorder stuff.
     * @param ourTrail the last Trail we used.
     * @param position the Clearing where the Ant is now.
     * @return true if the Ant has returned back successfully.
     * @throws InterruptedException InterruptedException to terminate.
     */

    public Clearing goOneStepBackWithImmediatReturn(Trail ourTrail,Clearing position) throws InterruptedException {
        SearchFoodPathCheck searchFood = ant.searchFood;
        Recorder recorder = ant.getRecorder();
        Trail targetTrail = searchFood.getTrailToStepBack(position, ourTrail); // get the reverse of ourTrail
        recorder.select(ant, targetTrail, ant.getCandidatesList(), Recorder.SelectionReason.IMMEDIATE_RETURN);
        boolean success =targetTrail.enterTrail(position, ant, EntryReason.IMMEDIATE_RETURN); // enter the trail
        EnterTrailRecorderStuff(position,targetTrail,success); // do the recorder stuff
        ourTrail = targetTrail; // update our Trail
        Clearing ourNextClearing = ourTrail.to(); // get our next Clearing
        boolean enterSuccess = ourNextClearing.enterClearing(ourTrail, ant, EntryReason.IMMEDIATE_RETURN); // enter the Clearing
        if (enterSuccess) {
           recorderStuffEnterClearingLeaveTrail(ourNextClearing,ourTrail);
        } else {
            if (ant.isDied()) {
                recorderStuffDeadDespawn(ourTrail);
            } else {
                recorderStuffTerminateNowOnLastTrail(ourTrail);
            }

            throw new InterruptedException();
        }
        position = ourNextClearing; // update the Ant Position(current Clearing);
        return position;
    }


    /**
     * this methode handle the No_Food_Return step including recorder stuff.
     * @param searchFood    reference of searchFoodPathCheck class to find the Path
     * @param currentPosition the current Clearing where the Ant is now.
     * @return  the new Clearing .
     * @throws InterruptedException  InterruptedException to terminate.
     */
    public Clearing GoBackByNoFoodReturn(SearchFoodPathCheck searchFood, Clearing currentPosition) throws InterruptedException {
        Clearing position = currentPosition;
        Recorder recorder = ant.getRecorder();
        Trail targetTrail = searchFood.getTrailByNofoodReturn(position, ant); // get the target Trail
        recorder.select(ant, targetTrail, ant.getCandidatesList(), Recorder.SelectionReason.NO_FOOD_RETURN);
        boolean success = targetTrail.enterTrail(position, ant, EntryReason.NO_FOOD_RETURN); // enter the Trail
        EnterTrailRecorderStuff(position,targetTrail,success); // do the recorder stuff Or terminate if success = false.
        Trail ourTrail = targetTrail;
        Clearing ourNextClearing = targetTrail.to();
        boolean enterSuccess = ourNextClearing.enterClearing(ourTrail, ant, EntryReason.NO_FOOD_RETURN);// enter Clearing
        if (enterSuccess) {
            recorderStuffEnterClearingLeaveTrail(ourNextClearing,ourTrail);
            com.pseuco.np21.shared.Trail.Pheromone foodPheromone = ourTrail.reverse().getOrUpdateFoodPheromone(false, null, false);
            ant.getRecorder().updateFood(ant, ourTrail.reverse(), foodPheromone); // recorder stuff.
        } else {
            if (ant.isDied()) {
                recorderStuffDeadDespawn(ourTrail);
            } else {
                recorderStuffTerminateNowOnLastTrail(ourTrail);
            }

            throw new InterruptedException();
        }
        position = ourNextClearing;
        return  position;
    }

    /**
     * this methode make the Ant leaving the Hill to terminate and sends recorder stuff.
     *
     * @param position the  hill
     * @throws InterruptedException  InterruptedException to terminate.
     */
    public void leaveAndInterruptAtHill( Clearing position) throws InterruptedException {
        position.leave();
        ant.getRecorder().leave(ant, position);
        if(! ant.getWorld().isFoodLeft()) {
            ant.getRecorder().despawn(ant, Recorder.DespawnReason.ENOUGH_FOOD_COLLECTED);
            ant.setDespawndTrue();
            Thread.currentThread().interrupt();
        } else {
            ant.getRecorder().despawn(ant, Recorder.DespawnReason.TERMINATED);
            ant.setDespawndTrue();
            Thread.currentThread().interrupt();
        }

        throw new InterruptedException();
    }

    /**
     *  this methode handle the way home including recorder stuff.
     * @param update false if the Ant took the last piece of Food from the Clearing
     * @param currentPosition the Clearing where the Ant has picked up the food
     * @param sequence the Clearing Sequence
     * @throws InterruptedException when the Ant should terminate.
     */
    public void homewardMoving ( boolean update, Clearing currentPosition,List<Clearing> sequence) throws InterruptedException {
        World<Clearing, Trail> world = ant.getWorld();
        Clearing position = currentPosition;
        HomeWardPathCheck homeward = new HomeWardPathCheck(ant);
        Trail target;
        while (position.id() != world.anthill().id()) { // do the Following till the Ant reaches the Hill.
            assert (sequence.size()>1);
            target = homeward.getTargetTrail(position); // get the Trail to take.
            if(ant.isAdventurer() ){
                ant.getRecorder().select(ant, target, ant.getCandidatesList(), Recorder.SelectionReason.RETURN_IN_SEQUENCE);
            } else {
                ant.getRecorder().select(ant, target, ant.getCandidatesList(), Recorder.SelectionReason.RETURN_FOOD);
            }
            Clearing ourNextClearing = target.to();
            if (update){  // update == true --> we update pheromones because there still some food on the Clearing.
                boolean success = target.enterTrail(position, ant, EntryReason.HEADING_BACK_HOME_WITH_UPDATING);// enter the Trail
                EnterTrailRecorderStuff(position,target,success); // do the recorder stuff
                Trail ourLastTrail = target;
                boolean enterSuccess = ourNextClearing.enterClearing(target, ant, EntryReason.HEADING_BACK_HOME_WITH_UPDATING);
                if (enterSuccess) {
                    recorderStuffEnterClearingLeaveTrail(ourNextClearing,target);
                    com.pseuco.np21.shared.Trail.Pheromone FoodPheromone = ourLastTrail.reverse().getOrUpdateFoodPheromone(false,null,false);
                    ant.getRecorder().updateFood(ant, ourLastTrail.reverse(), FoodPheromone); // recorder stuff
                } else {
                    if (ant.isDied()) {
                        recorderStuffDeadDespawn(ourLastTrail);
                    } else {
                        recorderStuffTerminateNowOnLastTrail(ourLastTrail);
                    }

                    throw new InterruptedException();
                }
            }else {  // Update == False --> no Food any more on that Clearing so don't update.
                boolean success = target.enterTrail(position, ant, EntryReason.HEADING_BACK_HOME_WITHOUT_UPDATING);// enter the Trail
                EnterTrailRecorderStuff(position,target,success); // do the recorder stuff
                Trail ourLastTrail = target;
                boolean enterSuccess =ourNextClearing.enterClearing(target, ant, EntryReason.HEADING_BACK_HOME_WITHOUT_UPDATING);
                if (enterSuccess) {
                    recorderStuffEnterClearingLeaveTrail(ourNextClearing,target);
                } else {
                    if (ant.isDied()) {
                        recorderStuffDeadDespawn(ourLastTrail);
                    } else {
                        recorderStuffTerminateNowOnLastTrail(ourLastTrail);
                    }

                    throw new InterruptedException();
                }
            }
            position = target.to();
        }
        boolean droppedSuccess =position.dropFood(position, ant); // try to drop the food in the Hill
        if (!droppedSuccess){
            ant.getRecorder().leave(ant,position);
             if(Thread.currentThread().isInterrupted()) {
                ant.getRecorder().despawn(ant, Recorder.DespawnReason.TERMINATED);
                 ant.setDespawndTrue();

            } else if(! ant.getWorld().isFoodLeft()) {
                ant.getRecorder().despawn(ant, Recorder.DespawnReason.ENOUGH_FOOD_COLLECTED);
                 ant.setDespawndTrue();
                 Thread.currentThread().interrupt();
            }

            throw new InterruptedException();
        }
        // clear all information
        sequence.clear();
        ant.getTrailsToVisitedClearings().clear();
        ant.getTrailSequence().clear();
        ant.setAntTONormalState();
        ant.getAlreadyEnteredTrails().clear();
        ant.getRecorder().returnedFood(ant);
        ant.addClearingToSequence(position);  // adding the antHill to the sequence
    }
}
