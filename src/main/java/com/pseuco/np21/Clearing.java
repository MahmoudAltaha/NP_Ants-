package com.pseuco.np21;

import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Representation of a clearing.
 * <p>
 * Manages ant and food levels on it.
 * <p>
 * You may change the code however you see fit.
 */
public class Clearing extends com.pseuco.np21.shared.Clearing<Clearing, Trail> {
    private int ants;
    private int food;
    private final ClearingEntry clearingEntry;
    private Lock lock;

    /**
     * Constructs a new clearing.
     *
     * @param name     of the clearing
     * @param food     the clearing starts with
     * @param capacity the clearing has access to
     */
    public Clearing(final String name, final int food, final Capacity capacity) {
        super(name, food, capacity);
        this.food = initialFood;
        this.clearingEntry = new ClearingEntry(this);
        this.lock= new ReentrantLock();
    }

    /**
     * Check whether there is still space left on this clearing.
     *
     * @return {@code true} iff there is space left
     */
    public boolean isSpaceLeft() {
        return capacity.isInfinite() || ants < capacity.value();
    }

    /**
     * Call this when an ant enters this clearing.
     */
    public void enter() {
        ants++;
    }

    /**
     * Call this when an ant leaves this clearing.
     */
    public void leave() {
        ants--;
    }

    /**
     * Check whether this clearing has food left.
     *
     * @return {@code true} iff there is food left
     */
    private boolean hasFood() {
        return food > 0;
    }

    /**
     * Call this when an ant picks up food at this clearing.
     */
    private void pickupFood() {
        food--;
    }

    /**
     * Call this when an ant places food at this clearing.
     */
    private void placeFood() {
        food++;
    }


    public ClearingEntry getClearingEntry() {
        return clearingEntry;
    }


    /**
     * this methode is the Only methode you should use to call hasFood/pickUpFood/placeFood.Otherwise we could have data race
     *
     * @param foodInClearing select the order you want to call.
     * @return always return how much food are left there.
     */
    public boolean getOrSetFood(FoodInClearing foodInClearing) {
        lock.lock();
        try {
            switch (foodInClearing) {
                case DROP_FOOD:
                    placeFood();
                    return hasFood();
                case PICKUP_FOOD:
                    pickupFood();
                    return hasFood();
                default:
                    return hasFood();
            }
        } finally {
            lock.unlock();
        }
    }

    /**
     * this methode will be used to enter this Clearing in a way that ensure concurrency.
     *
     * @param currentTrail the Current Trail which the Ant should left,
     * @param ant          the Ant
     * @param entryReason  the reason you have to enter this Clearing.
     * @return true if the entry was completed successfully.
     */
    public boolean enterClearing(Trail currentTrail, Ant ant, EntryReason entryReason)  {
        //this assert make sure that we get the right Clearing and the right Trail from The Ant methodes(run for example)
        assert (currentTrail.to().id == this.id);
        return clearingEntry.enter(currentTrail, ant, entryReason);
    }

    /**
     * this methode used to pickUp one piece of Food from this Clearing.
     *
     * @param ant The Ant
     * @return true if the Ant has pickedUp the food.
     */
    public boolean TakeOnPieceOfFood(Ant ant) throws InterruptedException {
        return clearingEntry.pickUPFood(ant);
    }


    /**
     * drop the food in the Hill and remove it from the Ant (this methode should call the drop methode in EntryClearing).
     *
     * @param c the Hill
     * @return true by successfully dropping food
     */
    public boolean dropFood(Clearing c, Ant ant) throws InterruptedException {
        return clearingEntry.dropFood(c, ant);
    }



}
