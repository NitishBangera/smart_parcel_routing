# Smart Parcel Routing System

## Design Decisions

The system is broken into microservices, namely:
- **Order Service**: Handles order creation and status updates.
- **Truck Service**: Handles truck registration and delivery assignments.

### API Gateway (Nginx)
- Acts as a reverse proxy.
- Routes requests to the appropriate service.
- Blocks internal APIs using path-based restrictions (e.g., `/orders/internal/` is denied).

### Scheduling
- A scheduled job assigns unassigned orders to trucks.
- The scheduler runs every hour between 6 AM and 12 AM using cron `0 0 6-23,0 * * *`.

### Data Filtering
- Delivery API supports:
  - Filtering by truck ID and/or date.
  - Defaults to deliveries from the last 7 days if no parameters are provided.

## Assumptions

- The database uses `OffsetDateTime` for delivery timestamps.
- Only deliveries not marked `CANCELLED` are considered.
- All services communicate over internal Docker network via service names.
- Date comparisons use UTC.

### Prerequisites

- Docker & Docker Compose
- Java 17+ and Gradle (for local testing)

### Running with Docker

1. Build and run:
    ```bash
    docker compose up --build
    ```

2. Access APIs via:
    - `http://localhost/orders/...`
    - `http://localhost/trucks/...`
    - `http://localhost/deliveries/...`

3. Internal endpoints like `/orders/internal/` will return `403 Forbidden`.

### Running Locally

1. Build each service using:
    ```bash
    gradle clean build
    ```

2. Run each Spring Boot application individually.

3. Use Postman or Curl to test endpoints.

### Sample API
- Create an order:
    ```bash
    curl -X POST http://localhost/orders -H "Content-Type: application/json" -d '{"pinCode": "400001", "address": "test3", "weight": 7, "volume": 4, "priority": "STANDARD"}'
    ```
- Get deliveries for a truck or on a specific date:
    ```bash
    curl http://localhost/deliveries?truckId=1&date=2025-06-14
    ```

## Optional: Dockerize the Service

Each service includes a Dockerfile:

```
# Stage 1: Build the application
FROM gradle:8.7-jdk21 AS builder
WORKDIR /app
COPY build.gradle settings.gradle ./
COPY src ./src
RUN gradle clean build

# Stage 2: Run the application
FROM eclipse-temurin:21-jre
WORKDIR /app
COPY --from=builder /app/build/libs/*.jar app.jar
EXPOSE 8081
ENTRYPOINT ["java", "-jar", "app.jar"]
```

The `docker-compose.yml` includes build instructions for:

- `order-service`
- `truck-service`
- `nginx` (as API gateway)

Use `docker compose down --rmi all` to remove old images before rebuilding.

---