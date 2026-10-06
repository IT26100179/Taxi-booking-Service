package com.taxibooking.model;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Ride Booking Model (Component 4 - Ride Booking & Trip Management)
 * Connects Passenger, Driver, Vehicle, and Fare.
 */
public class Booking {
    private String bookingId;
    private String passengerId;
    private String driverId;
    private String vehicleType;
    private String pickupLocation;
    private String dropLocation;
    private double distanceKm;
    private double fare;
    private String status; // PENDING, CONFIRMED, ON_TRIP, COMPLETED, CANCELLED
    private String bookingTime;

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public Booking() {
        this.status = "PENDING";
        this.bookingTime = LocalDateTime.now().format(FORMATTER);
    }

    public Booking(String bookingId, String passengerId, String driverId, String vehicleType,
                   String pickupLocation, String dropLocation, double distanceKm, double fare,
                   String status, String bookingTime) {
        this.bookingId = bookingId;
        this.passengerId = passengerId;
        this.driverId = (driverId != null && !driverId.isBlank()) ? driverId : "UNASSIGNED";
        this.vehicleType = vehicleType;
        this.pickupLocation = pickupLocation;
        this.dropLocation = dropLocation;
        this.distanceKm = distanceKm;
        this.fare = fare;
        this.status = (status != null && !status.isBlank()) ? status : "PENDING";
        this.bookingTime = (bookingTime != null && !bookingTime.isBlank()) ? bookingTime : LocalDateTime.now().format(FORMATTER);
    }

    public String toFileString() {
        return String.join("|",
                bookingId != null ? bookingId : "",
                passengerId != null ? passengerId : "",
                driverId != null ? driverId : "UNASSIGNED",
                vehicleType != null ? vehicleType : "Car",
                pickupLocation != null ? pickupLocation : "",
                dropLocation != null ? dropLocation : "",
                String.valueOf(distanceKm),
                String.valueOf(fare),
                status != null ? status : "PENDING",
                bookingTime != null ? bookingTime : LocalDateTime.now().format(FORMATTER)
        );
    }

    public static Booking fromFileString(String line) {
        if (line == null || line.trim().isEmpty()) {
            return null;
        }
        String[] parts = line.split("\\|", -1);
        if (parts.length < 10) {
            return null;
        }
        double dist = 0.0;
        double fare = 0.0;
        try {
            dist = Double.parseDouble(parts[6]);
            fare = Double.parseDouble(parts[7]);
        } catch (NumberFormatException ignored) {}

        return new Booking(parts[0], parts[1], parts[2], parts[3], parts[4], parts[5], dist, fare, parts[8], parts[9]);
    }

    public String getBookingId() {
        return bookingId;
    }

    public void setBookingId(String bookingId) {
        this.bookingId = bookingId;
    }

    public String getPassengerId() {
        return passengerId;
    }

    public void setPassengerId(String passengerId) {
        this.passengerId = passengerId;
    }

    public String getDriverId() {
        return driverId;
    }

    public void setDriverId(String driverId) {
        this.driverId = driverId;
    }

    public String getVehicleType() {
        return vehicleType;
    }

    public void setVehicleType(String vehicleType) {
        this.vehicleType = vehicleType;
    }

    public String getPickupLocation() {
        return pickupLocation;
    }

    public void setPickupLocation(String pickupLocation) {
        this.pickupLocation = pickupLocation;
    }

    public String getDropLocation() {
        return dropLocation;
    }

    public void setDropLocation(String dropLocation) {
        this.dropLocation = dropLocation;
    }

    public double getDistanceKm() {
        return distanceKm;
    }

    public void setDistanceKm(double distanceKm) {
        this.distanceKm = distanceKm;
    }

    public double getFare() {
        return fare;
    }

    public void setFare(double fare) {
        this.fare = fare;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getBookingTime() {
        return bookingTime;
    }

    public void setBookingTime(String bookingTime) {
        this.bookingTime = bookingTime;
    }
}
