# =========================================================
#  orchestrator/Dockerfile — PLACEHOLDER (Etapa 1)
#
#  Serviço Java/Spring Boot responsável pela orquestração.
#  Ainda NÃO há código-fonte: este Dockerfile só fixa a
#  imagem-base para isolar o ambiente. As etapas de build
#  (COPY do projeto, mvn/gradle package, runtime enxuto)
#  entram quando o código Spring Boot existir.
# =========================================================

# --- Imagem base de runtime (JDK 21 LTS) ---
FROM eclipse-temurin:21-jdk-jammy

WORKDIR /app

# Placeholder enquanto não há aplicação.
# Em etapas futuras (multi-stage build sugerido):
#   1) estágio "build"  -> compila com Maven/Gradle
#   2) estágio "runtime"-> copia apenas o .jar
#   ENTRYPOINT ["java","-jar","app.jar"]
CMD ["bash", "-c", "echo 'orchestrator placeholder — sem app ainda (Etapa 1)'; sleep infinity"]
