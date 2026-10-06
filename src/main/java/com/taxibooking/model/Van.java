package com.taxibooking.model;

/**
 * Concrete Van entity extending Vehicle
 */
public class Van extends Vehicle {

    public Van() {
        super();
        setType("Van");
        setCapacity(10);
        setRatePerKm(180.0);
    }

    public Van(String vehicleId, String plateNumber, String model, int capacity, double ratePerKm, boolean isAvailable) {
        super(vehicleId, plateNumber, model, "Van", capacity > 0 ? capacity : 10, ratePerKm > 0 ? ratePerKm : 180.0, isAvailable);
    }

    @Override
    public String getCategoryDetails() {
        return "Passenger Van (Up to " + getCapacity() + " passengers) - Luggage & Group Travel";
    }
}
