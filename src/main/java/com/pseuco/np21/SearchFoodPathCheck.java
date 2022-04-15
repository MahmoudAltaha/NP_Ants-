package com.pseuco.np21;


import java.util.List;

/**
 * this class will be used to find the correct Trail which the ant should take by food search.
 */
public class SearchFoodPathCheck {

    private final SearchFoodTrailHandler searchFoodTrailHandler;
    private final Ant ant;

    public SearchFoodPathCheck(Ant ant) {
        this.searchFoodTrailHandler = new SearchFoodTrailHandler();
        this.ant =ant;
    }


    /**
     * this methode is used to choose the right Trail according to the project description.
     *
     * @param  currentClearing the currentClearing
     * @return the targetTrail.
     */
     public Trail getTargetTrail(Clearing currentClearing) {
         assert checkTrail(currentClearing);
        List<Trail> trailList = currentClearing.connectsTo();
        return searchFoodTrailHandler.getTargetTrail(trailList,ant);
    }


    /**
     * this methode is used to check whether the Clearing has valid Connected Trail.
     *
     * @param c  Current Clearing.
     * @return   return true if you found a Trail.
     */
     public boolean checkTrail(Clearing c) {
        List<Trail> connectedTrails = c.connectsTo();
        return searchFoodTrailHandler.checkTrail(c,connectedTrails,ant);
    }


    /**
     * this methode is used to get the right reversedTrail wHen we Want to step back;
     * @param currentTwiceVisitedClearing   the currentTwiceVisitedClearing which we want to leave.
     * @param trailWeComeFrom           the Trail from which we come from the last time.
     * @return  the reversedTrail which we need to take
     */
     public Trail getTrailToStepBack(Clearing currentTwiceVisitedClearing,Trail trailWeComeFrom){
         //this assert make sure that we get the right Clearing and the right Trail from The Ant methodes(run for example)
        assert (trailWeComeFrom.to().id() == (currentTwiceVisitedClearing.id()));
         return trailWeComeFrom.reverse();
    }


    /**
     * this methode gives us the Trail which the Ant should take by No_FOOD_Return.
     * @param currentClearing the current Clearing where the Ant is now
     * @param ant the ant
     * @return The Trail to take.
     */
    public Trail getTrailByNofoodReturn(Clearing currentClearing, Ant ant) {
        return searchFoodTrailHandler.getTrailByNofoodReturn(currentClearing,ant);
    }

}
