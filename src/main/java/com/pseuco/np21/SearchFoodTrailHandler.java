package com.pseuco.np21;

import java.util.*;

/**
 * this class handle the check by searching for a valid Trail to Take by(FoodSearch/noFoodReturn/Immediate return)
 */
public class SearchFoodTrailHandler {

    /**
     * this methode used to remove the Trails which lead to the Clearing that i just visited and found that i
     * have visited it in tha past.  (we use this methode just in this class in "getTargetTrail")
     *
     * @param trailsList the Trails which are connected to the Current Clearing
     * @param ant the Ant
     */
    private void getListWithoutAlreadyEnteredTrails(List<Trail> trailsList, List<Trail> listWithoutAlreadyEnteredTrails, Ant ant){
       assert !(trailsList.isEmpty());
        for (Trail t : trailsList){
                if(! ant.alreadyEnteredTrails.containsKey(t.id()) ){
                    listWithoutAlreadyEnteredTrails.add(t);
            }
        }
    }

    /**
     * this methode remove all Trails with Map-Pheromone from the given list.
     * (we use this methode just in this class in "getTargetTrail")
     * @param trailList trailsList
     * @param ant ant
     */
    private void removeMapTrailsAndTheTrailWeComeFrom(List<Trail> trailList,List<Trail> listWithoutMapAndTrailWeComeFrom, Ant ant) {
        assert (!trailList.isEmpty());  // check if the list are not Empty
        for (Trail t : trailList) {
            // remove all Trails which are already has Map Value Or the last one in Sequence.
            if (!t.getOrUpdateFoodPheromone(false, null, false).isInfinite()) {
                if (ant.TrailSequence.isEmpty()) {
                    listWithoutMapAndTrailWeComeFrom.add(t);
                } else {
                    if (!(ant.TrailSequence.get(ant.TrailSequence.size() - 1).reverse().id() == t.id())) {
                        listWithoutMapAndTrailWeComeFrom.add(t);
                    }
                }
            }
        }
    }


    /**
     *  this methode checks if all the Trails in the list has Nap-Pheromones.
     * (we use this methode just in this class in "getTargetTrail")
     * @param trailList trailList
     * @return true if all the Trails in the list has Nap-Pheromones.
     */
    private boolean checkIfAllTrailsHasNaP(List<Trail> trailList){
        assert (!trailList.isEmpty());  // check if the list are not Empty
        for (Trail t : trailList) {
            com.pseuco.np21.shared.Trail.Pheromone p = t.getOrUpdateFoodPheromone(false,null,false);
            if ((p.isAPheromone())) {
                return false;
            }
        }
        return true;
    }

    /**
     * this methode separate one list into two Lists, one of them has all Nap_trails and the other one.
     * has all Non-NapTrails.  (we use this methode just in this class in "getTargetTrail")
     * @param trailList trailList
     * @param napTrails NapList
     * @param nonNapTrails NON-Nap List
     */
    private void separateNapTrailsFromNonNapTrails(List<Trail> trailList,List<Trail> napTrails,List<Trail> nonNapTrails){
        assert (!trailList.isEmpty());  // check if the list are not Empty
        for (Trail trail : trailList) {
            com.pseuco.np21.shared.Trail.Pheromone p = trail.getOrUpdateFoodPheromone(false,null,false);
           if (p.isAPheromone()){
               nonNapTrails.add(trail);
            }else{
               napTrails.add(trail);
           }

        }
    }

    /**
     * this methode used to add the Trails with min-Food Value to a min List. (we use this methode just in this class in "getTargetTrail")
     *
     * @param minTrailsList  List with min-Food Trails.
     * @param trailsListNonNap  the Trails which has all Trails that have nonNap Food-Pheromone value.
     */
    private void makeListWithJustMin(List<Trail> minTrailsList ,List<Trail> trailsListNonNap) {
        assert trailsListNonNap.size() != 0;
        if (trailsListNonNap.size() == 1) {
            minTrailsList.add(trailsListNonNap.get(0));
        } else {
            for (int i = 0; i < (trailsListNonNap.size() - 1); i++) {  // compare the NonNap-Pheromones and add the min-ones to the list.
                Trail t1 = trailsListNonNap.get(i);  // Trail 1
                Trail t2 = trailsListNonNap.get(i + 1); // Trail 2
                com.pseuco.np21.shared.Trail.Pheromone p1 = t1.getOrUpdateFoodPheromone(false, null, false);// Phe. of t1
                com.pseuco.np21.shared.Trail.Pheromone p2 = t2.getOrUpdateFoodPheromone(false, null, false);// Phe. of t2
                    if (p1.value() > p2.value()) {
                        minTrailsList.add(t2);
                        // remove t1 if it is present and all the Trails with the same food-Value, if not nothing is happening
                        for (int k = 0; k < minTrailsList.size(); k++) {
                            if (minTrailsList.get(k).getOrUpdateFoodPheromone(false, null,false).value() ==
                                    t1.getOrUpdateFoodPheromone(false, null, false).value()) {
                                minTrailsList.remove(minTrailsList.get(k));
                            }
                        }
                    } else if (p1.value() < p2.value()) {
                        if (!minTrailsList.contains(t1)) {
                            minTrailsList.add(t1);
                        }
                    } else {
                        if (!minTrailsList.contains(t1)) {
                            minTrailsList.add(t1);
                        }
                        minTrailsList.add(t2);
                    }
                }
            }
        }


    /**
     * this methode is used to choose the right Trail according to the project description.
     *
     * @param trailList the Trails from which we the right one choose.(ConnectedTrails
     * @param ant       Ant.
     * @return      The Target Trail.
     */
    public Trail getTargetTrail(List<Trail> trailList,Ant ant){
        assert (!trailList.isEmpty());  // check if the list are not Empty
        // make new list object which has the same TrailsObject in the trailsList
        List<Trail> trailsListToBeClearedAndChosenFrom = new LinkedList<>(trailList);
        assert(trailsListToBeClearedAndChosenFrom.size() == trailList.size());
       //add the Trails That we haven't entered before to this list.
         List<Trail> listWithoutEnteredTrails = new LinkedList<>();

         getListWithoutAlreadyEnteredTrails(trailsListToBeClearedAndChosenFrom,listWithoutEnteredTrails,ant);
        assert(!listWithoutEnteredTrails.isEmpty());

        // add the Trails that we haven't entered and has non_Map pheromone and are not the trail we just come From.
        List<Trail> listWithoutMapAndTheTrailWeComeFromORAlreadyVisited = new ArrayList<>();
        removeMapTrailsAndTheTrailWeComeFrom(listWithoutEnteredTrails,listWithoutMapAndTheTrailWeComeFromORAlreadyVisited,ant); // the same like above, remove Map Trails From The List.
        assert(!listWithoutMapAndTheTrailWeComeFromORAlreadyVisited.isEmpty());

        Trail targetTrail;
        boolean allNaP;  // check if all Trails has FoodPheromone = Nap
        allNaP = checkIfAllTrailsHasNaP(listWithoutMapAndTheTrailWeComeFromORAlreadyVisited);

        Random random = new Random();
        if (allNaP){  // if so then pick a NaP Trail randomly .
            int index = random.nextInt(listWithoutMapAndTheTrailWeComeFromORAlreadyVisited.size());
            targetTrail = listWithoutMapAndTheTrailWeComeFromORAlreadyVisited.get(index);
            ant.setCandidatesList(listWithoutMapAndTheTrailWeComeFromORAlreadyVisited);/////////////////////////
        }
        else {  // the trailList has Trails with non Nap-foodPheromone. it may also have Trails with Nap-ph tho.
            List<Trail> trailsListNonNap = new ArrayList<>(); // list with Trails which has non Nap-Food-ph.

            List<Trail> trailsListWithJustNap = new ArrayList<>();// list with Trails which has Nap-food-ph.

            separateNapTrailsFromNonNapTrails(listWithoutMapAndTheTrailWeComeFromORAlreadyVisited,trailsListWithJustNap,trailsListNonNap);

            List<Trail> minTrails = new ArrayList<>(); // list which should contains the min-NonNap Pheromones.

            makeListWithJustMin(minTrails,trailsListNonNap); // make List with just min Food-Pheromone Value.

            int randomIndex = random.nextInt(minTrails.size());//get random number

            Trail suggestedTrail = minTrails.get(randomIndex );// the randomly picked Trail which has the min Ph.

            // check if the impatience of the Ant are smaller than the min pheromone. if so get a random Nap-food-ph Trail.

            com.pseuco.np21.shared.Trail.Pheromone p = suggestedTrail.getOrUpdateFoodPheromone(false,null,false);

            if (ant.impatience() < p.value() && !trailsListWithJustNap.isEmpty()){

                int randomNapIndex = random.nextInt(trailsListWithJustNap.size());

                targetTrail = trailsListWithJustNap.get(randomNapIndex);
                ant.setCandidatesList(trailsListWithJustNap);
            } else{
                targetTrail = suggestedTrail; /* otherwise take that randomly picked Trail with NonNap-food-pheromone
                as a target Trail */
                ant.setCandidatesList(minTrails);
            }
        }
        return targetTrail;
    }

    /**
     * this methode is used to check whether the Clearing has a Connected Trail.
     *
     * @param c  Current Clearing.
     * @param connectedTrails the Trails that are connected to the currentClearing c.
     * @return   return true if you found a Trail.
     */
    public boolean checkTrail(Clearing c,List<Trail> connectedTrails,Ant ant) {
        if (connectedTrails.isEmpty()) {
            return false;
        }
        for (Trail t : connectedTrails) {
            // if the Trail not Map
            if (!t.getOrUpdateFoodPheromone(false, null, ant.isAdventurer()).isInfinite()) {
                // if the TrailSequence empty return true
                if (ant.TrailSequence.isEmpty() && ant.alreadyEnteredTrails.isEmpty()) {
                    return true;
                }
                // if the TrailSequence not empty then the list of AlreadyEnteredTrail is for sure not empty.
                assert (!ant.alreadyEnteredTrails.isEmpty());
                // if the Trail has been entered Already then return false
                if (!ant.alreadyEnteredTrails.containsKey(t.id())) {
                    // else if the Trail does not lead to last clearing return True;
                    if (ant.getClearingSequence().size() == 1) {
                        return true;
                    } else {
                        if (!(ant.TrailSequence.get(ant.TrailSequence.size() - 1).reverse().id() == t.id())) {
                            return true;
                        }
                    }
                }
            }
        }
        return false; // otherwise return false
    }




    /**
     * this methode used to get the Trail that leads to the previous clearing (used by No-Food-Return)
     * @param currentClearing current Clearing
     * @param ant Ant
     * @return the Trail
     */
    public Trail getTrailByNofoodReturn(Clearing currentClearing, Ant ant) {
        List<Trail> connectedTrails = currentClearing.connectsTo();
        assert !connectedTrails.isEmpty();
        Trail targetTrail = connectedTrails.get(0);
        for(Trail t : connectedTrails){
            assert !ant.TrailSequence.isEmpty();
            int size = ant.TrailSequence.size();
            if(ant.TrailSequence.get(size-1).reverse().id() == t.id() ){
                targetTrail = t;
            }
        }
        return targetTrail;
    }



}
