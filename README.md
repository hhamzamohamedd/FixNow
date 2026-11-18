#  FixNow – Smart Home Services Platform

<p align="center">
  <img src="https://img.shields.io/badge/Status-Stable%20Demo%20Release-green?style=for-the-badge"/>
  <img src="https://img.shields.io/badge/Version-v1.0--STABLE-blue?style=for-the-badge"/>
  <img src="https://img.shields.io/badge/Architecture-Layered%20MVC%20%2B%20Security-blue?style=for-the-badge"/>
  <img src="https://img.shields.io/badge/Type-Spring%20Boot%20Web%20App-success?style=for-the-badge"/>
</p>

<p align="center">
  <img src="https://img.shields.io/badge/Java-Backend-orange?style=for-the-badge&logo=openjdk&logoColor=white"/>
  <img src="https://img.shields.io/badge/Spring%20Boot-Core-green?style=for-the-badge&logo=springboot&logoColor=white"/>
  <img src="https://img.shields.io/badge/Spring%20MVC-Web%20Layer-blue?style=for-the-badge"/>
  <img src="https://img.shields.io/badge/Spring%20Security-Session%20Based-red?style=for-the-badge"/>
  <img src="https://img.shields.io/badge/Spring%20Data%20JPA-ORM-blue?style=for-the-badge"/>
  <img src="https://img.shields.io/badge/Thymeleaf-View%20Engine-orange?style=for-the-badge"/>
  <img src="https://img.shields.io/badge/H2-In%20Memory-lightgrey?style=for-the-badge"/>
  <img src="https://img.shields.io/badge/Maven-Build-red?style=for-the-badge&logo=apachemaven&logoColor=white"/>
</p>

---

## 📌 Overview

**FixNow** is a full-stack Spring Boot web application designed to connect customers with service technicians for home maintenance tasks such as:

- Plumbing services  
- Electrical repairs  
- Air conditioning maintenance  
- Locksmith services  

The system simulates a real-world service marketplace with role-based access, request dispatching, and real-time communication.

---

## ⚠️ Release Status

This is the **Stable Demo Release (v1.0-STABLE)**.

The system is fully functional for demonstration purposes and academic evaluation.

### ✔️ Stable Features
- Complete layered architecture (Controller → Service → Repository)
- Role-based system (Customer / Technician / Admin)
- Session-based authentication (Spring Security)
- Service request lifecycle (Create → Assign → Accept → Complete)
- Technician matching system
- Chat system between users
- Review & rating system
- Timeline tracking for requests
- Global exception handling
- DTO-based request/response design
- Thymeleaf server-side rendering

---

##  System Architecture

The project follows a **strict layered MVC architecture with separation of concerns**:

### 🔹 Presentation Layer
- Thymeleaf Templates
- HTML/CSS/JS static assets

### 🔹 Controller Layer
Handles HTTP requests and routing:
- AuthController
- CustomerController
- TechnicianController
- AdminController
- ChatController
- ErrorPageController

---

### 🔹 Service Layer (Business Logic)
Core system logic:
- AuthService
- CustomerService
- TechnicianService
- DispatchService
- TechnicianMatchingService
- ChatService
- RequestTimelineService

---

### 🔹 Repository Layer (Data Access)
Spring Data JPA repositories:
- UserRepo
- TechnicianRepo
- ServiceRequestRepo
- MessageRepo
- ReviewRepo

---

### 🔹 Model Layer (Entities)
Database entities:
- User
- Technician
- ServiceRequest
- Message
- Review
- Role (Enum)

---

### 🔹 Security Layer
- Session-based authentication
- Custom filter: `SessionAuthenticationFilter`
- Security configuration via `SecurityConfig`
- Session constants management

---

### 🔹 DTO Layer
Used to separate API/data transfer logic:
- LoginDTO
- RegisterDTO
- ServiceRequestDTO
- MessageDTO
- ReviewDTO
- UserDTO

---

### 🔹 Exception Handling
- GlobalExceptionHandler
- BadRequestException
- ResourceNotFoundException

---

## 🔄 System Flow

Client → Controller → Service → Repository → Database → Response → View

---

##  Tech Stack

- Java 17+
- Spring Boot
- Spring MVC
- Spring Security (Session-based auth)
- Spring Data JPA (Hibernate ORM)
- Thymeleaf
- H2 Database (demo mode)
- Maven
- HTML / CSS / JavaScript

---
---

## 🔐 Security Model

The system implements a robust security layer tailored for session-managed applications:

### 🔹 Authentication & Authorization
- **Session-Based Auth:** Managed via `SecurityConfig` and `SessionConstants`.
- **RBAC (Role-Based Access Control):** Strict separation between `CUSTOMER`, `TECHNICIAN`, and `ADMIN`.
- **Custom Filter:** `SessionAuthenticationFilter` validates sessions before reaching controllers.

### 🔹 Protection & Routing
- **Secure Route Mapping:** Role-specific access to private dashboards.
- **Error Handling:** Unauthorized attempts are redirected to custom templates (`403.html`, `404.html`, `500.html`).

---

## ⚙️ Core Features Explained

### 🔹 Service Request Lifecycle
The `ServiceRequest` entity follows a managed state machine:
- **Creation:** Customers submit service details via `createRequest.html`.
- **Matching:** `TechnicianMatchingService` identifies the best professional for the task.
- **Acceptance:** Technicians manage their workflow via `technicianDashboard.html`.
- **Completion:** Finalized status triggers the automated review & rating system.

### 🔹 Chat & Messaging System
- **Persistence:** Messages are stored in H2, ensuring persistent history for both parties.
- **Data Integrity:** Handled via `MessageDTO` to isolate the UI from database entities.

### 🔹 Customer AI Assistant
- A chat widget on the customer dashboard answers home-repair and FixNow questions.
- The assistant can explain the signed-in customer's request statuses; only up to 20 recent request IDs and statuses are sent as context.
- Gemini is called from the backend. The API key is never sent to the browser, and recent chat turns are kept in the signed-in session.
- The assistant is not a substitute for a qualified technician and will not provide step-by-step guidance for dangerous repair work.

To enable Gemini, create an API key in [Google AI Studio](https://aistudio.google.com/app/apikey) and set it in the server environment before starting the app:

```powershell
$env:GEMINI_API_KEY = "your-key"
.\mvnw.cmd spring-boot:run
```

The default model is `gemini-3.8-flash`; override it with `GEMINI_MODEL` if needed. The assistant allows 10 messages per customer per minute. The dashboard and rest of the app still start without an API key, but assistant replies return a clear configuration error until one is set. Never commit the key or paste it into the frontend.

### 🔹 Persistent Accounts and Data
- Normal and `dev` runs use the file-backed H2 database at `data/fixnow.mv.db`.
- Database schema updates are applied without dropping existing tables or accounts when the app stops.
- The `data/` folder is local and ignored by Git; back it up to preserve your accounts and service data.
- Automated tests continue to use a separate in-memory database.

### 🔹 Request Timeline
Implemented via `RequestTimelineService`, providing a chronological audit trail of every status change for transparency.

---

## 🛠️ Design Principles

The project strictly adheres to enterprise-level software patterns:

- **Layered Architecture:** Clear boundaries between all layers.
- **DTO Pattern:** Ensures no "Entity Leakage" by isolating models from the view layer.
- **Stateless Controllers:** Controllers handle routing; all logic is delegated to Services.
- **Global Exception Handling:** `GlobalExceptionHandler` ensures consistent error responses.

---

## 🔄 System Flow

`Client` → `Controller` → `Service` → `Repository` → `Database` → `Response` → `View`

---

## 🚀 Demo Readiness & Roadmap

### ✔️ Validated Scenarios (Stable)
- Multi-role login and registration flows.
- Full request lifecycle (Create → Assign → Accept → Complete).
- Interactive chat and review submission.

### 📈 Future Improvements
- **WebSockets:** Upgrade chat from polling to real-time communication.
- **Payments:** Integration with Stripe for automated service billing.
- **Cloud Migration:** Native profiles for AWS/Azure and PostgreSQL support.

---

## 🏁 Summary

**FixNow** is a comprehensive demonstration of a **Spring Boot "Service Marketplace"**. It showcases how to build a scalable, secure, and maintainable backend using **Java 17**, **Spring Data JPA**, and **Thymeleaf**, maintaining a professional and clean code structure throughout.

## 📂 Project Structure
```text
src/main/java/com/fix/fixnow
├── config
├── controller
├── dto
├── exception
├── model
├── repository
├── security
├── service
└── FixnowApplication.java

src/main/resources
├── application.properties
├── application-dev.properties

src/main/resources/templates
├── login.html
├── register.html
├── customerDashboard.html
├── technicianDashboard.html
├── createRequest.html
├── requestDetails.html
├── addReview.html
├── adminDashboard.html
└── error/
    ├── 403.html
    ├── 404.html
    └── 500.html

src/main/resources/static
├── css/fixnow-theme.css
└── js/fixnow-ui.js 
