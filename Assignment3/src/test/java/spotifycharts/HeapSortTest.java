package spotifycharts;

import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class HeapSortTest {

    @Test
    void testTopsHeapSortWithNumTopsLessThanListSize() {
        List<Integer> items = Arrays.asList(5, 7, 3, 8, 7, 9, 6, 2, 4);
        int numTops = 3;
        List<Integer> expected = Arrays.asList(9, 8, 7, 3, 5, 7, 6, 2, 4);
        Sorter<Integer> sorter = new SorterImpl<>();
        List<Integer> actual = sorter.topsHeapSort(numTops, items, Comparator.reverseOrder());
        assertEquals(expected, actual);
    }

    @Test
    void testQuickSortWithSingleElementList() {
        List<Integer> list = Arrays.asList(5);
        Sorter<Integer> sorter = new SorterImpl<>();
        List<Integer> result = sorter.quickSort(list, Comparator.naturalOrder());
        assertEquals(list, result);
    }


}
