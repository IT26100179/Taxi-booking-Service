package com.taxibooking.controller;

import com.taxibooking.repository.*;
import com.taxibooking.service.FareCalculationService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.Map;

/**
 * Controller for Landing Page and System Overview
 */
@Controller
public class HomeController {

    private final PassengerFileRepository passengerRepo;
    private final DriverFileRepository driverRepo;
    private final VehicleFileRepository vehicleRepo;
    private final BookingFileRepository bookingRepo;
    private final PaymentFileRepository paymentRepo;
    private final ReviewFileRepository reviewRepo;
    private final FareCalculationService fareService;

    public HomeController(PassengerFileRepository passengerRepo,
                          DriverFileRepository driverRepo,
                          VehicleFileRepository vehicleRepo,
                          BookingFileRepository bookingRepo,
                          PaymentFileRepository paymentRepo,
                          ReviewFileRepository reviewRepo,
                          FareCalculationService fareService) {
        this.passengerRepo = passengerRepo;
        this.driverRepo = driverRepo;
        this.vehicleRepo = vehicleRepo;
        this.bookingRepo = bookingRepo;
        this.paymentRepo = paymentRepo;
        this.reviewRepo = reviewRepo;
        this.fareService = fareService;
    }

    @GetMapping("/")
    public String index(Model model) {
        // Provide live counts and data from .txt files to the template
        model.addAttribute("passengerCount", passengerRepo.count());
        model.addAttribute("driverCount", driverRepo.count());
        model.addAttribute("vehicleCount", vehicleRepo.count());
        model.addAttribute("bookingCount", bookingRepo.count());
        model.addAttribute("paymentCount", paymentRepo.count());
        model.addAttribute("reviewCount", reviewRepo.count());

        // Sample entities for instant preview
        model.addAttribute("vehicles", vehicleRepo.findAll());
        model.addAttribute("drivers", driverRepo.findAll());
        model.addAttribute("recentBookings", bookingRepo.findAll());
        model.addAttribute("reviews", reviewRepo.findAll());

        return "index";
    }

    /**
     * Live Ajax endpoint to calculate fare polymorphically
     */
    @GetMapping("/api/estimate-fare")
    @ResponseBody
    public Map<String, Object> estimateFare(@RequestParam(defaultValue = "Car") String type,
                                           @RequestParam(defaultValue = "10.0") double distance) {
        double estimated = fareService.estimateFare(type, distance);
        Map<String, Double> allFares = fareService.compareAllFares(distance);

        return Map.of(
                "type", type,
                "distance", distance,
                "fare", estimated,
                "comparison", allFares
        );
    }
}
