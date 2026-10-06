package com.taxibooking.model;

/**
 * Driver Model (Component 2 - Driver Management)
 * Demonstrates Inheritance by extending User.
 */
public class Driver extends User {
    private String licenseNumber;
    private String vehicleType; // Car, Van, Bike
    private String status;      // AVAILABLE, BUSY, OFFLINE
    private double rating;

    public Driver() {
        super();
        setRole("DRIVER");
        this.status = "AVAILABLE";
        this.rating = 5.0;
    }

    public Driver(String id, String name, String email, String phone, String password,
                  String licenseNumber, String vehicleType, String status, double rating) {
        super(id, name, email, phone, password, "DRIVER");
        this.licenseNumber = licenseNumber;
        this.vehicleType = vehicleType;
        this.status = (status != null && !status.isBlank()) ? status : "AVAILABLE";
        this.rating = rating > 0 ? rating : 5.0;
    }

    @Override
    public String getDisplayInfo() {
        return "Driver: " + getName() + " [" + vehicleType + "] | Status: " + status + " | Rating: ⭐ " + rating;
    }

    public String toFileString() {
        return String.join("|",
                getId() != null ? getId() : "",
                getName() != null ? getName() : "",
                getEmail() != null ? getEmail() : "",
                getPhone() != null ? getPhone() : "",
                getPassword() != null ? getPassword() : "",
                getLicenseNumber() != null ? getLicenseNumber() : "",
                getVehicleType() != null ? getVehicleType() : "Car",
                getStatus() != null ? getStatus() : "AVAILABLE",
                String.valueOf(getRating())
        );
    }

    public static Driver fromFileString(String line) {
        if (line == null || line.trim().isEmpty()) {
            return null;
        }
        String[] parts = line.split("\\|", -1);
        if (parts.length < 9) {
            return null;
        }
        double rating = 5.0;
        try {
            rating = Double.parseDouble(parts[8]);
        } catch (NumberFormatException ignored) {}

        return new Driver(parts[0], parts[1], parts[2], parts[3], parts[4], parts[5], parts[6], parts[7], rating);
    }

    public String getLicenseNumber() {
        return licenseNumber;
    }

    public void setLicenseNumber(String licenseNumber) {
        this.licenseNumber = licenseNumber;
    }

    public String getVehicleType() {
        return vehicleType;
    }

    public void setVehicleType(String vehicleType) {
        this.vehicleType = vehicleType;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public double getRating() {
        return rating;
    }

    public void setRating(double rating) {
        this.rating = rating;
    }
}
