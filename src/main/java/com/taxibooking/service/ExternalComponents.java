package com.taxibooking.service;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Temporary stand-ins for other components. Replace these bodies with real
 * calls when the owners finish their work:
 *  - Component 03 (Vehicle): vehicle list and types
 *  - Component 04 (Booking): active bookings and today's trips
 */
public class ExternalComponents {

    // TODO Component 03: replace with VehicleFileRepository data
    private static final Map<String, String> VEHICLES = new LinkedHashMap<>();
    static {
        VEHICLES.put("V001", "Car");
        VEHICLES.put("V002", "Van");
        VEHICLES.put("V003", "Bike");
        VEHICLES.put("V004", "Car");
        VEHICLES.put("V005", "Van");
    }

    /** vehicleId -> vehicle type (Car / Van / Bike) */
    public static Map<String, String> getVehicleOptions() {
        return VEHICLES;
    }

    public static String getVehicleType(String vehicleId) {
        return VEHICLES.getOrDefault(vehicleId, "Car");
    }

    // TODO Component 04: true if the driver has a PENDING / CONFIRMED / ON_TRIP booking
    public static boolean hasActiveBooking(String driverId) {
        return false;
    }

    // TODO Component 04: today's trips for this driver
    public static List<String> getTodaysTrips(String driverId) {
        return new ArrayList<>();
    }
}
