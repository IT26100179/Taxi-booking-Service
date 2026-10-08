package com.taxibooking.model;

/** Part-time driver: keeps 65% of the fare. */
public class PartTimeDriver extends Driver {

    public static final double DRIVER_SHARE = 0.65;

    public PartTimeDriver(String id, String name, String email, String phone, String password,
                          String licenseNumber, String vehicleType, String status, double rating,
                          String nic, String vehicleId) {
        super(id, name, email, phone, password, licenseNumber, vehicleType, status, rating,
                nic, "PARTTIME", vehicleId);
    }

    @Override
    public double calculateCommission(double fare) {
        if (fare < 0) {
            throw new IllegalArgumentException("Fare cannot be negative");
        }
        return fare * DRIVER_SHARE;
    }
}
