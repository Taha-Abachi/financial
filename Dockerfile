# Use an official OpenJDK runtime as the base image
FROM openjdk:21-jdk

LABEL authors="t.abachi"
# Set the working directory inside the container
WORKDIR /app

# Copy the built JAR file into the container
COPY financial-0.0.1-SNAPSHOT.jar financial-0.0.1-SNAPSHOT.jar

# Expose the port your application runs on
EXPOSE 8081

# Command to run the application
ENTRYPOINT ["java", "-jar", "financial-0.0.1-SNAPSHOT.jar"]