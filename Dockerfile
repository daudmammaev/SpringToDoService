# Build stage
FROM maven:3.9.6-openjdk-21 AS build
WORKDIR /app

# Копируем pom.xml и скачиваем зависимости
COPY pom.xml .
RUN mvn dependency:go-offline -B

# Копируем исходный код и собираем приложение
COPY src ./src
RUN mvn clean package -DskipTests

# Runtime stage
FROM openjdk:21-jdk-slim
WORKDIR /app

# Создаем не-root пользователя для безопасности
RUN groupadd -r spring && useradd -r -g spring spring

# Копируем JAR из build stage
COPY --from=build /app/target/SpringToDo-0.0.1-SNAPSHOT.jar app.jar

# Настраиваем права и пользователя
RUN chown spring:spring app.jar
USER spring

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "/app/app.jar"]