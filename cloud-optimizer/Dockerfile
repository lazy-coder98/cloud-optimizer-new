# Standard Docker build — no BuildKit secrets. JWT is runtime-only via APP_AUTH_SIGNING_SECRET (or default in application.properties).
FROM maven:3.9-eclipse-temurin-17 AS build
WORKDIR /app

COPY pom.xml .
COPY cloud-optimizer-frontend/package.json cloud-optimizer-frontend/package-lock.json ./cloud-optimizer-frontend/
COPY src ./src
COPY cloud-optimizer-frontend ./cloud-optimizer-frontend

RUN mvn package -DskipTests -B

FROM eclipse-temurin:17-jre-jammy
WORKDIR /app
COPY --from=build /app/target/*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
