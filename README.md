# 🔗 URL Shortener

A production-oriented URL Shortener built with **Java 17**, **Spring Boot**, **Redis**, and **MySQL**. The application generates compact short URLs, performs fast redirections using Redis caching, tracks analytics, and includes IP-based rate limiting. The project is containerized with Docker and deployed using modern cloud services.

---

## ✨ Features

* Shorten long URLs
* Redirect using unique short codes
* Multiple short-code generation strategies

  * Base62
  * Random
  * UUID
  * Hash
* Redis Cache (Cache-Aside Pattern)
* Redis-based IP Rate Limiting
* URL Analytics

  * Click Count
  * Last Accessed Timestamp
* Duplicate URL Detection
* Collision Detection
* Global Exception Handling
* Request Validation
* Dockerized Deployment
* Unit Testing with JUnit 5 & Mockito

---

## 🛠️ Tech Stack

| Category   | Technologies                            |
| ---------- | --------------------------------------- |
| Backend    | Java 17, Spring Boot 3, Spring Data JPA |
| Database   | MySQL                                   |
| Cache      | Redis                                   |
| Build Tool | Maven                                   |
| Testing    | JUnit 5, Mockito                        |
| Deployment | Docker, Render, Railway, Netlify        |

---

## 🏗️ Architecture

```text
Client
   │
   ▼
Spring Boot REST API
   │
   ├── Redis (Cache + Rate Limiting)
   │
   └── MySQL (Source of Truth)
```

**Design Patterns**

* Strategy Pattern (Short Code Generation)
* Layered Architecture (Controller → Service → Repository)

---

## 📂 Project Structure

```text
src/main/java
├── controller
├── service
├── repository
├── entity
├── dto
├── strategy
├── config
├── exception
└── scheduler
```

---

## 🚀 Getting Started

### Prerequisites

* Java 17+
* Maven
* MySQL
* Redis
* Docker (Optional)

### Clone

```bash
git clone https://github.com/<your-username>/url-shortener.git
cd url-shortener
```

### Configure

Update the following properties (or environment variables):

```properties
SPRING_DATASOURCE_URL=
SPRING_DATASOURCE_USERNAME=
SPRING_DATASOURCE_PASSWORD=

SPRING_DATA_REDIS_HOST=
SPRING_DATA_REDIS_PORT=
SPRING_DATA_REDIS_PASSWORD=

BASE_URL=
```

### Run

```bash
mvn clean install
mvn spring-boot:run
```

Or with Docker

```bash
docker build -t url-shortener .
docker run -p 8080:8080 url-shortener
```

---

## 📌 API Endpoints

| Method | Endpoint                             | Description              |
| ------ | ------------------------------------ | ------------------------ |
| POST   | `/api/v1/urls/shorten`               | Create a short URL       |
| GET    | `/api/v1/urls/{shortCode}`           | Redirect to original URL |
| GET    | `/api/v1/urls/analytics/{shortCode}` | Get URL analytics        |

---

## 🧪 Testing

```bash
mvn test
```

* JUnit 5
* Mockito

---

## 🔮 Future Improvements

* JWT Authentication
* Custom URL Aliases
* QR Code Generation
* User Dashboard
* Swagger/OpenAPI
* CI/CD with GitHub Actions
* Prometheus & Grafana Monitoring
* Kafka-based Async Analytics
