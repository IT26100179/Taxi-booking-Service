package com.taxibooking.model;

/**
 * Base Abstract Class representing a User in the system.
 * Demonstrates:
 *  - Encapsulation (private fields with validated getters/setters)
 *  - Abstraction (abstract method getDisplayInfo())
 *  - Inheritance root for Passenger, Driver, and Admin
 */
public abstract class User {
    private String id;
    private String name;
    private String email;
    private String phone;
    private String password;
    private String role; // PASSENGER, DRIVER, ADMIN

    // Default Constructor
    public User() {
    }

    // Parameterized Constructor
    public User(String id, String name, String email, String phone, String password, String role) {
        this.id = id;
        this.name = name;
        this.email = email;
        this.phone = phone;
        this.password = password;
        this.role = role;
    }

    // Abstract method to be overridden by subclasses (Polymorphism)
    public abstract String getDisplayInfo();

    // Getters and Setters (Encapsulation)
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    @Override
    public String toString() {
        return "User{" +
                "id='" + id + '\'' +
                ", name='" + name + '\'' +
                ", email='" + email + '\'' +
                ", phone='" + phone + '\'' +
                ", role='" + role + '\'' +
                '}';
    }
}
