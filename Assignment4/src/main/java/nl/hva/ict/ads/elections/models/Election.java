package nl.hva.ict.ads.elections.models;

import nl.hva.ict.ads.utils.PathUtils;
import nl.hva.ict.ads.utils.xml.XMLParser;

import javax.xml.stream.XMLStreamException;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Path;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Holds all election data per consituency
 * Provides calculation methods for overall election results
 */
public class Election {

    private String name;

    // all (unique) parties in this election, organised by Id
    // will be build from the XML
    protected Map<Integer, Party> parties;

    // all (unique) constituencies in this election, identified by Id
    protected Set<Constituency> constituencies;

    public Election(String name) {
        this.name = name;

        // initialise this.parties and this.constituencies with an appropriate Map implementations
        this.parties = new HashMap<>();
        this.constituencies = new HashSet<>();
    }

    /**
     * finds all (unique) parties registered for this election
     *
     * @return all parties participating in at least one constituency, without duplicates
     */
    public Collection<Party> getParties() {
        // return all parties that have been registered for the election
        return parties.values();
    }

    /**
     * finds the party with a given id
     *
     * @param id
     * @return the party with given id, or null if no such party exists.
     */
    public Party getParty(int id) {
        // find the party with the given id

        return parties.get(id);
    }

    public Set<? extends Constituency> getConstituencies() {
        return this.constituencies;
    }

    /**
     * finds all unique candidates across all parties across all constituencies
     * organised by increasing party-id
     *
     * @return alle unique candidates organised by increasing party-id
     */
    public List<Candidate> getAllCandidates() {
        // find all candidates organised by increasing party-id

        return parties.values().stream()
                .flatMap(party -> party.getCandidates().stream())
                .sorted(Comparator.comparingInt(candidate -> candidate.getParty().getId()))
                .distinct()
                .collect(Collectors.toList());
    }

    /**
     * Retrieve for the given party the number of Candidates that have been registered per Constituency
     *
     * @param party
     * @return
     */
    public Map<Constituency, Integer> numberOfRegistrationsByConstituency(Party party) {
        // build a map with the number of candidate registrations per constituency

        return constituencies.stream()
                .collect(Collectors.toMap(
                        constituency -> constituency,
                        constituency -> (int) constituency.getAllCandidates().stream()
                                .filter(candidate -> candidate.getParty().equals(party))
                                .count()
                ));
    }

    /**
     * Finds all Candidates that have a duplicate name against another candidate in the election
     * (can be in the same party or in another party)
     *
     * @return
     */
    public Set<Candidate> getCandidatesWithDuplicateNames() {
        // build the collection of candidates with duplicate names across parties
        List<Candidate> allCandidates = getAllCandidates();

        return allCandidates.stream()
                .collect(Collectors.groupingBy(Candidate::getFullName, Collectors.counting()))
                .entrySet().stream()
                .filter(entry -> entry.getValue() > 1)
                .flatMap(entry -> allCandidates.stream()
                        .filter(candidate -> candidate.getFullName().equals(entry.getKey())))
                .collect(Collectors.toSet());
    }

    public void setParties(Map<Integer, Party> parties) {
        this.parties = parties;
    }

    /**
     * Retrieve from all constituencies the combined sub set of all polling stations that are located within the area of the specified zip codes
     * i.e. firstZipCode <= pollingStation.zipCode <= lastZipCode
     * All valid zip codes adhere to the pattern 'nnnnXX' with 1000 <= nnnn <= 9999 and 'AA' <= XX <= 'ZZ'
     *
     * @param firstZipCode
     * @param lastZipCode
     * @return the sub set of polling stations within the specified zipCode range
     */
    public Collection<PollingStation> getPollingStationsByZipCodeRange(String firstZipCode, String lastZipCode) {
        // retrieve all polling stations within the area of the given range of zip codes (inclusively)
        return constituencies.stream()
                .flatMap(constituency -> constituency.getPollingStations().stream())
                .filter(pollingStation -> isValidZipCode(pollingStation.getZipCode()))
                .filter(pollingStation -> isWithinRange(pollingStation.getZipCode(), firstZipCode, lastZipCode))
                .collect(Collectors.toList());
    }

    private boolean isWithinRange(String zipCode, String firstZipCode, String lastZipCode) {
        return zipCode.compareTo(firstZipCode) >= 0 && zipCode.compareTo(lastZipCode) <= 0;
    }

    private boolean isValidZipCode(String zipCode) {
        return zipCode.matches("^[1-9]\\d{3}[A-Z]{2}$");
    }

    /**
     * Retrieves per party the total number of votes across all candidates, constituencies and polling stations
     *
     * @return
     */
    public Map<Party, Integer> getVotesByParty() {
        // calculate the total number of votes per party
        return constituencies.stream()
                .flatMap(constituency -> constituency.getPollingStations().stream())
                .flatMap(pollingStation -> pollingStation.getVotesByCandidate().entrySet().stream())
                .collect(Collectors.groupingBy(entry -> entry.getKey().getParty(),
                        Collectors.summingInt(Map.Entry::getValue)));
    }

    /**
     * Retrieves per party the total number of votes across all candidates,
     * that were cast in one out of the given collection of polling stations.
     * This method is useful to prepare an election result for any sub-area of a Constituency.
     * Or to obtain statistics of special types of voting, e.g. by mail.
     *
     * @param pollingStations the polling stations that cover the sub-area of interest
     * @return
     */
    public Map<Party, Integer> getVotesByPartyAcrossPollingStations(Collection<PollingStation> pollingStations) {
        // calculate the total number of votes per party across the given polling stations
        return pollingStations.stream()
                .flatMap(pollingStation -> pollingStation.getVotesByCandidate().entrySet().stream())
                .collect(Collectors.groupingBy(entry -> entry.getKey().getParty(),
                        Collectors.summingInt(Map.Entry::getValue)));
    }


    /**
     * Transforms and sorts decreasingly vote counts by party into votes percentages by party
     * The party with the highest vote count shall be ranked upfront
     * The votes percentage by party is calculated from  100.0 * partyVotes / totalVotes;
     *
     * @return the sorted list of (party,votesPercentage) pairs with the highest percentage upfront
     */
    public static List<Map.Entry<Party, Double>> sortedElectionResultsByPartyPercentage(int tops, Map<Party, Integer> votesCounts) {
        //  transform the voteCounts input into a sorted list of entries holding votes percentage by party
        int totalVotes = getTotalVotes(votesCounts);

        List<Map.Entry<Party, Double>> results = votesCounts.entrySet().stream()
                .map(entry -> {
                    Party party = entry.getKey();
                    int votes = entry.getValue();
                    double percentage = (100.0 * votes) / totalVotes;
                    return new AbstractMap.SimpleEntry<>(party, percentage);
                })
                .sorted((e1, e2) -> Double.compare(e2.getValue(), e1.getValue()))
                .collect(Collectors.toList());

        return results.subList(0, Math.min(tops, results.size()));
    }

    /**
     * Find the most representative Polling Station, which has got its votes distribution across all parties
     * the most alike the distribution of overall total votes.
     * A perfect match is found, if for each party the percentage of votes won at the polling station
     * is identical to the percentage of votes won by the party overall in the election.
     * The most representative Polling Station has the smallest deviation from that perfect match.
     * <p>
     * There are different metrics possible to calculate a relative deviation between distributions.
     * You may use the helper method {@link #euclidianVotesDistributionDeviation(Map, Map)}
     * which calculates a relative least-squares deviation between two distributions.
     *
     * @return the most representative polling station.
     */
    public PollingStation findMostRepresentativePollingStation() {

        // calculate the overall total votes count distribution by Party
        //  and find the PollingStation with the lowest relative deviation between
        //  its votes count distribution and the overall distribution.


        Map<Party, Integer> overallVotesCount = getVotesByParty();

        return constituencies.stream()
                .flatMap(con -> con.getPollingStations().stream())
                .min((ps1, ps2) -> Double.compare(
                        euclidianVotesDistributionDeviation(getVotesByPartyAcrossPollingStations(Collections.singletonList(ps1)), overallVotesCount),
                        euclidianVotesDistributionDeviation(getVotesByPartyAcrossPollingStations(Collections.singletonList(ps2)), overallVotesCount)
                ))
                .orElse(null);
    }

    // Helper method to calculate the total votes count across all parties
    private static int getTotalVotes(Map<Party, Integer> votesCounts) {
        return votesCounts.values().stream()
                .mapToInt(Integer::intValue)
                .sum();
    }

    /**
     * Calculates the Euclidian distance between the relative distribution across parties of two voteCounts.
     * If the two relative distributions across parties are identical, then the distance will be zero
     * If some parties have relatively more votes in one distribution than the other, the outcome will be positive.
     * The lower the outcome, the more alike are the relative distributions of the voteCounts.
     * ratign of votesCounts1 relative to votesCounts2.
     * see https://towardsdatascience.com/9-distance-measures-in-data-science-918109d069fa
     *
     * @param votesCounts1 one distribution of votes across parties.
     * @param votesCounts2 another distribution of votes across parties.
     * @return de relative distance between the two distributions.
     */
    private double euclidianVotesDistributionDeviation(Map<Party, Integer> votesCounts1, Map<Party, Integer> votesCounts2) {
        // calculate total number of votes in both distributions
        int totalNumberOfVotes1 = integersSum(votesCounts1.values());
        int totalNumberOfVotes2 = integersSum(votesCounts2.values());

        // we calculate the distance as the sum of squares of relative voteCount distribution differences per party
        // if we compare two voteCounts that have the same relative distribution across parties, the outcome will be zero

        return votesCounts1.entrySet().stream()
                .mapToDouble(e -> Math.pow(e.getValue() / (double) totalNumberOfVotes1 -
                        votesCounts2.getOrDefault(e.getKey(), 0) / (double) totalNumberOfVotes2, 2))
                .sum();
    }

    /**
     * auxiliary method to calculate the total sum of a collection of integers
     *
     * @param integers
     * @return
     */
    public static int integersSum(Collection<Integer> integers) {
        return integers.stream().reduce(Integer::sum).orElse(0);
    }


    public String prepareSummary(int partyId) {
        Party party = this.getParty(partyId);
        StringBuilder summary = new StringBuilder()
                .append("\nSummary of ").append(party).append(":\n");

        // Report total number of candidates in the given party
        int totalCandidates = party.getCandidates().size();
        summary.append("Total number of candidates: ").append(totalCandidates).append("\n");

        // Report the list with all candidates in the given party
        summary.append("Candidates: ")
                .append(party.getCandidates().stream()
                        .map(Candidate::toString)
                        .collect(Collectors.joining(", ")))
                .append("\n");

        // Report total number of registrations for the given party
        int totalRegistrations = this.getConstituencies().stream()
                .mapToInt(constituency -> constituency.getCandidates(party).size())
                .sum();
        summary.append("Total number of registrations: ").append(totalRegistrations).append("\n");

        // Report the map of number of registrations by constituency for the given party
        Map<Constituency, Integer> registrationsByConstituency = this.getConstituencies().stream()
                .collect(Collectors.toMap(
                        constituency -> constituency,
                        constituency -> (int) constituency.getCandidates(party).stream().count()
                ));
        summary.append("Registrations by constituency: ").append(registrationsByConstituency).append("\n");

        return summary.toString();
    }

    public String prepareSummary() {

        StringBuilder summary = new StringBuilder()
                .append("\nElection summary of ").append(this.name).append(":\n");

        // Report the total number of parties in the election
        int totalParties = this.parties.size();
        summary.append("Total number of parties: ").append(totalParties).append("\n");

        // Report the list of all parties ordered by increasing party-Id
        List<Party> sortedParties = this.parties.values().stream()
                .sorted(Comparator.comparingInt(Party::getId))
                .collect(Collectors.toList());
        summary.append("Parties: ").append(sortedParties).append("\n");

        // Report the total number of constituencies in the election
        int totalConstituencies = this.constituencies.size();
        summary.append("Total number of constituencies: ").append(totalConstituencies).append("\n");

        // Report the total number of polling stations in the election
        int totalPollingStations = this.constituencies.stream()
                .mapToInt(con -> con.getPollingStations().size())
                .sum();
        summary.append("Total number of polling stations: ").append(totalPollingStations).append("\n");

        // Report the total number of (different) candidates in the election
        int totalCandidates = this.parties.values().stream()
                .flatMap(party -> party.getCandidates().stream())
                .distinct()
                .mapToInt(cand -> 1)
                .sum();
        summary.append("Total number of candidates: ").append(totalCandidates).append("\n");

        // Report the list with all candidates which have a counterpart with a duplicate name in a different party
        List<Candidate> duplicateCandidates = this.parties.values().stream()
                .flatMap(party -> party.getCandidates().stream())
                .collect(Collectors.groupingBy(Candidate::getFullName))
                .values().stream()
                .filter(candidates -> candidates.size() > 1)
                .flatMap(List::stream)
                .collect(Collectors.toList());
        summary.append("Candidates with duplicate names: ").append(duplicateCandidates).append("\n");

        return summary.toString();
    }

    /**
     * Reads all data of Parties, Candidates, Contingencies and PollingStations from available files in the given folder and its subfolders
     * This method can cope with any structure of sub folders, but does assume the file names to comply with the conventions
     * as found from downloading the files from https://data.overheid.nl/dataset/verkiezingsuitslag-tweede-kamer-2021
     * So, you can merge folders after unpacking the zip distributions of the data, but do not change file names.
     *
     * @param folderName the root folder with the data files of the election results
     * @return een Election met alle daarbij behorende gegevens.
     * @throws XMLStreamException bij fouten in een van de XML bestanden.
     * @throws IOException        als er iets mis gaat bij het lezen van een van de bestanden.
     */
    public static Election importFromDataFolder(String folderName) throws XMLStreamException, IOException {
        System.out.println("Loading election data from " + folderName);
        Election election = new Election(folderName);
        int progress = 0;
        Map<Integer, Constituency> kieskringen = new HashMap<>();
        for (Path constituencyCandidatesFile : PathUtils.findFilesToScan(folderName, "Kandidatenlijsten_TK2021_")) {
            XMLParser parser = new XMLParser(new FileInputStream(constituencyCandidatesFile.toString()));
            Constituency constituency = Constituency.importFromXML(parser, election.parties);
            //election.constituenciesM.put(constituency.getId(), constituency);
            election.constituencies.add(constituency);
            showProgress(++progress);
        }
        System.out.println();
        progress = 0;
        for (Path votesPerPollingStationFile : PathUtils.findFilesToScan(folderName, "Telling_TK2021_gemeente")) {
            XMLParser parser = new XMLParser(new FileInputStream(votesPerPollingStationFile.toString()));
            election.importVotesFromXml(parser);
            showProgress(++progress);
        }
        System.out.println();
        return election;
    }

    protected static void showProgress(final int progress) {
        System.out.print('.');
        if (progress % 50 == 0) System.out.println();
    }

    /**
     * Auxiliary method for parsing the data from the EML files
     * This methode can be used as-is and does not require your investigation or extension.
     */
    public void importVotesFromXml(XMLParser parser) throws XMLStreamException {
        if (parser.findBeginTag(Constituency.CONSTITUENCY)) {

            int constituencyId = 0;
            if (parser.findBeginTag(Constituency.CONSTITUENCY_IDENTIFIER)) {
                constituencyId = parser.getIntegerAttributeValue(null, Constituency.ID, 0);
                parser.findAndAcceptEndTag(Constituency.CONSTITUENCY_IDENTIFIER);
            }

            //Constituency constituency = this.constituenciesM.get(constituencyId);
            final int finalConstituencyId = constituencyId;
            Constituency constituency = this.constituencies.stream()
                    .filter(c -> c.getId() == finalConstituencyId)
                    .findFirst()
                    .orElse(null);

            //parser.findBeginTag(PollingStation.POLLING_STATION_VOTES);
            while (parser.findBeginTag(PollingStation.POLLING_STATION_VOTES)) {
                PollingStation pollingStation = PollingStation.importFromXml(parser, constituency, this.parties);
                if (pollingStation != null) constituency.add(pollingStation);
            }

            parser.findAndAcceptEndTag(Constituency.CONSTITUENCY);
        }
    }

    /**
     * HINTS:
     * getCandidatesWithDuplicateNames:
     *  Approach-1: first build a Map that counts the number of candidates per given name
     *              then build the collection from all candidates, excluding those whose name occurs only once.
     *  Approach-2: build a stream that is sorted by name
     *              apply a mapMulti that drops unique names but keeps the duplicates
     *              this approach probably requires complex lambda expressions that are difficult to justify
     */

}
