package lk.gamana.service;

import lk.gamana.model.Role;
import lk.gamana.model.RideStatus;
import lk.gamana.model.User;
import lk.gamana.repository.DriverProfileRepository;
import lk.gamana.repository.RideRepository;
import lk.gamana.repository.UserRepository;
import lk.gamana.storage.FileStore;
import lk.gamana.util.PasswordUtil;
import org.springframework.stereotype.Service;

@Service
public class DriverAccountService {

    private final FileStore store;
    private final UserRepository users;
    private final DriverProfileRepository profiles;
    private final RideRepository rides;

    public DriverAccountService(
            FileStore store,
            UserRepository users,
            DriverProfileRepository profiles,
            RideRepository rides) {
        this.store = store;
        this.users = users;
        this.profiles = profiles;
        this.rides = rides;
    }

    public void deleteAccount(String driverId, String password) {
        store.withLock(() -> {
            User driver = users.findById(driverId)
                    .filter(user -> user.getRole() == Role.DRIVER)
                    .orElseThrow(() -> new IllegalArgumentException(
                            "Driver account not found."));

            if (password == null || password.isBlank()
                    || !PasswordUtil.matches(
                    password, driver.getPasswordHash())) {
                throw new IllegalArgumentException(
                        "Your current password is incorrect.");
            }

            if (rides.isBusy(driverId)
                    || rides.findActiveByPassenger(driverId).isPresent()) {
                throw new IllegalArgumentException(
                        "Finish or cancel your unfinished ride first.");
            }

            boolean online = profiles.findByUserId(driverId)
                    .map(profile -> profile.isOnline())
                    .orElse(false);

            if (online) {
                throw new IllegalArgumentException(
                        "Go offline before deleting your account.");
            }

            boolean hasOffer = rides.findByStatus(RideStatus.OFFERED)
                    .stream()
                    .anyMatch(ride ->
                            driverId.equals(ride.getOfferedTo()));

            if (hasOffer) {
                throw new IllegalArgumentException(
                        "Decline your outstanding ride offer, or refresh "
                                + "the dashboard after it expires.");
            }

            var vehicleRows = store.read("vehicles.txt");
            var profileRows = store.read("driver_profiles.txt");
            var userRows = store.read("users.txt");

            vehicleRows.removeIf(row ->
                    row.length > 1 && driverId.equals(row[1]));

            profileRows.removeIf(row ->
                    row.length > 0 && driverId.equals(row[0]));

            userRows.removeIf(row ->
                    row.length > 0 && driverId.equals(row[0]));

            store.write("vehicles.txt", vehicleRows);
            store.write("driver_profiles.txt", profileRows);
            store.write("users.txt", userRows);

            return null;
        });
    }
}