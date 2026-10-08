package com.taxibooking.service;

import com.taxibooking.model.Driver;
import com.taxibooking.model.FullTimeDriver;
import com.taxibooking.model.PartTimeDriver;
import com.taxibooking.repository.DriverFileRepository;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.function.Consumer;

/**
 * Business logic for Driver Management (Component 2).
 * Uses DriverFileRepository for all drivers.txt reading/writing.
 */
@Service
public class DriverService {

    private final DriverFileRepository repository;

    public DriverService(DriverFileRepository repository) {
        this.repository = repository;
    }

    // ================= CREATE =================
    public synchronized Driver registerDriver(String name, String email, String phone, String password,
                                              String nic, String licence, String employmentType,
                                              String vehicleId) {
        if (isBlank(name) || isBlank(nic) || isBlank(licence) || isBlank(email)) {
            throw new IllegalArgumentException("Name, email, NIC and licence number are required");
        }
        if (password == null || password.length() < 4) {
            throw new IllegalArgumentException("Password must be at least 4 characters");
        }
        if (phone == null || !phone.matches("\\d{10}")) {
            throw new IllegalArgumentException("Phone number must be 10 digits");
        }
        if (name.contains("|") || email.contains("|") || licence.contains("|") || nic.contains("|")) {
            throw new IllegalArgumentException("The '|' character is not allowed");
        }
        for (Driver d : repository.findAll()) {
            if (licence.equalsIgnoreCase(d.getLicenseNumber())) {
                throw new IllegalArgumentException("Licence number is already registered");
            }
            if (nic.equalsIgnoreCase(d.getNic())) {
                throw new IllegalArgumentException("NIC is already registered");
            }
            if (email.equalsIgnoreCase(d.getEmail())) {
                throw new IllegalArgumentException("Email is already registered");
            }
        }
        checkVehicleFree(vehicleId, null);

        String id = nextId();
        String vehicleType = ExternalComponents.getVehicleType(vehicleId);
        Driver driver = "PARTTIME".equals(employmentType)
                ? new PartTimeDriver(id, name.trim(), email.trim(), phone, password, licence.trim(),
                vehicleType, Driver.OFFLINE, 0.0, nic.trim(), vehicleId)
                : new FullTimeDriver(id, name.trim(), email.trim(), phone, password, licence.trim(),
                vehicleType, Driver.OFFLINE, 0.0, nic.trim(), vehicleId);

        Driver saved = repository.save(driver);
        if (saved == null) {
            throw new IllegalStateException("Could not save the driver to drivers.txt");
        }
        return saved;
    }

    // ================= READ =================
    public Optional<Driver> login(String email, String password) {
        return repository.findAll().stream()
                .filter(d -> d.getEmail() != null && d.getEmail().equalsIgnoreCase(email))
                .filter(d -> d.getPassword() != null && d.getPassword().equals(password))
                .findFirst();
    }

    /** Overload 1: search by ID. */
    public Optional<Driver> searchDriver(String id) {
        return repository.findById(id);
    }

    /** Overload 2: search by name and vehicle type (blank = any). */
    public List<Driver> searchDriver(String name, String vehicleType) {
        List<Driver> result = new ArrayList<>();
        for (Driver d : repository.findAll()) {
            boolean nameOk = isBlank(name)
                    || (d.getName() != null && d.getName().toLowerCase().contains(name.trim().toLowerCase()));
            boolean typeOk = isBlank(vehicleType) || vehicleType.equalsIgnoreCase(d.getVehicleType());
            if (nameOk && typeOk) {
                result.add(d);
            }
        }
        return result;
    }

    public Optional<Driver> searchByLicence(String licence) {
        return repository.findAll().stream()
                .filter(d -> d.getLicenseNumber() != null && d.getLicenseNumber().equalsIgnoreCase(licence))
                .findFirst();
    }

    public List<Driver> getAllDrivers() {
        return repository.findAll();
    }

    /** KEY METHOD for Component 04: only AVAILABLE drivers of the given vehicle type. */
    public List<Driver> getAvailableDrivers(String vehicleType) {
        List<Driver> result = new ArrayList<>();
        for (Driver d : repository.findAll()) {
            if (Driver.AVAILABLE.equals(d.getAvailabilityStatus())
                    && d.getVehicleType() != null
                    && d.getVehicleType().equalsIgnoreCase(vehicleType)) {
                result.add(d);
            }
        }
        return result;
    }

    // ================= UPDATE =================
    /** Also used by Component 04: setAvailability(id, status). */
    public synchronized Driver setAvailability(String id, String status) {
        return modify(id, d -> d.setAvailability(status));
    }

    public synchronized Driver updateProfile(String id, String name, String email, String phone, String nic) {
        if (isBlank(name) || isBlank(email) || isBlank(nic)) {
            throw new IllegalArgumentException("Name, email and NIC are required");
        }
        if (phone == null || !phone.matches("\\d{10}")) {
            throw new IllegalArgumentException("Phone number must be 10 digits");
        }
        for (Driver other : repository.findAll()) {
            if (!other.getId().equals(id) && email.equalsIgnoreCase(other.getEmail())) {
                throw new IllegalArgumentException("Email belongs to another driver");
            }
        }
        return modify(id, d -> {
            d.setName(name.trim());
            d.setEmail(email.trim());
            d.setPhone(phone);
            d.setNic(nic.trim());
        });
    }

    public synchronized Driver updateLicence(String id, String licence) {
        for (Driver other : repository.findAll()) {
            if (!other.getId().equals(id) && licence != null
                    && licence.equalsIgnoreCase(other.getLicenseNumber())) {
                throw new IllegalArgumentException("Licence number belongs to another driver");
            }
        }
        return modify(id, d -> d.setLicenseNumber(licence));
    }

    public synchronized Driver reassignVehicle(String id, String vehicleId) {
        checkVehicleFree(vehicleId, id);
        String type = ExternalComponents.getVehicleType(vehicleId);
        return modify(id, d -> {
            d.setVehicleId(vehicleId);
            d.setVehicleType(type);
        });
    }

    /** Called with the average rating supplied by Component 06. */
    public synchronized Driver refreshRating(String id, double averageRating) {
        return modify(id, d -> d.setRating(averageRating));
    }

    public synchronized Driver suspendDriver(String id) {
        return modify(id, d -> d.setAvailability(Driver.SUSPENDED));
    }

    public synchronized Driver reinstateDriver(String id) {
        return modify(id, d -> d.setAvailability(Driver.OFFLINE));
    }

    // ================= DELETE =================
    public synchronized void deleteDriver(String id) {
        if (ExternalComponents.hasActiveBooking(id)) {
            throw new IllegalStateException("Driver " + id + " cannot be deleted because they have an "
                    + "active booking (PENDING, CONFIRMED or ON_TRIP). Complete or cancel the booking first, "
                    + "or suspend the driver instead.");
        }
        if (!repository.deleteById(id)) {
            throw new NoSuchElementException("Driver " + id + " not found");
        }
    }

    // ================= helpers =================
    private Driver modify(String id, Consumer<Driver> change) {
        Driver d = repository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Driver " + id + " not found"));
        change.accept(d);                       // may throw; file untouched if it does
        if (!repository.update(d)) {
            throw new IllegalStateException("Could not save changes to drivers.txt");
        }
        return d;
    }

    private void checkVehicleFree(String vehicleId, String ignoreDriverId) {
        if (isBlank(vehicleId)) {
            return;
        }
        for (Driver d : repository.findAll()) {
            if (vehicleId.equals(d.getVehicleId()) && !d.getId().equals(ignoreDriverId)) {
                throw new IllegalArgumentException("Vehicle " + vehicleId + " is already assigned to " + d.getId());
            }
        }
    }

    private String nextId() {
        int max = 200;
        for (Driver d : repository.findAll()) {
            String id = d.getId();
            if (id != null && id.startsWith("DRV-")) {
                try {
                    max = Math.max(max, Integer.parseInt(id.substring(4)));
                } catch (NumberFormatException ignored) {
                }
            }
        }
        return "DRV-" + (max + 1);
    }

    private boolean isBlank(String s) {
        return s == null || s.isBlank();
    }
}
