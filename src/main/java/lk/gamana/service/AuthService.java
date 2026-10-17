package lk.gamana.service;

import lk.gamana.dto.DriverRegisterForm;
import lk.gamana.dto.RegisterForm;
import lk.gamana.model.*;
import lk.gamana.repository.DriverProfileRepository;
import lk.gamana.repository.UserRepository;
import lk.gamana.repository.VehicleRepository;
import lk.gamana.storage.FileStore;
import lk.gamana.util.PasswordUtil;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class AuthService {
    private final UserRepository users;
    private final DriverProfileRepository profiles;
    private final VehicleRepository vehicles;

    public AuthService(UserRepository users, DriverProfileRepository profiles, VehicleRepository vehicles) {
        this.users = users;
        this.profiles = profiles;
        this.vehicles = vehicles;
    }

    public User registerPassenger(RegisterForm f) {
        String phone = FileStore.clean(f.getPhone());
        String email = FileStore.clean(f.getEmail());
        if (users.exists(Role.PASSENGER, phone, email)) {
            throw new AuthException("Phone or email is already registered");
        }

        User u = new User(
                users.nextId(),
                Role.PASSENGER,
                FileStore.clean(f.getFullName()),
                phone,
                email,
                PasswordUtil.hash(f.getPassword()),
                UserStatus.ACTIVE,
                0,
                0L,
                LocalDateTime.now()
        );
        users.save(u);
        return u;
    }

    public User registerDriver(DriverRegisterForm f) {
        String phone = FileStore.clean(f.getPhone());
        String email = FileStore.clean(f.getEmail());
        if (users.exists(Role.DRIVER, phone, email)) {
            throw new AuthException("Phone or email is already registered");
        }
        if (vehicles.existsByPlate(FileStore.clean(f.getPlateNo()))) {
            throw new AuthException("This plate number is already registered");
        }

        User u = new User(
                users.nextId(),
                Role.DRIVER,
                FileStore.clean(f.getFullName()),
                phone,
                email,
                PasswordUtil.hash(f.getPassword()),
                UserStatus.PENDING,
                0,
                0L,
                LocalDateTime.now()
        );
        users.save(u);
        profiles.save(new DriverProfile(
                u.getId(),
                FileStore.clean(f.getNic()),
                FileStore.clean(f.getLicenseNo()),
                false,
                "fort",
                ""
        ));
        vehicles.save(new Vehicle(
                vehicles.nextId(),
                u.getId(),
                f.getTypeId(),
                FileStore.clean(f.getPlateNo()),
                FileStore.clean(f.getMakeModel()),
                FileStore.clean(f.getColor())
        ));
        return u;
    }

    public User login(Role role, String phone, String password) {
        AuthException generic = new AuthException("Invalid phone or password");
        User u = users.findByRoleAndPhone(role, FileStore.clean(phone)).orElseThrow(() -> generic);

        long now = System.currentTimeMillis();
        if (u.getLockedUntil() > now) {
            long remainingMins = Math.max(1, (u.getLockedUntil() - now) / 60000L);
            throw new AuthException("Account is temporarily locked. Try again in " + remainingMins + " minute(s).");
        }

        if (!PasswordUtil.matches(password, u.getPasswordHash())) {
            u.setFailedAttempts(u.getFailedAttempts() + 1);
            if (u.getFailedAttempts() >= 5) {
                u.setLockedUntil(now + 15 * 60 * 1000L);
                u.setFailedAttempts(0);
            }
            users.save(u);
            throw generic;
        }

        if (u.getStatus() == UserStatus.SUSPENDED) {
            throw new AuthException("This account is suspended. Contact support.");
        }
        if (u.getStatus() == UserStatus.REJECTED) {
            String note = profiles.findByUserId(u.getId()).map(DriverProfile::getRejectionNote).orElse("");
            if (!note.isBlank()) {
                throw new AuthException("Account registration was rejected: " + note);
            }
            throw new AuthException("This account is not active. Contact support.");
        }

        u.setFailedAttempts(0);
        u.setLockedUntil(0L);
        users.save(u);
        return u;
    }
}
