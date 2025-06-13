# Stage 1: Build the application
FROM gradle:8.7-jdk21 AS builder

# Create app directory
WORKDIR /app

# Copy build files
COPY build.gradle settings.gradle ./
COPY src ./src

# Pre-download dependencies and build the app
RUN gradle clean build

# Stage 2: Run the application
FROM eclipse-temurin:21-jre

# Set working directory
WORKDIR /app

# Copy the JAR from the builder stage
COPY --from=builder /app/build/libs/*.jar app.jar

# Expose port
EXPOSE 8082

# Run the application
ENTRYPOINT ["java", "-jar", "app.jar"]