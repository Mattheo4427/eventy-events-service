# =========================
# 1️⃣ Build Stage
# =========================
FROM maven:3.9.9-eclipse-temurin-21 AS builder
WORKDIR /app

# Copy pom.xml and resolve dependencies first (for build caching)
COPY pom.xml .
RUN mvn dependency:go-offline

# Copy all source files and build
COPY src ./src
RUN mvn clean package -DskipTests

# =========================
# 2️⃣ Runtime Stage
# =========================
FROM eclipse-temurin:21-jre
WORKDIR /app

# Copy built JAR from builder
COPY --from=builder /app/target/eventy-events-service-*.jar app.jar

# Expose port
EXPOSE 8082

# Run the application
ENTRYPOINT ["java", "-jar", "app.jar"]
