package com.taxibooking.service;

import org.springframework.stereotype.Component;

/**
 * Concrete Fare Calculator for Bike
 * Polymorphic implementation of FareCalculator
 */
@Component
public class BikeFareCalculator implements FareCalculator {

    private static final double BASE_FEE = 80.0;
    private static final double RATE_PER_KM = 60.0;

    @Override
    public double calculateFare(double distanceKm) {
        if (distanceKm <= 0) return BASE_FEE;
        return BASE_FEE + (distanceKm * RATE_PER_KM);
    }

    @Override
    public String getVehicleType() {
        return "Bike";
    }

    @Override
    public double getRatePerKm() {
        return RATE_PER_KM;
    }
}
