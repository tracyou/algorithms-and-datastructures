package models;

public class Train {
    private final String origin;
    private final String destination;
    private final Locomotive engine;
    private Wagon firstWagon;

    /* Representation invariants:
        firstWagon == null || firstWagon.previousWagon == null
        engine != null
     */

    public Train(Locomotive engine, String origin, String destination) {
        this.engine = engine;
        this.destination = destination;
        this.origin = origin;
    }

    /**
     * Indicates whether the train has at least one connected Wagon
     *
     * @return if the train has a first wagon in boolean
     */
    public boolean hasWagons() {
        return this.firstWagon != null;
    }

    /**
     * A train is a passenger train when its first wagon is a PassengerWagon
     * (we do not worry about the possibility of mixed compositions here)
     *
     * @return if the first wagon in the train is an instance of the PassengerWagon class
     */
    public boolean isPassengerTrain() {
        return this.firstWagon instanceof PassengerWagon;
    }

    /**
     * A train is a freight train when its first wagon is a FreightWagon
     * (we do not worry about the possibility of mixed compositions here)
     *
     * @return if the first wagon in the train is an instance of the FreightWagon class
     */
    public boolean isFreightTrain() {
        return this.firstWagon instanceof FreightWagon;
    }

    public Locomotive getEngine() {
        return engine;
    }

    public Wagon getFirstWagon() {
        return firstWagon;
    }

    /**
     * Replaces the current sequence of wagons (if any) in the train
     * by the given new sequence of wagons (if any)
     *
     * @param wagon the first wagon of a sequence of wagons to be attached (can be null)
     */
    public void setFirstWagon(Wagon wagon) {
        this.firstWagon = wagon;
    }

    /**
     * @return the number of Wagons connected to the train
     */
    public int getNumberOfWagons() {
        // checks if a train has any wagons
        if (!this.hasWagons()) {
            // if it doesn't a '0' is returned
            return 0;
        } else {
            // if it does the sequence of the first wagon is returned
            return this.firstWagon.getSequenceLength();
        }

    }

    /**
     * @return the last wagon attached to the train
     */
    public Wagon getLastWagonAttached() {
        // checks if a train has any wagons
        if (!this.hasWagons()) {
            // if it doesn't a null is returned
            return null;
        } else {
            // if it does the last wagon attached to the sequence is returned
            return this.firstWagon.getLastWagonAttached();
        }
    }

    /**
     * @return the total number of seats on a passenger train
     * (return 0 for a freight train)
     */
    public int getTotalNumberOfSeats() {
        int total = 0;

        // checks if the train is a passenger train
        if (isPassengerTrain()) {
            // to prevent having the number of seats of the same wagon added up to the total over and over again we use
            // the current variable to switch the wagons in the loop
            PassengerWagon current = (PassengerWagon) this.firstWagon;

            // the for loop, loops through all the wagons in the train
            for (int i = 0; i < this.firstWagon.getSequenceLength(); i++) {
                // for each wagon in the loop the number of seats is added up with the total
                total += current.getNumberOfSeats();

                current = (PassengerWagon) current.getNextWagon();
            }
        }
        return total;

    }

    /**
     * calculates the total maximum weight of a freight train
     *
     * @return the total maximum weight of a freight train
     * (return 0 for a passenger train)
     */
    public int getTotalMaxWeight() {
        int total = 0;

        // checks if the train is a freight train
        if (isFreightTrain()) {
            // to prevent having the weight of the same wagon added up to the total over and over again we use the current
            // variable to switch the wagons in the loop
            FreightWagon current = (FreightWagon) this.firstWagon;

            // the for loop, loops through all the wagons in the train
            for (int i = 0; i < this.firstWagon.getSequenceLength(); i++) {
                // for each wagon in the loop the weight is added up with the total
                total += current.getMaxWeight();

                current = (FreightWagon) current.getNextWagon();
            }
        }
        return total;

    }

    /**
     * Finds the wagon at the given position (starting at 1 for the first wagon of the train)
     *
     * @param position
     * @return the wagon found at the given position
     * (return null if the position is not valid for this train)
     */
    public Wagon findWagonAtPosition(int position) {
        // if the position is less than 0, null will be returned
        if (position <= 0) return null;
        Wagon wagon = firstWagon;
        int currentPosition = 1;
        // while the while loop is not null and the currentPosition is less than the position the wagon gets reassigned
        // to the next and the currentPosition goes up with 1
        while (wagon != null && currentPosition < position) {
            wagon = wagon.getNextWagon();
            currentPosition++;
        }

        return wagon;
    }

    /**
     * Finds the wagon with a given wagonId
     *
     * @param wagonId
     * @return the wagon found
     * (return null if no wagon was found with the given wagonId)
     */
    public Wagon findWagonById(int wagonId) {
        Wagon currentWagon = firstWagon;

        // for each number of wagons
        for (int i = 1; i <= this.getNumberOfWagons(); i++) {
            // if currentWagon has the same as the given ID, the wagon
            if (currentWagon.getId() == wagonId) {
                return currentWagon;
            } else {
            // if it doesn't have the same ID the current wagon is reassigned with the next
                currentWagon = currentWagon.getNextWagon();
            }
        }

        return currentWagon;
    }

    /**
     * Determines if the given sequence of wagons can be attached to this train
     * Verifies if the type of wagons match the type of train (Passenger or Freight)
     * Verifies that the capacity of the engine is sufficient to also pull the additional wagons
     * Verifies that the wagon is not part of the train already
     * Ignores the predecessors before the head wagon, if any
     *
     * @param wagon the head wagon of a sequence of wagons to consider for attachment
     * @return whether type and capacity of this train can accommodate attachment of the sequence
     */
    public boolean canAttach(Wagon wagon) {
        // if train and wagon are both the same type or the first wagon is zero
        if (this.isFreightTrain() && wagon instanceof FreightWagon ||
                this.isPassengerTrain() && wagon instanceof PassengerWagon
                || this.firstWagon == null) {
            Wagon currentWagon = firstWagon;
            // to check if the wagon is already in the train we loop all the wagons in the train and false will be
            // returned if the id matches on of the id's in the wagon
            for (int i = 1; i <= getNumberOfWagons(); i++) {
                if (currentWagon.getId() == wagon.getId()) {
                    return false;
                }
                currentWagon = currentWagon.getNextWagon();
            }

            // the return checks if the amount wagons already in the train and the sequence of wagons combined are
            // smaller or equal the maximum capacity
            return this.getNumberOfWagons() + wagon.getSequenceLength() <= this.getEngine().getMaxWagons();

        }
        return false;
    }

    /**
     * Tries to attach the given sequence of wagons to the rear of the train
     * No change is made if the attachment cannot be made.
     * (when the sequence is not compatible or the engine has insufficient capacity)
     * if attachment is possible, the head wagon is first detached from its predecessors, if any
     *
     * @param wagon the head wagon of a sequence of wagons to be attached
     * @return whether the attachment could be completed successfully
     */
    public boolean attachToRear(Wagon wagon) {
        // if the train doesn't have wagon, the wagon becomes the first wagon and the front is detached
        if (!hasWagons()) {
            firstWagon = wagon;
            wagon.detachFront();
            return true;
        // if the first wagon in the train doesn't have a next wagon and the wagon can be attached, the front wagon is
        // detached and the wagon is attached to the first
        } else if (!firstWagon.hasNextWagon() && canAttach(wagon)) {
            wagon.detachFront();
            firstWagon.attachTail(wagon);
            return true;
        // else if the wagon can be attached, the front is detached, and it is attached to the last on in the train
        } else if (canAttach(wagon)) {
            wagon.detachFront();
            firstWagon.getLastWagonAttached().attachTail(wagon);
            return true;
        }

        return false;
    }

    /**
     * Tries to insert the given sequence of wagons at the front of the train
     * (the front is at position one, before the current first wagon, if any)
     * No change is made if the insertion cannot be made.
     * (when the sequence is not compatible or the engine has insufficient capacity)
     * if insertion is possible, the head wagon is first detached from its predecessors, if any
     *
     * @param wagon the head wagon of a sequence of wagons to be inserted
     * @return whether the insertion could be completed successfully
     */
    public boolean insertAtFront(Wagon wagon) {
        // checks if the wagon can be attached
        if (canAttach(wagon)) {
            // checks if the train has wagons
            if (this.hasWagons()) {
                // the front of the wagon is detached and the first wagon is attached to the wagon
                wagon.detachFront();
                wagon.getLastWagonAttached().attachTail(firstWagon);
            }

            // the first wagon is reassigned to the wagon
            firstWagon = wagon;
            return true;
        }

        return false;
    }

    /**
     * Tries to insert the given sequence of wagons at/before the given position in the train.
     * (The current wagon at given position including all its successors shall then be reattached
     * after the last wagon of the given sequence.)
     * No change is made if the insertion cannot be made.
     * (when the sequence is not compatible or the engine has insufficient capacity
     * or the given position is not valid for insertion into this train)
     * if insertion is possible, the head wagon of the sequence is first detached from its predecessors, if any
     *
     * @param position the position where the head wagon and its successors shall be inserted
     *                 1 <= position <= numWagons + 1
     *                 (i.e. insertion immediately after the last wagon is also possible)
     * @param wagon    the head wagon of a sequence of wagons to be inserted
     * @return whether the insertion could be completed successfully
     */
    public boolean insertAtPosition(int position, Wagon wagon) {
        Wagon currentWagon = this.findWagonAtPosition(position);
        // checks if the position is 1
        if (position == 1) {
            // the front is detached and the wagon is inserted at the front
            wagon.detachFront();
            return this.insertAtFront(wagon);
            // if the wagon can be attached
        } else if (canAttach(wagon)) {
            if (position == this.getNumberOfWagons() + 1) {
                // the front is detached
                // if the position is higher than the number of wagons the wagon gets attached to the tail
                wagon.detachFront();
                this.getLastWagonAttached().attachTail(wagon);
            } else if (currentWagon != null) {
                // if the position already has a wagon
                // the front is detached
                wagon.detachFront();

                // we then get the previous wagon of the currentWagon
                Wagon front = currentWagon.getPreviousWagon();

                // detach the front of the current wagon
                currentWagon.detachFront();

                // the wagon then gets attached to the front/previou swagon and the currentWagon then is attached to the
                // wagon
                front.attachTail(wagon);
                wagon.getLastWagonAttached().attachTail(currentWagon);
            } else {
                return false;
            }
            return true;
        }

        return false;
    }

    /**
     * Tries to remove one Wagon with the given wagonId from this train
     * and attach it at the rear of the given toTrain
     * No change is made if the removal or attachment cannot be made
     * (when the wagon cannot be found, or the trains are not compatible
     * or the engine of toTrain has insufficient capacity)
     *
     * @param wagonId the id of the wagon to be removed
     * @param toTrain the train to which the wagon shall be attached
     *                toTrain shall be different from this train
     * @return whether the move could be completed successfully
     */
    public boolean moveOneWagon(int wagonId, Train toTrain) {
        Wagon wagon = findWagonById(wagonId);

        // if there was no wagon found with the ID false is returned
        if (wagon == null) {
            return false;
        // if the wagon can not be attached to the selected train false is returned
        } else if (!toTrain.canAttach(wagon.getLastWagonAttached())) {
            return false;
        // if the wagon is the first, it's tail is detached
        } else if (wagon == firstWagon) {
            firstWagon = wagon.detachTail();
        // else the wagon is completely removed from its sequence
        } else {
            wagon.removeFromSequence();
        }

        // the wagon is attached to rear
        toTrain.attachToRear(wagon);

        return true;
    }

    /**
     * Tries to split this train before the wagon at given position and move the complete sequence
     * of wagons from the given position to the rear of toTrain.
     * No change is made if the split or re-attachment cannot be made
     * (when the position is not valid for this train, or the trains are not compatible
     * or the engine of toTrain has insufficient capacity)
     *
     * @param position 1 <= position <= numWagons
     * @param toTrain  the train to which the split sequence shall be attached
     *                 toTrain shall be different from this train
     * @return whether the move could be completed successfully
     */
    public boolean splitAtPosition(int position, Train toTrain) {

        // checks if the wagon has wagons, if there aren't any false is returned
        if (!this.hasWagons()) {
            return false;
        } else {
            Wagon wagon = findWagonAtPosition(position);
            // if the wagon is null or can't be attached to the wagon false is returned
            if (wagon == null || !toTrain.canAttach(wagon)) {
                return false;
            // else if the wagon is the first wagon it is attached to the rear and the first wagon is reassigned
            } else if (!wagon.hasPreviousWagon()) {
                toTrain.attachToRear(wagon);
                this.setFirstWagon(null);
            // else the wagon is reattached to the rear of the train
            } else {
                wagon.getPreviousWagon().detachTail();
                toTrain.attachToRear(wagon);
            }
            return true;
        }
    }

    /**
     * Reverses the sequence of wagons in this train (if any)
     * i.e. the last wagon becomes the first wagon
     * the previous wagon of the last wagon becomes the second wagon
     * etc.
     * (No change if the train has no wagons or only one wagon)
     */
    public void reverse() {

        // checks if the train has wagons
        if (this.hasWagons()) {

            // the wagons in the train are reversed with the reverseSequence method
            Wagon currentWagon = this.getFirstWagon();
            Wagon newFirstWagon = currentWagon.reverseSequence();
            this.setFirstWagon(newFirstWagon);
        }
    }

    @Override
    public String toString() {
        return "with " + getNumberOfWagons() + " wagons from " + origin + " to " + destination;

    }
}
