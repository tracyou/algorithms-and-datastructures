package nl.hva.ict.ads.elections.models;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.*;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class AdditionalElectionTests {

    @Test
    public void testGetPollingStationsByZipCodeRange() {
        Election election = new Election("Test Election");

        // Create constituencies
        Constituency constituency1 = new Constituency(1, "Constituency 1");
        Constituency constituency2 = new Constituency(2, "Constituency 2");

        // Create polling stations with different zip codes
        PollingStation pollingStation1 = new PollingStation("1", "1001AA", "first");
        PollingStation pollingStation2 = new PollingStation("2", "2002BB", "second");
        PollingStation pollingStation3 = new PollingStation("3", "3003CC", "third");

        // Add polling stations to constituencies
        constituency1.add(pollingStation1);
        constituency1.add(pollingStation2);
        constituency2.add(pollingStation3);

        // Add constituencies to the election
        election.constituencies.add(constituency1);
        election.constituencies.add(constituency2);

        // Get polling stations within the zip code range
        Collection<PollingStation> pollingStations = election.getPollingStationsByZipCodeRange("2000AA", "3003CC");

        // Assert the expected polling stations within the range
        List<PollingStation> expectedPollingStations = new ArrayList<>();
        expectedPollingStations.add(pollingStation2);
        expectedPollingStations.add(pollingStation3);
        assertEquals(expectedPollingStations, pollingStations);
    }


    @Test
    public void testForNonExistingParty() {
        Election election = new Election("Test Election");

        Party actualParty = election.getParty(3);
        Assertions.assertNull(actualParty);
    }
}

