package com.taxibooking.model;

/**
 * Concrete Bike entity extending Vehicle
 */
public class Bike extends Vehicle {

    public Bike() {
        super();
        setType("Bike");
        setCapacity(1);
        setRatePerKm(60.0);
    }

    public Bike(String vehicleId, String plateNumber, String model, int capacity, double ratePerKm, boolean isAvailable) {
        super(vehicleId, plateNumber, model, "Bike", 1, ratePerKm > 0 ? ratePerKm : 60.0, isAvailable);
    }

    @Override
    public String getCategoryDetails() {
        return "Single Passenger Scooter / Motorcycle - Quick City Commute";
    }
}
