# 1. Используем легкий образ Java 21 (только для запуска, не для сборки)
FROM eclipse-temurin:21-jre-alpine

# 2. Создаем папку для логов (как у вас в конфиге)
RUN mkdir -p /var/log/expense-tracker

# 3. Копируем собранный .jar файл внутрь образа
# (GitHub Actions сначала соберет jar, а потом скопирует его сюда)
COPY target/*.jar app.jar

# 4. Указываем, с каким профилем запускать
# Это заставит Spring читать именно application-prod.yml
ENV JAVA_OPTS="-Dspring.profiles.active=prod"

# 5. Команда запуска
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar /app.jar"]