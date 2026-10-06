package com.taxibooking.service;

/**
 * Interface for Polymorphic Fare Calculation (OOP Abstraction & Polymorphism)
 * Component 5 - Fare Calculation & Payment Management
 */
public interface FareCalculator {

    /**
     * Calculates ride fare based on trip distance.
     * @param distanceKm Trip distance in kilometers
     * @return Estimated total fare
     */
    double calculateFare(double distanceKm);

    /**
     * Returns the vehicle type associated with this calculator.
     */
    String getVehicleType();

    /**
     * Returns the rate per kilometer.
     */
    double getRatePerKm();
}
