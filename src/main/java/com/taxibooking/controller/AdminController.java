package com.taxibooking.controller;

import com.taxibooking.repository.*;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * Controller for Admin Overview & System Reports (Component 6)
 */
@Controller
@RequestMapping("/admin")
public class AdminController {

    private final PassengerFileRepository passengerRepo;
    private final DriverFileRepository driverRepo;
    private final VehicleFileRepository vehicleRepo;
    private final BookingFileRepository bookingRepo;
    private final PaymentFileRepository paymentRepo;
    private final ReviewFileRepository reviewRepo;

    public AdminController(PassengerFileRepository passengerRepo,
                           DriverFileRepository driverRepo,
                           VehicleFileRepository vehicleRepo,
                           BookingFileRepository bookingRepo,
                           PaymentFileRepository paymentRepo,
                           ReviewFileRepository reviewRepo) {
        this.passengerRepo = passengerRepo;
        this.driverRepo = driverRepo;
        this.vehicleRepo = vehicleRepo;
        this.bookingRepo = bookingRepo;
        this.paymentRepo = paymentRepo;
        this.reviewRepo = reviewRepo;
    }

    @GetMapping("")
    public String adminDashboard(Model model) {
        model.addAttribute("passengers", passengerRepo.findAll());
        model.addAttribute("drivers", driverRepo.findAll());
        model.addAttribute("vehicles", vehicleRepo.findAll());
        model.addAttribute("bookings", bookingRepo.findAll());
        model.addAttribute("payments", paymentRepo.findAll());
        model.addAttribute("reviews", reviewRepo.findAll());
        return "admin/dashboard";
    }
}
