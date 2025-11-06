# 🛒 E-Commerce Microservices Project

A complete **E-Commerce System** built using **Spring Boot Microservices**, **Java 21**, **MySQL**, and a simple **HTML/CSS/JavaScript frontend**.
It demonstrates **service-to-service communication**, **centralized service discovery**, **secure payment integration (Razorpay)**, and **API Gateway routing**.

---

## ✨ Highlights

* Built with **Java 21**, **Spring Boot 3.5**, and **Spring Cloud 2025**
* Includes **Razorpay payment gateway** integration with backend verification
* Implements **Eureka Service Registry**, **Spring Boot Admin**, and **API Gateway**
* Clean, modular, and production-style **microservices architecture**

---

## ⚙️ Tech Stack

**Backend:**

* Java 21 + Spring Boot 3.5.7
* Spring Cloud (2025.0.0) – Eureka, Gateway, OpenFeign
* Spring Boot Admin (3.5.5)
* REST APIs with Spring Web & Validation
* MySQL (Database)
* Razorpay SDK (Payment Gateway Integration)

**Frontend:**

* HTML, CSS, JavaScript (Vanilla)

---

## 🧩 Microservices Overview

| Service          | Port                       | Description                                  |
| ---------------- | -------------------------- | -------------------------------------------- |
| Service Registry | 8761                       | Eureka Server – Service Discovery            |
| Admin            | 1111                       | Spring Boot Admin – Monitoring Dashboard     |
| API Gateway      | 333                        | Routes requests to backend services          |
| E-Commerce       | 8081                       | Handles Product, Cart, and Order management  |
| Payment Service  | 9091                       | Handles payment processing and notifications |
| Frontend         | 5500 (VS Code Live Server) | Static web UI                                |

---

## 🗄️ Database Setup

1. **Install MySQL** (or use XAMPP/WAMP).
2. Create databases:

   ```sql
   CREATE DATABASE ecomdb;
   CREATE DATABASE paymentDb;
   ```
3. Update each service’s `application.properties`:

   ```properties
   spring.datasource.url=jdbc:mysql://localhost:3306/ecomdb
   spring.datasource.username=root
   spring.datasource.password=YourPassword
   ```

> 💡 *Ensure MySQL is running before starting your backend services.*

---

## 🚀 How to Run (Without Docker)

1. **Start MySQL** and confirm both databases exist.
2. **Run microservices in this order** from STS or IntelliJ:

   * `Service_registry` (Eureka Server)
   * `Admin` (Spring Boot Admin)
   * `ApiGateway`
   * `E_commerce`
   * `PaymentService`
3. **Run Frontend:**

   * Open `FrontEnd/index.html` in VS Code.
   * Right-click → *Open with Live Server.*
   * App will open at something like `http://127.0.0.1:5500`.

✅ **Access the system:**

* **Eureka Dashboard:** [http://localhost:8761](http://localhost:8761)
* **Spring Boot Admin:** [http://localhost:1111](http://localhost:1111)
* **API Gateway:** [http://localhost:333](http://localhost:333)
* **Frontend UI:** [http://127.0.0.1:5500](http://127.0.0.1:5500)

---

## 💳 Razorpay Configuration

To test payment integration:

* Add your **Razorpay Test API Keys** in the `application.properties` of `PaymentService`:

  ```properties
  razorpay.key.id=rzp_test_xxxxxx
  razorpay.key.secret=your_secret_key
  ```

> Use Razorpay’s test mode for safe demo transactions.

---

## 🧠 Key Features

* Modular **Microservices Architecture**
* Centralized **Service Discovery (Eureka)**
* **API Gateway** Routing & Load Management
* **Razorpay Integration** with secure backend verification
* **Spring Boot Admin** for health monitoring
* **MySQL Persistence** via Spring Data JPA
* **Inter-Service Communication** using OpenFeign
* Clean RESTful design & best coding practices
* Responsive **Static Frontend (UI)**

---

## 👨‍💻 Author

**Chandan Dhabele**
📍 Maharashtra, India
💼 Java Developer Aspirant

---

⭐ *If you like this project, don’t forget to star the repo!*
