package lk.gamana.controller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import lk.gamana.service.DriverAccountService;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class DriverAccountController {

    private final DriverAccountService accountService;

    public DriverAccountController(
            DriverAccountService accountService) {
        this.accountService = accountService;
    }

    @PostMapping("/driver/account/delete")
    public String deleteAccount(
            @RequestParam("currentPassword") String currentPassword,
            HttpServletRequest request,
            RedirectAttributes redirectAttributes) {

        HttpSession session = request.getSession(false);

        if (session == null) {
            return "redirect:/driver/login";
        }

        String driverId =
                (String) session.getAttribute("driverUserId");

        // Match the project's existing driver-session fallback.
        if (driverId == null
                && "DRIVER".equals(session.getAttribute("role"))) {
            driverId = (String) session.getAttribute("userId");
        }

        if (driverId == null || driverId.isBlank()) {
            return "redirect:/driver/login";
        }

        try {
            accountService.deleteAccount(driverId, currentPassword);
        } catch (IllegalArgumentException exception) {
            redirectAttributes.addFlashAttribute(
                    "error", exception.getMessage());
            return "redirect:/driver";
        }

        session.invalidate();

        redirectAttributes.addFlashAttribute(
                "success", "Your driver account has been deleted.");

        return "redirect:/driver/login";
    }
}