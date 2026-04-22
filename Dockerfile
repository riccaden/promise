# =============================================================================
# Dockerfile für Oblivio – Digitale Legacy-Plattform (PROMISE Framework)
# Multi-Stage Build: Trennt Build- und Runtime-Umgebung für minimale Image-Grösse
# Wird auf Railway deployed (railway.json referenziert dieses Dockerfile)
# =============================================================================

# --- Stage 1: BUILD ---
# Verwendet Eclipse Temurin JDK 21 auf Alpine Linux als Build-Umgebung
# JDK (nicht JRE) wird benötigt, da Maven hier den Source-Code kompiliert
FROM eclipse-temurin:21-jdk-alpine AS build

# Arbeitsverzeichnis im Container setzen
WORKDIR /app

# Maven Wrapper und Konfiguration kopieren (ermöglicht Build ohne lokal installiertes Maven)
COPY mvnw .
COPY .mvn .mvn
# pom.xml separat kopieren für besseres Docker Layer-Caching
COPY pom.xml .

# Windows-Zeilenumbrüche (CRLF) aus mvnw entfernen und ausführbar machen
# Notwendig, da die Datei möglicherweise auf Windows bearbeitet wurde
RUN sed -i 's/\r$//' mvnw && chmod +x mvnw

# Alle Maven-Dependencies vorab herunterladen (eigener Layer für Docker-Cache)
# Bei Codeänderungen wird dieser Layer wiederverwendet, solange pom.xml unverändert bleibt
RUN ./mvnw dependency:go-offline -B

# Quellcode erst nach Dependency-Download kopieren (Cache-Optimierung)
COPY src src

# Spring Boot Anwendung bauen – Tests werden übersprungen für schnellere Builds
# Das resultierende JAR landet unter /app/target/
RUN ./mvnw clean package -DskipTests

# --- Stage 2: PRODUCTION ---
# Verwendet nur JRE (nicht JDK) – deutlich kleineres Image für die Laufzeit
FROM eclipse-temurin:21-jre-alpine

# Arbeitsverzeichnis im Container setzen
WORKDIR /app

# curl wird für den HEALTHCHECK-Befehl benötigt (Spring Boot Actuator Endpoint)
RUN apk add --no-cache curl

# Nur das fertige JAR aus der Build-Stage kopieren – kein Quellcode im Production-Image
COPY --from=build /app/target/*.jar app.jar

# Sicherheit: Nicht-Root-Benutzer erstellen und verwenden
# Verhindert, dass die Anwendung mit Root-Rechten läuft
RUN addgroup -g 1001 -S appuser && adduser -u 1001 -S appuser -G appuser
USER appuser

# Port 8080 dokumentieren – Railway setzt den tatsächlichen Port via $PORT Environment Variable
EXPOSE 8080

# Health Check: Prüft alle 30 Sekunden den Spring Boot Actuator Health-Endpoint
# start-period=40s gibt der Anwendung Zeit zum Hochfahren bevor Checks starten
HEALTHCHECK --interval=30s --timeout=3s --start-period=40s --retries=3 \
  CMD curl -f http://localhost:${PORT:-8080}/actuator/health || exit 1

# Anwendung mit dem Production-Profil starten (aktiviert Supabase DB-Konfiguration etc.)
ENTRYPOINT ["java", "-Dspring.profiles.active=prod", "-jar", "app.jar"]
