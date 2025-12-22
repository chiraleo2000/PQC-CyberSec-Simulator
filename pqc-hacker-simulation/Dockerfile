FROM maven:3.9-eclipse-temurin-17 as builder
WORKDIR /app

# Copy parent pom and crypto core first
COPY pom.xml .
COPY pqc-crypto-core/pom.xml pqc-crypto-core/
COPY pqc-crypto-core/src pqc-crypto-core/src

# Install crypto core
RUN mvn install -pl pqc-crypto-core -DskipTests -q

# Copy service
COPY pqc-hacker-simulation/pom.xml pqc-hacker-simulation/
COPY pqc-hacker-simulation/src pqc-hacker-simulation/src

# Build service
RUN mvn package -pl pqc-hacker-simulation -am -DskipTests -q

FROM eclipse-temurin:17-jre-alpine
WORKDIR /app
COPY --from=builder /app/pqc-hacker-simulation/target/*.jar app.jar
EXPOSE 8083
ENTRYPOINT ["java", "-jar", "app.jar"]
