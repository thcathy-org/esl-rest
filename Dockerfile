FROM openjdk:10
ADD ./build/libs/esl-rest.jar /app.jar
HEALTHCHECK --interval=5s --timeout=10s --retries=3 CMD curl -sS http://localhost:8080 || exit 1
ENTRYPOINT ["java","-Djava.security.egd=file:/dev/./urandom","-jar","/app.jar","-Xmx2g -XX:+UseConcMarkSweepGC -XX:+PrintGCDetails -XX:+PrintHeapAtGC"]
