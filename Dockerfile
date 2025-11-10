# Stage 1: Build the Java application (Koristimo stabilnu JDK 25 i dodajemo Maven)
# Koristimo pouzdan tag za JDK 25
FROM eclipse-temurin:25-jdk-alpine AS build
WORKDIR /app

# Instaliraj Maven i ostale osnovne alate potrebne za build
RUN apk update && apk add maven

# Kopiraj datoteke
COPY pom.xml .
COPY src ./src
# Izgradi JAR datoteku
RUN mvn clean package -DskipTests

# Stage 2: Create the final lightweight image (Koristimo JRE 25 za pokretanje)
FROM eclipse-temurin:25-jre-alpine
WORKDIR /app
# Kopiraj JAR datoteku i preimenuj
COPY --from=build /app/target/*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]