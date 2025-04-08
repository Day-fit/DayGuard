FROM maven:3.9.9-amazoncorretto-21-al2023

COPY src/ app/src/
COPY pom.xml app/pom.xml

WORKDIR /app

### Update the system
RUN dnf update -y

### Compile the project
RUN \
    mvn clean package -DskipTests && \
    chmod +x target/DayGuard-*.jar && \
    mv target/DayGuard-*.jar target/DayGuard.jar

CMD [ "java", "-jar", "target/DayGuard.jar" ]
