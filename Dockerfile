FROM eclipse-temurin:17-jre-jammy

# Create app directory
WORKDIR /app

# Copy JAR file
COPY target/todo-app-1.0.0.jar app.jar

# Create non-root user
RUN groupadd -r spring && useradd -r -g spring spring
USER spring

# Expose port
EXPOSE 8080

# Health check
HEALTHCHECK --interval=30s --timeout=3s --start-period=5s --retries=3 \
    CMD curl -f http://localhost:8080/actuator/health || exit 1

# Run the application
ENTRYPOINT ["java", "-jar", "/app/app.jar"]