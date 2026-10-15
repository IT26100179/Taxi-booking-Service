package lk.gamana.security;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import lk.gamana.model.Role;
import org.springframework.web.servlet.HandlerInterceptor;

import java.io.IOException;

public class RoleInterceptor implements HandlerInterceptor {
    private final Role required;
    private final String loginUrl;

    public RoleInterceptor(Role required, String loginUrl) {
        this.required = required;
        this.loginUrl = loginUrl;
    }

    @Override
    public boolean preHandle(HttpServletRequest req, HttpServletResponse res, Object handler)
            throws IOException {
        HttpSession s = req.getSession(false);
        if (s == null) {
            res.sendRedirect(loginUrl);
            return false;
        }

        String specificId = switch (required) {
            case PASSENGER -> (String) s.getAttribute("passengerUserId");
            case DRIVER -> (String) s.getAttribute("driverUserId");
            case ADMIN -> (String) s.getAttribute("adminUserId");
        };

        if (specificId != null && !specificId.isBlank()) {
            return true;
        }

        // For passenger routes, any authenticated user (e.g. Admin or Driver testing rides) is allowed
        if (required == Role.PASSENGER) {
            String anyUser = (String) s.getAttribute("userId");
            if (anyUser == null || anyUser.isBlank()) {
                anyUser = (String) s.getAttribute("adminUserId");
            }
            if (anyUser == null || anyUser.isBlank()) {
                anyUser = (String) s.getAttribute("driverUserId");
            }
            if (anyUser != null && !anyUser.isBlank()) {
                s.setAttribute("passengerUserId", anyUser);
                return true;
            }
        }

        String role = (String) s.getAttribute("role");
        if (role != null && role.equals(required.name())) {
            String uid = (String) s.getAttribute("userId");
            if (uid != null && !uid.isBlank()) {
                return true;
            }
        }

        res.sendRedirect(loginUrl);
        return false;
    }
}
