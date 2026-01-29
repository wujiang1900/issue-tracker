# Use pre-built JAR approach (simpler and faster)
FROM eclipse-temurin:17-jre-alpine
WORKDIR /app

# Copy the pre-built JAR file from target directory
COPY target/*.jar app.jar

EXPOSE 8080
ENTRYPOINT ["java", "-jar", "-Dspring.profiles.active=docker", "app.jar"]
