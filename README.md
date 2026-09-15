# 🚖 Taxi & Cab Service Booking Platform

A modern, responsive web application designed for passengers and drivers to easily book, track, and manage taxi rides. Developed as part of the academic coursework for **SE1020 - Object Oriented Programming (10% Grade Weightage)**.

---

## 📌 Project Overview (ව්යාපෘති විස්තරය)

**Taxi & Cab Service Booking Platform** යනු මගීන්ට (Passengers) සහ රියදුරන්ට (Drivers) පහසුවෙන් ටැක්සි රථ වෙන් කරගැනීමට සහ කළමනාකරණය කිරීමට නිර්මාණය කරන ලද වෙබ් යෙදුමකි (Web Application).

මෙහි ප්රධාන අරමුණ වන්නේ **Object-Oriented Programming (OOP)** සංකල්ප, **Data Management / File Handling**, සහ **Java Web Technologies** භාවිතයෙන් ප්රායෝගික, කාර්යක්ෂම සහ පරිශීලක හිතකාමී පද්ධතියක් ගොඩනැගීමයි.

---

## 🎯 Key Objectives (ප්රධාන අරමුණු)

1. **OOP Concepts Integration (වස්තු-නැඹුරු වැඩසටහන්කරණ සංකල්ප)**:
   - **Encapsulation (ගුලිගත කිරීම)**: Private data fields with getters/setters in entities (User, Driver, Passenger, Booking, Payment, Vehicle).
   - **Inheritance (උරුමවීම)**: `User` base class inherited by `Passenger`, `Driver`, and `Admin`.
   - **Polymorphism (බහුරූපතාව)**: Method overriding for customized fare calculations, notification services, and payment processors.
   - **Abstraction (වියුක්තකරණය)**: Abstract classes and Interfaces for services (e.g., `IBookingService`, `IPaymentGateway`, `VehicleFactory`).

2. **Data Management & CRUD Operations (දත්ත කළමනාකරණය)**:
   - File Read/Write (e.g., `.txt` / `.dat` file handling) or MySQL Database integration.
   - Implementation of at least 3 core CRUD operations:
     - 🚗 Ride/Booking Management (Create, View, Update status, Cancel)
     - 👤 User & Driver Profiles (Register, View profile, Update details, Deactivate)
     - 💳 Payment & Billing Records (Record transaction, View invoice, Refund/Update, Delete)

3. **User-Friendly Interface (පරිශීලක හිතකාමී අතුරුමුහුණත)**:
   - Responsive, modern UI using HTML5, CSS3 (Bootstrap / Custom CSS), and JavaScript.
   - Interactive booking form, ride status tracker, and driver availability dashboard.

4. **Team Collaboration & Version Control**:
   - Version control using Git and GitHub with structured commit history and clear branching strategy.

---

## 🛠️ Technology Stack (තාක්ෂණික මෙවලම්)

| Component | Technology |
|---|---|
| **Backend** | Java (JDK 21), Spring Boot / Java Servlets & JSP |
| **Frontend** | HTML5, CSS3 (Bootstrap / Modern CSS), JavaScript |
| **Data Storage** | File Handling (`.txt` / `.json`) or MySQL Database |
| **Version Control** | Git & GitHub |
| **Build Tool** | Maven / Gradle |

---

## 📂 Project Architecture (Planned)

```text
Taxi-booking-Service/
├── src/
│   ├── main/
│   │   ├── java/com/taxibooking/
│   │   │   ├── model/         # User, Passenger, Driver, Vehicle, Booking, Payment
│   │   │   ├── service/       # Business logic & OOP Abstractions / Interfaces
│   │   │   ├── dao/           # Data Access (File Handler / Database Repository)
│   │   │   └── controller/    # Web endpoints / Servlets
│   │   └── webapp/ or resources/
│   │       ├── css/           # Styling sheets
│   │       ├── js/            # Client-side validation & interactivity
│   │       └── views/         # HTML / JSP templates
├── data/                      # Data storage files (.txt) if using File Handling
├── .gitignore
└── README.md
```

---

## 🚀 Getting Started

### Prerequisites
- **Java Development Kit (JDK 21+)**
- **Git**
- Web Browser (Chrome, Firefox, Edge)

### Setup & Run
```bash
# Clone the repository
git clone https://github.com/thisarana-s/Taxi-booking-Service.git

# Navigate to project folder
cd Taxi-booking-Service
```

---

## 👥 Contributors & Course Info
- **Course**: SE1020 - Object Oriented Programming
- **Weightage**: 10% Continuous Assessment
- **Author / Developer**: thisarana-s
