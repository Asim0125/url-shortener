<div align="center">

# 🔗 High-Performance URL Shortener

### ⚡ Scalable URL Shortening Service with Redis Caching & Asynchronous Analytics

<p>
  A backend URL shortening service built with 
  <b>Java</b>, <b>Spring Boot</b>, <b>PostgreSQL</b>, and <b>Redis</b>.
</p>

<p>
  Designed with caching, efficient URL generation, asynchronous analytics
  processing, and persistent data storage.
</p>

<br/>

![Java](https://img.shields.io/badge/Java-17-orange?style=for-the-badge&logo=openjdk)
![Spring Boot](https://img.shields.io/badge/Spring%20Boot-4.0.1-brightgreen?style=for-the-badge&logo=springboot)
![PostgreSQL](https://img.shields.io/badge/PostgreSQL-Database-blue?style=for-the-badge&logo=postgresql)
![Redis](https://img.shields.io/badge/Redis-Caching-red?style=for-the-badge&logo=redis)
![Docker](https://img.shields.io/badge/Docker-Containerized-2496ED?style=for-the-badge&logo=docker)
![Maven](https://img.shields.io/badge/Maven-Build-C71A36?style=for-the-badge&logo=apachemaven)
![JUnit](https://img.shields.io/badge/JUnit-Testing-25A162?style=for-the-badge&logo=junit5)

</div>

---

## 📚 Table of Contents

- [Overview](#overview)
- [Problem Statement](#problem-statement)
- [Solution](#solution)
- [Key Features](#key-features)
- [System Architecture](#system-architecture)
- [Application Workflow](#application-workflow)
- [URL Shortening Flow](#url-shortening-flow)
- [URL Redirection Flow](#url-redirection-flow)
- [Redis Caching Strategy](#redis-caching-strategy)
- [Analytics Processing](#analytics-processing)
- [Base62 Encoding](#base62-encoding)
- [Data Persistence](#data-persistence)
- [Technology Stack](#technology-stack)
- [API Documentation](#api-documentation)
- [API Examples](#api-examples)
- [Project Structure](#project-structure)
- [Configuration](#configuration)
- [Running the Project](#running-the-project)
- [Testing](#testing)
- [Design Considerations](#design-considerations)
- [Future Improvements](#future-improvements)

---

## Overview

The **High-Performance URL Shortener** is a backend service that converts long, unwieldy URLs into short, shareable links — and instantly redirects users from the short link back to the original destination.

For example:

```text
Original URL
https://example.com/products/category/something/very-long-url

                         ↓

Short URL
http://localhost:8080/api/url/Ab1x9Z
```

Beyond the basic "shorten and redirect" functionality, the project is built to reflect how such a service would actually be designed at scale: a caching layer sits in front of the database to keep redirects fast under repeated load, short codes are generated using a compact Base62 scheme instead of storing long random strings, and click activity is recorded asynchronously so that analytics tracking never slows down the redirect path itself.

The goal of this project is to demonstrate backend engineering fundamentals that are directly relevant to production systems — REST API design, caching strategy, database schema design, concurrency-safe ID generation, and clean layered architecture — rather than just a CRUD wrapper around a database table.

---

## Problem Statement

Traditional URL sharing mein long URLs inconvenient hote hain. Is project ka purpose long URLs ko short, manageable URLs mein convert karna hai.

Long URLs are hard to share, remember, and track — especially across social media, SMS, printed material, or QR codes where character limits and readability matter. There is also no easy way to measure how many times a shared link was actually clicked. The system needs to solve this while remaining fast even as the number of stored URLs and incoming redirect requests grows.

---

## Solution

The system provides a backend service that generates unique short URLs and redirects users to the original URLs.

Every long URL submitted by a client is assigned a unique, compact short code. This mapping is persisted in PostgreSQL for durability and cached in Redis for low-latency reads. When a short URL is visited, the service resolves it — preferring the cache — and issues a redirect to the original URL, while recording the click event for analytics without blocking the redirect response.

---

## Key Features

- URL shortening using Base62 encoding
- Fast URL redirection
- Redis-based caching
- Click analytics
- PostgreSQL persistence
- REST APIs
- Docker support
- Unit testing

---

## System Architecture

The application follows a layered backend architecture consisting of:

- Controller Layer
- Service Layer
- Repository Layer
- PostgreSQL Database
- Redis Cache

```
┌─────────────┐      ┌──────────────────┐      ┌─────────────────┐
│   Client     │ ───▶ │  Controller Layer │ ───▶ │  Service Layer   │
└─────────────┘      └──────────────────┘      └─────────────────┘
                                                          │
                                          ┌───────────────┼───────────────┐
                                          ▼                               ▼
                                 ┌────────────────┐             ┌──────────────────┐
                                 │   Redis Cache    │             │ Repository Layer  │
                                 │  (short → long)   │             │   (Spring Data)   │
                                 └────────────────┘             └──────────────────┘
                                                                          │
                                                                          ▼
                                                                 ┌──────────────────┐
                                                                 │   PostgreSQL      │
                                                                 │  (URL mappings,   │
                                                                 │   click events)    │
                                                                 └──────────────────┘
```

- **Controller Layer** — exposes REST endpoints, validates incoming requests, and delegates to the service layer.
- **Service Layer** — contains the core business logic: short code generation, cache-aside lookups, and analytics event dispatch.
- **Repository Layer** — Spring Data JPA repositories responsible for all database access.
- **Redis Cache** — sits between the service and repository layers to absorb repeated reads for popular short URLs.
- **PostgreSQL** — the system of record for URL mappings and click analytics.

---

## Application Workflow

1. User sends a long URL.
2. Backend validates the URL.
3. A unique short code is generated.
4. URL mapping is stored in PostgreSQL.
5. Frequently accessed URLs are cached in Redis.
6. When the short URL is opened, the system retrieves the original URL.
7. Click analytics are recorded.

---

## URL Shortening Flow

1. Client sends a POST request.
2. Spring Boot receives the request.
3. Service generates a unique short code.
4. Original URL and short code are stored.
5. Response containing the short URL is returned.

```
Client            Controller            Service              Repository        Database
  │  POST /shorten     │                     │                     │               │
  │────────────────────▶                     │                     │               │
  │                     │  shortenUrl(url)    │                     │               │
  │                     │────────────────────▶                     │               │
  │                     │                     │  generateCode()     │               │
  │                     │                     │  save(mapping)      │               │
  │                     │                     │────────────────────▶               │
  │                     │                     │                     │  INSERT       │
  │                     │                     │                     │──────────────▶
  │                     │                     │◀────────────────────               │
  │                     │◀────────────────────                     │               │
  │◀────────────────────                      │                     │               │
  │  { shortUrl }       │                     │                     │               │
```

---

## URL Redirection Flow

1. User opens the short URL.
2. Application searches Redis cache first.
3. If found, the original URL is returned.
4. If not found, PostgreSQL is queried.
5. The result can then be cached.
6. User is redirected to the original URL.

```
Client            Controller            Service            Redis          Database
  │  GET /{code}        │                    │                │               │
  │─────────────────────▶                    │                │               │
  │                      │  resolve(code)     │                │               │
  │                      │───────────────────▶                │               │
  │                      │                    │  GET code      │               │
  │                      │                    │───────────────▶               │
  │                      │                    │ ◀── HIT/MISS ──               │
  │                      │                    │  (if MISS) query DB            │
  │                      │                    │────────────────────────────────▶
  │                      │                    │ ◀────── originalUrl ───────────
  │                      │                    │  SET code → url (cache)        │
  │                      │                    │───────────────▶               │
  │                      │◀───────────────────                │               │
  │◀── 302 Redirect ─────                     │                │               │
```

---

## Redis Caching Strategy

Redis is used as a fast-access cache for frequently requested short URLs.

The application checks Redis before querying PostgreSQL, reducing unnecessary database reads for cached URLs. This follows a **cache-aside (lazy-loading)** pattern:

- On a redirect request, the service first checks Redis for the short code.
- **Cache hit** — the original URL is returned immediately without touching the database.
- **Cache miss** — the service falls back to PostgreSQL, returns the result, and populates the cache for subsequent requests.
- Each cache entry is stored with a **TTL (time-to-live)** so stale or rarely-used entries are naturally evicted rather than growing the cache indefinitely.
- On URL creation, the mapping can optionally be pre-warmed into the cache so the very first redirect is also fast.

This keeps the hot path (redirects, which vastly outnumber URL creations in a real system) off the database for the vast majority of requests.

---

## Analytics Processing

The application tracks URL clicks and processes analytics so that URL usage can be monitored without making every request depend directly on a database write.

Click events (timestamp, short code, and optionally referrer/user-agent) are published to an internal event handler and persisted **asynchronously**, using Spring's `@Async` support (or a lightweight event bus). This decouples the redirect response — which must stay fast — from the analytics write path, so a slow or contended analytics table never adds latency to the user-facing redirect.

Aggregated click counts can then be exposed through a read endpoint (e.g. total clicks per short code, clicks over time) for basic usage insights.

---

## Base62 Encoding

Base62 encoding is used to generate compact URL identifiers using:

- A–Z
- a–z
- 0–9

This allows numeric identifiers to be represented using shorter strings. A monotonically increasing internal ID (or a hash-derived numeric value) is converted into a Base62 string, so instead of storing a URL against an ID like `482910573`, the system stores and serves a short code like `Ab1x9Z`.

Because the alphabet has 62 characters, a 6-character Base62 code can represent over **56 billion** unique combinations (62⁶ ≈ 5.68 × 10¹⁰) — comfortably enough for a growing service while keeping short URLs genuinely short.

---

## Data Persistence

PostgreSQL is used as the persistent database for storing URL mappings and application data.

The database provides reliable long-term storage for the shortened URLs. Core tables include:

| Table | Purpose |
|---|---|
| `url_mapping` | Stores `short_code`, `original_url`, `created_at`, `expires_at` (optional) |
| `click_event` | Stores `short_code`, `clicked_at`, `referrer`, `user_agent` for analytics |

`short_code` is indexed (and unique) to keep both the write path (collision checks) and the read path (redirect lookups on cache miss) efficient as the table grows.

---

## Technology Stack

| Technology | Purpose |
|---|---|
| Java | Backend programming |
| Spring Boot | REST API and application framework |
| PostgreSQL | Persistent database |
| Redis | Caching |
| JUnit | Testing |
| Mockito | Mocking |
| Docker | Containerization |
| Maven | Build management |

---

## API Documentation

### Create Short URL

```http
POST /api/url/shorten
Content-Type: application/json
```

**Request Body**

```json
{
  "originalUrl": "https://example.com/products/category/something/very-long-url"
}
```

**Response `201 Created`**

```json
{
  "shortCode": "Ab1x9Z",
  "shortUrl": "http://localhost:8080/api/url/Ab1x9Z",
  "originalUrl": "https://example.com/products/category/something/very-long-url",
  "createdAt": "2026-09-18T10:15:30Z"
}
```

---

### Redirect to Original URL

```http
GET /api/url/{shortCode}
```

**Response `302 Found`**

Redirects the client to the original URL via the `Location` header. Also asynchronously records a click event for that `shortCode`.

---

### Get URL Details

```http
GET /api/url/details/{shortCode}
```

**Response `200 OK`**

```json
{
  "shortCode": "Ab1x9Z",
  "originalUrl": "https://example.com/products/category/something/very-long-url",
  "createdAt": "2026-09-18T10:15:30Z",
  "totalClicks": 42
}
```

---

### Get Click Analytics

```http
GET /api/url/analytics/{shortCode}
```

**Response `200 OK`**

```json
{
  "shortCode": "Ab1x9Z",
  "totalClicks": 42,
  "lastAccessedAt": "2026-09-18T14:02:11Z"
}
```

---

### Delete Short URL

```http
DELETE /api/url/{shortCode}
```

**Response `204 No Content`**

Removes the mapping from PostgreSQL and evicts the corresponding entry from Redis.

---

## API Examples

**cURL — Create a short URL**

```bash
curl -X POST http://localhost:8080/api/url/shorten \
  -H "Content-Type: application/json" \
  -d '{"originalUrl": "https://example.com/a/very/long/path"}'
```

**cURL — Follow the short URL**

```bash
curl -L http://localhost:8080/api/url/Ab1x9Z
```

**cURL — Check analytics**

```bash
curl http://localhost:8080/api/url/analytics/Ab1x9Z
```

---

## Project Structure

```
url-shortener/
├── src/
│   ├── main/
│   │   ├── java/com/example/urlshortener/
│   │   │   ├── controller/
│   │   │   │   └── UrlController.java
│   │   │   ├── service/
│   │   │   │   ├── UrlService.java
│   │   │   │   └── AnalyticsService.java
│   │   │   ├── repository/
│   │   │   │   ├── UrlRepository.java
│   │   │   │   └── ClickEventRepository.java
│   │   │   ├── model/
│   │   │   │   ├── UrlMapping.java
│   │   │   │   └── ClickEvent.java
│   │   │   ├── dto/
│   │   │   │   ├── ShortenRequest.java
│   │   │   │   └── ShortenResponse.java
│   │   │   ├── util/
│   │   │   │   └── Base62Encoder.java
│   │   │   ├── config/
│   │   │   │   ├── RedisConfig.java
│   │   │   │   └── AsyncConfig.java
│   │   │   └── UrlShortenerApplication.java
│   │   └── resources/
│   │       ├── application.yml
│   │       └── db/migration/   (Flyway scripts)
│   └── test/
│       └── java/com/example/urlshortener/
│           ├── service/UrlServiceTest.java
│           └── controller/UrlControllerTest.java
├── docker-compose.yml
├── Dockerfile
├── pom.xml
└── README.md
```

---

## Configuration

Key application properties (`application.yml`):

```yaml
server:
  port: 8080

spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/urlshortener
    username: postgres
    password: postgres
  jpa:
    hibernate:
      ddl-auto: validate
    show-sql: false
  data:
    redis:
      host: localhost
      port: 6379

app:
  base-url: http://localhost:8080/api/url
  cache:
    ttl-seconds: 3600
```

Environment variables can be used in place of hard-coded values for `DB_URL`, `DB_USERNAME`, `DB_PASSWORD`, `REDIS_HOST`, and `REDIS_PORT` when running via Docker.

---

## Running the Project

**Prerequisites**

- Java 17+
- Maven 3.8+
- Docker & Docker Compose (for PostgreSQL and Redis)

**1. Clone the repository**

```bash
git clone https://github.com/<your-username>/url-shortener.git
cd url-shortener
```

**2. Start PostgreSQL and Redis**

```bash
docker-compose up -d
```

**3. Build and run the application**

```bash
mvn clean install
mvn spring-boot:run
```

The application will be available at `http://localhost:8080`.

**4. (Optional) Run everything in Docker**

```bash
docker-compose up --build
```

---

## Testing

The project includes unit and integration tests covering the service and controller layers.

```bash
mvn test
```

- **JUnit 5** — test framework
- **Mockito** — mocking repository and cache dependencies in service-layer unit tests
- **Testcontainers** *(recommended addition)* — spins up real PostgreSQL and Redis containers for integration tests, so cache and persistence behavior is verified against real infrastructure rather than mocks alone

---

## Design Considerations

- **Cache-aside over write-through** — chosen because redirect reads vastly outnumber URL-creation writes, so it's more efficient to populate the cache lazily on first read than to write every new mapping into Redis upfront.
- **Base62 over random string generation** — guarantees no collisions (since it's derived from a unique numeric ID) without needing a separate collision-check query on every write, unlike naive random-string approaches.
- **Asynchronous analytics** — decouples click tracking from the redirect response so analytics writes (and any future spikes in write volume) never add latency to the user-facing path.
- **TTL-based cache eviction** — avoids unbounded Redis memory growth while keeping recently/frequently accessed URLs fast.
- **Trade-off:** the current design assumes a single application instance for short-code generation. In a multi-instance deployment, ID generation would need a distributed-safe strategy (e.g. a database sequence, Snowflake-style ID generator, or a Redis `INCR`-based counter) to avoid collisions across instances.

---

## Future Improvements

- Custom aliases (user-defined short codes)
- URL expiry (TTL per link, not just per cache entry)
- Rate limiting for anonymous users (e.g. Bucket4j + Redis)
- JWT-based authentication so users can manage their own links
- Distributed ID generation for multi-instance deployments
- Richer analytics (geo-location, device/browser breakdown via User-Agent parsing)
- Kafka-based event streaming for click analytics at higher scale
- API rate-limit and usage dashboards via Spring Boot Actuator + Micrometer
