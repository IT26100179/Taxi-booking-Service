package com.taxibooking.controller;

import com.taxibooking.model.Driver;
import com.taxibooking.service.DriverService;
import com.taxibooking.service.ExternalComponents;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.TreeSet;

/**
 * Web controller for Driver Management (Component 2).
 * Connects the Thymeleaf pages to DriverService.
 */
@Controller
@RequestMapping("/drivers")
public class DriverController {

    private final DriverService driverService;

    public DriverController(DriverService driverService) {
        this.driverService = driverService;
    }

    // ---------- Admin: list / availability page ----------
    @GetMapping("")
    public String list(@RequestParam(required = false) String q,
                       @RequestParam(required = false) String status,
                       @RequestParam(required = false) String vehicleType,
                       Model model) {
        List<Driver> drivers = driverService.searchDriver(q, vehicleType);   // overloaded search
        if (status != null && !status.isBlank()) {
            drivers.removeIf(d -> !d.getAvailabilityStatus().equals(status));
        }
        model.addAttribute("drivers", drivers);
        model.addAttribute("q", q);
        model.addAttribute("selectedStatus", status);
        model.addAttribute("selectedType", vehicleType);
        model.addAttribute("statuses", List.of(Driver.OFFLINE, Driver.AVAILABLE, Driver.ON_TRIP, Driver.SUSPENDED));
        model.addAttribute("vehicleTypes", new TreeSet<>(ExternalComponents.getVehicleOptions().values()));
        return "driver/list";
    }

    // ---------- Register ----------
    @GetMapping("/register")
    public String registerForm(Model model) {
        model.addAttribute("vehicles", ExternalComponents.getVehicleOptions());
        return "driver/register";
    }

    @PostMapping("/register")
    public String register(@RequestParam String name, @RequestParam String email,
                           @RequestParam String phone, @RequestParam String password,
                           @RequestParam String nic, @RequestParam String licence,
                           @RequestParam String employmentType, @RequestParam String vehicleId,
                           RedirectAttributes ra) {
        try {
            Driver d = driverService.registerDriver(name, email, phone, password, nic, licence,
                    employmentType, vehicleId);
            ra.addFlashAttribute("success", "Driver " + d.getId() + " registered (status OFFLINE, rating 0.0)");
            return "redirect:/drivers";
        } catch (IllegalArgumentException | IllegalStateException e) {
            ra.addFlashAttribute("error", e.getMessage());
            return "redirect:/drivers/register";
        }
    }

    // ---------- Login / logout ----------
    @GetMapping("/login")
    public String loginForm() {
        return "driver/login";
    }

    @PostMapping("/login")
    public String login(@RequestParam String email, @RequestParam String password,
                        HttpSession session, RedirectAttributes ra) {
        Optional<Driver> driver = driverService.login(email, password);
        if (driver.isEmpty()) {
            ra.addFlashAttribute("error", "Invalid email or password");
            return "redirect:/drivers/login";
        }
        session.setAttribute("loggedUser", driver.get().getId());   // team-wide session names
        session.setAttribute("userRole", "DRIVER");
        return "redirect:/drivers/dashboard";
    }

    @GetMapping("/logout")
    public String logout(HttpSession session) {
        session.invalidate();
        return "redirect:/drivers/login";
    }

    // ---------- Driver dashboard ----------
    @GetMapping("/dashboard")
    public String dashboard(HttpSession session, Model model) {
        String id = (String) session.getAttribute("loggedUser");
        if (id == null || !"DRIVER".equals(session.getAttribute("userRole"))) {
            return "redirect:/drivers/login";
        }
        Optional<Driver> driver = driverService.searchDriver(id);
        if (driver.isEmpty()) {
            session.invalidate();
            return "redirect:/drivers/login";
        }
        model.addAttribute("driver", driver.get());
        model.addAttribute("trips", ExternalComponents.getTodaysTrips(id));
        return "driver/dashboard";
    }

    @PostMapping("/status")
    public String changeStatus(@RequestParam String status, HttpSession session, RedirectAttributes ra) {
        String id = (String) session.getAttribute("loggedUser");
        if (id == null) {
            return "redirect:/drivers/login";
        }
        try {
            driverService.setAvailability(id, status);
            ra.addFlashAttribute("success", "Status changed to " + status);
        } catch (IllegalArgumentException | IllegalStateException | NoSuchElementException e) {
            ra.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/drivers/dashboard";
    }

    // ---------- Edit profile ----------
    @GetMapping("/{id}/edit")
    public String editForm(@PathVariable String id, Model model, RedirectAttributes ra) {
        Optional<Driver> driver = driverService.searchDriver(id);
        if (driver.isEmpty()) {
            ra.addFlashAttribute("error", "Driver " + id + " not found");
            return "redirect:/drivers";
        }
        model.addAttribute("driver", driver.get());
        model.addAttribute("vehicles", ExternalComponents.getVehicleOptions());
        return "driver/edit";
    }

    @PostMapping("/{id}/edit")
    public String edit(@PathVariable String id, @RequestParam String name, @RequestParam String email,
                       @RequestParam String phone, @RequestParam String nic,
                       @RequestParam String licence, @RequestParam String vehicleId,
                       HttpSession session, RedirectAttributes ra) {
        try {
            driverService.updateProfile(id, name, email, phone, nic);
            driverService.updateLicence(id, licence);
            driverService.reassignVehicle(id, vehicleId);
            ra.addFlashAttribute("success", "Profile updated for " + id);
        } catch (IllegalArgumentException | IllegalStateException | NoSuchElementException e) {
            ra.addFlashAttribute("error", e.getMessage());
            return "redirect:/drivers/" + id + "/edit";
        }
        return id.equals(session.getAttribute("loggedUser")) ? "redirect:/drivers/dashboard" : "redirect:/drivers";
    }

    // ---------- Suspend / reinstate / delete (admin) ----------
    @PostMapping("/{id}/suspend")
    public String suspend(@PathVariable String id, RedirectAttributes ra) {
        return run(ra, () -> driverService.suspendDriver(id), "Driver " + id + " suspended");
    }

    @PostMapping("/{id}/reinstate")
    public String reinstate(@PathVariable String id, RedirectAttributes ra) {
        return run(ra, () -> driverService.reinstateDriver(id), "Driver " + id + " reinstated (OFFLINE)");
    }

    @PostMapping("/{id}/delete")
    public String delete(@PathVariable String id, RedirectAttributes ra) {
        return run(ra, () -> driverService.deleteDriver(id), "Driver " + id + " permanently deleted");
    }

    private String run(RedirectAttributes ra, Runnable action, String successMessage) {
        try {
            action.run();
            ra.addFlashAttribute("success", successMessage);
        } catch (IllegalArgumentException | IllegalStateException | NoSuchElementException e) {
            ra.addFlashAttribute("error", e.getMessage());     // e.g. "cannot be deleted..." message
        }
        return "redirect:/drivers";
    }
}