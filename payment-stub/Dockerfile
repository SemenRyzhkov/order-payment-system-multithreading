# syntax=docker/dockerfile:1

FROM gradle:8.10.2-jdk21 AS builder
WORKDIR /workspace
COPY . .
RUN --mount=type=cache,target=/home/gradle/.gradle \
    ./gradlew --no-daemon :payment-stub:bootJar --configuration-cache --build-cache -x test

FROM eclipse-temurin:21-jre
WORKDIR /app
ENV JAVA_OPTS=""
COPY --from=builder /workspace/payment-stub/build/libs/*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["sh","-c","java $JAVA_OPTS -jar app.jar"]