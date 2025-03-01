FROM gradle:8.7-jdk21

COPY . .

RUN gradle installDist

CMD ["java", "-jar", "build/libs/app-0.0.1-SNAPSHOT.jar"]