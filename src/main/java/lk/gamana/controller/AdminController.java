package lk.gamana.controller;

import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import lk.gamana.dto.PromoForm;
import lk.gamana.dto.VehicleTypeForm;
import lk.gamana.model.*;
import lk.gamana.repository.*;
import lk.gamana.service.AuditService;
import lk.gamana.service.RideService;
import lk.gamana.util.Places;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDate;
import java.util.*;

@Controller
@RequestMapping("/admin")
public class AdminController {

    private final UserRepository userRepository;
    private final DriverProfileRepository profileRepository;
    private final VehicleRepository vehicleRepository;
    private final VehicleTypeRepository vehicleTypeRepository;
    private final PromoRepository promoRepository;
    private final RideRepository rideRepository;
    private final RideService rideService;
    private final AuditService auditService;

    public AdminController(UserRepository userRepository,
                           DriverProfileRepository profileRepository,
                           VehicleRepository vehicleRepository,
                           VehicleTypeRepository vehicleTypeRepository,
                           PromoRepository promoRepository,
                           RideRepository rideRepository,
                           RideService rideService,
                           AuditService auditService) {
        this.userRepository = userRepository;
        this.profileRepository = profileRepository;
        this.vehicleRepository = vehicleRepository;
        this.vehicleTypeRepository = vehicleTypeRepository;
        this.promoRepository = promoRepository;
        this.rideRepository = rideRepository;
        this.rideService = rideService;
        this.auditService = auditService;
    }

    private String getAdminId(HttpSession session) {
        String id = (String) session.getAttribute("adminUserId");
        return id != null ? id : (String) session.getAttribute("userId");
    }

    @GetMapping("/api/status")
    @ResponseBody
    public Map<String, Object> apiStatus(HttpSession session) {
        String adminId = getAdminId(session);
        if (adminId == null) {
            return Map.of("authenticated", false);
        }
        rideService.expireOverdueOffers();
        Map<String, Object> resp = new HashMap<>();
        resp.put("authenticated", true);

        List<Ride> allRides = rideRepository.findAll();
        resp.put("totalRides", allRides.size());
        long completedCount = allRides.stream().filter(r -> r.getStatus() == RideStatus.COMPLETED).count();
        long totalRevenue = allRides.stream().filter(r -> r.getStatus() == RideStatus.COMPLETED).mapToLong(Ride::getFareFinal).sum();
        long platformCommission = Math.round(totalRevenue * 0.15);
        long onlineDrivers = profileRepository.findOnline().stream()
                .filter(p -> userRepository.findById(p.getUserId()).map(u -> u.getStatus() == UserStatus.ACTIVE).orElse(false))
                .count();
        long pendingDrivers = userRepository.findAll().stream()
                .filter(u -> u.getRole() == Role.DRIVER && u.getStatus() == UserStatus.PENDING)
                .count();

        resp.put("completedCount", completedCount);
        resp.put("totalRevenue", totalRevenue);
        resp.put("platformCommission", platformCommission);
        resp.put("onlineDrivers", onlineDrivers);
        resp.put("pendingDrivers", pendingDrivers);

        // Recent rides (last 6)
        List<Map<String, Object>> recentRidesList = allRides.stream()
                .sorted(Comparator.comparing(Ride::getRequestedAt, Comparator.nullsLast(Comparator.reverseOrder())))
                .limit(6)
                .map(r -> {
                    Map<String, Object> m = new HashMap<>();
                    m.put("id", r.getId());
                    m.put("pickup", r.getPickupPlace());
                    m.put("drop", r.getDropPlace());
                    m.put("distanceKm", String.format(Locale.US, "%.1f", r.getDistanceKm()));
                    m.put("fare", r.getFareFinal() > 0 ? r.getFareFinal() : r.getFareEstimate());
                    m.put("status", r.getStatus().name());
                    return m;
                })
                .toList();
        resp.put("recentRides", recentRidesList);

        // Recent audits (last 5)
        List<Map<String, Object>> auditList = auditService.findRecent(5).stream()
                .map(a -> {
                    Map<String, Object> m = new HashMap<>();
                    m.put("action", a.getAction());
                    m.put("target", a.getTarget());
                    m.put("adminId", a.getAdminId());
                    m.put("time", a.getTimestamp() != null ? a.getTimestamp().format(java.time.format.DateTimeFormatter.ofPattern("MM-dd HH:mm:ss")) : "");
                    return m;
                })
                .toList();
        resp.put("recentAudits", auditList);

        return resp;
    }

    @GetMapping
    public String dashboard(HttpSession session, Model model) {
        String adminId = getAdminId(session);
        rideService.expireOverdueOffers();

        List<Ride> allRides = rideRepository.findAll();
        long totalRides = allRides.size();
        List<Ride> completedRides = allRides.stream()
                .filter(r -> r.getStatus() == RideStatus.COMPLETED)
                .toList();
        long completedCount = completedRides.size();
        long totalRevenue = completedRides.stream().mapToLong(Ride::getFareFinal).sum();
        long platformCommission = Math.round(totalRevenue * 0.15);
        long driverEarnings = totalRevenue - platformCommission;

        long onlineDrivers = profileRepository.findOnline().stream()
                .filter(p -> userRepository.findById(p.getUserId())
                        .map(u -> u.getStatus() == UserStatus.ACTIVE).orElse(false))
                .count();

        long pendingDrivers = userRepository.findAll().stream()
                .filter(u -> u.getRole() == Role.DRIVER && u.getStatus() == UserStatus.PENDING)
                .count();

        model.addAttribute("totalRides", totalRides);
        model.addAttribute("completedCount", completedCount);
        model.addAttribute("totalRevenue", totalRevenue);
        model.addAttribute("platformCommission", platformCommission);
        model.addAttribute("driverEarnings", driverEarnings);
        model.addAttribute("onlineDrivers", onlineDrivers);
        model.addAttribute("pendingDrivers", pendingDrivers);
        model.addAttribute("recentAudits", auditService.findRecent(10));
        model.addAttribute("recentRides", allRides.stream()
                .sorted(Comparator.comparing(Ride::getRequestedAt).reversed())
                .limit(6)
                .toList());

        return "admin/dashboard";
    }

    public record DriverView(User user, DriverProfile profile, Vehicle vehicle, String vehicleTypeName) {}

    @GetMapping("/drivers")
    public String drivers(@RequestParam(value = "status", required = false, defaultValue = "ALL") String filterStatus,
                          Model model) {
        List<User> driverUsers = userRepository.findAll().stream()
                .filter(u -> u.getRole() == Role.DRIVER)
                .filter(u -> "ALL".equalsIgnoreCase(filterStatus) || u.getStatus().name().equalsIgnoreCase(filterStatus))
                .sorted(Comparator.comparing(User::getCreatedAt).reversed())
                .toList();

        Map<Integer, String> typeMap = new HashMap<>();
        vehicleTypeRepository.findAll().forEach(vt -> typeMap.put(vt.getId(), vt.getName()));

        List<DriverView> views = new ArrayList<>();
        for (User u : driverUsers) {
            DriverProfile p = profileRepository.findByUserId(u.getId())
                    .orElseGet(() -> new DriverProfile(u.getId(), "", "", false, "fort", ""));
            Vehicle v = vehicleRepository.findByDriver(u.getId()).orElse(null);
            String typeName = (v != null && typeMap.containsKey(v.getTypeId())) ? typeMap.get(v.getTypeId()) : "N/A";
            views.add(new DriverView(u, p, v, typeName));
        }

        model.addAttribute("drivers", views);
        model.addAttribute("currentFilter", filterStatus.toUpperCase());
        model.addAttribute("places", Places.ALL);
        return "admin/drivers";
    }

    @PostMapping("/drivers/{id}/approve")
    public String approveDriver(@PathVariable("id") String driverId,
                                HttpSession session,
                                RedirectAttributes ra) {
        String adminId = getAdminId(session);
        userRepository.findById(driverId).ifPresent(u -> {
            u.setStatus(UserStatus.ACTIVE);
            userRepository.save(u);
            auditService.log(adminId, "APPROVE_DRIVER", driverId);
        });
        ra.addFlashAttribute("success", "Driver " + driverId + " has been approved successfully.");
        return "redirect:/admin/drivers";
    }

    @PostMapping("/drivers/{id}/reject")
    public String rejectDriver(@PathVariable("id") String driverId,
                               @RequestParam(value = "note", required = false, defaultValue = "Documents did not meet criteria") String note,
                               HttpSession session,
                               RedirectAttributes ra) {
        String adminId = getAdminId(session);
        userRepository.findById(driverId).ifPresent(u -> {
            u.setStatus(UserStatus.REJECTED);
            userRepository.save(u);
            profileRepository.findByUserId(driverId).ifPresent(p -> {
                p.setOnline(false);
                p.setRejectionNote(note);
                profileRepository.save(p);
            });
            auditService.log(adminId, "REJECT_DRIVER", driverId + " (" + note + ")");
        });
        ra.addFlashAttribute("info", "Driver application rejected.");
        return "redirect:/admin/drivers";
    }

    @PostMapping("/drivers/{id}/suspend")
    public String suspendDriver(@PathVariable("id") String driverId,
                                HttpSession session,
                                RedirectAttributes ra) {
        String adminId = getAdminId(session);
        if (rideRepository.isBusy(driverId)) {
            ra.addFlashAttribute("error", "Cannot suspend driver while on an active trip!");
            return "redirect:/admin/drivers";
        }
        userRepository.findById(driverId).ifPresent(u -> {
            u.setStatus(UserStatus.SUSPENDED);
            userRepository.save(u);
            profileRepository.findByUserId(driverId).ifPresent(p -> {
                p.setOnline(false);
                profileRepository.save(p);
            });
            auditService.log(adminId, "SUSPEND_DRIVER", driverId);
        });
        ra.addFlashAttribute("info", "Driver suspended.");
        return "redirect:/admin/drivers";
    }

    @PostMapping("/drivers/{id}/reinstate")
    public String reinstateDriver(@PathVariable("id") String driverId,
                                  HttpSession session,
                                  RedirectAttributes ra) {
        String adminId = getAdminId(session);
        userRepository.findById(driverId).ifPresent(u -> {
            u.setStatus(UserStatus.ACTIVE);
            userRepository.save(u);
            auditService.log(adminId, "REINSTATE_DRIVER", driverId);
        });
        ra.addFlashAttribute("success", "Driver reinstated to active status.");
        return "redirect:/admin/drivers";
    }

    @GetMapping("/rides")
    public String rides(@RequestParam(value = "status", required = false, defaultValue = "ALL") String filterStatus,
                        Model model) {
        rideService.expireOverdueOffers();
        List<Ride> list = rideRepository.findAll().stream()
                .filter(r -> "ALL".equalsIgnoreCase(filterStatus) || r.getStatus().name().equalsIgnoreCase(filterStatus))
                .sorted(Comparator.comparing(Ride::getRequestedAt).reversed())
                .toList();

        Map<Integer, String> typeNames = new HashMap<>();
        vehicleTypeRepository.findAll().forEach(vt -> typeNames.put(vt.getId(), vt.getName()));

        Map<String, String> userNames = new HashMap<>();
        userRepository.findAll().forEach(u -> userNames.put(u.getId(), u.getFullName()));

        model.addAttribute("rides", list);
        model.addAttribute("currentFilter", filterStatus.toUpperCase());
        model.addAttribute("typeNames", typeNames);
        model.addAttribute("userNames", userNames);
        model.addAttribute("places", Places.ALL);
        return "admin/rides";
    }

    @GetMapping("/rates")
    public String rates(Model model) {
        model.addAttribute("types", vehicleTypeRepository.findAll());
        return "admin/rates";
    }

    @PostMapping("/rates")
    public String updateRate(@RequestParam("id") int id,
                             @RequestParam("baseFare") double baseFare,
                             @RequestParam("perKmRate") double perKmRate,
                             HttpSession session,
                             RedirectAttributes ra) {
        String adminId = getAdminId(session);
        vehicleTypeRepository.findById(id).ifPresent(vt -> {
            vt.setBaseFare(baseFare);
            vt.setPerKmRate(perKmRate);
            vehicleTypeRepository.save(vt);
            auditService.log(adminId, "UPDATE_RATES", vt.getName() + " (base=" + baseFare + ", km=" + perKmRate + ")");
        });
        ra.addFlashAttribute("success", "Rates updated successfully!");
        return "redirect:/admin/rates";
    }

    @GetMapping("/promos")
    public String promos(Model model) {
        model.addAttribute("promos", promoRepository.findAll());
        if (!model.containsAttribute("form")) {
            PromoForm form = new PromoForm();
            form.setValidTo(LocalDate.now().plusMonths(3));
            form.setUsageLimit(100);
            model.addAttribute("form", form);
        }
        return "admin/promos";
    }

    @PostMapping("/promos")
    public String createPromo(@Valid @ModelAttribute("form") PromoForm form,
                              BindingResult result,
                              HttpSession session,
                              RedirectAttributes ra) {
        String adminId = getAdminId(session);
        if (result.hasErrors()) {
            ra.addFlashAttribute("org.springframework.validation.BindingResult.form", result);
            ra.addFlashAttribute("form", form);
            return "redirect:/admin/promos";
        }
        String cleanCode = form.getCode().trim().toUpperCase();
        if (promoRepository.findByCode(cleanCode).isPresent()) {
            ra.addFlashAttribute("error", "Promo code " + cleanCode + " already exists!");
            return "redirect:/admin/promos";
        }

        Promo p = new Promo(cleanCode, form.getDiscountPct(), form.getValidTo(), form.getUsageLimit(), 0);
        promoRepository.save(p);
        auditService.log(adminId, "CREATE_PROMO", cleanCode + " (" + form.getDiscountPct() + "%)");
        ra.addFlashAttribute("success", "Promo code " + cleanCode + " created successfully!");
        return "redirect:/admin/promos";
    }
}
