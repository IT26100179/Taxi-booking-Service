package lk.gamana.controller;

import jakarta.servlet.http.HttpSession;
import lk.gamana.model.*;
import lk.gamana.repository.DriverProfileRepository;
import lk.gamana.repository.RideRepository;
import lk.gamana.repository.UserRepository;
import lk.gamana.repository.VehicleRepository;
import lk.gamana.repository.VehicleTypeRepository;
import lk.gamana.service.RideException;
import lk.gamana.service.RideService;
import lk.gamana.util.Places;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.*;

@Controller
@RequestMapping("/driver")
public class DriverController {

    private final RideService rideService;
    private final RideRepository rideRepository;
    private final UserRepository userRepository;
    private final DriverProfileRepository profileRepository;
    private final VehicleRepository vehicleRepository;
    private final VehicleTypeRepository vehicleTypeRepository;

    public DriverController(RideService rideService,
                            RideRepository rideRepository,
                            UserRepository userRepository,
                            DriverProfileRepository profileRepository,
                            VehicleRepository vehicleRepository,
                            VehicleTypeRepository vehicleTypeRepository) {
        this.rideService = rideService;
        this.rideRepository = rideRepository;
        this.userRepository = userRepository;
        this.profileRepository = profileRepository;
        this.vehicleRepository = vehicleRepository;
        this.vehicleTypeRepository = vehicleTypeRepository;
    }

    private String getDriverId(HttpSession session) {
        String id = (String) session.getAttribute("driverUserId");
        return id != null ? id : (String) session.getAttribute("userId");
    }

    @GetMapping("/api/status")
    @ResponseBody
    public Map<String, Object> apiStatus(HttpSession session) {
        String driverId = getDriverId(session);
        if (driverId == null) {
            return Map.of("authenticated", false);
        }
        rideService.expireOverdueOffers();
        Map<String, Object> resp = new HashMap<>();
        resp.put("authenticated", true);

        Optional<Ride> offer = rideRepository.findCurrentOfferForDriver(driverId);
        if (offer.isPresent()) {
            resp.put("hasOffer", true);
            resp.put("offerId", offer.get().getId());
        } else {
            resp.put("hasOffer", false);
            resp.put("offerId", "");
        }

        Optional<Ride> active = rideRepository.findActiveByDriver(driverId);
        if (active.isPresent()) {
            resp.put("hasActive", true);
            resp.put("activeId", active.get().getId());
            resp.put("activeStatus", active.get().getStatus().name());
        } else {
            resp.put("hasActive", false);
            resp.put("activeId", "");
            resp.put("activeStatus", "");
        }

        resp.put("rating", rideService.calculateDriverRating(driverId));
        resp.put("completed", rideService.countCompletedRidesForDriver(driverId));
        resp.put("earnings", Math.round(rideService.calculateDriverEarnings(driverId)));

        return resp;
    }

    @GetMapping
    public String dashboard(HttpSession session, Model model) {
        String driverId = getDriverId(session);
        rideService.expireOverdueOffers();

        User user = userRepository.findById(driverId).orElseThrow();
        DriverProfile profile = profileRepository.findByUserId(driverId)
                .orElseGet(() -> new DriverProfile(driverId, "", "", false, "fort", ""));
        Vehicle vehicle = vehicleRepository.findByDriver(driverId).orElse(null);

        model.addAttribute("currentUser", user);
        model.addAttribute("profile", profile);
        model.addAttribute("vehicle", vehicle);
        if (vehicle != null) {
            vehicleTypeRepository.findById(vehicle.getTypeId()).ifPresent(vt -> model.addAttribute("vehicleType", vt));
        }

        model.addAttribute("places", Places.ALL.values());
        model.addAttribute("earnings", Math.round(rideService.calculateDriverEarnings(driverId)));
        model.addAttribute("rating", rideService.calculateDriverRating(driverId));
        model.addAttribute("completedTrips", rideService.countCompletedRidesForDriver(driverId));

        if (user.getStatus() == UserStatus.ACTIVE) {
            Optional<Ride> activeRide = rideRepository.findActiveByDriver(driverId);
            if (activeRide.isPresent()) {
                Ride ride = activeRide.get();
                model.addAttribute("activeRide", ride);
                model.addAttribute("pickupPlace", Places.getOrResolve(ride.getPickupPlace(), ride.getPickupLat(), ride.getPickupLng()));
                model.addAttribute("dropPlace", Places.getOrResolve(ride.getDropPlace(), ride.getDropLat(), ride.getDropLng()));
                userRepository.findById(ride.getPassengerId()).ifPresent(p -> model.addAttribute("passengerUser", p));
                model.addAttribute("driverCut", Math.round(ride.getFareEstimate() * 0.85));
            } else {
                Optional<Ride> offer = rideRepository.findCurrentOfferForDriver(driverId);
                if (offer.isPresent()) {
                    Ride r = offer.get();
                    model.addAttribute("offerRide", r);
                    model.addAttribute("pickupPlace", Places.getOrResolve(r.getPickupPlace(), r.getPickupLat(), r.getPickupLng()));
                    model.addAttribute("dropPlace", Places.getOrResolve(r.getDropPlace(), r.getDropLat(), r.getDropLng()));
                    model.addAttribute("driverCut", Math.round(r.getFareEstimate() * 0.85));
                    model.addAttribute("secondsRemaining", Math.max(0, (r.getOfferDeadline() - System.currentTimeMillis()) / 1000));
                }
                model.addAttribute("recentRides", rideRepository.findByDriver(driverId).stream().limit(5).toList());
            }
        }

        return "driver/dashboard";
    }

    @PostMapping("/online")
    public String goOnline(@RequestParam(value = "currentPlace", required = false) String place,
                           HttpSession session,
                           RedirectAttributes ra) {
        String driverId = getDriverId(session);
        User user = userRepository.findById(driverId).orElseThrow();
        if (user.getStatus() != UserStatus.ACTIVE) {
            ra.addFlashAttribute("error", "Your account is not active yet.");
            return "redirect:/driver";
        }
        profileRepository.findByUserId(driverId).ifPresent(p -> {
            p.setOnline(true);
            if (place != null && Places.ALL.containsKey(place)) {
                p.setCurrentPlace(place);
            }
            profileRepository.save(p);
        });
        ra.addFlashAttribute("success", "You are now ONLINE and available for ride requests!");
        return "redirect:/driver";
    }

    @PostMapping("/offline")
    public String goOffline(HttpSession session, RedirectAttributes ra) {
        String driverId = getDriverId(session);
        if (rideRepository.isBusy(driverId)) {
            ra.addFlashAttribute("error", "Cannot go offline while on an active ride!");
            return "redirect:/driver";
        }
        profileRepository.findByUserId(driverId).ifPresent(p -> {
            p.setOnline(false);
            profileRepository.save(p);
        });
        ra.addFlashAttribute("info", "You are now OFFLINE.");
        return "redirect:/driver";
    }

    @PostMapping("/area")
    public String updateArea(@RequestParam("currentPlace") String place,
                             HttpSession session,
                             RedirectAttributes ra) {
        String driverId = getDriverId(session);
        if (Places.ALL.containsKey(place)) {
            profileRepository.findByUserId(driverId).ifPresent(p -> {
                p.setCurrentPlace(place);
                profileRepository.save(p);
            });
            ra.addFlashAttribute("success", "Current area updated to " + Places.ALL.get(place).name());
        }
        return "redirect:/driver";
    }

    @PostMapping("/rides/{id}/accept")
    public String acceptOffer(@PathVariable("id") String rideId,
                              HttpSession session,
                              RedirectAttributes ra) {
        String driverId = getDriverId(session);
        try {
            rideService.accept(rideId, driverId);
            ra.addFlashAttribute("success", "Ride accepted! Head towards passenger pickup location.");
        } catch (RideException e) {
            ra.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/driver";
    }

    @PostMapping("/rides/{id}/decline")
    public String declineOffer(@PathVariable("id") String rideId,
                               HttpSession session,
                               RedirectAttributes ra) {
        String driverId = getDriverId(session);
        try {
            rideService.decline(rideId, driverId);
            ra.addFlashAttribute("info", "Ride offer declined.");
        } catch (RideException e) {
            ra.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/driver";
    }

    @PostMapping("/rides/{id}/arrived")
    public String markArrived(@PathVariable("id") String rideId,
                              HttpSession session,
                              RedirectAttributes ra) {
        String driverId = getDriverId(session);
        try {
            rideService.arrived(rideId, driverId);
            ra.addFlashAttribute("success", "Status updated: Arrived at pickup!");
        } catch (RideException e) {
            ra.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/driver";
    }

    @PostMapping("/rides/{id}/start")
    public String startTrip(@PathVariable("id") String rideId,
                            HttpSession session,
                            RedirectAttributes ra) {
        String driverId = getDriverId(session);
        try {
            rideService.startTrip(rideId, driverId);
            ra.addFlashAttribute("success", "Trip started! Drive safely to the destination.");
        } catch (RideException e) {
            ra.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/driver";
    }

    @PostMapping("/rides/{id}/complete")
    public String completeTrip(@PathVariable("id") String rideId,
                               HttpSession session,
                               RedirectAttributes ra) {
        String driverId = getDriverId(session);
        try {
            Ride r = rideService.completeTrip(rideId, driverId);
            ra.addFlashAttribute("success", "Trip completed! Please collect Rs " + r.getFareFinal() + " cash from the passenger.");
        } catch (RideException e) {
            ra.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/driver";
    }
}
