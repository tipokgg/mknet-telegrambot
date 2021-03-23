FROM openjdk:11
ARG JAR_FILE=target/*.jar
COPY ${JAR_FILE} schedule_telegrambot-1.0-SNAPSHOT-shaded.jar
ENTRYPOINT ["java","-jar","/schedule_telegrambot-1.0-SNAPSHOT-shaded.jar"]