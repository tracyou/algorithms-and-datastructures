package models;

public class Violation implements Comparable<Violation> {
    private final Car car;
    private final String city;
    private int offencesCount;

    public Violation(Car car, String city) {
        this.car = car;
        this.city = city;
        this.offencesCount = 1;
    }

    public static int compareByLicensePlateAndCity(Violation v1, Violation v2) {
        int result = v1.getCar().getLicensePlate().compareTo(v2.getCar().getLicensePlate());
        // If the license plates are the same, compare the cities of the two violations
        if (result == 0) {
            result = v1.getCity().compareTo(v2.getCity());
        }
        // Return the result of the comparison
        return result;
    }



    /**
     * Aggregates this violation with the other violation by adding their counts and
     * nullifying identifying attributes car and/or city that do not match
     * identifying attributes that match are retained in the result.
     * This method can be used for aggregating violations applying different grouping criteria
     * @param other
     * @return  a new violation with the accumulated offencesCount and matching identifying attributes.
     */
    public Violation combineOffencesCounts(Violation other) {
        Violation combinedViolation = new Violation(
                // nullify the car attribute iff this.car does not match other.car
                this.car != null && this.car.equals(other.car) ? this.car : null,
                // nullify the city attribute iff this.city does not match other.city
                this.city != null && this.city.equals(other.city) ? this.city : null);

        // add the offences counts of both original violations
        combinedViolation.setOffencesCount(this.offencesCount + other.offencesCount);

        return combinedViolation;
    }

    public Car getCar() {
        return car;
    }

    public String getCity() {
        return city;
    }

    public int getOffencesCount() {
        return offencesCount;
    }

    public void setOffencesCount(int offencesCount) {
        this.offencesCount = offencesCount;
    }

    @Override
    public int compareTo(Violation other) {
        return Integer.compare(this.offencesCount, other.offencesCount);
    }

    @Override
    public String toString() {
        String carString = car != null ? car.getLicensePlate() : "null";
        return carString + "/" + getCity() + "/" + getOffencesCount();
    }
}
