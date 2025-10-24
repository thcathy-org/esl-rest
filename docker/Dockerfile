FROM openjdk:21-jdk
ADD ./build/libs/esl-rest.jar /app.jar
ENTRYPOINT ["java","-Djava.security.egd=file:/dev/./urandom","-Xmx2g","-Xms512m","-Xlog:gc","-XX:+HeapDumpOnOutOfMemoryError","-XX:HeapDumpPath=/log/java_pid%p.hprof","-Dlogging.config=classpath:logback-docker.xml","-jar","/app.jar"]
