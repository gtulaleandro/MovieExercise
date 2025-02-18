FROM amazoncorretto:17-alpine AS build
WORKDIR /app

COPY build/libs/demo-0.0.1-SNAPSHOT.jar app.jar

FROM amazoncorretto:17-alpine
WORKDIR /app

COPY --from=build /app/app.jar app.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]
