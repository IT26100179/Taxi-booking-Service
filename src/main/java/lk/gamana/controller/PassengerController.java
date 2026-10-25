package lk.gamana.controller;

import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import lk.gamana.dto.BookingForm;
import lk.gamana.dto.RateForm;
import lk.gamana.model.*;
import lk.gamana.repository.RideRepository;
import lk.gamana.repository.UserRepository;
import lk.gamana.repository.VehicleRepository;
import lk.gamana.repository.VehicleTypeRepository;
import lk.gamana.service.RideException;
import lk.gamana.service.RideService;
import lk.gamana.util.Places;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Controller
@RequestMapping("/passenger")
public class PassengerController {

    private final RideService rideService;
    private final RideRepository rideRepository;
    private final UserRepository userRepository;
    private final VehicleRepository vehicleRepository;
    private final VehicleTypeRepository vehicleTypeRepository;

    public PassengerController(RideService rideService,
                               RideRepository rideRepository,
                               UserRepository userRepository,
                               VehicleRepository vehicleRepository,
                               VehicleTypeRepository vehicleTypeRepository) {
        this.rideService = rideService;
        this.rideRepository = rideRepository;
        this.userRepository = userRepository;
        this.vehicleRepository = vehicleRepository;
        this.vehicleTypeRepository = vehicleTypeRepository;
    }

    private String getPassengerId(HttpSession session) {
        if (session == null) return null;
        String id = (String) session.getAttribute("passengerUserId");
        if (id != null && !id.isBlank()) return id;
        String role = (String) session.getAttribute("role");
        if ("PASSENGER".equals(role)) {
            return (String) session.getAttribute("userId");
        }
        id = (String) session.getAttribute("userId");
        if (id != null && !id.isBlank()) return id;
        id = (String) session.getAttribute("adminUserId");
        if (id != null && !id.isBlank()) return id;
        id = (String) session.getAttribute("driverUserId");
        if (id != null && !id.isBlank()) return id;
        return null;
    }

    @GetMapping("/api/status")
    @ResponseBody
    public Map<String, Object> apiStatus(HttpSession session) {
        String passengerId = getPassengerId(session);
        if (passengerId == null) {
            return Map.of("authenticated", false);
        }
        rideService.expireOverdueOffers();
        Map<String, Object> resp = new HashMap<>();
        resp.put("authenticated", true);

        Optional<Ride> activeRide = rideRepository.findActiveByPassenger(passengerId);
        if (activeRide.isPresent()) {
            Ride r = activeRide.get();
            resp.put("active", true);
            resp.put("rideId", r.getId());
            resp.put("status", r.getStatus().name());
            resp.put("driverId", r.getDriverId() != null ? r.getDriverId() : "");
        } else {
            resp.put("active", false);
            Optional<Ride> lastFinished = rideRepository.findLastFinishedByPassenger(passengerId);
            if (lastFinished.isPresent() && lastFinished.get().getStatus() == RideStatus.COMPLETED && lastFinished.get().getRating() == 0) {
                resp.put("unratedRideId", lastFinished.get().getId());
                resp.put("needsRating", true);
            } else {
                resp.put("needsRating", false);
            }
        }
        return resp;
    }

    @GetMapping
    public String dashboard(HttpSession session, Model model) {
        String passengerId = getPassengerId(session);
        if (passengerId == null) {
            return "redirect:/login";
        }
        rideService.expireOverdueOffers();

        userRepository.findById(passengerId).ifPresent(u -> model.addAttribute("currentUser", u));

        Optional<Ride> activeRide = rideRepository.findActiveByPassenger(passengerId);
        if (activeRide.isPresent()) {
            Ride ride = activeRide.get();
            model.addAttribute("activeRide", ride);
            model.addAttribute("pickupPlace", Places.ALL.get(ride.getPickupPlace()));
            model.addAttribute("dropPlace", Places.ALL.get(ride.getDropPlace()));
            vehicleTypeRepository.findById(ride.getTypeId()).ifPresent(vt -> model.addAttribute("vehicleType", vt));

            if (ride.getDriverId() != null && !ride.getDriverId().isBlank()) {
                userRepository.findById(ride.getDriverId()).ifPresent(d -> model.addAttribute("driverUser", d));
                vehicleRepository.findByDriver(ride.getDriverId()).ifPresent(v -> model.addAttribute("driverVehicle", v));
                model.addAttribute("driverRating", rideService.calculateDriverRating(ride.getDriverId()));
                model.addAttribute("driverTrips", rideService.countCompletedRidesForDriver(ride.getDriverId()));
            }
        } else {
            // Check if there is an unrated completed ride to rate
            Optional<Ride> lastFinished = rideRepository.findLastFinishedByPassenger(passengerId);
            if (lastFinished.isPresent()) {
                Ride last = lastFinished.get();
                if (last.getStatus() == RideStatus.COMPLETED && last.getRating() == 0) {
                    model.addAttribute("unratedRide", last);
                    model.addAttribute("rateForm", new RateForm());
                    if (last.getDriverId() != null && !last.getDriverId().isBlank()) {
                        userRepository.findById(last.getDriverId()).ifPresent(d -> model.addAttribute("driverUser", d));
                    }
                }
            }

            model.addAttribute("places", Places.ALL.values());
            model.addAttribute("types", vehicleTypeRepository.findAll());
            if (!model.containsAttribute("bookingForm")) {
                model.addAttribute("bookingForm", new BookingForm());
            }
        }

        return "passenger/dashboard";
    }

    @PostMapping("/book")
    public String bookRide(@Valid @ModelAttribute("bookingForm") BookingForm form,
                           BindingResult result,
                           HttpSession session,
                           RedirectAttributes ra) {
        String passengerId = getPassengerId(session);
        if (result.hasErrors()) {
            ra.addFlashAttribute("org.springframework.validation.BindingResult.bookingForm", result);
            ra.addFlashAttribute("bookingForm", form);
            return "redirect:/passenger";
        }
        try {
            rideService.book(passengerId, form);
            ra.addFlashAttribute("success", "Ride requested successfully! Searching for nearby drivers...");
        } catch (RideException e) {
            ra.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/passenger";
    }

    @PostMapping("/rides/{id}/cancel")
    public String cancelRide(@PathVariable("id") String rideId,
                             HttpSession session,
                             RedirectAttributes ra) {
        String passengerId = getPassengerId(session);
        try {
            rideService.cancel(rideId, passengerId, true);
            ra.addFlashAttribute("info", "Ride has been cancelled.");
        } catch (RideException e) {
            ra.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/passenger";
    }

    @PostMapping("/rides/{id}/rate")
    public String rateRide(@PathVariable("id") String rideId,
                           @Valid @ModelAttribute("rateForm") RateForm form,
                           BindingResult result,
                           HttpSession session,
                           RedirectAttributes ra) {
        String passengerId = getPassengerId(session);
        if (result.hasErrors()) {
            ra.addFlashAttribute("error", "Please provide a valid rating between 1 and 5 stars.");
            return "redirect:/passenger";
        }
        try {
            rideService.rate(rideId, passengerId, form.getRating(), form.getComment());
            ra.addFlashAttribute("success", "Thank you for rating your ride!");
        } catch (RideException e) {
            ra.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/passenger";
    }

    @GetMapping("/history")
    public String history(HttpSession session, Model model) {
        String passengerId = getPassengerId(session);
        if (passengerId == null) {
            return "redirect:/login";
        }
        userRepository.findById(passengerId).ifPresent(u -> model.addAttribute("currentUser", u));

        List<Ride> list = rideRepository.findByPassenger(passengerId);
        Map<Integer, String> typeNames = new HashMap<>();
        vehicleTypeRepository.findAll().forEach(vt -> typeNames.put(vt.getId(), vt.getName()));

        Map<String, String> driverNames = new HashMap<>();
        for (Ride r : list) {
            if (r.getDriverId() != null && !r.getDriverId().isBlank() && !driverNames.containsKey(r.getDriverId())) {
                userRepository.findById(r.getDriverId()).ifPresent(d -> driverNames.put(d.getId(), d.getFullName()));
            }
        }

        model.addAttribute("rides", list);
        model.addAttribute("typeNames", typeNames);
        model.addAttribute("driverNames", driverNames);
        model.addAttribute("places", Places.ALL);
        return "passenger/history";
    }
}
