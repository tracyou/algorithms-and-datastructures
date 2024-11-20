package models;

import java.io.File;
import java.io.FileNotFoundException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.*;
import java.util.function.Function;

public class TrafficTracker {
    private final String TRAFFIC_FILE_EXTENSION = ".txt";
    private final String TRAFFIC_FILE_PATTERN = ".+\\" + TRAFFIC_FILE_EXTENSION;

    private OrderedList<Car> cars;                  // the reference list of all known Cars registered by the RDW
    private OrderedList<Violation> violations;      // the accumulation of all offences by car and by city

    public TrafficTracker() {
        Comparator<Car> carComparator = Comparator.comparing(Car::getLicensePlate);
        cars = new OrderedArrayList<>(carComparator);

        Comparator<Violation> violationComparator = Comparator.comparing(Violation::getCar).thenComparing(Violation::getCity);
        violations = new OrderedArrayList<>(violationComparator);
    }

    /**
     * imports all registered cars from a resource file that has been provided by the RDW
     *
     * @param resourceName
     */
    public void importCarsFromVault(String resourceName) {
        this.cars.clear();

        // load all cars from the text file
        int numberOfLines = importItemsFromFile(this.cars,
                createFileFromURL(Objects.requireNonNull(TrafficTracker.class.getResource(resourceName))),
                Car::fromLine);

        // sort the cars for efficient later retrieval
        this.cars.sort();

        System.out.printf("Imported %d cars from %d lines in %s.\n", this.cars.size(), numberOfLines, resourceName);
    }

    /**
     * imports and merges all raw detection data of all entry gates of all cities from the hierarchical file structure of the vault
     * accumulates any offences against purple rules into this.violations
     *
     * @param resourceName
     */
    public void importDetectionsFromVault(String resourceName) {
        this.violations.clear();

        int totalNumberOfOffences =
                this.mergeDetectionsFromVaultRecursively(
                        createFileFromURL(TrafficTracker.class.getResource(resourceName)));

        System.out.printf("Found %d offences among detections imported from files in %s.\n",
                totalNumberOfOffences, resourceName);
    }

    /**
     * traverses the detections vault recursively and processes every data file that it finds
     *
     * @param file
     */
    private int mergeDetectionsFromVaultRecursively(File file) {
        int totalNumberOfOffences = 0;

        if (file.isDirectory()) {
            // the file is a folder (a.k.a. directory)
            // retrieve a list of all files and sub folders in this directory
            File[] filesInDirectory = Objects.requireNonNullElse(file.listFiles(), new File[0]);

            // recursively process all files and sub folders from the filesInDirectory list.
            for (File subFile : filesInDirectory) {
                totalNumberOfOffences += mergeDetectionsFromVaultRecursively(subFile);
            }

        } else if (file.getName().matches(TRAFFIC_FILE_PATTERN)) {
            // the file is a regular file that matches the target pattern for raw detection files
            // process the content of this file and merge the offences found into this.violations
            totalNumberOfOffences += this.mergeDetectionsFromFile(file);
        }

        return totalNumberOfOffences;
    }

    /**
     * imports another batch detection data from the filePath text file
     * and merges the offences into the earlier imported and accumulated violations
     *
     * @param file
     */
    private int mergeDetectionsFromFile(File file) {

        // re-sort the accumulated violations for efficient searching and merging
        this.violations.sort();

        // use a regular ArrayList to load the raw detection info from the file
        List<Detection> newDetections = new ArrayList<>();

        // import detections from file into newDetections list
        importItemsFromFile(newDetections, file, line -> Detection.fromLine(line, cars));

        System.out.printf("Imported %d detections from %s.\n", newDetections.size(), file.getPath());

        int totalNumberOfOffences = 0; // tracks the number of offences that emerges from the data in this file

        // loop through every detection from the newDetections list and check for violations
        for (Detection detection : newDetections) {
            Violation violation = detection.validatePurple();
            if (violation != null) {
                boolean merged = false;
                // Loop through every existing violation and check if there is a match with the new violation
                for (Violation existingViolation : this.violations) {
                    if (Violation.compareByLicensePlateAndCity(existingViolation, violation) == 0) {
                        // Merge the violations if there is a match
                        existingViolation.setOffencesCount(existingViolation.getOffencesCount() + violation.getOffencesCount());
                        merged = true;
                        break;
                    }
                }
                // Add the new violation if it is not merged with an existing one
                if (!merged) {
                    this.violations.add(violation);
                }
                totalNumberOfOffences += violation.getOffencesCount();
            }
        }

        return totalNumberOfOffences;
    }

    /**
     * calculates the total revenue of fines from all violations,
     * Trucks pay €25 per offence, Coaches €35 per offence
     *
     * @return the total amount of money recovered from all violations
     */
    public double calculateTotalFines() {
        // the fine for trucks is set to €25 and for coaches is set to €35
        double truckFine = 25.0;
        double coachFine = 35.0;
        // initialize the totalFine to 0
        double totalFine = 0.0;

        // for each violation in the list
        for (Violation violation : this.violations) {
            if (violation.getCar().getCarType() == Car.CarType.Truck) {
                // if the car is a truck add the fine for each offence to the totalFine
                totalFine += truckFine * violation.getOffencesCount();
            } else if (violation.getCar().getCarType() == Car.CarType.Coach) {
                // if the car is a coach add the fine for each offence to the totalFine
                totalFine += coachFine * violation.getOffencesCount();
            } else {
                // if the car is not a truck or a coach add 0 to the totalFine
                totalFine += 0.0;
            }
        }
        return totalFine;
    }

    /**
     * Prepares a list of topNumber of violations that show the highest offencesCount
     * when this.violations are aggregated by car across all cities.
     *
     * @param topNumber the requested top number of violations in the result list
     * @return a list of topNum items that provides the top aggregated violations
     */
    public List<Violation> topViolationsByCar(int topNumber) {

        // Create a HashMap to store the aggregated violations by car
        Map<Car, Violation> violationsByCar = new HashMap<>();

        // Loop through all violations in the traffic tracker and aggregate them by car
        for (Violation violation : this.violations) {
            Car car = violation.getCar();
            if (violationsByCar.containsKey(car)) {
                violationsByCar.get(car).setOffencesCount(
                        violationsByCar.get(car).getOffencesCount() + violation.getOffencesCount());
            } else {
                violationsByCar.put(car, new Violation(car, violation.getCity()));
            }
        }

        // Convert the aggregated violations into a list and sort it by decreasing offencesCount
        List<Violation> sortedList = new ArrayList<>(violationsByCar.values());
        sortedList.sort((v1, v2) -> v2.getOffencesCount() - v1.getOffencesCount());

        // Return the topNumber of violations from the sorted list
        return sortedList.subList(0, Math.min(topNumber, sortedList.size()));
    }

    /**
     * Prepares a list of topNumber of violations that show the highest offencesCount
     * when this.violations are aggregated by city across all cars.
     *
     * @param topNumber the requested top number of violations in the result list
     * @return a list of topNum items that provides the top aggregated violations
     */
    public List<Violation> topViolationsByCity(int topNumber) {

        // Create a HashMap to store the aggregated violations by city
        Map<String, Violation> violationsByCity = new HashMap<>();

        // Loop through all violations in the traffic tracker and aggregate them by city
        for (Violation violation : this.violations) {
            String city = violation.getCity();
            if (violationsByCity.containsKey(city)) {
                violationsByCity.put(city, violationsByCity.get(city).combineOffencesCounts(violation));
            } else {
                violationsByCity.put(city, new Violation(null, city));
            }
        }

        // Convert the aggregated violations into a list and sort it by decreasing offencesCount
        List<Violation> sortedList = new ArrayList<>(violationsByCity.values());
        sortedList.sort(Collections.reverseOrder());

        // Return the topNumber of violations from the sorted list
        return sortedList.subList(0, Math.min(topNumber, sortedList.size()));
    }


    /**
     * imports a collection of items from a text file which provides one line for each item
     *
     * @param items     the list to which imported items shall be added
     * @param file      the source text file
     * @param converter a function that can convert a text line into a new item instance
     * @param <E>       the (generic) type of each item
     */
    public static <E> int importItemsFromFile(List<E> items, File file, Function<String, E> converter) {
        int numberOfLines = 0;

        Scanner scanner = createFileScanner(file);

        // read all source lines from the scanner,
        // convert each line to an item of type E
        // and add each successfully converted item into the list
        while (scanner.hasNext()) {
            // input another line with author information
            String line = scanner.nextLine();
            numberOfLines++;

            // convert the line to an instance of E
            E item = converter.apply(line);

            // add a successfully converted item to the list of items
            if (item != null) {
                items.add(item);
            }
        }

        //System.out.printf("Imported %d lines from %s.\n", numberOfLines, file.getPath());
        return numberOfLines;
    }

    /**
     * helper method to create a scanner on a file and handle the exception
     *
     * @param file
     * @return
     */
    private static Scanner createFileScanner(File file) {
        try {
            return new Scanner(file);
        } catch (FileNotFoundException e) {
            throw new RuntimeException("FileNotFound exception on path: " + file.getPath());
        }
    }

    private static File createFileFromURL(URL url) {
        try {
            return new File(url.toURI().getPath());
        } catch (URISyntaxException e) {
            throw new RuntimeException("URI syntax error found on URL: " + url.getPath());
        }
    }

    public OrderedList<Car> getCars() {
        return this.cars;
    }

    public OrderedList<Violation> getViolations() {
        return this.violations;
    }
}
