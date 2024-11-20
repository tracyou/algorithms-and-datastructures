package models;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.function.BinaryOperator;
import java.util.function.Function;

public class OrderedArrayList<E>
        extends ArrayList<E>
        implements OrderedList<E> {

    protected Comparator<? super E> sortOrder;   // the comparator that has been used with the latest sort
    protected int nSorted;                       // the number of sorted items in the first section of the list
    // representation-invariant
    //      all items at index positions 0 <= index < nSorted have been ordered by the given sortOrder comparator
    //      other items at index position nSorted <= index < size() can be in any order amongst themselves
    //              and also relative to the sorted section

    public OrderedArrayList() {
        this(null);
    }

    public OrderedArrayList(Comparator<? super E> sortOrder) {
        super();
        this.sortOrder = sortOrder;
        this.nSorted = 0;
    }

    public Comparator<? super E> getSortOrder() {
        return this.sortOrder;
    }

    @Override
    public void clear() {
        super.clear();
        this.nSorted = 0;
    }

    @Override
    public void sort(Comparator<? super E> c) {
        super.sort(c);
        this.sortOrder = c;
        this.nSorted = this.size();
    }

    @Override
    public void add(int index, E item) {
        super.add(index, item);
        nSorted = Math.min(nSorted, index);
    }

    @Override
    public E remove(int index) {
        E item = super.remove(index);
        nSorted--;
        return item;
    }

    @Override
    public boolean remove(Object object) {
        int index = super.indexOf(object);
        if (index == -1) {
            return false;
        } else {
            super.remove(index);
            nSorted--;
            return true;
        }
    }

    @Override
    public void sort() {
        if (this.nSorted < this.size()) {
            this.sort(this.sortOrder);
        }
    }

    @Override
    public int indexOf(Object item) {
        // efficient search can be done only if you have provided an sortOrder for the list
        if (this.getSortOrder() != null) {
            return indexOfByIterativeBinarySearch((E) item);
        } else {
            return super.indexOf(item);
        }
    }

    @Override
    public int indexOfByBinarySearch(E searchItem) {
        if (searchItem != null) {
            // some arbitrary choice to use the iterative or the recursive version
            return indexOfByRecursiveBinarySearch(searchItem);
        } else {
            return -1;
        }
    }

    /**
     * finds the position of the searchItem by an iterative binary search algorithm in the
     * sorted section of the arrayList, using the this.sortOrder comparator for comparison and equality test.
     * If the item is not found in the sorted section, the unsorted section of the arrayList shall be searched by linear search.
     * The found item shall yield a 0 result from the this.sortOrder comparator, and that need not to be in agreement with the .equals test.
     * Here we follow the comparator for sorting items and for deciding on equality.
     *
     * @param searchItem the item to be searched on the basis of comparison by this.sortOrder
     * @return the position index of the found item in the arrayList, or -1 if no item matches the search item.
     */
    public int indexOfByIterativeBinarySearch(E searchItem) {
        int from = 0;
        int to = nSorted - 1;

        // Iteratively divide the search interval in half until the search item is found or the interval is empty
        while (from <= to) {
            // Find the middle index
            int midIndex = (from + to) / 2;
            // Compare the search item with the middle item
            int number = sortOrder.compare(searchItem, get(midIndex));
            if (number < 0) {
                // Search the left half if the search item is less than the middle element,
                to = midIndex - 1;
            } else if (number > 0) {
                // Search the right half if the search item is greater than the middle element,
                from = midIndex + 1;
            } else {
                // Returns the index of the middle element if the search item is equal to the middle element,
                return midIndex;
            }
        }

        // If the search item was not found in the sorted part of the list then we do a  linear search
        for (int i = 0; i < size(); i++) {
            if (sortOrder.compare(searchItem, get(i)) == 0) {
                return i;
            }
        }

        // -1 will be returned if the search item was not found in the list
        return -1;
    }

    /**
     * finds the position of the searchItem by a recursive binary search algorithm in the
     * sorted section of the arrayList, using the this.sortOrder comparator for comparison and equality test.
     * If the item is not found in the sorted section, the unsorted section of the arrayList shall be searched by linear search.
     * The found item shall yield a 0 result from the this.sortOrder comparator, and that need not to be in agreement with the .equals test.
     * Here we follow the comparator for sorting items and for deciding on equality.
     *
     * @param searchItem the item to be searched on the basis of comparison by this.sortOrder
     * @return the position index of the found item in the arrayList, or -1 if no item matches the search item.
     */
    public int indexOfByRecursiveBinarySearch(E searchItem) {
        return recursiveSearch(searchItem, 0, nSorted - 1);
    }

    public int recursiveSearch(E searchItem, int from, int to) {
        if (from <= to) {
            // Find the middle index
            int midIndex = (from + to) / 2;
            // Compare the search item with the middle item
            int number = sortOrder.compare(searchItem, get(midIndex));
            if (number < 0) {
                // Search the left half if the search item is less than the middle element,
                return recursiveSearch(searchItem, from, midIndex - 1);
            } else if (number > 0) {
                // Search the right half if the search item is greater than the middle element,
                return recursiveSearch(searchItem, midIndex + 1, to);
            } else {
                return midIndex;
            }
        }

        // If the search item was not found in the sorted part of the list then we do a  linear search
        for (int i = 0; i < size(); i++) {
            if (sortOrder.compare(searchItem, get(i)) == 0) {
                return i;
            }
        }

        // -1 will be returned if the search item was not found in the list
        return -1;
    }


    /**
     * finds a match of newItem in the list and applies the merger operator with the newItem to that match
     * i.e. the found match is replaced by the outcome of the merge between the match and the newItem
     * If no match is found in the list, the newItem is added to the list.
     *
     * @param newItem
     * @param merger  a function that takes two items and returns an item that contains the merged content of
     *                the two items according to some merging rule.
     *                e.g. a merger could add the value of attribute X of the second item
     *                to attribute X of the first item and then return the first item
     * @return whether a new item was added to the list or not
     */
    @Override
    public boolean merge(E newItem, BinaryOperator<E> merger) {
        if (newItem == null) return false;
        // Find the index of the matched item by a recursive binary search algorithm
        int matchedItemIndex = this.indexOfByRecursiveBinarySearch(newItem);

        // If no match is found add the newItem to the list
        if (matchedItemIndex < 0) {
            this.add(newItem);
            return true;
        } else {
        // If a match is found apply the merger function to the matched item and the newItem
        // and replace the matched item with the merged item in the list
            this.set(matchedItemIndex, newItem);
            return false;
        }
    }

    /**
     * calculates the total sum of contributions of all items in the list
     *
     * @param mapper a function that calculates the contribution of a single item
     * @return the total sum of all contributions
     */
    @Override
    public double aggregate(Function<E, Double> mapper) {
        // Initialize sum to 0.0
        double sum = 0.0;

        // Iterate through each item in the list
        for (E e : this) {
            // Use the mapper function to calculate the contribution of the item and add it to the sum
            sum += mapper.apply(e);
        }

        return sum;
    }
}
