FROM openjdk:10
ENV JAVA_OPTS -Xmx2g
ADD ./build/libs/esl-rest.jar /app.jar
HEALTHCHECK --interval=30s --timeout=300s --retries=3 CMD curl -sS http://localhost:8080 || exit 1
ENTRYPOINT exec java -Djava.security.egd=file:/dev/./urandom $JAVA_OPTS -jar /app.jar
