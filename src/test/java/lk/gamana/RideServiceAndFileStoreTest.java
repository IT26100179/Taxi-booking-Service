package lk.gamana;

import lk.gamana.dto.BookingForm;
import lk.gamana.dto.DriverRegisterForm;
import lk.gamana.dto.RegisterForm;
import lk.gamana.model.*;
import lk.gamana.repository.*;
import lk.gamana.service.AuthService;
import lk.gamana.service.FareService;
import lk.gamana.service.RideException;
import lk.gamana.service.RideService;
import lk.gamana.storage.FileStore;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

class RideServiceAndFileStoreTest {

    @TempDir
    Path tempDir;

    private FileStore fileStore;
    private UserRepository userRepository;
    private DriverProfileRepository profileRepository;
    private VehicleRepository vehicleRepository;
    private VehicleTypeRepository vehicleTypeRepository;
    private PromoRepository promoRepository;
    private RideRepository rideRepository;
    private FareService fareService;
    private AuthService authService;
    private RideService rideService;

    @BeforeEach
    void setup() throws IOException {
        fileStore = new FileStore(tempDir.toString());
        userRepository = new UserRepository(fileStore);
        profileRepository = new DriverProfileRepository(fileStore);
        vehicleRepository = new VehicleRepository(fileStore);
        vehicleTypeRepository = new VehicleTypeRepository(fileStore);
        promoRepository = new PromoRepository(fileStore);
        rideRepository = new RideRepository(fileStore);
        fareService = new FareService();
        authService = new AuthService(userRepository, profileRepository, vehicleRepository);
        rideService = new RideService(rideRepository, userRepository, profileRepository,
                vehicleRepository, vehicleTypeRepository, promoRepository, fareService, fileStore);

        // Seed vehicle types
        vehicleTypeRepository.save(new VehicleType(1, "BIKE", 50, 45));
        vehicleTypeRepository.save(new VehicleType(2, "TUK", 80, 65));
        vehicleTypeRepository.save(new VehicleType(3, "CAR", 150, 95));
        vehicleTypeRepository.save(new VehicleType(4, "VAN", 220, 120));
    }

    @Test
    void testPassengerAndDriverLifecycle() {
        // 1. Register Passenger
        RegisterForm pf = new RegisterForm();
        pf.setFullName("Nadeesha Perera");
        pf.setPhone("0771234567");
        pf.setEmail("nade@mail.com");
        pf.setPassword("Password123!");
        User passenger = authService.registerPassenger(pf);
        assertEquals(Role.PASSENGER, passenger.getRole());
        assertEquals(UserStatus.ACTIVE, passenger.getStatus());

        // 2. Register Driver
        DriverRegisterForm df = new DriverRegisterForm();
        df.setFullName("Kasun Silva");
        df.setPhone("0712345678");
        df.setEmail("kasun@mail.com");
        df.setPassword("Password123!");
        df.setNic("199912345678");
        df.setLicenseNo("B1234567");
        df.setTypeId(2); // TUK
        df.setPlateNo("WP KA-4821");
        df.setMakeModel("Bajaj RE");
        df.setColor("Yellow");
        User driver = authService.registerDriver(df);
        assertEquals(Role.DRIVER, driver.getRole());
        assertEquals(UserStatus.PENDING, driver.getStatus());

        // 3. Admin approves driver
        driver.setStatus(UserStatus.ACTIVE);
        userRepository.save(driver);

        // 4. Driver goes online at fort
        DriverProfile profile = profileRepository.findByUserId(driver.getId()).orElseThrow();
        profile.setOnline(true);
        profile.setCurrentPlace("fort");
        profileRepository.save(profile);

        // 5. Passenger books a ride from fort to bamba
        BookingForm bf = new BookingForm();
        bf.setPickup("fort");
        bf.setDrop("bamba");
        bf.setTypeId(2);
        Ride ride = rideService.book(passenger.getId(), bf);

        assertEquals(RideStatus.OFFERED, ride.getStatus());
        assertEquals(driver.getId(), ride.getOfferedTo());
        assertTrue(ride.getDistanceKm() > 0);
        assertTrue(ride.getFareEstimate() > 0);

        // 6. Driver accepts ride
        Ride accepted = rideService.accept(ride.getId(), driver.getId());
        assertEquals(RideStatus.ACCEPTED, accepted.getStatus());
        assertEquals(driver.getId(), accepted.getDriverId());

        // 7. Driver marks arrived
        Ride arrived = rideService.arrived(ride.getId(), driver.getId());
        assertEquals(RideStatus.ARRIVED, arrived.getStatus());

        // 8. Driver starts trip
        Ride onTrip = rideService.startTrip(ride.getId(), driver.getId());
        assertEquals(RideStatus.ONTRIP, onTrip.getStatus());

        // 9. Driver completes trip
        Ride completed = rideService.completeTrip(ride.getId(), driver.getId());
        assertEquals(RideStatus.COMPLETED, completed.getStatus());
        assertEquals("PAID", completed.getPaymentStatus());
        assertEquals(ride.getFareEstimate(), completed.getFareFinal());

        // 10. Passenger rates the ride
        rideService.rate(ride.getId(), passenger.getId(), 5, "Smooth and fast!");
        Ride rated = rideRepository.findById(ride.getId()).orElseThrow();
        assertEquals(5, rated.getRating());
        assertEquals("Smooth and fast!", rated.getRatingComment());

        // 11. Check driver earnings and rating
        assertEquals(5.0, rideService.calculateDriverRating(driver.getId()));
        double expectedEarnings = completed.getFareFinal() * 0.85;
        assertEquals(expectedEarnings, rideService.calculateDriverEarnings(driver.getId()), 0.001);
    }

    @Test
    void testDoubleBookingPrevention() {
        // Register and approve a driver who is online
        DriverRegisterForm df = new DriverRegisterForm();
        df.setFullName("Active Driver");
        df.setPhone("0719999999");
        df.setPassword("Password123!");
        df.setNic("199512345678");
        df.setLicenseNo("B9999999");
        df.setTypeId(1);
        df.setPlateNo("WP BIKE-1");
        df.setMakeModel("Honda CD70");
        df.setColor("Black");
        User driver = authService.registerDriver(df);
        driver.setStatus(UserStatus.ACTIVE);
        userRepository.save(driver);

        DriverProfile profile = profileRepository.findByUserId(driver.getId()).orElseThrow();
        profile.setOnline(true);
        profile.setCurrentPlace("fort");
        profileRepository.save(profile);

        // Register passenger
        RegisterForm pf = new RegisterForm();
        pf.setFullName("Test User");
        pf.setPhone("0779999999");
        pf.setPassword("Password123!");
        User passenger = authService.registerPassenger(pf);

        BookingForm bf = new BookingForm();
        bf.setPickup("fort");
        bf.setDrop("pettah");
        bf.setTypeId(1);
        Ride r = rideService.book(passenger.getId(), bf);
        assertEquals(RideStatus.OFFERED, r.getStatus());

        // Attempting to book again while ride is active must fail
        assertThrows(RideException.class, () -> rideService.book(passenger.getId(), bf));
    }

    @Test
    void testSamePickupAndDropRejected() {
        RegisterForm pf = new RegisterForm();
        pf.setFullName("Same Place User");
        pf.setPhone("0778888888");
        pf.setPassword("Password123!");
        User passenger = authService.registerPassenger(pf);

        BookingForm bf = new BookingForm();
        bf.setPickup("fort");
        bf.setDrop("fort");
        bf.setTypeId(1);

        assertThrows(RideException.class, () -> rideService.book(passenger.getId(), bf));
    }
}
