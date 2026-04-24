FROM maven:3.9.9-eclipse-temurin-21 AS build

WORKDIR /app
COPY . .
RUN mvn -pl pricing-service-java -am -DskipTests package && \
    cp "$(find pricing-service-java/target -maxdepth 1 -name '*.jar' ! -name 'original-*' | head -n 1)" /tmp/app.jar

FROM eclipse-temurin:21-jre

WORKDIR /app
COPY --from=build /tmp/app.jar /app/app.jar

EXPOSE 10000
CMD ["sh", "-c", "java $JAVA_OPTS -Dserver.port=${PORT:-10000} -jar /app/app.jar"]
