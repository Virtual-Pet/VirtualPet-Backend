FROM eclipse-temurin:21-jdk-alpine AS builder

WORKDIR /apps

COPY . .

RUN ./mvnw clean package -DskipTests

FROM eclipse-temurin:21-jre-alpine

WORKDIR /apps

COPY --from=builder /apps/target/*.jar app.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]