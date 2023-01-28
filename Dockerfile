FROM openjdk:19-jdk-slim
COPY verbo.jar /app/verbo.jar

CMD ["java", "-jar", "/app/verbo.jar"]