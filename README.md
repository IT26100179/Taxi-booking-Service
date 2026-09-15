# 🚖 Taxi & Cab Service Booking Platform

A modern, responsive web application designed for passengers and drivers to easily book, track, and manage taxi rides. Developed as part of the academic coursework for **SE1020 - Object Oriented Programming (10% Grade Weightage)**.

---

## 📌 Project Overview

The **Taxi & Cab Service Booking Platform** is a web-based application built to streamline cab booking operations for passengers, drivers, and administrators. The platform delivers an intuitive booking experience, transparent fare estimation, dynamic driver assignment, and transaction record tracking.

The primary academic goal of this project is to showcase real-world implementation of **Object-Oriented Programming (OOP)** principles, robust **Data Management / File Handling**, and **Java Web Technologies** following clean architectural patterns.

---

## 🎯 Key Objectives

### 1. Object-Oriented Programming (OOP) Principles
- **Encapsulation**:
  - Secure state management with private fields and validated getters/setters across core entity models (`User`, `Passenger`, `Driver`, `Vehicle`, `Booking`, `Payment`).
- **Inheritance**:
  - A comprehensive user hierarchy starting from a base `User` superclass, extended by specialized child classes such as `Passenger`, `Driver`, and `Admin`.
  - Vehicle hierarchy extending from a base `Vehicle` class into `Car`, `Van`, and `Bike`.
- **Polymorphism**:
  - Method overriding for dynamic fare calculation algorithms based on vehicle category and ride distance.
  - Polymorphic notification and payment processing mechanisms.
- **Abstraction**:
  - Abstract classes and interfaces (e.g., `BookingService`, `PaymentGateway`, `DataRepository`) to decouple business logic from underlying data storage mechanisms.

### 2. Data Management & CRUD Operations
- Robust implementation supporting either **File Handling** (`.txt` / `.dat` file streams) or a relational **MySQL Database**.
- At least three core CRUD modules:
  - 🚗 **Ride / Booking Management**: Create ride requests, view active rides, update trip status (Pending, Confirmed, Completed, Cancelled), and cancel bookings.
  - 👤 **User & Driver Profiles**: Register accounts, view profile details, update contact/vehicle information, and deactivate profiles.
  - 💳 **Payment & Transaction Logs**: Generate billing statements, view transaction history, process refunds/adjustments, and remove expired logs.

### 3. User-Friendly Interface (UI/UX)
- Responsive, clean, and accessible UI crafted with HTML5, modern CSS3 (Bootstrap / custom styling), and JavaScript.
- Dynamic input validation, responsive fare preview, interactive booking forms, and intuitive driver availability toggles.

### 4. Version Control & Collaborative Workflow
- Structured Git version control using clear commit conventions and feature-based branch management hosted on GitHub.

---

## 🛠️ Technology Stack

| Layer | Technology |
|---|---|
| **Backend** | Java (JDK 21), Java Servlets & JSP / Spring Boot |
| **Frontend** | HTML5, CSS3 (Bootstrap 5 / Modern CSS), JavaScript (ES6+) |
| **Data Persistence** | Java File I/O (`.txt` / serialization) or MySQL Database |
| **Build & Tooling** | Apache Maven / Gradle |
| **Version Control** | Git & GitHub |

---

## 📂 System Architecture

```text
Taxi-booking-Service/
├── src/
│   ├── main/
│   │   ├── java/com/taxibooking/
│   │   │   ├── model/         # User, Passenger, Driver, Vehicle, Booking, Payment
│   │   │   ├── service/       # Business logic & OOP Interfaces
│   │   │   ├── dao/           # Data Access Layer (File Handler / DB Repositories)
│   │   │   ├── util/          # Helpers, file paths, validators
│   │   │   └── controller/    # Servlets / Web controllers
│   │   └── webapp/ or resources/
│   │       ├── css/           # Modern stylesheets
│   │       ├── js/            # Client-side validation & scripts
│   │       └── views/         # JSP / HTML template pages
├── data/                      # Data storage files (.txt) for File Handling
├── .gitignore                 # Standard Java & IDE gitignore
└── README.md                  # Project documentation
```

---

## 🚀 Getting Started

### Prerequisites
- **Java Development Kit (JDK 21 or higher)**
- **Git**
- Modern Web Browser (Chrome, Firefox, Edge, Safari)

### Installation & Local Setup
```bash
# 1. Clone the repository
git clone https://github.com/IT26100179/Taxi-booking-Service.git

# 2. Navigate to the project directory
cd Taxi-booking-Service
```

---

## 👥 Project Team & Course Information

- **Institution**: Sri Lanka Institute of Information Technology (SLIIT)
- **Course**: SE1020 - Object Oriented Programming
- **Academic Year / Semester**: Year 1 Semester 2 (Y1S2)
- **Batch / Class**: `Y1S2.WE.IT.0101`
- **Group ID**: `WE02`
- **Assessment Weightage**: 10% Continuous Assessment

### Group Members (Group WE02)

| # | Student ID | Student Name | Group / Batch | Role / Contribution Area |
|---|---|---|---|---|
| 1 | **IT26100179** | **Gunasena G. T. S.** | `Y1S2.WE.IT.0101` (WE02) | Project Lead / Core Architecture |
| 2 | **IT26100432** | **Rajapaksha P. L.** | `Y1S2.WE.IT.0101` (WE02) | Group Member |
| 3 | **IT26101197** | **Silva W. O. V.** | `Y1S2.WE.IT.0101` (WE02) | Group Member |
| 4 | **IT26100311** | **Gamalath D. T.** | `Y1S2.WE.IT.0101` (WE02) | Group Member |
| 5 | **IT26100472** | **Sivathuwarani T.** | `Y1S2.WE.IT.0101` (WE02) | Group Member |
| 6 | **IT26100552** | **Ahamad A. A.** | `Y1S2.WE.IT.0101` (WE02) | Group Member |

