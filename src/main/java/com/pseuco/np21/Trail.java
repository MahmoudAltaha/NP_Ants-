package com.pseuco.np21;

import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Representation of a trail.
 * <p>
 * Manages ant and pheromone levels on it.
 * <p>
 * You may change the code however you see fit.
 */
public class Trail extends com.pseuco.np21.shared.Trail<Clearing, Trail> {
    private Pheromone anthill;
    private Pheromone food;
    private int ants;
    private boolean shouldBeUpdated;
    private final TrailEntry trailEntry = new TrailEntry(this);;    // to handle the entering to this Trail in a concurrent way.
    private Lock lock =  new ReentrantLock();



    private Trail(final Trail reverse) {
        super(reverse);
        this.anthill = Pheromone.NOT_A_PHEROMONE;
        this.food = Pheromone.NOT_A_PHEROMONE;
        this.ants = 0;
        this.shouldBeUpdated = false;
    }

    /**
     * Constructs a new trail given the connected clearings.
     *
     * @param from clearing the trail leads away from
     * @param to   clearing the trail leads to
     */
    public Trail(final Clearing from, final Clearing to) {
        super(from, to, (reverse) -> new Trail((Trail) reverse));

        this.anthill = Pheromone.NOT_A_PHEROMONE;
        this.food = Pheromone.NOT_A_PHEROMONE;
        this.ants = 0;
    }



    public boolean isShouldBeUpdated() {
        return shouldBeUpdated;
    }

    public void setShouldBeUpdated(boolean shouldBeUpdated) {
        this.shouldBeUpdated = shouldBeUpdated;
    }



    public TrailEntry getTrailEntry(){
        return this.trailEntry;
    }


    /**
     * Get the anthill pheromone level.
     *
     * @return anthill pheromone level
     */
    public Pheromone anthill() {
        return anthill;
    }

    /**
     * Get the food pheromone level.
     *
     * @return food pheromone level
     */
    public Pheromone food() {
        return food;
    }

    /**
     * this methode is the only methode we should use to get/update the Food Pheromone otherwise we could have DataRace.
     * @param write   true if you are about to update the Food-Pheromone, false otherwise.
     * @param p       the new Food pheromone that we want to register. (null if we want to get the Food-Ph only)
     * @param explorer  state of the Ant (it doesn't matter which value if we want just to get the Food-ph)
     * @return          the Food-pheromone.(the old or the new one)
     */
     public Pheromone getOrUpdateFoodPheromone(boolean write, Pheromone p, boolean explorer){
       lock.lock();
       try {
           if (write){
               updateFood(p,explorer);
           }
           return food();
       }
       finally {
           lock.unlock();
       }
    }

    /**
     * this methode is the only methode we should use to get/update the Hill Pheromone otherwise we could have DataRace.
     * @param write   true if you are about to update the Hill-Pheromone, false otherwise.
     * @param p       the new Hill pheromone that we want to register. (null if we want to get the Hill-Ph only)
     * @return          the Hill-pheromone.(the old or the new one)
     */
     public Pheromone getOrUpdateHillPheromone(boolean write, Pheromone p){
      lock.lock();
      try {
          if (write){
              updateAnthill(p);
          }
          return anthill();
      }
      finally {
          lock.unlock();
      }

    }

    /**
     * Update the anthill pheromone level.
     *
     * @param p the new pheromone level
     */
    public void updateAnthill(final Pheromone p) {
        if (!p.isInfinite() && anthill.isAPheromone() && PHEROMONE_COMPARATOR.compare(p, anthill) > 0)
            return;
        anthill = p;
    }

    /**
     * Update the food pheromone level.
     *
     * @param p        the new pheromone level
     * @param explorer {@code true} iff the ant is in exploration mode
     */
    public void updateFood(final Pheromone p, final boolean explorer) {
        if (explorer && !p.isInfinite() && food.isAPheromone() && PHEROMONE_COMPARATOR.compare(p, food) > 0)
            return;
        food = p;
    }

    /**
     * Check whether there is still space left on this trail.
     *
     * @return {@code true} iff there is space left
     */
    public boolean isSpaceLeft() {
        return capacity.isInfinite() || ants < capacity.value();
    }

    /**
     * Call this when an ant enters this trail.
     */
    public void enter() {
        ants++;
    }

    /**
     * Call this when an ant leaves this trail.
     */
    public void leave() {
        ants--;
    }


    /**
     * this methode will be used to enter this Trail in way that ensure concurrency.
     * @param currentClearing  the Current Clearing which the Ant should left,
     * @param ant       the Ant
     * @param entryReason   the reason you have to enter this Trail.
     * @return      true if the entry was completed successfully.
     */
    public boolean enterTrail(Clearing currentClearing,Ant ant,EntryReason entryReason) {
        //this assert make sure that we get the right Clearing and the right Trail from The Ant methodes(run for example)
        assert (currentClearing.id() == this.from.id());
        return trailEntry.enter(currentClearing,ant,entryReason);
    }


}
