# 🚖 Taxi & Cab Service Booking Platform

A modern, responsive web application designed for passengers and drivers to easily book, track, and manage taxi rides. Developed as part of the academic coursework for **SE1020 - Object Oriented Programming (10% Grade Weightage)**.

---

## 📌 Project Overview

The **Taxi & Cab Service Booking Platform** is a web-based enterprise application built to streamline cab booking operations for passengers, drivers, and administrators. The platform delivers an intuitive booking experience, transparent fare estimation, dynamic driver assignment, and transaction record tracking.

The primary academic goal of this project is to showcase real-world implementation of **Object-Oriented Programming (OOP)** principles, robust **File Handling (`.txt` data persistence)**, and modern **Java Spring Boot Web Technologies** following clean architectural patterns.

---

## 🎯 Key Objectives & OOP Principles

### 1. Encapsulation
- Secure internal state management with `private` fields and validated public getters/setters across core entity models (`User`, `Passenger`, `Driver`, `Vehicle`, `Booking`, `Payment`, `Review`).
- Defensive copying and encapsulation of file read/write serialization logic inside model classes.

### 2. Inheritance
- **User Hierarchy**:
  - Base abstract class `User.java` (defines common properties: `id`, `name`, `email`, `phone`, `password`, `role`).
  - Specialized child classes: `Passenger.java` and `Driver.java` extend `User`.
- **Vehicle Hierarchy**:
  - Base abstract class `Vehicle.java` (defines `vehicleId`, `plateNumber`, `model`, `type`, `capacity`, `ratePerKm`).
  - Concrete vehicle classes: `Car.java`, `Van.java`, and `Bike.java` extend `Vehicle`.

### 3. Polymorphism
- **Dynamic Fare Calculation Engine**:
  - Common interface `FareCalculator.java` with method `calculateFare(double distanceKm)`.
  - Concrete polymorphic strategies: `CarFareCalculator`, `VanFareCalculator`, and `BikeFareCalculator`.
  - Injected via `FareCalculationService` to compute fares dynamically at runtime based on the selected vehicle type.

### 4. Abstraction & Architecture
- **Generic Data Access Layer**:
  - `FileRepository<T, ID>` interface defining universal CRUD contracts (`findAll`, `findById`, `save`, `update`, `deleteById`).
  - `AbstractFileRepository<T, ID>` abstract base class implementing thread-safe file I/O operations with `ReentrantReadWriteLock`.
  - Concrete file repositories: `PassengerFileRepository`, `DriverFileRepository`, `VehicleFileRepository`, `BookingFileRepository`, `PaymentFileRepository`, `ReviewFileRepository`.

---

## 🛠️ Technology Stack

| Layer | Technology | Description |
|---|---|---|
| **Programming Language** | Java (JDK 21) | Core OOP Language |
| **Backend Framework** | Spring Boot 3.3.4 (MVC) | REST Controllers, Service Layer, Dependency Injection |
| **View Template Engine** | Thymeleaf 3 | Server-Side HTML Rendering |
| **Frontend Styling** | HTML5, CSS3, Bootstrap 5 | Responsive UI & Modern Obsidian/Gold Theme |
| **Data Persistence** | Java File I/O (`data/*.txt`) | Delimited text files with concurrent read/write locks |
| **Build & Management** | Apache Maven | Dependency management & project lifecycle |
| **Version Control** | Git & GitHub | Collaborative feature-branch workflow |

---

## 📂 System Architecture & Folder Layout

```text
Taxi-booking-Service/
├── pom.xml                                    # Maven configuration & Spring Boot dependencies
├── data/                                      # Persistent storage text files
│   ├── passengers.txt                         # Passenger accounts & profiles
│   ├── drivers.txt                            # Registered drivers & status
│   ├── vehicles.txt                           # Fleet registry (Cars, Vans, Bikes)
│   ├── bookings.txt                           # Ride bookings & dispatch status
│   ├── payments.txt                           # Billing records & payment status
│   └── reviews.txt                            # Driver ratings & customer feedback
├── src/
│   ├── main/
│   │   ├── java/com/taxibooking/
│   │   │   ├── TaxiBookingApplication.java    # Spring Boot Main Entry Point
│   │   │   ├── model/                         # OOP Entities (User, Passenger, Driver, Vehicle, Car, Van, Bike, Booking, Payment, Review)
│   │   │   ├── service/                       # FareCalculator interface & polymorphic implementations
│   │   │   ├── repository/                    # Generic FileRepository & AbstractFileRepository implementations
│   │   │   └── controller/                    # Spring MVC Controllers (HomeController, AdminController)
│   │   └── resources/
│   │       ├── application.properties         # App & File storage properties
│   │       ├── static/
│   │       │   ├── css/style.css              # Custom modern dark/gold UI styling
│   │       │   └── js/main.js                 # Interactive fare calculator & dynamic scripts
│   │       └── templates/
│   │           ├── fragments/layout.html      # Shared Navigation Bar, Header & Footer
│   │           ├── index.html                 # Main Landing Page & Live Fare Estimator
│   │           └── admin/dashboard.html       # Central Admin Inspection Dashboard
├── .gitignore
└── README.md
```

---

## 👥 Project Team & Component Allocation (Group WE02)

- **Institution**: Sri Lanka Institute of Information Technology (SLIIT)
- **Course**: SE1020 - Object Oriented Programming
- **Academic Year / Semester**: Year 1 Semester 2 (Y1S2)
- **Group ID**: `WE02`
- **Assessment Weightage**: 10% Continuous Assessment

### Group Members & Component Matrix

| # | Student ID | Student Name | Assigned Component | Associated Files & Storage | Recommended Branch |
|---|---|---|---|---|---|
| 1 | **IT26100179** | **Gunasena G. T. S.** | Project Architecture & Core Setup | `TaxiBookingApplication`, `pom.xml`, Base Models | `main` |
| 2 | **IT26100432** | **Rajapaksha P. L.** | Passenger Management | `Passenger.java`, `PassengerFileRepository`, `data/passengers.txt` | `feature/passenger-management` |
| 3 | **IT26101197** | **Silva W. O. V.** | Driver Management | `Driver.java`, `DriverFileRepository`, `data/drivers.txt` | `feature/driver-management` |
| 4 | **IT26100311** | **Gamalath D. T.** | Vehicle & Fleet Management | `Vehicle.java`, `Car/Van/Bike`, `VehicleFileRepository`, `data/vehicles.txt` | `feature/vehicle-fleet` |
| 5 | **IT26100472** | **Sivathuwarani T.** | Ride Booking & Trip Management | `Booking.java`, `BookingFileRepository`, `data/bookings.txt` | `feature/ride-booking` |
| 6 | **IT26100552** | **Ahamad A. A.** | Fare Calculation & Payments | `FareCalculator`, `Payment.java`, `PaymentFileRepository`, `data/payments.txt` | `feature/fare-payments` |

---

## 🌿 Collaborative Git Branching Guidelines

All team members must follow this standard workflow to prevent merge conflicts:

### 1. Before Starting Work: Always Pull Latest `main`
```bash
git checkout main
git pull origin main
```

### 2. Create Your Feature Branch
```bash
# Example for Driver Management:
git checkout -b feature/driver-management
```

### 3. Make Changes and Commit
```bash
git add .
git commit -m "feat(driver): add driver registration and status update"
```

### 4. Push Branch to GitHub
```bash
git push -u origin feature/driver-management
```

### 5. Open a Pull Request (PR) on GitHub
- Go to `https://github.com/IT26100179/Taxi-booking-Service`
- Click **"Compare & pull request"**
- Set base to `main` and compare to your feature branch.
- Request a review and merge.

---

## 🚀 Running the Project Locally

### Prerequisites
- **Java 21 JDK** installed
- **Git**

### Steps
```bash
# 1. Clone the repository
git clone https://github.com/IT26100179/Taxi-booking-Service.git
cd Taxi-booking-Service

# 2. Run with Maven (or open in IntelliJ IDEA / Eclipse)
mvn spring-boot:run

# 3. Access in browser
# http://localhost:8080
```
