# Stage 1: Build the application
# Use a Maven image with Java 17 to build the project. This stage is temporary.
FROM maven:3.8.2-jdk-17 AS build

# Set the working directory inside the container to /app.
WORKDIR /app

# Copy the pom.xml file and the source code from your repository into the container.
COPY pom.xml .
COPY src ./src

# Run the Maven build command to compile your code and package it into a JAR file.
RUN mvn clean package -DskipTests

#---------------------------------------------------------------------------------

# Stage 2: Create a lightweight image for running the application
# Use a much smaller Java Runtime Environment (JRE) base image for the final product.
FROM openjdk:17-jre-slim

# Expose the port where your application will run. For web apps, this is commonly 8080.
EXPOSE 8080

# Copy the executable JAR file from the "build" stage to the final image.
COPY --from=build /app/target/*.jar app.jar

# Define the command to start your application when the container launches.
ENTRYPOINT ["java", "-jar", "app.jar"]
