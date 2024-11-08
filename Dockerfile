# Build stage
FROM maven:3.8.4-openjdk-17 as build

WORKDIR /app

# Copy pom.xml
COPY pom.xml .

# Download dependencies
RUN mvn dependency:go-offline -B

# Copy source code
COPY src ./src

# Build the application
RUN mvn package -DskipTests

# Production stage
FROM openjdk:17-slim

WORKDIR /app

# Install netcat (nc) to enable "wait-for-it.sh"
RUN apt-get update && apt-get install -y netcat

# Copy the built JAR file from the build stage
COPY --from=build /app/target/*.jar app.jar

# Copy the wait-for-it script into the image
COPY wait-for-it.sh /app/wait-for-it.sh

# Make the wait-for-it script executable
RUN chmod +x /app/wait-for-it.sh

# Expose the port the application will run on
EXPOSE 8080

# Use wait-for-it to wait for MySQL to be ready, then run the application
ENTRYPOINT ["./wait-for-it.sh", "mysql:3306", "--", "java", "-jar", "app.jar"]