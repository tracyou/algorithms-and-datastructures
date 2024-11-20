package spotifycharts;

import java.util.ArrayList;
import java.util.List;

/**
 * The code from this class is based on NameSorterMain.js from NameSorter with some minor adjustments,
 * so it can be used for our speed test
 * Link to the original code: https://dlo.mijnhva.nl/d2l/le/content/467483/viewContent/1316949/View
 */
public class SpeedTest {

    public static void main(String[] args) {
//        bubbleQuickHeapSortSpeedTest();
        heapSortWithChangingTopSpeedTest();
    }
    public static void bubbleQuickHeapSortSpeedTest() {
        SimpleTable<Double> results = new SimpleTable<>(4,18, Double::sum, (a,b)-> a*b);
        List<Song> songs = new ArrayList<>();
        List<Song> sortedSongs;
        SorterImpl<Song> sorterImpl = new SorterImpl<>();
        final int REPEATS = 10;
        final int MAX_N = 500_000;

        long started;
        double duration;

        for (int iteration = 1; iteration <= REPEATS; iteration++) {
            System.out.printf("\nTest iteration-%d\n", iteration);
            for (int n = 100, row = 0; n < MAX_N; n *= 2, row++ ) {
                System.out.printf("\nSorting %d songs:\n", n);

                results.add(0, row, (double)n);

                songs.clear();
                ChartsCalculator chartsCalculator = new ChartsCalculator(0);
                songs = new ArrayList<>(chartsCalculator.registerStreamedSongs(n));

                sortedSongs = new ArrayList<>(songs);
                System.out.println(sortedSongs.size());
                System.gc();
                started = System.nanoTime();
                sorterImpl.selInsBubSort(sortedSongs, Song::compareByHighestStreamsCountTotal);
                duration = 1E-6*(System.nanoTime() - started);
                System.out.printf("Bubble sort took %.2f msec\n", duration);
                results.add(1, row, duration);

                sortedSongs = new ArrayList<>(songs);
                System.gc();
                started = System.nanoTime();
                sorterImpl.quickSort(sortedSongs, Song::compareByHighestStreamsCountTotal);
                duration = 1E-6*(System.nanoTime() - started);
                System.out.printf("Quick sort took %.2f msec\n", duration);
                results.add(2, row, duration);

                sortedSongs = new ArrayList<>(songs);
                System.gc();
                started = System.nanoTime();
                sorterImpl.topsHeapSort(5, sortedSongs, Song::compareByHighestStreamsCountTotal);
                duration = 1E-6*(System.nanoTime() - started);
                System.out.printf("Top heap sort took %.2f msec\n", duration);
                results.add(3, row, duration);
            }
        }

        results.multiplyAll(1.0/REPEATS);
        System.out.printf("\nSummary of %d repeats:\n", REPEATS);
        System.out.println("Item count; Bubble sort; Quick sort; Top heap sort;");
        System.out.println(results.csv("%.2f"));
    }

    public static void heapSortWithChangingTopSpeedTest() {
        SimpleTable<Double> results = new SimpleTable<>(2,18, Double::sum, (a,b)-> a*b);
        List<Song> songs = new ArrayList<>();
        SorterImpl<Song> sorterImpl = new SorterImpl<>();
        List<Song> sortedSongs;
        final int REPEATS = 10;
        final int MAX_N = 150_000;
        long started;
        double duration;

        for (int iteration = 1; iteration <= REPEATS; iteration++) {
            System.out.printf("\nTest iteration-%d\n", iteration);
            songs.clear();
            ChartsCalculator chartsCalculator = new ChartsCalculator(0);
            songs = new ArrayList<>(chartsCalculator.registerStreamedSongs(200000));
            for (int n = 100, row = 0; n < MAX_N; n *= 2, row++ ) {
                System.out.printf("\nSorting %d songs:\n", n);

                results.add(0, row, (double)n);
                sortedSongs = new ArrayList<>(songs);
                System.gc();
                started = System.nanoTime();
                sorterImpl.topsHeapSort(n, sortedSongs, Song::compareByHighestStreamsCountTotal);
                duration = 1E-6*(System.nanoTime() - started);
                System.out.printf("Top heap sort took %.2f msec\n", duration);
                results.add(1, row, duration);
            }
        }

        results.multiplyAll(1.0/REPEATS);
        System.out.printf("\nSummary of %d repeats:\n", REPEATS);
        System.out.println("Top count; Top heap sort;");
        System.out.println(results.csv("%.2f"));
    }
}
