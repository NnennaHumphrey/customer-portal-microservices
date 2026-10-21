FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /workspace
COPY . .
ARG MODULE
RUN mvn -B -q -pl ${MODULE} -am -DskipTests package

FROM eclipse-temurin:21-jre
ARG MODULE
WORKDIR /app
COPY --from=build /workspace/${MODULE}/target/${MODULE}-*.jar app.jar
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]
