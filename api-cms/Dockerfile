FROM eclipse-temurin:21-jdk-alpine AS builder
WORKDIR /app

COPY gradle/ gradle/
COPY gradlew .
COPY settings.gradle .
COPY build.gradle .

RUN chmod +x ./gradlew
RUN ./gradlew dependencies --no-daemon

COPY src ./src
RUN ./gradlew bootJar --no-daemon -x test

FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

COPY --from=builder /app/build/libs/*.jar app.jar

ENTRYPOINT ["java", "-jar", "app.jar"]