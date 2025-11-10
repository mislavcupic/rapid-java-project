# Stage 1: Build the Java application (Koristimo Maven za Build s JDK 25)
# NAPOMENA: Ažurirali smo verziju na 25
FROM maven:3.9.6-eclipse-temurin-25 AS build
WORKDIR /app
# Kopiraj datoteke
COPY pom.xml .
COPY src ./src
# Izgradi JAR datoteku
RUN mvn clean package -DskipTests

# Stage 2: Create the final lightweight image (Koristimo JRE 25 za pokretanje)
# NAPOMENA: Ažurirali smo verziju na 25
FROM eclipse-temurin:25-jre-alpine
WORKDIR /app
# Kopiraj JAR datoteku i preimenuj
COPY --from=build /app/target/*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]