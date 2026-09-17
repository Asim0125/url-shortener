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

# 📖 Overview

The **High-Performance URL Shortener** is a backend service that converts long URLs into short, shareable links.

For example:

```text
Original URL
https://example.com/products/category/something/very-long-url

                         ↓

Short URL
http://localhost:8080/api/url/Ab1
