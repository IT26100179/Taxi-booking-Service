package com.taxibooking.model;

/**
 * Base Abstract Class for Vehicles (Component 3 - Vehicle & Fleet Management)
 * Demonstrates:
 *  - Abstraction & Inheritance root for Car, Van, and Bike
 *  - Encapsulation
 */
public abstract class Vehicle {
    private String vehicleId;
    private String plateNumber;
    private String model;
    private String type; // Car, Van, Bike
    private int capacity;
    private double ratePerKm;
    private boolean isAvailable;

    public Vehicle() {
        this.isAvailable = true;
    }

    public Vehicle(String vehicleId, String plateNumber, String model, String type, int capacity, double ratePerKm, boolean isAvailable) {
        this.vehicleId = vehicleId;
        this.plateNumber = plateNumber;
        this.model = model;
        this.type = type;
        this.capacity = capacity;
        this.ratePerKm = ratePerKm;
        this.isAvailable = isAvailable;
    }

    // Abstract method implemented polymorphically by Car, Van, Bike
    public abstract String getCategoryDetails();

    public String toFileString() {
        return String.join("|",
                vehicleId != null ? vehicleId : "",
                plateNumber != null ? plateNumber : "",
                model != null ? model : "",
                type != null ? type : "Car",
                String.valueOf(capacity),
                String.valueOf(ratePerKm),
                String.valueOf(isAvailable)
        );
    }

    public static Vehicle fromFileString(String line) {
        if (line == null || line.trim().isEmpty()) {
            return null;
        }
        String[] parts = line.split("\\|", -1);
        if (parts.length < 7) {
            return null;
        }
        String id = parts[0];
        String plate = parts[1];
        String model = parts[2];
        String type = parts[3];
        int cap = 4;
        double rate = 100.0;
        boolean avail = true;
        try {
            cap = Integer.parseInt(parts[4]);
            rate = Double.parseDouble(parts[5]);
            avail = Boolean.parseBoolean(parts[6]);
        } catch (NumberFormatException ignored) {}

        if ("Van".equalsIgnoreCase(type)) {
            return new Van(id, plate, model, cap, rate, avail);
        } else if ("Bike".equalsIgnoreCase(type)) {
            return new Bike(id, plate, model, cap, rate, avail);
        } else {
            return new Car(id, plate, model, cap, rate, avail);
        }
    }

    public String getVehicleId() {
        return vehicleId;
    }

    public void setVehicleId(String vehicleId) {
        this.vehicleId = vehicleId;
    }

    public String getPlateNumber() {
        return plateNumber;
    }

    public void setPlateNumber(String plateNumber) {
        this.plateNumber = plateNumber;
    }

    public String getModel() {
        return model;
    }

    public void setModel(String model) {
        this.model = model;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public int getCapacity() {
        return capacity;
    }

    public void setCapacity(int capacity) {
        this.capacity = capacity;
    }

    public double getRatePerKm() {
        return ratePerKm;
    }

    public void setRatePerKm(double ratePerKm) {
        this.ratePerKm = ratePerKm;
    }

    public boolean isAvailable() {
        return isAvailable;
    }

    public void setAvailable(boolean available) {
        isAvailable = available;
    }
}
