FROM openjdk:21-jdk

USER root
VOLUME /app

COPY --chown=root:root build/libs/binance-app-*.jar /app/binance.jar

EXPOSE 8085
ENTRYPOINT ["java", "-jar", "/app/binance.jar"]