FROM maven:3.9.9-amazoncorretto-21-al2023

COPY src/ app/src/
COPY pom.xml app/pom.xml

WORKDIR /app

### Update the system and install dependencies
RUN dnf update -y && \
    curl -sL https://rpm.nodesource.com/setup_22.x | bash - && \
    dnf install -y nodejs

### Compile the project
RUN \
    mvn clean package -DskipTests && \
    chmod +x target/DayGuard-*.jar && \
    mv target/DayGuard-*.jar target/DayGuard.jar && \
    cd src/main/resources/static && \
    npm install && \
    npx vite build


CMD [ "java", "-jar", "target/DayGuard.jar" ]
