package com.pseuco.np21;

import java.util.*;

/**
 * this class used to get the Target Trail by heading back home. we use it in the (AntRunHandler class)
 */
public class HomeWardPathCheck {

    private final Ant ant;

    /**
     * constructor
     *
     * @param ant Ant
     */

    public HomeWardPathCheck(Ant ant) {
        this.ant = ant;
    }


    /**
     * this methode used to get the right Trail on the way Home.
     * @param currentClearing the current Clearing where the Ant is now.
     * @return the Trail to take.
     */
    public Trail getTargetTrail(Clearing currentClearing){

        List<Clearing> sequence =ant.getClearingSequence();  // the sequence
        assert (sequence.size() > 1);
        List<Trail> connectedTrails = currentClearing.connectsTo(); // the out Trails from the Current Clearing
        List<Trail> candidates = new ArrayList<>();
        if (ant.isAdventurer()){ // check if the Ant is an Adventurer.
            int currentClearingNumberFromTheSequence = 0; // get the index of the currentClearing from sequence.
            for (Clearing clearing : sequence) {     // by looping the sequence
                if (clearing.id() != currentClearing.id()) {
                    currentClearingNumberFromTheSequence++;
                } else {
                    break;
                }
            }
            //now return the Trail which leads to the Clearing which is Ordered in the sequence -->
            for (Trail target : connectedTrails) {    //--> exactly one index behind the CurrentClearing
                if (target.to().id() == sequence.get(currentClearingNumberFromTheSequence - 1).id()) {
                    candidates.add(target);
                    ant.setCandidatesList(candidates);// this is the list we give the recorder signal by selecting the Trail
                    return target;
                }
            }
        }
        List<Trail> listToBeCleared = new ArrayList<>(connectedTrails);
        assert ( listToBeCleared.size() == connectedTrails.size());
        List<Trail> listWithoutMapOrNapTrailsAndTheTrailWeComeFrom = new ArrayList<>();

        removeMapOrNapTrailsAndTheTrailWeComeFrom(listToBeCleared,listWithoutMapOrNapTrailsAndTheTrailWeComeFrom,
                currentClearing,ant);

        assert (! listWithoutMapOrNapTrailsAndTheTrailWeComeFrom.isEmpty());

        List<Trail> minTrails = new ArrayList<>(); // all Trails which has the same min antHill-pheromone

        makeListWithJustMin(minTrails,listWithoutMapOrNapTrailsAndTheTrailWeComeFrom);

        Random random = new Random();

        int randomIndex = random.nextInt(minTrails.size());//get random number
        Trail targetTrail =  minTrails.get(randomIndex);
        ant.setCandidatesList(minTrails);
        return  targetTrail  ;// now return a random Trails which has min-antHill-Pheromone.

    }


    /**
     * this methode used to add the Trails with min-Hill Value to a min List. (we use this methode just in this class in "getTargetTrail")
     * @param minTrailsList  List with min-Hill Trails.
     * @param connectedTrails   the Trails which has all Trails .
     */
    private void makeListWithJustMin(List<Trail> minTrailsList ,List<Trail> connectedTrails){
        assert connectedTrails.size() != 0;
        if (connectedTrails.size() ==1){
            minTrailsList.add(connectedTrails.get(0));
        }else {
            for (int i = 0; i < (connectedTrails.size() - 1); i++) {  // compare the Pheromones and add the min-ones to the list.
                Trail t1 = connectedTrails.get(i);
                Trail t2 = connectedTrails.get(i + 1);
                com.pseuco.np21.shared.Trail.Pheromone p1 = t1.getOrUpdateHillPheromone(false, null);
                com.pseuco.np21.shared.Trail.Pheromone p2 = t2.getOrUpdateHillPheromone(false, null);

                    if (p1.value() > p2.value()) {
                        minTrailsList.add(t2);
                        // remove t1 if it is present and all the Trails with the same Hill-Value , if not nothing is happening(is written in javaDoc)
                        for (int k = 0; k < minTrailsList.size(); k++) {
                            int hillValueOfTrailInList = minTrailsList.get(k).getOrUpdateHillPheromone(false, null).value();
                            if (hillValueOfTrailInList == t1.getOrUpdateHillPheromone(false, null).value()) {
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
     * this methode remove all Trails with Map-Pheromone from the given list. (we use this methode just in this class in "getTargetTrail")
     * @param trailList trailsList
     * @param ant ant
     */
    private void removeMapOrNapTrailsAndTheTrailWeComeFrom(List<Trail> trailList,List<Trail> listWithoutMapOrNapTrailsAndTheTrailWeComeFrom
            ,Clearing currentClearing, Ant ant) {
        assert (!trailList.isEmpty());  // check if the list are not Empty
        assert ( ant.getClearingSequence().size() > 1);
        for (Trail t : trailList) {
            // remove all Trails which are already has Map Value Or the last one in Sequence.
            com.pseuco.np21.shared.Trail.Pheromone p = t.getOrUpdateHillPheromone(false, null);
            boolean b = trailToClearingAfterCurrentInSequence(currentClearing, t);
            if (!b) {
                assert (ant.getClearingSequence().size() > 1);
                    if (p.isAPheromone() && !p.isInfinite()) {
                        listWithoutMapOrNapTrailsAndTheTrailWeComeFrom.add(t);
                    }
            }
        }
    }
    /**
     * this methode used to check if the given Trail leads to a Clearing which index in sequence bigger than the current Clearing
     *  (we use this methode just in this class in "getTargetTrail")
      * @param current Current Clearing
     * @param trailToBeChecked  the Trail to be checked
     * @return  true if the Trail Leads to a Clearing that is ordered after the Current Clearing in the Sequence
     */
        private boolean trailToClearingAfterCurrentInSequence(Clearing current , Trail trailToBeChecked){
            int currentClearingNumberFromTheSequence = 0; // get the index of the currentClearing from sequence.
            List<Clearing> sequence = ant.getClearingSequence();
            for (Clearing clearing : sequence) {     // by looping the sequence
                if (clearing.id() != current.id()) {
                    currentClearingNumberFromTheSequence++;
                } else {
                    break;
                }
            }
            if (currentClearingNumberFromTheSequence == sequence.size() -1){
                return  false;
            }
            assert (sequence.size() > 1);
            return trailToBeChecked.to().id() == sequence.get(currentClearingNumberFromTheSequence + 1).id();
        }


}
