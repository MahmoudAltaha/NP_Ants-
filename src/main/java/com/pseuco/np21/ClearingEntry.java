package com.pseuco.np21;


import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * this class can be seen as a gate for the clearing. each Clearing has one gate which is necessary
 * to ensure the concurrency
 */
public class ClearingEntry {
    private final Clearing clearing;

    public final Lock clearingLock = new ReentrantLock();

    public Condition isSpaceLeft= clearingLock.newCondition();
    private final ClearingEntryHandler handler;

    /**
     * construct a clearingEntry
     * @param clearing clearing
     */
    public ClearingEntry(Clearing clearing) {
        this.clearing = clearing;
       this.handler= new ClearingEntryHandler();
    }


    /**
     *  getter
     * @return  the condition
     */
    public Condition getIsSpaceLeft() {
        return isSpaceLeft;
    }
    public Lock getClearingLock(){
        return clearingLock;
    }



    /**
     * this methode will be used to handle the entering to a Clearing according to the behavior of an Ant.
     *
     * @param t   The Trail from which the Ant comes.
     * @param ant   The ant which want to enter this clearing .
     * @return    true if the Ant has entered the Clearing successfully, false when the ant died Or should terminate now
     */
    private boolean enterClearingFoodSearch(Trail t,Ant ant){
        this.clearingLock.lock();  // take the Lock of this Clearing
        try {
            t.getTrailEntry().getTrailLook().lock();  // take the lock of the Trail you want to leave.
            try {
                while (!this.clearing.isSpaceLeft())  // wait for space,,if the Ant has waited more than its disguise she can pass.
                    if (!this.isSpaceLeft.await(ant.disguise(), TimeUnit.MILLISECONDS)) {
                        t.leave();    // the Ant is dead so leave
                        t.getTrailEntry().getIsSpaceLeft().signalAll();// signalAll that you left the Trail .
                        ant.setDied(true);
                        return false;
                    }
                if (!ant.getWorld().isFoodLeft() || Thread.currentThread().isInterrupted()) {
                    t.leave(); // leave the Trail.
                    t.getTrailEntry().getIsSpaceLeft().signalAll(); // signal all to the threads which are waiting  to enter the Trail we left
                    return false;
                } else {
                    this.clearing.enter(); // enter the Clearing
                    t.leave();
                    t.getTrailEntry().getIsSpaceLeft().signalAll(); // signal all to the threads which are waiting  to enter the Trail we left
                    if (ant.getClearingSequence().contains(this.clearing)){
                        ant.getTrailsToVisitedClearings().put(t.id(),t);
                    }
                    ant.addClearingToSequence(this.clearing);
                }
            }
            finally{   t.getTrailEntry().getTrailLook().unlock();} // release the lock of the Trail we left.
           this.handler.pheromonesUpdatingFoodSearch(t,ant);
        }
        catch (InterruptedException e){ // Thread interrupted while he is in wait mode.
            t.leave();
            Thread.currentThread().interrupt();
            return false;
        }
        finally{
            this.clearingLock.unlock();
        }
        return true;
    }


    /**
     * this methode will be used to step back after being in a Trail which is connected
     *  to an already visited clearing. case d). it does the same entering but we need to separate them to know
     *  which reason we will give to the Recorder.
     *
     * @param t    The Trail from which the Ant comes.
     * @param ant    The Ant which want to enter to this clearing .
     * @return     true if the Ant has entered the Clearing successfully.false when the ant died Or should terminate now
     */
    private boolean immediateReturnTOClearing(Trail t,Ant ant){
        this.clearingLock.lock();  // take the Lock of this Clearing
        try {
            t.getTrailEntry().getTrailLook().lock();  // take the lock of the Trail you want to leave.
            try {
                while (!this.clearing.isSpaceLeft())  // wait for space,,if the Ant has waited more than its disguise she can pass.
                    if (!this.isSpaceLeft.await(ant.disguise(), TimeUnit.MILLISECONDS)) {
                        t.leave(); // the Ant is dead so leave
                        t.getTrailEntry().getIsSpaceLeft().signalAll(); // signalAll that you left the Trail .
                        ant.setDied(true);
                        return false;
                    }
                if ( ! ant.getWorld().isFoodLeft() || Thread.currentThread().isInterrupted()) {
                    t.leave();
                    t.getTrailEntry().getIsSpaceLeft().signalAll(); // signal all to the threads which are waiting  to enter the Trail we left
                   return false;
                } else {
                   this.clearing.enter();
                    t.leave();
                    t.getTrailEntry().getIsSpaceLeft().signalAll(); // signal all to the threads which are waiting  to enter the Trail we left
                }
            }
                finally{   t.getTrailEntry().getTrailLook().unlock();} // release the lock of the Trail we left.
        }
        catch (InterruptedException e){ // Thread interrupted while he is in wait mode.
            t.leave();
            Thread.currentThread().interrupt();
            return false;
        }
        finally{
            this.clearingLock.unlock();
        }
        return true;
    }

    /**
     * this methode will be used to step back
     * from a Trail which was connected to a clearing for which it has no options
     * to reach food other than turning back. it does the same entering but we need to separate them to know
     * which reason we will give to the Recorder.
     *
     * @param t    The Trail from which the Ant comes.
     * @param ant    The Ant which want to enter to this clearing .
     * @return     true if the Ant has entered the Clearing successfully.false when the ant died Or should terminate now
     */
    private boolean noFoodReturnTOClearing(Trail t,Ant ant) {
        this.clearingLock.lock();  // take the Lock of this Clearing
        try {
            t.getTrailEntry().getTrailLook().lock();  // take the lock of the Trail you want to leave.
            try {
                while (!this.clearing.isSpaceLeft())  // wait for space,,if the Ant has waited more than its disguise she can pass.
                    if (!this.isSpaceLeft.await(ant.disguise(), TimeUnit.MILLISECONDS)) {
                        t.leave(); // the Ant is dead so leave
                        t.getTrailEntry().getIsSpaceLeft().signalAll(); // signalAll that you left the Trail .
                        ant.setDied(true);
                        return false;
                    }
                if ( !ant.getWorld().isFoodLeft() || Thread.currentThread().isInterrupted() ) {
                    t.leave();
                    t.getTrailEntry().getIsSpaceLeft().signalAll(); // signal all to the threads which are waiting  to enter the Trail we left
                    return false;
                } else {
                    this.clearing.enter();
                    t.leave();
                    t.getTrailEntry().getIsSpaceLeft().signalAll(); // signal all to the threads which are waiting  to enter the Trail we left
                    handler.pheromonesUpdatingNoFoodReturn(t, ant);
                }
            } finally {
                t.getTrailEntry().getTrailLook().unlock();// release the lock of the Trail we left.
            }
        }
        catch (InterruptedException e){ // Thread interrupted while he is in wait mode.
            t.leave();
            Thread.currentThread().interrupt();
            return false;
        }
        finally{
            this.clearingLock.unlock();
        }
        return true;
    }


    /**
     * this methode will be used to pick up one piece of food.
     * @param ant    The Ant which want to enter to this clearing .
     * @return      true if the food was collected successfully. if so you can start the homeward. false when the ant should terminate now
     */
    public boolean pickUPFood(Ant ant)  {
        this.clearingLock.lock();
        try {
            if (! ant.getWorld().isFoodLeft()  || Thread.currentThread().isInterrupted()) {
                this.clearing.leave(); // if the thread noticed that he is interrupted then leave the clearing
                this.isSpaceLeft.signalAll();
               return false;
            }
                if (!this.clearing.getOrSetFood(FoodInClearing.HAS_FOOD)) {  // check i there are no food exit with false.
                    return false;
                }
                ant.setHoldFood(true);  // the Ant now has food
                this.clearing.getOrSetFood(FoodInClearing.PICKUP_FOOD); // remove the picked up food from this Clearing.
        }
            finally {
            this.clearingLock.unlock();
        }
        return  true;
    }

    /**
     * this methode will be used to enter the Clearing when the Ant is willing to reach the Hill
     * @param t the current Trail which the Ant should leave.
     * @param ant  the Ant
     * @return true if the Ant has entered the Clearing successfully. false when the ant died Or should terminate now
     */
    private boolean homewardEnterClearing(Trail t, Ant ant, boolean update) {
        this.clearingLock.lock();  // take the Lock of this Clearing
        try {
            t.getTrailEntry().getTrailLook().lock();  // take the lock of the Trail you want to leave.
            try {
                while (!this.clearing.isSpaceLeft())  // wait for space,,if the Ant has waited more than its disguise she can pass.
                    if (!this.isSpaceLeft.await(ant.disguise(), TimeUnit.MILLISECONDS)) {
                        t.leave(); // the Ant is dead so leave
                        t.getTrailEntry().getIsSpaceLeft().signalAll(); // signalAll that you left the Trail .
                        ant.setDied(true);
                        return false;
                    }
                if ( ! ant.getWorld().isFoodLeft()  || Thread.currentThread().isInterrupted()) {
                    t.leave();
                    t.getTrailEntry().getIsSpaceLeft().signalAll(); // signal all to the threads which are waiting  to enter the Trail we left
                    return false;
                } else {
                    this.clearing.enter();
                    t.leave();
                    t.getTrailEntry().getIsSpaceLeft().signalAll(); // signal all to the threads which are waiting  to enter the Trail we left
                }
            } finally{ t.getTrailEntry().getTrailLook().unlock();}// take the lock of the Trail you want to leave.
            if (update) {
                handler.pheromonesUpdatingHomeward(t, this.clearing, ant);
            }
        } catch (InterruptedException e){ // Thread interrupted while he is in wait mode.
            t.leave();
            Thread.currentThread().interrupt();
            return false;
        }
        finally {
                this.clearingLock.unlock();
            }
            return true;
        }

    /**
     * drop the food in the Hill
     *
     * @param c the Hill
     * @return true by successfully dropping food. false when the ant should terminate now
     */
    public boolean dropFood(Clearing c, Ant ant) {
        this.clearingLock.lock();
        try {
            if (!ant.getWorld().isFoodLeft()  || Thread.currentThread().isInterrupted()) {
                this.clearing.leave(); // if the thread noticed that he is interrupted then leave the Hill
                return false;
            } else { //if the thread noticed that he is interrupted then he wont drop the food and should be dspawned.
                if (c.id() == ant.getWorld().anthill().id()) {
                    c.getOrSetFood(FoodInClearing.DROP_FOOD);
                    ant.getWorld().foodCollected();
                    ant.setHoldFood(false);
                }
            }
        } finally {
            this.clearingLock.unlock();
        }
        return true;
    }

    /**
     * this methode will be used to enter this Clearing in way that ensure concurrency.
     *
     * @param currentTrail  the Current Trail which the Ant should left,
     * @param ant       the Ant
     * @param entryReason   the reason you have to enter this Clearing.
     * @return      true if the entry was completed successfully.
     */
    public boolean enter(Trail currentTrail,Ant ant,EntryReason entryReason) {
        return switch (entryReason) {
            case FOOD_SEARCH -> this.enterClearingFoodSearch(currentTrail, ant);
            case IMMEDIATE_RETURN -> this.immediateReturnTOClearing(currentTrail, ant);
            case NO_FOOD_RETURN -> this.noFoodReturnTOClearing(currentTrail, ant);
            case HEADING_BACK_HOME_WITH_UPDATING -> this.homewardEnterClearing(currentTrail, ant, true);
            case HEADING_BACK_HOME_WITHOUT_UPDATING -> this.homewardEnterClearing(currentTrail, ant, false);
        };
    }

}
