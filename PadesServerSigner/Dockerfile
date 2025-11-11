# Base image with Maven + Java 11
FROM maven:3.9.6-eclipse-temurin-11 AS builder

WORKDIR /app

# Copy pom.xml and download dependencies
COPY pom.xml .
RUN mvn dependency:go-offline

# Default command (can be overridden later)
CMD ["mvn", "spring-boot:run"]