FROM maven:3.6.0-jdk-11 as builder
ADD . /app
WORKDIR /app
RUN mvn clean install

FROM openjdk:11
ADD . /app
WORKDIR /app
COPY --from=builder /app/target/KinoBot-1.0-SNAPSHOT-jar-with-dependencies.jar target/KinoBot-1.0-SNAPSHOT-jar-with-dependencies.jar
ENTRYPOINT ["java","-jar","target/KinoBot-1.0-SNAPSHOT-jar-with-dependencies.jar", "TelegramBotApplicationKt"]