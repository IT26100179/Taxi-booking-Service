# 🚖 Gamana - Taxi & Cab Service Platform

Gamana is an enterprise-grade ride-hailing and fleet management platform developed with Java, Spring Boot, Thymeleaf, and pure File-Based Persistence (No external SQL/NoSQL database dependency).

---

## 👥 Team & Module Distribution

| Member ID | Role / Module | Assigned CRUD Operations | OOP Concepts Applied | File Storage Entity |
|---|---|---|---|---|
| **IT26100472** | Core Architecture & Data Engine | Generic FileStore CRUD Engine, Model serialization | Encapsulation, Information Hiding, Abstraction | `FileStore.java` (Atomic thread-safe store) |
| **IT26100311** | Authentication & Security | User Registration & Profile CRUD | Inheritance (`AuthException`), Polymorphism (`RoleInterceptor`), Hiding (SHA-256) | `data/users.txt` |
| **IT26100552** | Driver Management Portal | Driver Application & Vehicle Allocation CRUD | Encapsulation, Model Abstraction | `data/driver_profiles.txt`, `data/vehicles.txt` |
| **IT26100432** | Pricing Engine & Marketing | Fare Rate Configuration & Promo Code CRUD | Abstraction (`FareService`), Dynamic Polymorphism | `data/vehicle_types.txt`, `data/promos.txt` |
| **IT26101197** | Passenger Booking & Trip Lifecycle | Ride Booking, Real-time Tracking & Ride Cancellation CRUD | Encapsulation, State Machine Pattern, Inheritance | `data/rides.txt` |
| **IT26100179** | Admin Dashboard & System Integration | Driver Moderation (Approve/Suspend/Reinstate) & Audit Logs CRUD | Abstraction (`AuditService`), Information Hiding | `data/audit_logs.txt`, Platform Aggregates |

---

## 🎯 Architecture & Rubric Highlights

### 1. Functionality of Assigned CRUD Operations
- **Create**: User registration, driver onboarding, ride bookings, promo code creation, audit logging.
- **Read**: Dynamic driver filtering, passenger ride history, platform metrics, fare previews.
- **Update**: Ride lifecycle transitions (`REQUESTED -> OFFERED -> ACCEPTED -> ARRIVED -> ONTRIP -> COMPLETED`), driver online toggle, fare rate tuning.
- **Delete**: Ride cancellation with business rule validation, application rejection with feedback.

### 2. OOP Concepts
- **Encapsulation**: Private fields across all models with sanitized getters/setters.
- **Information Hiding**: Passwords hashed with salt via SHA-256; internal atomic `.tmp` file swaps hidden from service layer.
- **Inheritance**: Custom domain exceptions (`AuthException`, `RideException`) extending `RuntimeException`.
- **Polymorphism**: Spring `HandlerInterceptor` overriding in `RoleInterceptor`; generic `Supplier<T>` action locking.
- **Abstraction**: Repository pattern isolating controllers from low-level file I/O.

### 3. File Handling Implementation
- **Atomic File Writes**: Writes to `.tmp` first, then atomically replaces original via `Files.move` (zero file corruption).
- **Concurrency Control**: `ReentrantLock` ensures synchronized multithreaded read/write operations.
- **Storage**: Plain text files with pipe delimiters (`|`) encoded in `StandardCharsets.UTF_8`.

### 4. User Interface Design
- Reusable Thymeleaf fragments (`layout.html`), responsive mobile-friendly layouts, Leaflet map previews, and custom CSS styling.
