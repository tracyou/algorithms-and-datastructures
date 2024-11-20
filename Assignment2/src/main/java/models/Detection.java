package models;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.temporal.ChronoUnit;
import java.util.List;

import static models.Car.CarType;
import static models.Car.FuelType;

public class Detection {
    private final Car car;                  // the car that was detected
    private final String city;              // the name of the city where the detector was located
    private final LocalDateTime dateTime;   // date and time of the detection event

    /* Representation Invariant:
     *      every Detection shall be associated with a valid Car
     */

    public Detection(Car car, String city, LocalDateTime dateTime) {
        this.car = car;
        this.city = city;
        this.dateTime = dateTime;
    }

    /**
     * Parses detection information from a line of text about a car that has entered an environmentally controlled zone
     * of a specified city.
     * the format of the text line is: lisensePlate, city, dateTime
     * The licensePlate shall be matched with a car from the provided list.
     * If no matching car can be found, a new Car shall be instantiated with the given lisensePlate and added to the list
     * (besides the license plate number there will be no other information available about this car)
     *
     * @param textLine
     * @param cars     a list of known cars, ordered and searchable by licensePlate
     *                 (i.e. the indexOf method of the list shall only consider the lisensePlate when comparing cars)
     * @return a new Detection instance with the provided information
     * or null if the textLine is corrupt or incomplete
     */
    public static Detection fromLine(String textLine, List<Car> cars) {
        // Split the line into its parts
        String[] parts = textLine.split(",");

        // Check if the line has exactly three parts
        if (parts.length != 3) {
            return null;
        }

        // Extract the license plate, city, and date and time from the line
        String licensePlate = parts[0].trim();
        String city = parts[1].trim();
        LocalDateTime dateTime;

        // Parse the date and time using the ISO_LOCAL_DATE_TIME format
        try {
            dateTime = LocalDateTime.parse(parts[2].trim(), DateTimeFormatter.ISO_LOCAL_DATE_TIME);
        } catch (DateTimeParseException e) {
            return null;
        }

        // Check if the car with the given license plate already exists in the list of cars
        int carIndex = cars.indexOf(new Car(licensePlate));
        Car car;
        if (carIndex == -1) {
        // If the car does not exist, create a new Car object and add it to the list of cars
            car = new Car(licensePlate);
            cars.add(car);
        } else {
        // If the car already exists, retrieve it from the list
            car = cars.get(carIndex);
        }

        // Create a new Detection object with the retrieved or created Car object, the city, and the date and time
        return new Detection(car, city, dateTime);
    }

    /**
     * Validates a detection against the purple conditions for entering an environmentally restricted zone
     * I.e.:
     * Diesel trucks and diesel coaches with an emission category of below 6 may not enter a purple zone
     *
     * @return a Violation instance if the detection saw an offence against the purple zone rule/
     * null if no offence was found.
     */
    public Violation validatePurple() {
        // Get the car from the detection
        Car car = this.getCar();

        // Get the emission category of the car
        int emissionCategory = car.getEmissionCategory();

        // Get the type of car
        CarType carType = car.getCarType();

        // If the car is either a coach or a truck
        if (carType.equals(CarType.Coach) || carType.equals(CarType.Truck)) {
            // Check if the emission category is less than 6
            if (emissionCategory < 6) {
                // If so, create a new violation for this car and city
                return new Violation(car, this.getCity());
            }
        }

        // If no violation was created, return null
        return null;
    }

    public Car getCar() {
        return car;
    }

    public String getCity() {
        return city;
    }

    public LocalDateTime getDateTime() {
        return dateTime;
    }


    @Override
    public String toString() {
        return car.getLicensePlate() + "/" + getCity() + "/" + getDateTime().toString();
    }

}
