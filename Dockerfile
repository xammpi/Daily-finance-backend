FROM maven:3.9-eclipse-temurin-21-alpine AS builder

WORKDIR /app

COPY pom.xml .
COPY src ./src
RUN mvn clean package -DskipTests
FROM eclipse-temurin:21-jre-alpine

WORKDIR /app
RUN mkdir -p /var/log/expense-tracker
COPY --from=builder /app/target/*.jar app.jar

ENV JAVA_OPTS="-Dspring.profiles.active=prod"
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar /app.jar"]