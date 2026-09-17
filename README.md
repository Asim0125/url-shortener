# 🚀 High-Performance URL Shortener

![Java](https://img.shields.io/badge/Java-17-orange) ![Spring Boot](https://img.shields.io/badge/Spring_Boot-4.0.1-green) ![Redis](https://img.shields.io/badge/Redis-Caching-red) ![PostgreSQL](https://img.shields.io/badge/PostgreSQL-Database-blue) ![Docker](https://img.shields.io/badge/Docker-Ready-2496ED)

A scalable, high-performance URL shortening service built with Java Spring Boot. Designed to handle high-traffic scenarios using **Redis Caching** for fast reads and **Asynchronous Batching** for write-heavy analytics.

---

## 🏗 System Architecture & Workflow

This system is designed to minimize Database load and maximize response time.

<img width="831" height="454" alt="System Design URLShortener drawio" src="https://github.com/user-attachments/assets/8671e8d9-4729-4a0a-a7bd-ba0ef3350eb1" />


### ✨ Key Features (Engineering Decisions)

1.  **High-Speed Redirection (Read Optimization)**
    * Uses **Redis** to cache original URLs (`getOriginalUrl`).
    * If cache hits, the Database is **never touched**, resulting in sub-millisecond response times.
    * Uses **Base62 Encoding** for efficient and short URL generation.

2.  **Scalable Analytics (Write Optimization)**
    * **Problem:** Updating the database for every single click crashes the DB under high load (10k+ rps).
    * **Solution:** Implemented a **"Write-Behind" strategy**. Clicks are incremented atomically in Redis first. A background scheduler flushes data to PostgreSQL in batches every 60 seconds.
    * **Benefit:** Reduces DB write operations by ~99%.

3.  **Data Integrity**
    * Includes automatic fallback: If the database sync fails, analytics data is preserved in Redis to prevent data loss.

---

## 🛠 Tech Stack

* **Core:** Java 17, Spring Boot 4
* **Database:** PostgreSQL
* **Caching & Message Broker:** Redis
* **Testing:** JUnit 5, Mockito (Unit Testing with High Coverage)
* **Containerization:** Docker & Docker Compose

---

## 🚀 How to Run

### Option 1: Using Docker (Recommended)
Make sure you have Docker installed.

```bash
# 1. Clone the repository
git clone https://github.com/AdnSmile/url-shortener

# 2. Run with Docker Compose (App + DB + Redis)
docker-compose up -d
```

### Option 2: Manual Run
1.  Ensure PostgreSQL runs on port `5432`.
2.  Ensure Redis runs on port `6379`.
3.  Configure `application.properties` accordingly.
4.  Run the JAR:
    ```bash
    ./mvnw spring-boot:run
    ```

---

## 🔌 API Endpoints

| Method | Endpoint               | Description |
| :--- |:-----------------------| :--- |
| `POST` | `/api/url/shorten`     | Create a new short URL |
| `GET` | `/api/url/{shortCode}` | Redirect to original URL |

### Example Usage (cURL)

**1. Shorten a URL:**
```bash
curl -X POST http://localhost:8080/api/url/shorten \
     -H "Content-Type: application/json" \
     -d '{"longUrl": "https://www.linkedin.com/in/nessafitri/"}'
```
*Response:* `Ab1`

**2. Access the URL:**
Open browser: `http://localhost:8080/api/url/Ab1`

---

## ✅ Testing
This project includes comprehensive Unit Tests using **JUnit 5** and **Mockito**.

```bash
# Run all tests
./mvnw test
```
*Tested Scenarios:*
* Cache Hit vs Cache Miss logic.
* Base62 encoding/decoding validation.
* Analytics batch processing simulation.
