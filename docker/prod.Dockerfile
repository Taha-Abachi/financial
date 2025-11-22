FROM 192.168.15.3:5000/library/gradle:8.10.2 AS builder

WORKDIR /app

COPY build.gradle settings.gradle ./
COPY src ./src

RUN gradle clean build --no-daemon

FROM 192.168.15.3:5000/library/openjdk:21-jdk

WORKDIR /app

COPY --from=builder /app/build/libs/*.jar app.jar

EXPOSE 8081

ENTRYPOINT ["java", "-jar", "app.jar"]
