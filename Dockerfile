# ---- build stage ----
FROM maven:3.9-eclipse-temurin-25 AS build
WORKDIR /app

# dependency cache
COPY pom.xml .
RUN mvn -q -DskipTests dependency:go-offline

# build
COPY src ./src
RUN mvn -q -DskipTests package

# ---- runtime stage ----
FROM eclipse-temurin:25-jre
WORKDIR /app

# non-root user (daha güvenli)
RUN useradd -m appuser
USER appuser

COPY --from=build /app/target/*.jar /app/app.jar

EXPOSE 1905
ENTRYPOINT ["java","-jar","/app/app.jar"]