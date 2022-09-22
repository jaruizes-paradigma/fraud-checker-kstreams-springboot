ARG BUILD_IMAGE=maven:3-openjdk-11-slim
ARG RUNTIME_IMAGE=adoptopenjdk/openjdk11:jre-11.0.9.1_1-alpine

FROM ${BUILD_IMAGE} AS MAVEN_TOOL_CHAIN
COPY pom.xml /tmp/
COPY src /tmp/src/
WORKDIR /tmp/
RUN mvn package

FROM ${RUNTIME_IMAGE}
COPY --from=MAVEN_TOOL_CHAIN /tmp/target/*.jar /app/generator.jar
EXPOSE 8090
CMD ["java", "-jar", "/app/generator.jar"]