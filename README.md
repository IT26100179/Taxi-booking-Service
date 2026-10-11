## True Taxi & Cab service 

A simple ride hailing web application (Uber / PickMe style) built for a university project. It has three separate roles, each with its own login:

- Passenger: registers, logs in, books a ride, cancels, rates the driver, sees ride history.
- Driver: registers, waits for admin approval, goes online, accepts or declines ride requests, completes trips.
- Admin: logs in through a separate admin panel, approves drivers, manages fare rates and promo codes, monitors rides.

The project is intentionally basic. It uses only the languages and storage required by the assignment.

---

## 1. Languages, technologies and project rules

| Area | What is used |
|---|---|
| Backend | Java 17, Spring Boot 3 (Spring Web MVC) |
| Pages | HTML5 and CSS3, written as Thymeleaf templates (Thymeleaf is the HTML template engine that comes with Spring Boot, so pages are plain HTML with a few `th:` attributes) |
| UI enhancement | JavaScript (form validation, fare preview, confirm dialogs, show/hide password) |
| Styling | Bootstrap 5 through a CDN link (allowed by the assignment, optional) |
| Data storage | Plain `.txt` files, read and written with Java (`java.nio.file`) |
| Login | Normal `HttpSession` (server side session) |
| Build tool | Maven |

Rules for this project:

- No database. All data is stored in `.txt` files.
- No JWT, no WebSockets, no Socket.IO, no background tasks. The server only answers normal page requests.
- No live GPS. A driver chooses the area where they are from a list, and the system uses that area to find the nearest driver.
- A passenger or driver sees the latest status by pressing the Refresh button (or reloading the page).
- No extra libraries. Passwords are hashed with Java's built-in `MessageDigest` (SHA-256 with a random salt).

---

## 2. Roles and access rules

| Role | Sign-up | Login URL | Dashboard | Notes |
|---|---|---|---|---|
| Passenger | Public | `/login` | `/passenger` | Active immediately after registration |
| Driver | Public | `/driver/login` | `/driver` | Status is `PENDING` until an admin approves |
| Admin | None (created automatically on first start) | `/admin/login` | `/admin` | Separate login page, no public link to it |

Rules:

1. A passenger can never open driver or admin pages, a driver can never open passenger or admin pages, and so on. This is enforced on the server by an interceptor, not only by hiding menu items.
2. Each login page accepts only its own role. A driver phone number typed on the passenger login returns the same message as a wrong password.
3. There is no public sign-up for admins.

---

## 3. Project structure

```text
gamana/
  pom.xml
  .gitignore                     # contains: data/
  data/                          # created automatically, all .txt storage
    users.txt
    driver_profiles.txt
    vehicles.txt
    vehicle_types.txt
    promos.txt
    rides.txt
    audit.txt
  src/main/java/lk/gamana/
    GamanaApplication.java
    config/
      WebConfig.java             # registers the role interceptors
      DataSeeder.java            # first admin, vehicle types, sample promo
    storage/
      FileStore.java             # low level read/write with a lock
    util/
      PasswordUtil.java          # salted SHA-256
      Places.java                # fixed list of places with coordinates
    model/
      User.java  Role.java  UserStatus.java
      DriverProfile.java  Vehicle.java  VehicleType.java  Promo.java
      Ride.java  RideStatus.java
    repository/
      UserRepository.java  DriverProfileRepository.java  VehicleRepository.java
      VehicleTypeRepository.java  PromoRepository.java  RideRepository.java
    service/
      AuthService.java
      RideService.java           # booking, matching, state changes
      FareService.java
      AuditService.java
    security/
      RoleInterceptor.java
    controller/
      AuthController.java
      PassengerController.java
      DriverController.java
      AdminController.java
  src/main/resources/
    application.properties
    templates/
      login.html  register.html
      driver/login.html  driver/register.html  driver/dashboard.html
      passenger/dashboard.html  passenger/history.html
      admin/login.html  admin/dashboard.html  admin/drivers.html
      admin/rides.html  admin/rates.html  admin/promos.html
      fragments/layout.html      # navbar and footer
    static/
      css/app.css
      js/booking.js  js/validation.js
```

---

## 4. Data storage design (.txt files)

### 4.1 File format

One record per line, fields separated by the pipe character `|`. Before saving, every text typed by a user is cleaned: any `|` or line break is replaced with a space, so a record can never break the file format.

`users.txt`

```text
id|role|fullName|phone|email|passwordHash|status|failedAttempts|lockedUntilMs|createdAt
u-1001|ADMIN|Super Admin|0770000000|admin@gamana.lk|c2FsdA==:aGFzaA==|ACTIVE|0|0|2026-10-10T08:00:00
u-1002|PASSENGER|Nadeesha Perera|0771234567|nade@mail.com|...|ACTIVE|0|0|2026-10-10T09:15:00
u-1003|DRIVER|Kasun Silva|0712345678|kasun@mail.com|...|PENDING|0|0|2026-10-10T09:20:00
```

`driver_profiles.txt`

```text
userId|nicNumber|licenseNo|online|currentPlace|rejectionNote
u-1003|199912345678|B1234567|false|fort|
```

`vehicles.txt`

```text
id|driverId|typeId|plateNo|makeModel|color
v-1|u-1003|2|WP KA-4821|Bajaj RE|Yellow
```

`vehicle_types.txt`

```text
id|name|baseFare|perKmRate
1|BIKE|50|45
2|TUK|80|65
3|CAR|150|95
4|VAN|220|120
```

`promos.txt`

```text
code|discountPct|validTo|usageLimit|usedCount
UOC10|10|2026-12-31|100|0
```

`rides.txt` (payment and rating are stored inside the ride record to keep the project small)

```text
id|passengerId|driverId|typeId|promoCode|pickupPlace|dropPlace|status|distanceKm|fareEstimate|fareFinal|offeredTo|rejectedDrivers|offerDeadlineMs|requestedAt|completedAt|paymentStatus|rating|ratingComment
r-1|u-1002|u-1003|2|UOC10|uoc|bamba|COMPLETED|3.2|290|290|u-1003|u-1005|0|2026-10-10T10:00:00|2026-10-10T10:18:00|PAID|5|Good ride
```

`audit.txt`

```text
timestamp|adminId|action|target
2026-10-10T09:30:00|u-1001|APPROVE_DRIVER|u-1003
```

### 4.2 Places (no GPS, no map service)

```java
package lk.gamana.util;

import java.util.LinkedHashMap;
import java.util.Map;

public final class Places {
    public record Place(String key, String name, double lat, double lng) {}

    public static final Map<String, Place> ALL = new LinkedHashMap<>();
    static {
        add("fort",     "Colombo Fort",           6.9344, 79.8428);
        add("pettah",   "Pettah",                 6.9366, 79.8510);
        add("gface",    "Galle Face",             6.9271, 79.8450);
        add("maradana", "Maradana",               6.9290, 79.8650);
        add("koll",     "Kollupitiya",            6.9100, 79.8500);
        add("uoc",      "University of Colombo",  6.9022, 79.8611);
        add("borella",  "Borella",                6.9147, 79.8776);
        add("rajag",    "Rajagiriya",             6.9100, 79.8950);
        add("bamba",    "Bambalapitiya",          6.8896, 79.8567);
        add("havelock", "Havelock Town",          6.8860, 79.8650);
        add("wella",    "Wellawatte",             6.8740, 79.8600);
        add("nugeg",    "Nugegoda",               6.8649, 79.8997);
        add("dehi",     "Dehiwala",               6.8511, 79.8650);
    }
    private static void add(String k, String n, double lat, double lng) {
        ALL.put(k, new Place(k, n, lat, lng));
    }
    private Places() {}
}
```

### 4.3 FileStore (shared read and write helper)

All repositories use this class. One lock makes every read-modify-write safe when several requests arrive together. Writing goes to a temporary file first and then replaces the real file, so a crash in the middle of a write cannot corrupt the data.

```java
package lk.gamana.storage;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.*;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Supplier;

@Component
public class FileStore {
    private final Path dir;
    private final ReentrantLock lock = new ReentrantLock();

    public FileStore(@Value("${app.data-dir}") String dataDir) throws IOException {
        this.dir = Path.of(dataDir);
        Files.createDirectories(dir);
    }

    /** Run a block so only one thread reads and writes the files at a time. */
    public <T> T withLock(Supplier<T> action) {
        lock.lock();
        try { return action.get(); } finally { lock.unlock(); }
    }

    public List<String[]> read(String file) {
        Path p = dir.resolve(file);
        List<String[]> rows = new ArrayList<>();
        try {
            if (!Files.exists(p)) return rows;
            for (String line : Files.readAllLines(p, StandardCharsets.UTF_8)) {
                if (!line.isBlank()) rows.add(line.split("\\|", -1));
            }
            return rows;
        } catch (IOException e) { throw new UncheckedIOException(e); }
    }

    public void write(String file, List<String[]> rows) {
        Path target = dir.resolve(file);
        Path tmp = dir.resolve(file + ".tmp");
        try {
            List<String> lines = rows.stream().map(r -> String.join("|", r)).toList();
            Files.write(tmp, lines, StandardCharsets.UTF_8);
            Files.move(tmp, target, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) { throw new UncheckedIOException(e); }
    }

    /** Remove characters that would break the file format. */
    public static String clean(String s) {
        return s == null ? "" : s.replace("|", " ").replace("\r", " ").replace("\n", " ").trim();
    }
}
```

### 4.4 Repository example (users)

Every other repository follows the same pattern.

```java
@Repository
public class UserRepository {
    private static final String FILE = "users.txt";
    private final FileStore store;
    public UserRepository(FileStore store) { this.store = store; }

    public List<User> findAll() {
        return store.read(FILE).stream().map(User::fromRow).toList();
    }
    public Optional<User> findById(String id) {
        return findAll().stream().filter(u -> u.getId().equals(id)).findFirst();
    }
    public Optional<User> findByRoleAndPhone(Role role, String phone) {
        return findAll().stream()
            .filter(u -> u.getRole() == role && u.getPhone().equals(phone)).findFirst();
    }
    public boolean exists(Role role, String phone, String email) {
        return findAll().stream().anyMatch(u -> u.getRole() == role &&
            (u.getPhone().equals(phone) || (!email.isBlank() && u.getEmail().equalsIgnoreCase(email))));
    }

    /** Insert a new user, or replace the line that has the same id. */
    public void save(User user) {
        store.withLock(() -> {
            List<String[]> rows = store.read(FILE);
            boolean replaced = false;
            for (int i = 0; i < rows.size(); i++) {
                if (rows.get(i)[0].equals(user.getId())) { rows.set(i, user.toRow()); replaced = true; }
            }
            if (!replaced) rows.add(user.toRow());
            store.write(FILE, rows);
            return null;
        });
    }

    public String nextId() {
        int max = findAll().stream()
            .mapToInt(u -> Integer.parseInt(u.getId().substring(2))).max().orElse(1000);
        return "u-" + (max + 1);
    }
}
```

`User` is a plain Java class with `fromRow(String[])` and `toRow()` that convert between a line and an object. Use `String.valueOf(...)` for numbers and `LocalDateTime.toString()` / `LocalDateTime.parse(...)` for dates.

---

## 5. Authentication design (basic)

### 5.1 Passwords

```java
package lk.gamana.util;

import java.nio.charset.StandardCharsets;
import java.security.*;
import java.util.Base64;

public final class PasswordUtil {
    /** Returns "salt:hash" (both Base64). */
    public static String hash(String password) {
        byte[] salt = new byte[16];
        new SecureRandom().nextBytes(salt);
        return Base64.getEncoder().encodeToString(salt) + ":" + sha256(salt, password);
    }

    public static boolean matches(String password, String stored) {
        String[] parts = stored.split(":", 2);
        byte[] salt = Base64.getDecoder().decode(parts[0]);
        return MessageDigest.isEqual(
            sha256(salt, password).getBytes(StandardCharsets.UTF_8),
            parts[1].getBytes(StandardCharsets.UTF_8));
    }

    private static String sha256(byte[] salt, String password) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            md.update(salt);
            return Base64.getEncoder().encodeToString(md.digest(password.getBytes(StandardCharsets.UTF_8)));
        } catch (NoSuchAlgorithmException e) { throw new IllegalStateException(e); }
    }
    private PasswordUtil() {}
}
```

A salted SHA-256 hash is enough for this assignment. (In a real product, use BCrypt or Argon2.)

### 5.2 Session rules

- After a successful login the server stores `userId` and `role` in the `HttpSession`.
- The session id is renewed at login (`request.changeSessionId()`).
- Session timeout is 30 minutes. Logout calls `session.invalidate()`.
- The login error is always the same: "Invalid phone or password".
- 5 wrong attempts lock the account for 15 minutes (`failedAttempts` and `lockedUntilMs` in `users.txt`).

### 5.3 AuthService

```java
@Service
public class AuthService {
    private final UserRepository users;
    private final DriverProfileRepository profiles;
    private final VehicleRepository vehicles;
    // constructor omitted

    public User registerPassenger(RegisterForm f) {
        String phone = FileStore.clean(f.getPhone());
        String email = FileStore.clean(f.getEmail());
        if (users.exists(Role.PASSENGER, phone, email))
            throw new AuthException("Phone or email is already registered");

        User u = new User(users.nextId(), Role.PASSENGER, FileStore.clean(f.getFullName()),
            phone, email, PasswordUtil.hash(f.getPassword()), UserStatus.ACTIVE, 0, 0L, LocalDateTime.now());
        users.save(u);
        return u;
    }

    public User registerDriver(DriverRegisterForm f) {
        String phone = FileStore.clean(f.getPhone());
        String email = FileStore.clean(f.getEmail());
        if (users.exists(Role.DRIVER, phone, email))
            throw new AuthException("Phone or email is already registered");
        if (vehicles.existsByPlate(FileStore.clean(f.getPlateNo())))
            throw new AuthException("This plate number is already registered");

        User u = new User(users.nextId(), Role.DRIVER, FileStore.clean(f.getFullName()),
            phone, email, PasswordUtil.hash(f.getPassword()), UserStatus.PENDING, 0, 0L, LocalDateTime.now());
        users.save(u);
        profiles.save(new DriverProfile(u.getId(), FileStore.clean(f.getNic()),
            FileStore.clean(f.getLicenseNo()), false, "fort", ""));
        vehicles.save(new Vehicle(vehicles.nextId(), u.getId(), f.getTypeId(),
            FileStore.clean(f.getPlateNo()), FileStore.clean(f.getMakeModel()), FileStore.clean(f.getColor())));
        return u;
    }

    public User login(Role role, String phone, String password) {
        AuthException generic = new AuthException("Invalid phone or password");
        User u = users.findByRoleAndPhone(role, FileStore.clean(phone)).orElseThrow(() -> generic);

        long now = System.currentTimeMillis();
        if (u.getLockedUntil() > now)
            throw new AuthException("Too many attempts. Try again in 15 minutes.");

        if (!PasswordUtil.matches(password, u.getPasswordHash())) {
            u.setFailedAttempts(u.getFailedAttempts() + 1);
            if (u.getFailedAttempts() >= 5) {
                u.setLockedUntil(now + 15 * 60 * 1000L);
                u.setFailedAttempts(0);
            }
            users.save(u);
            throw generic;
        }
        if (u.getStatus() == UserStatus.SUSPENDED || u.getStatus() == UserStatus.REJECTED)
            throw new AuthException("This account is not active. Contact support.");

        u.setFailedAttempts(0);
        u.setLockedUntil(0L);
        users.save(u);
        return u;   // a PENDING driver can log in, but cannot go online
    }
}
```

### 5.4 Login controller (one page per role)

```java
@Controller
public class AuthController {
    private final AuthService auth;
    public AuthController(AuthService auth) { this.auth = auth; }

    @GetMapping("/login")        public String passengerLoginPage() { return "login"; }
    @GetMapping("/driver/login") public String driverLoginPage()    { return "driver/login"; }
    @GetMapping("/admin/login")  public String adminLoginPage()     { return "admin/login"; }

    @PostMapping("/login")
    public String passengerLogin(LoginForm f, HttpServletRequest req, Model m) {
        return doLogin(Role.PASSENGER, f, req, m, "login", "/passenger");
    }
    @PostMapping("/driver/login")
    public String driverLogin(LoginForm f, HttpServletRequest req, Model m) {
        return doLogin(Role.DRIVER, f, req, m, "driver/login", "/driver");
    }
    @PostMapping("/admin/login")
    public String adminLogin(LoginForm f, HttpServletRequest req, Model m) {
        return doLogin(Role.ADMIN, f, req, m, "admin/login", "/admin");
    }

    private String doLogin(Role role, LoginForm f, HttpServletRequest req,
                           Model m, String errorView, String okUrl) {
        try {
            User u = auth.login(role, f.getPhone(), f.getPassword());
            req.getSession(true);
            req.changeSessionId();                     // new session id after login
            HttpSession s = req.getSession();
            s.setAttribute("userId", u.getId());
            s.setAttribute("role", u.getRole().name());
            return "redirect:" + okUrl;
        } catch (AuthException e) {
            m.addAttribute("error", e.getMessage());
            return errorView;
        }
    }

    @PostMapping("/logout")
    public String logout(HttpSession session) {
        session.invalidate();
        return "redirect:/login";
    }
}
```

### 5.5 Role guard (interceptor)

```java
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
        String role = (s == null) ? null : (String) s.getAttribute("role");
        if (role == null || !role.equals(required.name())) {
            res.sendRedirect(loginUrl);
            return false;
        }
        return true;
    }
}
```

```java
@Configuration
public class WebConfig implements WebMvcConfigurer {
    @Override
    public void addInterceptors(InterceptorRegistry reg) {
        reg.addInterceptor(new RoleInterceptor(Role.ADMIN, "/admin/login"))
           .addPathPatterns("/admin/**").excludePathPatterns("/admin/login");

        reg.addInterceptor(new RoleInterceptor(Role.DRIVER, "/driver/login"))
           .addPathPatterns("/driver/**")
           .excludePathPatterns("/driver/login", "/driver/register");

        reg.addInterceptor(new RoleInterceptor(Role.PASSENGER, "/login"))
           .addPathPatterns("/passenger/**");
    }
}
```

Every controller method that shows or changes data takes the user id from the session, never from the request. For example, ride history uses `session.getAttribute("userId")`, so a passenger cannot read another passenger's rides.

### 5.6 Login page example (HTML + Bootstrap)

```html
<!doctype html>
<html lang="en" xmlns:th="http://www.thymeleaf.org">
<head>
  <meta charset="utf-8">
  <meta name="viewport" content="width=device-width, initial-scale=1">
  <title>Passenger login - Gamana</title>
  <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/css/bootstrap.min.css" rel="stylesheet">
</head>
<body class="bg-light">
  <main class="container py-5" style="max-width: 420px">
    <h1 class="h3 mb-3">Passenger login</h1>
    <div th:if="${error}" class="alert alert-danger" th:text="${error}"></div>
    <form method="post" th:action="@{/login}" id="loginForm">
      <div class="mb-3">
        <label class="form-label" for="phone">Phone number</label>
        <input class="form-control" id="phone" name="phone" required pattern="0[0-9]{9}">
      </div>
      <div class="mb-3">
        <label class="form-label" for="password">Password</label>
        <input class="form-control" id="password" name="password" type="password" required minlength="8">
        <div class="form-check mt-2">
          <input class="form-check-input" type="checkbox" id="showPw">
          <label class="form-check-label" for="showPw">Show password</label>
        </div>
      </div>
      <button class="btn btn-success w-100">Log in</button>
    </form>
    <p class="mt-3 mb-0">New here? <a th:href="@{/register}">Create a passenger account</a></p>
    <p class="mb-0">Driving with us? <a th:href="@{/driver/login}">Driver login</a></p>
  </main>
  <script th:src="@{/js/validation.js}"></script>
</body>
</html>
```

The public pages link only to the passenger and driver logins. There is no link to `/admin/login`.

---

## 6. Scenarios

### 6.1 Passenger scenarios

| ID | Scenario | Expected behaviour |
|---|---|---|
| P1 | Register with valid name, phone, password | Account saved as `ACTIVE`, user is logged in and lands on the booking page |
| P2 | Register with a phone or email that already exists | Error "Phone or email is already registered" |
| P3 | Register with a weak password or invalid phone | Validation message under the field |
| P4 | Log in with correct details | Session created, redirected to `/passenger` |
| P5 | Log in with wrong password | Generic error. After 5 failures the account locks for 15 minutes |
| P6 | Passenger tries to open `/driver` or `/admin` | Redirected to the login page |
| P7 | Session expires | Next request redirects to login. After login the active ride is shown again |
| P8 | Book a ride (pickup, drop, vehicle type, optional promo) | Fare estimate shown, ride saved as `REQUESTED`, system finds the nearest driver |
| P9 | Invalid or expired promo code | Message "Promo code not recognised", price unchanged |
| P10 | Pickup and drop are the same | Booking is rejected with a message |
| P11 | Passenger already has an active ride | New booking is blocked |
| P12 | No driver available for the chosen vehicle type | Ride becomes `NODRIVER`, passenger can try again or choose another type |
| P13 | Driver accepts | Driver name, plate and vehicle shown after the passenger presses Refresh |
| P14 | Cancel before the trip starts | Ride `CANCELLED`, driver becomes free |
| P15 | Trip completed | Final fare shown, payment recorded as cash `PAID`, rating form shown |
| P16 | Rate the driver (1 to 5 and a comment) | Saved in the ride record, one rating per ride |
| P17 | View ride history | Only the logged in passenger's rides |
| P18 | Log out | Session invalidated |

### 6.2 Driver scenarios

| ID | Scenario | Expected behaviour |
|---|---|---|
| D1 | Register with personal details, NIC, licence number and vehicle | Status `PENDING` |
| D2 | Log in while `PENDING` | Dashboard shows "Waiting for admin approval", Go online is disabled |
| D3 | Admin approves | Status `ACTIVE`, Go online works after the next page load |
| D4 | Admin rejects | Status `REJECTED` with a reason shown to the driver on login |
| D5 | Log in while `SUSPENDED` | Error "This account is not active" |
| D6 | Go online and choose current area | `online=true` and `currentPlace` saved |
| D7 | Change area while online | `currentPlace` updated, used for the next matching |
| D8 | Go offline | `online=false`. Not allowed during an active trip |
| D9 | A ride is offered | Offer card appears on the dashboard after Refresh, with route, distance and earnings |
| D10 | Accept the offer | Ride `ACCEPTED`, driver is busy |
| D11 | Decline the offer, or let it expire (60 seconds) | Offer moves to the next nearest driver |
| D12 | Press Arrived, then Start trip | Statuses `ARRIVED`, then `ONTRIP` |
| D13 | Press Complete trip | Status `COMPLETED`, earnings updated, driver is free again |
| D14 | Two drivers accept the same ride | Only the first succeeds. The other sees "This ride is no longer available" |
| D15 | Driver closes the browser during a trip | After login the dashboard shows the active ride again |
| D16 | Driver tries to open passenger or admin pages | Redirected to the login page |

### 6.3 Admin scenarios

| ID | Scenario | Expected behaviour |
|---|---|---|
| A1 | Open `/admin/login` | Separate page, not linked from the public site |
| A2 | Log in with the seeded admin account | Dashboard with statistics |
| A3 | Passenger or driver credentials on the admin login | Generic "Invalid phone or password" |
| A4 | Review a pending driver | See details, approve or reject with a note |
| A5 | Suspend or reinstate a driver | Not allowed while the driver is on a trip. A suspended driver is set offline |
| A6 | Change fare rates | Applies to new estimates only. Existing rides keep their saved fare |
| A7 | Create a promo code | Code, percentage, expiry date, usage limit |
| A8 | Monitor rides | Table with filter by status |
| A9 | Reports | Total rides, completed rides, revenue, platform commission (15%), online drivers |
| A10 | Every admin action | One line appended to `audit.txt` |

### 6.4 System scenarios

- Offer expiry without a background task: whenever a passenger page, driver dashboard, accept or decline is opened, the service first checks for offers past their deadline and passes them to the next driver. If no driver is left, the ride becomes `NODRIVER`.
- Application restart: all state is in the `.txt` files, so everything continues after a restart.
- Double click on "Request ride": blocked by the active ride check (P11).
- Missing data file: `FileStore.read` returns an empty list and the seeder recreates the admin and vehicle types.
- Concurrent requests: all writes go through `FileStore.withLock`.
- User types `|` or a line break in a text field: it is replaced with a space before saving.

---

## 7. Core implementation notes

### 7.1 Fare calculation

```java
@Service
public class FareService {
    /** Straight line distance between two places, times 1.3 as a road factor. */
    public double distanceKm(Places.Place a, Places.Place b) {
        double r = 6371.0;
        double dLat = Math.toRadians(b.lat() - a.lat()), dLng = Math.toRadians(b.lng() - a.lng());
        double h = Math.sin(dLat / 2) * Math.sin(dLat / 2)
                 + Math.cos(Math.toRadians(a.lat())) * Math.cos(Math.toRadians(b.lat()))
                 * Math.sin(dLng / 2) * Math.sin(dLng / 2);
        return 2 * r * Math.asin(Math.sqrt(h)) * 1.3;
    }

    public long fare(VehicleType t, double km, int discountPct) {
        double gross = t.getBaseFare() + km * t.getPerKmRate();
        return Math.round(gross * (100 - discountPct) / 100.0 / 10.0) * 10;
    }
}
```

### 7.2 Booking and matching

```java
public Ride book(String passengerId, BookingForm f) {
    return store.withLock(() -> {
        if (rides.findActiveByPassenger(passengerId).isPresent())
            throw new RideException("You already have an active ride");
        if (f.getPickup().equals(f.getDrop()))
            throw new RideException("Pickup and drop-off must be different");

        VehicleType type = types.findById(f.getTypeId()).orElseThrow();
        int disc = promos.discountFor(f.getPromo());               // 0 if invalid or expired
        double km = fares.distanceKm(Places.ALL.get(f.getPickup()), Places.ALL.get(f.getDrop()));
        long fare = fares.fare(type, km, disc);

        Ride r = new Ride(rides.nextId(), passengerId, f.getTypeId(), f.getPromo(),
            f.getPickup(), f.getDrop(), km, fare);
        dispatch(r);                                                // sets OFFERED or NODRIVER
        rides.save(r);
        return r;
    });
}

private void dispatch(Ride r) {
    Optional<String> next = nextDriver(r);
    if (next.isEmpty()) { r.setStatus(RideStatus.NODRIVER); r.setOfferedTo(""); return; }
    r.setStatus(RideStatus.OFFERED);
    r.setOfferedTo(next.get());
    r.setOfferDeadline(System.currentTimeMillis() + 60_000);
}

private Optional<String> nextDriver(Ride r) {
    Places.Place pickup = Places.ALL.get(r.getPickupPlace());
    return profiles.findOnline().stream()
        .filter(p -> users.findById(p.getUserId()).map(u -> u.getStatus() == UserStatus.ACTIVE).orElse(false))
        .filter(p -> !rides.isBusy(p.getUserId()))
        .filter(p -> vehicles.findByDriver(p.getUserId())
                        .map(v -> v.getTypeId() == r.getTypeId()).orElse(false))
        .filter(p -> !r.getRejectedDrivers().contains(p.getUserId()))
        .min(Comparator.comparingDouble(p ->
            fares.distanceKm(Places.ALL.get(p.getCurrentPlace()), pickup)))
        .map(DriverProfile::getUserId);
}
```

Flow:

1. Passenger books. Ride is saved as `REQUESTED`, then `dispatch` runs.
2. `nextDriver` picks the nearest online, approved, free driver with the right vehicle type who has not rejected this ride.
3. Ride becomes `OFFERED` with `offeredTo` and a 60 second deadline.
4. The driver accepts, or declines, or the deadline passes. Decline and expiry add the driver to `rejectedDrivers` and run `dispatch` again.
5. If nobody is left, the status becomes `NODRIVER`.

### 7.3 Expire old offers (no background task)

```java
public void expireOverdueOffers() {
    store.withLock(() -> {
        long now = System.currentTimeMillis();
        for (Ride r : rides.findByStatus(RideStatus.OFFERED)) {
            if (r.getOfferDeadline() < now) {
                r.addRejected(r.getOfferedTo());
                dispatch(r);
                rides.save(r);
            }
        }
        return null;
    });
}
```

Call `rideService.expireOverdueOffers()` at the start of the passenger dashboard, driver dashboard, accept and decline handlers.

### 7.4 Prevent double accept

```java
public Ride accept(String rideId, String driverId) {
    return store.withLock(() -> {
        expireOverdueOffers();
        Ride r = rides.findById(rideId).orElseThrow(() -> new RideException("Ride not found"));
        if (r.getStatus() != RideStatus.OFFERED || !driverId.equals(r.getOfferedTo()))
            throw new RideException("This ride is no longer available");
        if (rides.isBusy(driverId))
            throw new RideException("You already have an active ride");
        r.setStatus(RideStatus.ACCEPTED);
        r.setDriverId(driverId);
        rides.save(r);
        return r;
    });
}
```

The check and the write happen inside one lock, so two drivers cannot both win.

### 7.5 Ride state machine

```text
REQUESTED -> OFFERED -> ACCEPTED -> ARRIVED -> ONTRIP -> COMPLETED
     |           |          |          |
     +-----------+----------+----------+--> CANCELLED
OFFERED -> NODRIVER
```

```java
private static final Map<RideStatus, Set<RideStatus>> ALLOWED = Map.of(
    RideStatus.REQUESTED, Set.of(RideStatus.OFFERED, RideStatus.NODRIVER, RideStatus.CANCELLED),
    RideStatus.OFFERED,   Set.of(RideStatus.OFFERED, RideStatus.ACCEPTED, RideStatus.NODRIVER, RideStatus.CANCELLED),
    RideStatus.ACCEPTED,  Set.of(RideStatus.ARRIVED, RideStatus.CANCELLED),
    RideStatus.ARRIVED,   Set.of(RideStatus.ONTRIP, RideStatus.CANCELLED),
    RideStatus.ONTRIP,    Set.of(RideStatus.COMPLETED)
);

private void move(Ride r, RideStatus next) {
    if (!ALLOWED.getOrDefault(r.getStatus(), Set.of()).contains(next))
        throw new RideException("Invalid status change: " + r.getStatus() + " to " + next);
    r.setStatus(next);
}
```

On `COMPLETED`: set `fareFinal = fareEstimate`, `paymentStatus = PAID`, `completedAt = now`, and increase the promo `usedCount` if a promo was used.

### 7.6 Driver earnings and ratings (calculated, not stored)

- Earnings = sum of `fareFinal` of the driver's completed rides, times 0.85 (15% platform commission).
- Driver rating = average of `rating` over the driver's rated rides.

### 7.7 JavaScript for UI enhancement

The page never talks to the server in the background. JavaScript only improves the forms.

`static/js/booking.js` shows the fare for every vehicle type while the passenger changes the pickup and drop-off. The page prints the places and rates into `data-` attributes, and JavaScript reads them:

```html
<!-- inside passenger/dashboard.html -->
<select id="pickup" name="pickup" class="form-select">
  <option th:each="p : ${places}" th:value="${p.key}" th:text="${p.name}"
          th:attr="data-lat=${p.lat},data-lng=${p.lng}"></option>
</select>
<select id="drop" name="drop" class="form-select"> ...same options... </select>

<div class="row g-2 mt-2">
  <div class="col-6" th:each="t : ${types}">
    <label class="border rounded p-2 d-block">
      <input type="radio" name="typeId" th:value="${t.id}" required>
      <b th:text="${t.name}"></b>
      <span class="fare" th:attr="data-base=${t.baseFare},data-km=${t.perKmRate}">-</span>
    </label>
  </div>
</div>
```

```javascript
// static/js/booking.js
function roadKm(a, b) {
  const R = 6371, rad = d => d * Math.PI / 180;
  const dLat = rad(b.lat - a.lat), dLng = rad(b.lng - a.lng);
  const h = Math.sin(dLat / 2) ** 2 +
            Math.cos(rad(a.lat)) * Math.cos(rad(b.lat)) * Math.sin(dLng / 2) ** 2;
  return 2 * R * Math.asin(Math.sqrt(h)) * 1.3;
}

function place(select) {
  const o = select.options[select.selectedIndex];
  return { lat: parseFloat(o.dataset.lat), lng: parseFloat(o.dataset.lng) };
}

function updateFares() {
  const pickup = document.getElementById('pickup');
  const drop = document.getElementById('drop');
  const btn = document.getElementById('requestBtn');
  const same = pickup.value === drop.value;
  btn.disabled = same;
  document.getElementById('sameMsg').hidden = !same;

  const km = roadKm(place(pickup), place(drop));
  document.querySelectorAll('.fare').forEach(el => {
    const fare = Number(el.dataset.base) + km * Number(el.dataset.km);
    el.textContent = same ? '-' : 'Rs ' + (Math.round(fare / 10) * 10);
  });
}

document.getElementById('pickup').addEventListener('change', updateFares);
document.getElementById('drop').addEventListener('change', updateFares);
updateFares();

// confirm before cancelling
document.querySelectorAll('form.confirm').forEach(f =>
  f.addEventListener('submit', e => { if (!confirm('Cancel this ride?')) e.preventDefault(); }));
```

The server calculates the real fare again when the form is submitted. The JavaScript preview is only a convenience.

`static/js/validation.js`: show or hide the password, check that the phone number matches `0xxxxxxxxx`, check password length (at least 8) and that "password" and "confirm password" match before submitting.

Status pages have a normal Refresh link (`<a th:href="@{/passenger}">Refresh status</a>`). Optionally add `<meta http-equiv="refresh" content="15">` to the active ride page. It is plain HTML and needs no server push.

---

## 8. Pages and routes

All routes are normal page requests and form posts.

| Method | URL | Role | Purpose |
|---|---|---|---|
| GET, POST | `/login`, `/register` | public | Passenger login and sign-up |
| GET, POST | `/driver/login`, `/driver/register` | public | Driver login and sign-up |
| GET, POST | `/admin/login` | public | Admin login |
| POST | `/logout` | any | End session |
| GET | `/passenger` | passenger | Booking form, or the active ride with Refresh |
| POST | `/passenger/book` | passenger | Request a ride |
| POST | `/passenger/rides/{id}/cancel` | passenger | Cancel a ride |
| POST | `/passenger/rides/{id}/rate` | passenger | Rate the driver |
| GET | `/passenger/history` | passenger | Ride history |
| GET | `/driver` | driver | Dashboard (status, offer, active ride, earnings) |
| POST | `/driver/online`, `/driver/offline` | driver | Availability (with chosen area) |
| POST | `/driver/area` | driver | Change current area |
| POST | `/driver/rides/{id}/accept`, `/decline` | driver | Respond to an offer |
| POST | `/driver/rides/{id}/arrived`, `/start`, `/complete` | driver | Trip steps |
| GET | `/admin` | admin | Dashboard statistics |
| GET | `/admin/drivers` | admin | Driver list (filter by status) |
| POST | `/admin/drivers/{id}/approve`, `/reject`, `/suspend`, `/reinstate` | admin | Driver management |
| GET | `/admin/rides` | admin | All rides (filter by status) |
| GET, POST | `/admin/rates` | admin | View and change fare rates |
| GET, POST | `/admin/promos` | admin | List and create promo codes |

---

## 9. Security checklist (basic level)

- Never store or show plain passwords. Never show `passwordHash`.
- Validate every form on the server with Bean Validation (`@NotBlank`, `@Pattern`, `@Size`) in addition to the browser checks. Run `FileStore.clean` on every text field before saving.
- Thymeleaf escapes output by default. Always use `th:text`, never `th:utext`, for user text.
- Check ownership in every action: a passenger can only cancel or rate their own rides, a driver can only act on rides offered or assigned to them.
- Use the user id from the session, never an id sent by the browser.
- Session cookie: `HttpOnly` and `SameSite=Strict`.
- Never commit `data/` or real passwords to Git (`.gitignore`). Change the default admin password after the first login.
- Write every admin action to `audit.txt`.

---

## 10. Setup and run

### 10.1 Requirements

- JDK 17 or newer
- Maven 3.9 or newer

### 10.2 `pom.xml` dependencies

```xml
<dependencies>
  <dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-web</artifactId>
  </dependency>
  <dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-thymeleaf</artifactId>
  </dependency>
  <dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-validation</artifactId>
  </dependency>
  <dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-test</artifactId>
    <scope>test</scope>
  </dependency>
</dependencies>
```

### 10.3 `application.properties`

```properties
server.port=8080
app.data-dir=./data
app.admin.phone=0770000000
app.admin.email=admin@gamana.lk
app.admin.password=ChangeMe123!

server.servlet.session.timeout=30m
server.servlet.session.cookie.http-only=true
server.servlet.session.cookie.same-site=strict
spring.thymeleaf.cache=false
```

### 10.4 First start seeder

```java
@Component
public class DataSeeder implements CommandLineRunner {
    // inject UserRepository, VehicleTypeRepository, PromoRepository
    // and the app.admin.* values with @Value

    @Override
    public void run(String... args) {
        if (users.findByRoleAndPhone(Role.ADMIN, adminPhone).isEmpty()) {
            users.save(new User(users.nextId(), Role.ADMIN, "Super Admin", adminPhone, adminEmail,
                PasswordUtil.hash(adminPassword), UserStatus.ACTIVE, 0, 0L, LocalDateTime.now()));
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
```

### 10.5 Run

```bash
git clone <your-repo-url> gamana
cd gamana
mvn spring-boot:run
```

- Passenger: `http://localhost:8080/login`
- Driver: `http://localhost:8080/driver/login`
- Admin: `http://localhost:8080/admin/login` (phone and password from `application.properties`)

The `data/` folder and all `.txt` files are created automatically on the first start.

---

## 11. Manual test checklist

1. Register a passenger, log out, log in again.
2. Register a driver, log in, confirm Go online is disabled.
3. Open `/admin/login`, approve the driver. Check that `audit.txt` has a new line.
4. Driver refreshes the dashboard, chooses an area and goes online.
5. Passenger books a ride with the same vehicle type. Driver presses Refresh, sees the offer and accepts.
6. Passenger presses Refresh and sees the driver details.
7. Driver presses Arrived, Start trip, Complete trip. Passenger rates the driver.
8. Admin sees the ride, revenue and rating. Open `rides.txt` and confirm the record is correct.
9. Negative tests:
   - wrong password five times (account locks)
   - passenger session opening `/admin` and `/driver` (redirected to login)
   - duplicate phone number registration
   - two drivers accepting the same ride (second one fails)
   - same pickup and drop-off
   - no online driver for the chosen vehicle type (`NODRIVER`)
   - invalid promo code
   - text containing `|` in a name field (file must not break)
   - stop and restart the application during a trip (ride is still there)
   - decline an offer, then check it moves to the next driver

Optional JUnit tests: `PasswordUtil` (hash and match), `FareService`, ride state machine, `FileStore` (write, read, two threads writing together).

---

## 12. Development plan

| Phase | Work |
|---|---|
| 1 | Project setup, `FileStore`, models, repositories, seeder |
| 2 | Registration, three login pages, session, interceptors, base layout with Bootstrap |
| 3 | Admin driver approval, fare rates and promo pages |
| 4 | Booking, fare estimate, matching, state machine, offer expiry |
| 5 | Driver dashboard, passenger status page, ratings, ride history |
| 6 | Admin reports, audit log, validation, JavaScript enhancements, testing, report writing |

---

## 13. Known limitations

- Text file storage reads the whole file on every operation. It is fine for a prototype with hundreds of records, not for thousands.
- There are no transactions or foreign key checks, so the code checks that referenced ids exist.
- There is no real GPS or map. Locations are a fixed list of places.
- Passengers and drivers update their screens with the Refresh button, so there are no live updates.
- Salted SHA-256 is acceptable for a university project. A real product should use BCrypt or Argon2.
- Payments are cash only.