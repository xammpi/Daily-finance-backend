# --- ЭТАП 1: Сборка (Build) ---
# Используем образ с Maven и Java 21
FROM maven:3.9-eclipse-temurin-21-alpine AS builder

WORKDIR /app

# Копируем настройки сборки и исходный код
COPY pom.xml .
COPY src ./src

# Запускаем сборку (тесты пропускаем, чтобы не нужна была база данных)
RUN mvn clean package -DskipTests

# --- ЭТАП 2: Запуск (Run) ---
# Используем легкий образ только для запуска
FROM eclipse-temurin:21-jre-alpine

WORKDIR /app

# Копируем готовый файл из первого этапа
COPY --from=builder /app/target/*.jar app.jar

# Команда запуска
ENTRYPOINT ["java", "-jar", "/app.jar"]