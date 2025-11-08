# Stage 1: Build the application with Java 21
FROM maven:3-eclipse-temurin-21 AS build
WORKDIR /app
COPY pom.xml .
COPY src ./src

# Run the build command to create the .jar file
RUN mvn clean package -DskipTests

# ---

# Stage 2: Create the final, lightweight Java 21 image
FROM eclipse-temurin:21-jdk-alpine
WORKDIR /app

# Copy the .jar file from the 'build' stage
COPY --from=build /app/target/notevault-0.0.1-SNAPSHOT.jar app.jar

# Run the application
ENTRYPOINT ["java","-jar","app.jar"]