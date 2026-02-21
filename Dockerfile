# Stage 1: Build the application
FROM maven:3.9.5-eclipse-temurin-17-alpine AS builder
WORKDIR /app
COPY pom.xml .
RUN mvn dependency:go-offline -B
COPY src ./src
RUN mvn clean package -DskipTests

# Stage 2: Create the runtime image
FROM eclipse-temurin:17-jre-alpine
WORKDIR /app
COPY --from=builder /app/target/*.jar app.jar

# Railway injects the PORT environment variable dynamically
ENV PORT=9090
ENV SPRING_PROFILES_ACTIVE=default
EXPOSE $PORT

# Start the application using Railway's PORT and active profile
ENTRYPOINT ["sh", "-c", "java -jar app.jar --server.port=${PORT}"]
