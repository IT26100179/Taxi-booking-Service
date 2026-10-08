package com.taxibooking.model;

/** Full-time driver: keeps 80% of the fare. */
public class FullTimeDriver extends Driver {

    public static final double DRIVER_SHARE = 0.80;

    public FullTimeDriver(String id, String name, String email, String phone, String password,
                          String licenseNumber, String vehicleType, String status, double rating,
                          String nic, String vehicleId) {
        super(id, name, email, phone, password, licenseNumber, vehicleType, status, rating,
                nic, "FULLTIME", vehicleId);
    }

    @Override
    public double calculateCommission(double fare) {
        if (fare < 0) {
            throw new IllegalArgumentException("Fare cannot be negative");
        }
        return fare * DRIVER_SHARE;
    }
}