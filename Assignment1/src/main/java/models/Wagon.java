package models;

public abstract class Wagon {
    protected int id;               // some unique ID of a Wagon
    private Wagon nextWagon;        // another wagon that is appended at the tail of this wagon
    // a.k.a. the successor of this wagon in a sequence
    // set to null if no successor is connected
    private Wagon previousWagon;    // another wagon that is prepended at the front of this wagon
    // a.k.a. the predecessor of this wagon in a sequence
    // set to null if no predecessor is connected


    // representation invariant propositions:
    // tail-connection-invariant:   wagon.nextWagon == null or wagon == wagon.nextWagon.previousWagon
    // front-connection-invariant:  wagon.previousWagon == null or wagon = wagon.previousWagon.nextWagon

    public Wagon(int wagonId) {
        this.id = wagonId;
    }

    public int getId() {
        return id;
    }

    public Wagon getNextWagon() {
        return nextWagon;
    }

    public Wagon getPreviousWagon() {
        return previousWagon;
    }

    /**
     * @return whether this wagon has a wagon appended at the tail
     */
    public boolean hasNextWagon() {
        return this.nextWagon != null;
    }

    /**
     * @return whether this wagon has a wagon prepended at the front
     */
    public boolean hasPreviousWagon() {
        return this.previousWagon != null;
    }

    /**
     * Returns the last wagon attached to it,
     * if there are no wagons attached to it then this wagon is the last wagon.
     *
     * @return the last wagon
     */
    public Wagon getLastWagonAttached() {
        Wagon current = this;

        // loops over the list of wagons until there is no nextWagon
        while (current.hasNextWagon()) {
            current = current.nextWagon;
        }

        return current;
    }

    /**
     * @return the length of the sequence of wagons towards the end of its tail
     * including this wagon itself.
     */
    public int getSequenceLength() {
        // initialize the length as 1, since we are counting the current wagon too
        int length = 1;

        // set the current wagon to this wagon
        Wagon current = this;

        // loop through the sequence of wagons increment the length by 1 for each wagon
        while (current.hasNextWagon()) {
            current = current.nextWagon;
            length++;
        }

        return length;
    }

    /**
     * Attaches the tail wagon and its connected successors behind this wagon,
     * if and only if this wagon has no wagon attached at its tail
     * and if the tail wagon has no wagon attached in front of it.
     *
     * @param tail the wagon to attach behind this wagon.
     * @throws IllegalStateException if this wagon already has a wagon appended to it.
     * @throws IllegalStateException if tail is already attached to a wagon in front of it.
     *                               The exception should include a message that reports the conflicting connection,
     *                               e.g.: "%s is already pulling %s"
     *                               or:   "%s has already been attached to %s"
     */
    public void attachTail(Wagon tail) {
        // check if both wagons are unattached and attaches the tail to this wagon
        if (!this.hasNextWagon() & !tail.hasPreviousWagon()) {
            this.nextWagon = tail;
            tail.previousWagon = this;
            // check if this wagon already has a wagon attached at its tail
        } else if (this.hasNextWagon()) {
            throw new IllegalStateException(String.format("%s has already been attached to %s", this.toString(), this.nextWagon.toString()));
            // check if tail already has a wagon attached at its front
        } else if (tail.hasPreviousWagon()) {
            throw new IllegalStateException(String.format("%s has already been attached to %s", tail.toString(), tail.previousWagon.toString()));
        }
    }

    /**
     * Detaches the tail from this wagon and returns the first wagon of this tail.
     *
     * @return the first wagon of the tail that has been detached
     * or <code>null</code> if it had no wagons attached to its tail.
     */
    public Wagon detachTail() {
        Wagon toBeReturned = null;

        // checks if this wagon has a wagon attached to it's tail
        if (this.hasNextWagon()) {
            toBeReturned = this.nextWagon;

            // remove the reference from this wagon to the detached wagon and the other way around
            this.nextWagon.previousWagon = null;
            this.nextWagon = null;
        }

        return toBeReturned;
    }

    /**
     * Detaches this wagon from the wagon in front of it.
     * No action if this wagon has no previous wagon attached.
     *
     * @return the former previousWagon that has been detached from,
     * or <code>null</code> if it had no previousWagon.
     */
    public Wagon detachFront() {
        Wagon toBeReturned = null;

        // check if there is a wagon attached in front of it
        if (this.hasPreviousWagon()) {
            toBeReturned = this.previousWagon;

            // remove the reference from this wagon to the detached wagon and the other way around
            this.previousWagon.nextWagon = null;
            this.previousWagon = null;

        }

        return toBeReturned;
    }

    /**
     * Replaces the tail of the <code>front</code> wagon by this wagon and its connected successors
     * Before such reconfiguration can be made,
     * the method first disconnects this wagon form its predecessor,
     * and the <code>front</code> wagon from its current tail.
     *
     * @param front the wagon to which this wagon must be attached to.
     */
    public void reAttachTo(Wagon front) {
        this.detachFront();
        front.detachTail();
        front.attachTail(this);
    }

    /**
     * Removes this wagon from the sequence that it is part of,
     * and reconnects its tail to the wagon in front of it, if any.
     */
    public void removeFromSequence() {
        Wagon front = this.previousWagon;
        Wagon tail = this.nextWagon;

        // detach this wagon from its current front wagon
        this.detachFront();
        // detach this wagon from its tail wagon
        this.detachTail();

        // reattach the detached front and tail if they are there
        if (front != null & tail != null) {
            tail.reAttachTo(front);
        }
    }


    /**
     * Reverses the order in the sequence of wagons from this Wagon until its final successor.
     * The reversed sequence is attached again to the wagon in front of this Wagon, if any.
     * No action if this Wagon has no succeeding next wagon attached.
     *
     * @return the new start Wagon of the reversed sequence (with is the former last Wagon of the original sequence)
     */
    public Wagon reverseSequence() {
        Wagon previousList = getPreviousWagon();
        Wagon current = this;
        Wagon reversedList = null;
        Wagon next;

        //loop through all wagons
        while (current != null) {
            next = current.getNextWagon();
            current.removeFromSequence();

            // attach the current wagon to the beginning of the reversed list
            if (reversedList != null) {
                current.attachTail(reversedList);
            }
            reversedList = current;
            current = next;
        }
        // reattach the reversed sequence to the wagon in front of this wagon
        if (previousList != null) {
            previousList.attachTail(reversedList);
        }
        return reversedList;
    }

    @Override
    public String toString() {
        return "[Wagon-" + id + "]";
    }
}
