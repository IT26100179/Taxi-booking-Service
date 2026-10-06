package com.taxibooking.model;

/**
 * Concrete Car entity extending Vehicle
 */
public class Car extends Vehicle {

    public Car() {
        super();
        setType("Car");
        setCapacity(4);
        setRatePerKm(120.0);
    }

    public Car(String vehicleId, String plateNumber, String model, int capacity, double ratePerKm, boolean isAvailable) {
        super(vehicleId, plateNumber, model, "Car", capacity > 0 ? capacity : 4, ratePerKm > 0 ? ratePerKm : 120.0, isAvailable);
    }

    @Override
    public String getCategoryDetails() {
        return "Sedan / Hatchback (Up to " + getCapacity() + " passengers) - AC Comfort";
    }
}
