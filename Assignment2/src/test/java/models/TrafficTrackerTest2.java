package models;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Locale;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class TrafficTrackerTest2 {
    private final static String VAULT_NAME = "/test1";

    TrafficTracker trafficTracker;

    @BeforeEach
    private void setup() {
        Locale.setDefault(Locale.ENGLISH);
        trafficTracker = new TrafficTracker();
        trafficTracker.importCarsFromVault(VAULT_NAME + "/cars.txt");
        trafficTracker.importDetectionsFromVault(VAULT_NAME + "/detections");
    }

    @Test
    public void testTopViolationsByCar() {
        List<Violation> topViolations = trafficTracker.topViolationsByCar(1);
        assertEquals(1, topViolations.size());
    }

    @Test
    public void testTopViolationsByCity() {
        List<Violation> topViolations = trafficTracker.topViolationsByCity(2);
        assertEquals(2, topViolations.size());
    }

    @Test
    public void calculateTotalFines() {
        // Check if 0 will be returned if the tracker has no violation
        TrafficTracker tracker = new TrafficTracker();
        assertEquals(0, tracker.calculateTotalFines());

        // Checks if total fines of all the violations in the tracker is calculated correctly
        assertEquals(175, trafficTracker.calculateTotalFines());
    }
}

