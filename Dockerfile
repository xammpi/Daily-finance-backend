# ЭТАП 1: Сборка (Builder)
FROM maven:3.9-eclipse-temurin-21-alpine AS builder
WORKDIR /app
COPY pom.xml .
COPY src ./src
# Сборка происходит здесь, внутри контейнера!
RUN mvn clean package -DskipTests

# ЭТАП 2: Запуск (Runner)
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
RUN mkdir -p /var/log/expense-tracker
# Копируем готовый jar из первого этапа
COPY --from=builder /app/target/*.jar app.jar
ENV JAVA_OPTS="-Dspring.profiles.active=prod"
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar /app.jar"]