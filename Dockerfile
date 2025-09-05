# Use a Maven image that includes Java and Maven
FROM maven:3-openjdk-17 AS build

# Set the working directory inside the container
WORKDIR /app

# Copy the pom.xml and the entire src directory
COPY pom.xml .
COPY src ./src

# Build the Java application using Maven
RUN mvn clean package -DskipTests

# Use a lean OpenJDK image for the final runtime
FROM openjdk:17-jdk-slim

# Set the working directory
WORKDIR /app

# Copy the built jar file from the build stage
COPY --from=build /app/target/*.jar app.jar

# Expose the port your application will run on
EXPOSE 8080

# Run the application
CMD ["java", "-jar", "app.jar"]
