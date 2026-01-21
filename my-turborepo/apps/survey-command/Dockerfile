# -------- Build stage --------
FROM maven:3.9.6-eclipse-temurin-17 AS build

WORKDIR /app

# Copiamos solo lo necesario primero (mejor cache)
COPY pom.xml .
RUN mvn -B dependency:go-offline

# Copiamos el código
COPY src ./src

# Build del jar
RUN mvn -B clean package -DskipTests


# -------- Runtime stage --------
FROM eclipse-temurin:17-jre

WORKDIR /app

# Copiamos el jar desde el stage de build
COPY --from=build /app/target/*.jar app.jar

EXPOSE 8082

ENTRYPOINT ["java","-jar","app.jar"]
