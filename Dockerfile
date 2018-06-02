FROM openjdk:10
ADD ./build/libs/esl-rest.jar /app.jar
ENTRYPOINT ["java","-Djava.security.egd=file:/dev/./urandom","-jar","/app.jar","-Xmx1g -XX:+UseConcMarkSweepGC -XX:+PrintGCDetails -XX:+PrintHeapAtGC"]
