# Step 1: run tests and build über jar
FROM maven:3.8-eclipse-temurin-11-alpine as build

# declarar variables de entorno
ARG github_actor
ARG github_token
ENV GITHUB_ACTOR=$github_actor
ENV GITHUB_TOKEN=$github_token

WORKDIR /build

# Cache Maven dependencies
COPY ./pom.xml .
COPY ./settings.xml .
RUN mvn -e -s settings.xml dependency:go-offline

# Cache Maven dependencies
COPY ../pom.xml .
RUN mvn dependency:go-offline

# Build project
COPY ./src/ ./src/
RUN mvn -e -s settings.xml package -DskipTests


# Step 2: package über jar
FROM eclipse-temurin:11-jre-alpine
LABEL maintainer="avirgili@eclub.com.py"

# Create system user
RUN addgroup --system spring
RUN adduser --system spring --ingroup spring
USER spring:spring

WORKDIR /home/spring
COPY --from=build --chown=spring:spring build/target/*.jar ./app.jar
CMD ["java", "-jar", "app.jar"]