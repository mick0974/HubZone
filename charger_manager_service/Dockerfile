# Build stage
FROM maven:3.8.8-eclipse-temurin-21-alpine AS build
WORKDIR /build
COPY pom.xml .
RUN mvn dependency:go-offline
COPY src ./src
RUN mvn clean package -DskipTests

# Run stage
FROM eclipse-temurin:21-jdk-alpine
WORKDIR /HubZone
COPY --from=build /build/target/*.jar charger_gateway.jar
ENTRYPOINT ["java","-jar","charger_gateway.jar"]
