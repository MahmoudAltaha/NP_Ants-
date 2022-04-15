package com.pseuco.np21;

public class ClearingEntryHandler {

    /**
     * this methode used to update the Pheromones by Food search moving.
     * @param t  the Trail which the Pheromones of its reverse Trail need to be updated.
     * @param ant  the ant
     */
    public void pheromonesUpdatingFoodSearch(Trail t , Ant ant)  {
            com.pseuco.np21.shared.Trail.Pheromone hillPheromone = t.reverse().getOrUpdateHillPheromone(false, null);
            com.pseuco.np21.shared.Trail.Pheromone newPheromone;
            // if the  Clearing was not twice in the sequence then update Hill-Pheromone. (no special cases)
            if (!ant.getTrailsToVisitedClearings().containsKey(t.id())) { // if this Trail took the Ant to an Already visited Trail , don't update
                // get the new Hill_Pheromone value
                if (hillPheromone.isAPheromone()) {
                    int w = ant.getClearingSequence().size() - 1;
                    int value = Math.min(hillPheromone.value(), w);
                    newPheromone = com.pseuco.np21.shared.Trail.Pheromone.get(value);
                    t.reverse().getOrUpdateHillPheromone(true, newPheromone); // update the HIll-Pheromone.
                    t.setShouldBeUpdated(true); // flag to the Recorder to know if we are updating or not
                } else {
                    int w = ant.getClearingSequence().size() - 1;
                    newPheromone = com.pseuco.np21.shared.Trail.Pheromone.get(w);
                    t.reverse().getOrUpdateHillPheromone(true, newPheromone); // update the HIll-Pheromone.
                    t.setShouldBeUpdated(true);
                }

            } else {
                t.setShouldBeUpdated(false);
            }
        }

    /**
     * this methode used to update the Pheromones by NO_Food_Return moving.
     * @param t the Trail which the Pheromones of its reverse Trail need to be updated.
     * @param ant the ant.
     */
        public void pheromonesUpdatingNoFoodReturn  (Trail t, Ant ant) {
                //create a Map food-Pheromone .
                com.pseuco.np21.shared.Trail.Pheromone mapPheromone = com.pseuco.np21.shared.Trail.Pheromone.get((-1));
                //update the Food-Pheromone of the Trail to Map.
                assert mapPheromone.isInfinite();
                t.reverse().getOrUpdateFoodPheromone(true, mapPheromone, ant.isAdventurer());
            }


    /**
     * this methode used to update the Pheromones by Food search moving.
     * @param t the Trail which the Pheromones of its reverse Trail need to be updated.
     * @param clearing the Current Clearing
     * @param ant the Ant
     */
    public void pheromonesUpdatingHomeward  (Trail t,Clearing clearing, Ant ant)  {
            com.pseuco.np21.shared.Trail.Pheromone currentPheromone = t.reverse().getOrUpdateFoodPheromone(false, null, false);
            com.pseuco.np21.shared.Trail.Pheromone newPheromone;
                int currentClearingNumberFromTheSequence = 0; // get the index of the currentClearing from sequence.
                for (int i = 0; i < ant.getClearingSequence().size(); i++) {     // by looping the sequence
                    if (ant.getClearingSequence().get(i).id() != clearing.id()) {
                        currentClearingNumberFromTheSequence++;
                    } else {
                        break;
                    }
                }/*  sequence={A,B,Curr,C,D,Last} we want to update on Trail (curr->B)
                currIndexInSeq = 2; , LastIndexInSeq =size()-1 = 5
                5 - 2 = 3  ->> we have three Trails between the last und curr and the fourth is our Trail
                (1) Last->D, (2) D->C , now we must write (3) C->Curr,
                 SO size()=6 - (CurrIndex= 2) = 4 the right result is 3  so we do (-1)
                */
                int r = (ant.getClearingSequence().size() - 1) - currentClearingNumberFromTheSequence;

                if (!ant.isAdventurer()) {
                    newPheromone = com.pseuco.np21.shared.Trail.Pheromone.get(r);
                } else {
                    if (currentPheromone.isAPheromone() && (!currentPheromone.isInfinite())) {
                        int minPheromoneValue = Math.min(r, currentPheromone.value());
                        newPheromone = com.pseuco.np21.shared.Trail.Pheromone.get(minPheromoneValue);
                    } else {
                        newPheromone = com.pseuco.np21.shared.Trail.Pheromone.get(r);
                    }
                }
                t.reverse().getOrUpdateFoodPheromone(true, newPheromone, ant.isAdventurer()); // update the Food-Pheromone.
    }





}
