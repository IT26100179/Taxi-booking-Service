package com.taxibooking.service;

import org.springframework.stereotype.Component;

/**
 * Concrete Fare Calculator for Van
 * Polymorphic implementation of FareCalculator
 */
@Component
public class VanFareCalculator implements FareCalculator {

    private static final double BASE_FEE = 250.0;
    private static final double RATE_PER_KM = 180.0;

    @Override
    public double calculateFare(double distanceKm) {
        if (distanceKm <= 0) return BASE_FEE;
        return BASE_FEE + (distanceKm * RATE_PER_KM);
    }

    @Override
    public String getVehicleType() {
        return "Van";
    }

    @Override
    public double getRatePerKm() {
        return RATE_PER_KM;
    }
}
