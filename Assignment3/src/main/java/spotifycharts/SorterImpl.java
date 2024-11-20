package spotifycharts;

import java.util.Comparator;
import java.util.List;

public class SorterImpl<E> implements Sorter<E> {

    /**
     * Sorts all items by selection or insertion sort using the provided comparator
     * for deciding relative ordening of two items
     * Items are sorted 'in place' without use of an auxiliary list or array
     *
     * @param items
     * @param comparator
     * @return the items sorted in place
     */
    public List<E> selInsBubSort(List<E> items, Comparator<E> comparator) {
        // implement selection sort or insertion sort or bubble sort

        int n = items.size();

        // One by one move boundary of unsorted subarray
        for (int i = 0; i < n - 1; i++) {
            // Find the minimum element in unsorted array
            int min_idx = i;
            for (int j = i + 1; j < n; j++)
                if (comparator.compare(items.get(j), items.get(min_idx)) < 0)
                    min_idx = j;

            // Swap the found minimum element with the first element
            swap(items, min_idx, i);
        }

        return items;   // replace as you find appropriate
    }

    /**
     * Sorts all items by quick sort using the provided comparator
     * for deciding relative ordening of two items
     * Items are sorted 'in place' without use of an auxiliary list or array
     *
     * @param items
     * @param comparator
     * @return the items sorted in place
     */
    public List<E> quickSort(List<E> items, Comparator<E> comparator) {
        // provide a recursive quickSort implementation,
        //  that is different from the example given in the lecture

            quickSortPart(items, 0, items.size() - 1, comparator);

            return items; // replace as you find appropriate
    }

    /* The main function that implements QuickSort
          arr[] --> Array to be sorted,
          low --> Starting index,
          high --> Ending index
 */
    private void quickSortPart(List<E> items, int low, int high, Comparator<E> comparator) {
        if (low < high) {

            // pi is partitioning index, items.get(pi) is now at right place
            int pi = partition(items, low, high, comparator);

            // Separately sort elements before
            // partition and after partition
            quickSortPart(items, low, pi - 1, comparator);
            quickSortPart(items, pi + 1, high, comparator);
        }
    }

    /* This function takes last element as pivot, places
   the pivot element at its correct position in sorted
   array, and places all smaller (smaller than pivot)
   to left of pivot and all greater elements to right
   of pivot */
    private int partition(List<E> items, int low, int high, Comparator<E> comparator) {

        // pivot
        E pivot = items.get(high);

        // Index of smaller element and
        // indicates the right position
        // of pivot found so far
        int i = low - 1;

        for (int j = low; j <= high - 1; j++) {

            // If current element is smaller
            // than the pivot
            if (comparator.compare(items.get(j), pivot) < 0) {

                // Increment index of
                // smaller element
                i++;
                swap(items, i, j);
            }
        }

        swap(items, i + 1, high);
        return (i + 1);
    }

    // A utility function to swap two elements
    private void swap(List<E> items, int i, int j) {
        E temp = items.get(i);
        items.set(i, items.get(j));
        items.set(j, temp);
    }

    /**
     * Identifies the lead collection of numTops items according to the ordening criteria of comparator
     * and organizes and sorts this lead collection into the first numTops positions of the list
     * with use of (zero-based) heapSwim and heapSink operations.
     * The remaining items are kept in the tail of the list, in arbitrary order.
     * Items are sorted 'in place' without use of an auxiliary list or array or other positions in items
     *
     * @param numTops    the size of the lead collection of items to be found and sorted
     * @param items
     * @param comparator
     * @return the items list with its first numTops items sorted according to comparator
     * all other items >= any item in the lead collection
     */
    public List<E> topsHeapSort(int numTops, List<E> items, Comparator<E> comparator) {

        // the lead collection of numTops items will be organised into a (zero-based) heap structure
        // in the first numTops list positions using the reverseComparator for the heap condition.
        // that way the root of the heap will contain the worst item of the lead collection
        // which can be compared easily against other candidates from the remainder of the list
        Comparator<E> reverseComparator = comparator.reversed();

        // initialise the lead collection with the first numTops items in the list
        for (int heapSize = 2; heapSize <= numTops; heapSize++) {
            // repair the heap condition of items[0..heapSize-2] to include new item items[heapSize-1]
            heapSwim(items, heapSize, reverseComparator);
        }

        // insert remaining items into the lead collection as appropriate
        for (int i = numTops; i < items.size(); i++) {
            // loop-invariant: items[0..numTops-1] represents the current lead collection in a heap data structure
            //  the root of the heap is the currently trailing item in the lead collection,
            //  which will lose its membership if a better item is found from position i onwards
            E item = items.get(i);
            E worstLeadItem = items.get(0);
            if (comparator.compare(item, worstLeadItem) < 0) {
                // item < worstLeadItem, so shall be included in the lead collection
                items.set(0, item);
                // demote worstLeadItem back to the tail collection, at the orginal position of item
                items.set(i, worstLeadItem);
                // repair the heap condition of the lead collection
                heapSink(items, numTops, reverseComparator);
            }
        }

        // the first numTops positions of the list now contain the lead collection
        // the reverseComparator heap condition applies to this lead collection
        // now use heapSort to realise full ordening of this collection
        for (int i = numTops - 1; i > 0; i--) {
            // loop-invariant: items[i+1..numTops-1] contains the tail part of the sorted lead collection
            // position 0 holds the root item of a heap of size i+1 organised by reverseComparator
            // this root item is the worst item of the remaining front part of the lead collection

            //  swap item[0] and item[i];
            //  this moves item[0] to its designated position
            swap(items, 0, i);

            // the new root may have violated the heap condition
            //  repair the heap condition on the remaining heap of size i
            heapSink(items.subList(0, i), i, reverseComparator);
        }

        return items;
    }

    /**
     * Repairs the zero-based heap condition for items[heapSize-1] on the basis of the comparator
     * all items[0..heapSize-2] are assumed to satisfy the heap condition
     * The zero-bases heap condition says:
     * all items[i] <= items[2*i+1] and items[i] <= items[2*i+2], if any
     * or equivalently:     all items[i] >= items[(i-1)/2]
     *
     * @param items
     * @param heapSize
     * @param comparator
     */
    protected void heapSwim(List<E> items, int heapSize, Comparator<E> comparator) {
        // swim items[heapSize-1] up the heap until
        //      i==0 || items[(i-1]/2] <= items[i]
        int i = heapSize - 1;
        while (i > 0 && comparator.compare(items.get((i - 1) / 2), items.get(i)) > 0) {
            // swap items[(i-1)/2] and items[i]
            swap(items, i, (i - 1) / 2);
            // update i to the parent index
            i = (i - 1) / 2;
        }
    }

    /**
     * Repairs the zero-based heap condition for its root items[0] on the basis of the comparator
     * all items[1..heapSize-1] are assumed to satisfy the heap condition
     * The zero-bases heap condition says:
     * all items[i] <= items[2*i+1] and items[i] <= items[2*i+2], if any
     * or equivalently:     all items[i] >= items[(i-1)/2]
     *
     * @param items
     * @param heapSize
     * @param comparator
     */
    protected void heapSink(List<E> items, int heapSize, Comparator<E> comparator) {
        // sink items[0] down the heap until
        //      2*i+1>=heapSize || (items[i] <= items[2*i+1] && items[i] <= items[2*i+2])
        int i = 0;
        while (2 * i + 1 < heapSize) {
            int j = 2 * i + 1;
            if (j + 1 < heapSize && comparator.compare(items.get(j), items.get(j + 1)) > 0) {
                j++;
            }
            if (comparator.compare(items.get(i), items.get(j)) <= 0) {
                break;
            }
            swap(items, i, j);
            i = j;
        }
    }
}
