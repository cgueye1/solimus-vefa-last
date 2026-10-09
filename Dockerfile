#FROM eclipse-temurin:17-jdk-alpine

# Crée le dossier pour les certificats (avec -p pour éviter les erreurs)
#RUN mkdir -p /certs/vefa

# Dossier de travail
#WORKDIR /app

# Copie le JAR dans le conteneur
#COPY target/*.jar app.jar

# Lancement de l'application
#ENTRYPOINT ["java", "-jar", "/app/app.jar"]


#FROM maven:3.8.4-openjdk-17-slim AS build
FROM eclipse-temurin:17-jdk-alpine
WORKDIR /app
COPY pom.xml .
COPY src ./src
RUN mvn clean package -DskipTests \
    && ls -l target/
FROM amazoncorretto:17-alpine-jdk
LABEL maintainer="Cheikh  GUEYE contactwakana@gmail.com"
WORKDIR /app
COPY target/*.jar app.jar
# Copie du logo depuis le contexte de build local
RUN ls -l
ENTRYPOINT ["java","-jar","/app/app.jar"]