# Stage 1: Build
FROM maven:3.9.6-eclipse-temurin-17 AS build
WORKDIR /bank-rest-main
COPY . .
RUN mvn clean package -DskipTests

# Stage 2: Run
FROM eclipse-temurin:17-jre-jammy
WORKDIR /bank-rest-main
COPY --from=build /bank-rest-main/target/*.jar app.jar
ENTRYPOINT ["java", "-jar", "app.jar"]