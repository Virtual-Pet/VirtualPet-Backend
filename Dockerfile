# syntax=docker/dockerfile:1.7

############################
# Build stage
############################
FROM eclipse-temurin:25-jdk-alpine AS builder

WORKDIR /workspace

# Copy build descriptors first so the dependency layer is cached.
COPY .mvn/ .mvn/
COPY mvnw pom.xml ./
RUN chmod +x mvnw && ./mvnw -B -q dependency:go-offline

# Now copy sources and build.
COPY src/ src/
RUN ./mvnw -B -q -DskipTests clean package \
    && mkdir -p target/extracted \
    && java -Djarmode=tools -jar target/*.jar extract --layers --launcher --destination target/extracted

############################
# Runtime stage
############################
FROM eclipse-temurin:25-jre-alpine

RUN addgroup -S app && adduser -S app -G app

WORKDIR /app

# Copy the exploded Spring Boot layers in dependency order so the layer cache
# survives most source-only changes.
COPY --from=builder --chown=app:app /workspace/target/extracted/dependencies/          ./
COPY --from=builder --chown=app:app /workspace/target/extracted/spring-boot-loader/    ./
COPY --from=builder --chown=app:app /workspace/target/extracted/snapshot-dependencies/ ./
COPY --from=builder --chown=app:app /workspace/target/extracted/application/           ./

USER app

ENV JAVA_TOOL_OPTIONS="-XX:MaxRAMPercentage=75 -XX:+ExitOnOutOfMemoryError -Djava.security.egd=file:/dev/./urandom"
ENV SERVER_PORT=8080

EXPOSE 8080

HEALTHCHECK --interval=30s --timeout=5s --start-period=40s --retries=3 \
    CMD wget -qO- "http://localhost:${SERVER_PORT}/actuator/health" | grep -q '"status":"UP"' || exit 1

ENTRYPOINT ["java", "org.springframework.boot.loader.launch.JarLauncher"]
