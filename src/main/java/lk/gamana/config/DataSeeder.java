package lk.gamana.config;

import lk.gamana.model.Promo;
import lk.gamana.model.Role;
import lk.gamana.model.User;
import lk.gamana.model.UserStatus;
import lk.gamana.model.VehicleType;
import lk.gamana.repository.PromoRepository;
import lk.gamana.repository.UserRepository;
import lk.gamana.repository.VehicleTypeRepository;
import lk.gamana.util.PasswordUtil;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Component
public class DataSeeder implements CommandLineRunner {

    private final UserRepository users;
    private final VehicleTypeRepository vehicleTypes;
    private final PromoRepository promos;

    @Value("${app.admin.phone}")
    private String adminPhone;

    @Value("${app.admin.email}")
    private String adminEmail;

    @Value("${app.admin.password}")
    private String adminPassword;

    public DataSeeder(UserRepository users, VehicleTypeRepository vehicleTypes, PromoRepository promos) {
        this.users = users;
        this.vehicleTypes = vehicleTypes;
        this.promos = promos;
    }

    @Override
    public void run(String... args) {
        if (users.findByRoleAndPhone(Role.ADMIN, adminPhone).isEmpty()) {
            users.save(new User(
                    users.nextId(),
                    Role.ADMIN,
                    "Super Admin",
                    adminPhone,
                    adminEmail,
                    PasswordUtil.hash(adminPassword),
                    UserStatus.ACTIVE,
                    0,
                    0L,
                    LocalDateTime.now()
            ));
        }

        if (vehicleTypes.findAll().isEmpty()) {
            vehicleTypes.save(new VehicleType(1, "BIKE", 50, 45));
            vehicleTypes.save(new VehicleType(2, "TUK", 80, 65));
            vehicleTypes.save(new VehicleType(3, "CAR", 150, 95));
            vehicleTypes.save(new VehicleType(4, "VAN", 220, 120));
        }

        if (promos.findAll().isEmpty()) {
            promos.save(new Promo("UOC10", 10, LocalDate.now().plusYears(1), 100, 0));
        }
    }
}
