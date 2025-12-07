# Fullstack Java Project

## Mariana Alves Diniz (3TIW)

## Folder structure

- Readme.md
- _architecture_: this folder contains documentation regarding the architecture of your system.
- `docker-compose.yml` : to start the backend (starts all microservices)
- _backend-java_: contains microservices written in java
- _demo-artifacts_: contains images, files, etc that are useful for demo purposes.
- _frontend-web_: contains the Angular webclient

Each folder contains its own specific `.gitignore` file.  
**:warning: complete these files asap, so you don't litter your repository with binary build artifacts!**

## How to setup and run this application

This project is a distributed application built with **Spring Boot**, **Spring Cloud**, **Docker**, **MySQL**, and **RabbitMQ**, organized into independent microservices that communicate with each other through REST and messaging.

The architecture includes:

- **Config Server** (centralized configuration)
- **Discovery Server (Eureka)**
- **API Gateway**
- **PostService**, **ReviewService**, **CommentService**
- **RabbitMQ** for asynchronous messaging
- Separate databases per microservice for proper isolation

  ---

##  How to Run the Project
### Requirements
- **Docker** and **Docker Compose**
- **Java 17+**
- **Maven**

---

##  1. Start the Infrastructure (MySQL + RabbitMQ)

Use the provided `docker-compose.yml`:

```bash
docker compose up -d
```

## 2. Required Startup Order for Microservices

To ensure proper configuration loading and registration, start the services in this exact order:

- 1️⃣ ConfigService
- 2️⃣ DiscoveryService (Eureka)
- 3️⃣ PostService
- 4️⃣ ReviewService
- 5️⃣ CommentService
- 6️⃣ GatewayService

```bash
mvn spring-boot:run -pl config-service
mvn spring-boot:run -pl discovery-service
mvn spring-boot:run -pl post-service
mvn spring-boot:run -pl review-service
mvn spring-boot:run -pl comment-service
mvn spring-boot:run -pl gateway-service


  

