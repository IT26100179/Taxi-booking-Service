package com.taxibooking.service;

import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Service to manage Polymorphic Fare Calculations (Component 5)
 */
@Service
public class FareCalculationService {

    private final Map<String, FareCalculator> calculatorMap = new HashMap<>();

    public FareCalculationService(List<FareCalculator> calculators) {
        for (FareCalculator calc : calculators) {
            calculatorMap.put(calc.getVehicleType().toLowerCase(), calc);
        }
    }

    /**
     * Polymorphically estimates fare based on vehicle type and distance
     */
    public double estimateFare(String vehicleType, double distanceKm) {
        if (vehicleType == null) {
            vehicleType = "car";
        }
        FareCalculator calculator = calculatorMap.get(vehicleType.toLowerCase());
        if (calculator == null) {
            calculator = calculatorMap.get("car"); // Default to Car
        }
        return calculator.calculateFare(distanceKm);
    }

    public Map<String, Double> compareAllFares(double distanceKm) {
        Map<String, Double> comparison = new HashMap<>();
        for (Map.Entry<String, FareCalculator> entry : calculatorMap.entrySet()) {
            comparison.put(entry.getValue().getVehicleType(), entry.getValue().calculateFare(distanceKm));
        }
        return comparison;
    }
}
