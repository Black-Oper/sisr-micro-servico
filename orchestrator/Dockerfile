# syntax=docker/dockerfile:1
# =========================================================
#  orchestrator/Dockerfile — build multi-stage (Java 21 / Spring Boot)
#
#  Estágio 1 (build): compila com Maven e empacota o .jar (pula testes —
#    testes usam Testcontainers e rodam no `mvn test`, não no build da imagem).
#  Estágio 2 (runtime): imagem JRE enxuta só com o .jar.
# =========================================================

# --- build ---
FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /app
COPY pom.xml .
COPY src ./src
RUN --mount=type=cache,target=/root/.m2 \
    mvn -B -ntp clean package -Dmaven.test.skip=true

# --- runtime ---
FROM eclipse-temurin:21-jre-jammy
WORKDIR /app
COPY --from=build /app/target/orchestrator-0.0.1-SNAPSHOT.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
