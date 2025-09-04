# Stage 1: Build the application
# Use a more reliable Java/Maven image. `eclipse-temurin` is a great choice.
FROM eclipse-temurin:17-jdk-focal AS build

# Set the working directory inside the container
WORKDIR /app

# Copy the build file (`pom.xml`) from your repository into the container.
COPY pom.xml .

# Copy all your Java source code and resources into the container.
COPY src ./src

# Build your application using Maven. The `clean package` command compiles your code
# and packages it into a JAR file. `-DskipTests` makes the build faster.
RUN mvn clean package -DskipTests

#---------------------------------------------------------------------------------

# Stage 2: Create a lightweight image for running the application
# Use a much smaller JRE (Java Runtime Environment) base image for the final, runnable image.
FROM eclipse-temurin:17-jre-focal

# Expose the port your application will run on. This is commonly 8080.
EXPOSE 8080

# Copy the executable JAR file from the "build" stage to the final image.
# The `*` is a wildcard, which handles different JAR names.
COPY --from=build /app/target/*.jar app.jar

# This command tells the container how to start your application.
ENTRYPOINT ["java", "-jar", "app.jar"]
