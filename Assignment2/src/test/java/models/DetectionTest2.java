package models;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import static org.junit.jupiter.api.Assertions.*;

public class DetectionTest2 {
    Car scoda, audi, bmw, mercedes, icova, volvo1, volvo2, daf1, daf2, kamaz;
    List<Car> cars;

    @BeforeEach
    private void setup() {
        Locale.setDefault(Locale.ENGLISH);
        scoda = new Car("1-AAA-02", 6, Car.CarType.Car, Car.FuelType.Gasoline, LocalDate.of(2014,1,31));
        audi = new Car("AA-11-BB", 4, Car.CarType.Car, Car.FuelType.Diesel, LocalDate.of(1998,1,31));
        mercedes = new Car("VV-11-BB", 4, Car.CarType.Van, Car.FuelType.Diesel, LocalDate.of(1998,1,31));
        bmw = new Car("A-123-BB", 4, Car.CarType.Car, Car.FuelType.Gasoline, LocalDate.of(2019,1,31));
        icova = new Car("1-TTT-99", 5, Car.CarType.Truck, Car.FuelType.Lpg, LocalDate.of(2011,1,31));
        volvo1 = new Car("1-TTT-01", 5, Car.CarType.Truck, Car.FuelType.Diesel, LocalDate.of(2009,1,31));
        volvo2 = new Car("1-TTT-02", 6, Car.CarType.Truck, Car.FuelType.Diesel, LocalDate.of(2011,1,31));
        daf1 = new Car("1-CCC-01", 5, Car.CarType.Coach, Car.FuelType.Diesel, LocalDate.of(2009,1,31));
        daf2 = new Car("1-CCC-02", 6, Car.CarType.Coach, Car.FuelType.Diesel, LocalDate.of(2011,1,31));
        kamaz = new Car("1-AAAA-0000");
        cars = new ArrayList<>(List.of(scoda, audi, mercedes, bmw, icova, volvo1, volvo2, daf1, daf2, kamaz));
    }

    @Test
    public void validatePurple() {
        // Create detections and add them to a list
        Detection detection1 = new Detection(volvo2, "Amsterdam", LocalDateTime.of(2022, 10, 1, 12, 11, 10));
        Detection detection2 = new Detection(volvo1, "Rotterdam", LocalDateTime.of(2022, 10, 1, 12, 11, 10));
        Detection detection3 = new Detection(daf1, "Den Haag", LocalDateTime.of(2022, 10, 1, 12, 11, 10));
        Detection detection4 = new Detection(daf2, "Utrecht", LocalDateTime.of(2022, 10, 1, 12, 11, 10));
        List<Detection> detections = new ArrayList<>(List.of(detection1, detection2, detection3, detection4));

        // Check for violations
        List<Violation> violations = new ArrayList<>();
        for (Detection detection : detections) {
            Violation violation = detection.validatePurple();
            if (violation != null) {
                violations.add(new Violation(detection.getCar(), violation.getCity()));
            }
        }

        // Check the number of violations
        assertEquals(2, violations.size());

        // Check the violations
        assertTrue(violations.stream().anyMatch(violation ->
                violation.getCity().equals("Rotterdam")
                        && violation.getCar().getLicensePlate().equals("1-TTT-01")
        ));
        assertTrue(violations.stream().anyMatch(violation ->
                violation.getCity().equals("Den Haag")
                        && violation.getCar().getLicensePlate().equals("1-CCC-01")
        ));
    }
}
