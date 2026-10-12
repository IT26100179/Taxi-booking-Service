package lk.gamana.controller;

import jakarta.servlet.http.HttpSession;
import lk.gamana.repository.VehicleTypeRepository;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class HomeController {

    private final VehicleTypeRepository vehicleTypes;

    public HomeController(VehicleTypeRepository vehicleTypes) {
        this.vehicleTypes = vehicleTypes;
    }

    @GetMapping({"/", "/landing"})
    public String index(HttpSession session, Model model) {
        model.addAttribute("vehicleTypes", vehicleTypes.findAll());
        return "index";
    }
}
