# Build stage
FROM maven:3.9.6-eclipse-temurin-17 AS build
WORKDIR /app
COPY apps/zombie-manager/pom.xml ./apps/zombie-manager/pom.xml
COPY apps/zombie-manager/src ./apps/zombie-manager/src
WORKDIR /app/apps/zombie-manager
RUN mvn clean package -DskipTests

# Run stage
FROM eclipse-temurin:17-jre-jammy
WORKDIR /app
COPY --from=build /app/apps/zombie-manager/target/*.jar app.jar
ENTRYPOINT ["java", "-jar", "app.jar"]
