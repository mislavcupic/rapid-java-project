# Stage 1: Build the Java application (Koristimo Maven za Build s JDK 25)
# RJEŠENJE: Koristimo generički tag koji koristi najnoviji Maven + Temurin 25
FROM eclipse-temurin:25-jdk-focal AS build
# ILI OVO: FROM maven:3.9.6-eclipse-temurin-21 (pa popraviti pom.xml na 21)
# Ali nastavljamo s 25 kao što ste tražili:
WORKDIR /app
# Kopiraj datoteke
COPY pom.xml .
COPY src ./src
# Izgradi JAR datoteku
RUN mvn clean package -DskipTests

# Stage 2: Create the final lightweight image (Koristimo JRE 25 za pokretanje)
# RJEŠENJE: Koristimo generički tag koji je pouzdan za JRE 25
FROM eclipse-temurin:25-jre-alpine
WORKDIR /app
# Kopiraj JAR datoteku i preimenuj
COPY --from=build /app/target/*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]