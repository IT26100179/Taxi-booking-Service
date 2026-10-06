package com.taxibooking.model;

import java.time.LocalDate;

/**
 * Passenger Model (Component 1 - Passenger Management)
 * Demonstrates Inheritance by extending User.
 */
public class Passenger extends User {
    private String address;
    private String registeredDate;

    public Passenger() {
        super();
        setRole("PASSENGER");
    }

    public Passenger(String id, String name, String email, String phone, String password, String address, String registeredDate) {
        super(id, name, email, phone, password, "PASSENGER");
        this.address = address;
        this.registeredDate = (registeredDate != null && !registeredDate.isBlank()) 
                ? registeredDate 
                : LocalDate.now().toString();
    }

    @Override
    public String getDisplayInfo() {
        return "Passenger: " + getName() + " (" + getId() + ") | Contact: " + getPhone() + " | Address: " + address;
    }

    // Convert object to delimited text string for File Handling
    public String toFileString() {
        return String.join("|",
                getId() != null ? getId() : "",
                getName() != null ? getName() : "",
                getEmail() != null ? getEmail() : "",
                getPhone() != null ? getPhone() : "",
                getPassword() != null ? getPassword() : "",
                getAddress() != null ? getAddress() : "",
                getRegisteredDate() != null ? getRegisteredDate() : LocalDate.now().toString()
        );
    }

    // Factory method to parse delimited line from .txt file
    public static Passenger fromFileString(String line) {
        if (line == null || line.trim().isEmpty()) {
            return null;
        }
        String[] parts = line.split("\\|", -1);
        if (parts.length < 7) {
            return null;
        }
        return new Passenger(parts[0], parts[1], parts[2], parts[3], parts[4], parts[5], parts[6]);
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getRegisteredDate() {
        return registeredDate;
    }

    public void setRegisteredDate(String registeredDate) {
        this.registeredDate = registeredDate;
    }
}
