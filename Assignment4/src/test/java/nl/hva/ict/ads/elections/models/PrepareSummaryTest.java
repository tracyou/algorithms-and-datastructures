package nl.hva.ict.ads.elections.models;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;


public class PrepareSummaryTest {

    private Election election;

    @BeforeEach
    public void setUp() {
        this.election = new Election("Sample Election");

        Party party1 = new Party(1, "Party 1");
        Candidate candidate1 = new Candidate("John", "Doe", "Candidate 1", party1);
        Candidate candidate2 = new Candidate("Jane", "Smith", "Candidate 2", party1);
        Map<Integer, Party> parties = new HashMap<>();
        parties.put(1, party1);
        election.setParties(parties);
        party1.getCandidates().add(candidate1);
        party1.getCandidates().add(candidate2);

        Party party2 = new Party(2, "Party 2");
        Candidate candidate3 = new Candidate("Alice", "Johnson", "Candidate 3", party2);
        Candidate candidate4 = new Candidate("Bob", "Williams", "Candidate 4", party2);
        parties.put(2, party2);
        party2.getCandidates().add(candidate3);
        party2.getCandidates().add(candidate4);

        Party studentsParty = new Party(101, "Students Party");
        Candidate candidate12 = new Candidate("B.", null, "Candidate", studentsParty);
        parties.put(101, studentsParty);
        studentsParty.getCandidates().add(candidate12);
    }

    @Test
    public void testPrepareSummaryWithPartyId() {
        String summary = election.prepareSummary(1);
        String expectedSummary = "\nSummary of Party{id=1,name='Party 1'}:\n" +
                "Total number of candidates: 2\n" +
                "Candidates: Candidate{partyId=1,name='John Doe Candidate 1'}, Candidate{partyId=1,name='Jane Smith Candidate 2'}\n" +
                "Total number of registrations: 0\n" +
                "Registrations by constituency: {}\n";
        assertEquals(expectedSummary, summary);
    }
    @Test
    public void testPrepareSummaryWithoutPartyId() {
        String summary = election.prepareSummary();
        String expectedSummary = "\nElection summary of Sample Election:\n" +
                "Total number of parties: 3\n" +
                "Parties: [Party{id=1,name='Party 1'}, Party{id=2,name='Party 2'}, Party{id=101,name='Students Party'}]\n" +
                "Total number of constituencies: 0\n" +
                "Total number of polling stations: 0\n" +
                "Total number of candidates: 5\n" +
                "Candidates with duplicate names: []\n";

        assertEquals(expectedSummary, summary);
    }
    @Test
    public void testPrepareSummaryWithoutDuplicates() {
        // Query election summary
        String summary = election.prepareSummary();

        // Verify the outcome
        String expectedSummary = "\nElection summary of Sample Election:\n" +
                "Total number of parties: 3\n" +
                "Parties: [Party{id=1,name='Party 1'}, Party{id=2,name='Party 2'}, Party{id=101,name='Students Party'}]\n" +
                "Total number of constituencies: 0\n" +
                "Total number of polling stations: 0\n" +
                "Total number of candidates: 5\n" +
                "Candidates with duplicate names: []\n";

        System.out.println(expectedSummary + summary);
        assertEquals(expectedSummary, summary);
    }
}
