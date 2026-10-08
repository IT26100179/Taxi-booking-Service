package com.taxibooking.model;

import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * Driver Model (Component 2 - Driver Management)
 * Demonstrates:
 *  - Inheritance:     User -> Driver -> FullTimeDriver / PartTimeDriver
 *  - Encapsulation:   availabilityStatus is private and changed ONLY via setAvailability()
 *  - Polymorphism:    calculateCommission() is overridden in the subclasses
 */
public class Driver extends User {

    public static final String OFFLINE = "OFFLINE";
    public static final String AVAILABLE = "AVAILABLE";
    public static final String ON_TRIP = "ON_TRIP";
    public static final String SUSPENDED = "SUSPENDED";

    // Allowed status changes (state machine)
    private static final Map<String, List<String>> TRANSITIONS = new HashMap<>();
    static {
        TRANSITIONS.put(OFFLINE, List.of(AVAILABLE, SUSPENDED));
        TRANSITIONS.put(AVAILABLE, List.of(OFFLINE, ON_TRIP, SUSPENDED));
        TRANSITIONS.put(ON_TRIP, List.of(AVAILABLE));      // a trip must finish first
        TRANSITIONS.put(SUSPENDED, List.of(OFFLINE));      // reinstate goes to OFFLINE
    }

    private String licenseNumber;
    private String vehicleType;          // Car, Van, Bike
    private String availabilityStatus;   // PRIVATE: only setAvailability() may change it
    private double rating;
    private String nic;
    private String employmentType;       // FULLTIME / PARTTIME
    private String vehicleId;

    // ---- Existing constructors (kept so teammates' code still compiles) ----
    public Driver() {
        super();
        setRole("DRIVER");
        this.availabilityStatus = AVAILABLE;
        this.rating = 5.0;
        this.employmentType = "FULLTIME";
    }

    public Driver(String id, String name, String email, String phone, String password,
                  String licenseNumber, String vehicleType, String status, double rating) {
        this(id, name, email, phone, password, licenseNumber, vehicleType,
                (status != null && !status.isBlank()) ? status : AVAILABLE,
                rating > 0 ? rating : 5.0, "", "FULLTIME", "");
    }

    // ---- Full constructor (used by file loading and registration) ----
    public Driver(String id, String name, String email, String phone, String password,
                  String licenseNumber, String vehicleType, String status, double rating,
                  String nic, String employmentType, String vehicleId) {
        super(id, name, email, phone, password, "DRIVER");
        this.licenseNumber = licenseNumber;
        this.vehicleType = vehicleType;
        this.availabilityStatus = normalise(status);   // initial value, not a "transition"
        setRating(rating);
        this.nic = nic;
        this.employmentType = employmentType;
        this.vehicleId = vehicleId;
    }

    // ---- Encapsulation: the ONLY way to change availability ----
    public void setAvailability(String newStatus) {
        String target = normalise(newStatus);
        if (target.equals(availabilityStatus)) {
            return;
        }
        if (!TRANSITIONS.get(availabilityStatus).contains(target)) {
            throw new IllegalStateException(
                    "Invalid status change: " + availabilityStatus + " -> " + target);
        }
        this.availabilityStatus = target;
    }

    public String getAvailabilityStatus() {
        return availabilityStatus;
    }

    public List<String> getAllowedTransitions() {
        return TRANSITIONS.get(availabilityStatus);
    }

    // Old name kept for teammates' code; it also goes through the rules
    public String getStatus() {
        return availabilityStatus;
    }

    public void setStatus(String status) {
        setAvailability(status);
    }

    private static String normalise(String status) {
        if (status == null || status.isBlank()) {
            throw new IllegalArgumentException("Status is required");
        }
        String s = status.trim().toUpperCase(Locale.ROOT);
        if (s.equals("BUSY")) {
            s = ON_TRIP;                       // old data used BUSY
        }
        if (!TRANSITIONS.containsKey(s)) {
            throw new IllegalArgumentException("Unknown status: " + status);
        }
        return s;
    }

    // ---- Polymorphism: subclasses override this ----
    public double calculateCommission(double fare) {
        return fare * 0.80;     // default; FullTimeDriver and PartTimeDriver override
    }

    @Override
    public String getDisplayInfo() {
        return "Driver: " + getName() + " [" + vehicleType + "] | Status: "
                + availabilityStatus + " | Rating: ⭐ " + rating;
    }

    // ---- File handling ----
    public String toFileString() {
        return String.join("|",
                nz(getId()), nz(getName()), nz(getEmail()), nz(getPhone()), nz(getPassword()),
                nz(licenseNumber), vehicleType != null ? vehicleType : "Car",
                availabilityStatus, String.format(Locale.US, "%.1f", rating),
                nz(nic), nz(employmentType), nz(vehicleId));
    }

    public static Driver fromFileString(String line) {
        if (line == null || line.trim().isEmpty()) {
            return null;
        }
        String[] p = line.split("\\|", -1);
        if (p.length < 9) {
            return null;
        }
        try {
            double rating = 5.0;
            try {
                rating = Double.parseDouble(p[8]);
            } catch (NumberFormatException ignored) {}
            String nic = p.length > 9 ? p[9] : "";
            String type = (p.length > 10 && !p[10].isBlank()) ? p[10] : "FULLTIME";
            String vehicleId = p.length > 11 ? p[11] : "";

            if ("PARTTIME".equals(type)) {
                return new PartTimeDriver(p[0], p[1], p[2], p[3], p[4], p[5], p[6], p[7], rating, nic, vehicleId);
            }
            return new FullTimeDriver(p[0], p[1], p[2], p[3], p[4], p[5], p[6], p[7], rating, nic, vehicleId);
        } catch (RuntimeException e) {
            return null;        // bad line is skipped instead of crashing the app
        }
    }

    private static String nz(String s) {
        return s != null ? s : "";
    }

    // ---- Getters / setters (with validation) ----
    public String getLicenseNumber() { return licenseNumber; }

    public void setLicenseNumber(String licenseNumber) {
        if (licenseNumber == null || licenseNumber.isBlank()) {
            throw new IllegalArgumentException("Licence number is required");
        }
        this.licenseNumber = licenseNumber.trim();
    }

    public String getVehicleType() { return vehicleType; }
    public void setVehicleType(String vehicleType) { this.vehicleType = vehicleType; }

    public double getRating() { return rating; }

    public void setRating(double rating) {
        if (rating < 0.0 || rating > 5.0) {
            throw new IllegalArgumentException("Rating must be between 0.0 and 5.0");
        }
        this.rating = rating;
    }

    public String getNic() { return nic; }
    public void setNic(String nic) { this.nic = nic; }

    public String getEmploymentType() { return employmentType; }

    public String getVehicleId() { return vehicleId; }
    public void setVehicleId(String vehicleId) { this.vehicleId = vehicleId; }
}