package lk.gamana.controller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import lk.gamana.dto.DriverRegisterForm;
import lk.gamana.dto.LoginForm;
import lk.gamana.dto.RegisterForm;
import lk.gamana.model.Role;
import lk.gamana.model.User;
import lk.gamana.repository.VehicleTypeRepository;
import lk.gamana.service.AuthException;
import lk.gamana.service.AuthService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class AuthController {

    private final AuthService auth;
    private final VehicleTypeRepository vehicleTypes;

    public AuthController(AuthService auth, VehicleTypeRepository vehicleTypes) {
        this.auth = auth;
        this.vehicleTypes = vehicleTypes;
    }

    @GetMapping("/login")
    public String passengerLoginPage(Model model) {
        model.addAttribute("form", new LoginForm());
        return "login";
    }

    @PostMapping("/login")
    public String passengerLogin(@Valid @ModelAttribute("form") LoginForm f,
                                 BindingResult binding,
                                 HttpServletRequest req,
                                 Model m) {
        if (binding.hasErrors()) {
            return "login";
        }
        return doLogin(Role.PASSENGER, f, req, m, "login", "/passenger");
    }

    @GetMapping("/register")
    public String passengerRegisterPage(Model model) {
        model.addAttribute("form", new RegisterForm());
        return "register";
    }

    @PostMapping("/register")
    public String passengerRegister(@Valid @ModelAttribute("form") RegisterForm f,
                                    BindingResult binding,
                                    HttpServletRequest req,
                                    Model m) {
        if (binding.hasErrors()) {
            return "register";
        }
        if (!f.getPassword().equals(f.getConfirmPassword())) {
            m.addAttribute("error", "Passwords do not match");
            return "register";
        }
        try {
            User u = auth.registerPassenger(f);
            HttpSession s = req.getSession(true);
            s.setAttribute("passengerUserId", u.getId());
            s.setAttribute("passengerUserName", u.getFullName());
            s.setAttribute("userId", u.getId());
            s.setAttribute("role", u.getRole().name());
            s.setAttribute("userName", u.getFullName());
            return "redirect:/passenger";
        } catch (AuthException e) {
            m.addAttribute("error", e.getMessage());
            return "register";
        }
    }

    @GetMapping("/driver/login")
    public String driverLoginPage(@RequestParam(value = "registered", required = false) Boolean registered,
                                  Model model) {
        model.addAttribute("form", new LoginForm());
        if (Boolean.TRUE.equals(registered)) {
            model.addAttribute("info", "Registration submitted successfully! Your account is pending admin approval.");
        }
        return "driver/login";
    }

    @PostMapping("/driver/login")
    public String driverLogin(@Valid @ModelAttribute("form") LoginForm f,
                              BindingResult binding,
                              HttpServletRequest req,
                              Model m) {
        if (binding.hasErrors()) {
            return "driver/login";
        }
        return doLogin(Role.DRIVER, f, req, m, "driver/login", "/driver");
    }

    @GetMapping("/driver/register")
    public String driverRegisterPage(Model model) {
        model.addAttribute("form", new DriverRegisterForm());
        model.addAttribute("types", vehicleTypes.findAll());
        return "driver/register";
    }

    @PostMapping("/driver/register")
    public String driverRegister(@Valid @ModelAttribute("form") DriverRegisterForm f,
                                 BindingResult binding,
                                 Model m) {
        m.addAttribute("types", vehicleTypes.findAll());
        if (binding.hasErrors()) {
            return "driver/register";
        }
        if (!f.getPassword().equals(f.getConfirmPassword())) {
            m.addAttribute("error", "Passwords do not match");
            return "driver/register";
        }
        try {
            auth.registerDriver(f);
            return "redirect:/driver/login?registered=true";
        } catch (AuthException e) {
            m.addAttribute("error", e.getMessage());
            return "driver/register";
        }
    }

    @GetMapping("/admin/login")
    public String adminLoginPage(Model model) {
        model.addAttribute("form", new LoginForm());
        return "admin/login";
    }

    @PostMapping("/admin/login")
    public String adminLogin(@Valid @ModelAttribute("form") LoginForm f,
                             BindingResult binding,
                             HttpServletRequest req,
                             Model m) {
        if (binding.hasErrors()) {
            return "admin/login";
        }
        return doLogin(Role.ADMIN, f, req, m, "admin/login", "/admin");
    }

    private String doLogin(Role role, LoginForm f, HttpServletRequest req,
                           Model m, String errorView, String okUrl) {
        try {
            User u = auth.login(role, f.getPhone(), f.getPassword());
            HttpSession s = req.getSession(true);
            if (role == Role.PASSENGER) {
                s.setAttribute("passengerUserId", u.getId());
                s.setAttribute("passengerUserName", u.getFullName());
            } else if (role == Role.DRIVER) {
                s.setAttribute("driverUserId", u.getId());
                s.setAttribute("driverUserName", u.getFullName());
            } else if (role == Role.ADMIN) {
                s.setAttribute("adminUserId", u.getId());
                s.setAttribute("adminUserName", u.getFullName());
            }
            s.setAttribute("userId", u.getId());
            s.setAttribute("role", u.getRole().name());
            s.setAttribute("userName", u.getFullName());
            return "redirect:" + okUrl;
        } catch (AuthException e) {
            m.addAttribute("error", e.getMessage());
            return errorView;
        }
    }

    @GetMapping("/logout")
    public String logoutGet(HttpSession session, HttpServletRequest req) {
        performLogout(session, req);
        return "redirect:/";
    }

    @PostMapping("/logout")
    public String logout(HttpSession session, HttpServletRequest req) {
        performLogout(session, req);
        return "redirect:/";
    }

    private void performLogout(HttpSession session, HttpServletRequest req) {
        if (session != null) {
            String referer = req.getHeader("Referer");
            if (referer != null && referer.contains("/admin")) {
                session.removeAttribute("adminUserId");
                session.removeAttribute("adminUserName");
            } else if (referer != null && referer.contains("/driver")) {
                session.removeAttribute("driverUserId");
                session.removeAttribute("driverUserName");
            } else if (referer != null && referer.contains("/passenger")) {
                session.removeAttribute("passengerUserId");
                session.removeAttribute("passengerUserName");
            } else {
                session.invalidate();
                return;
            }
            if (session.getAttribute("adminUserId") == null &&
                session.getAttribute("driverUserId") == null &&
                session.getAttribute("passengerUserId") == null) {
                session.invalidate();
            }
        }
    }
}
