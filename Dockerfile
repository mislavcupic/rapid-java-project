# Stage 1: Build the Java application (Koristimo Maven za Build s JDK 21)
FROM maven:3.9.6-eclipse-temurin-21 AS build
WORKDIR /app
# Kopiraj datoteke
COPY pom.xml .
COPY src ./src
# Izgradi JAR datoteku
RUN mvn clean package -DskipTests

# Stage 2: Create the final lightweight image (Koristimo JRE 21 za pokretanje)
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
# Kopiraj JAR datoteku i preimenuj
COPY --from=build /app/target/*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]