# Virtual Pet — Backend

Spring Boot 4 / Java 25 backend service.

## Stack

- Java 25 (Temurin), Maven (wrapper checked in)
- Spring Boot 4.0 (web, security, data-jpa, data-redis, validation, mail, actuator)
- PostgreSQL 17 (Flyway migrations, multi-schema)
- Redis 7.4
- JWT auth (jjwt)
- springdoc-openapi (Swagger UI)
- Spotless + google-java-format, JaCoCo, Maven Enforcer

## Requirements

- JDK 25
- Docker / Docker Compose (for the full stack)
- Maven is **not** required — use the bundled `./mvnw`

## Run locally

```sh
cp .env.example .env       # then edit secrets
docker compose up --build
```

The API is exposed at `http://localhost:8080`.

| Endpoint                          | What it is                  |
| --------------------------------- | --------------------------- |
| `GET /actuator/health`            | Liveness / readiness probe  |
| `GET /actuator/prometheus`        | Prometheus metrics          |
| `GET /swagger-ui.html`            | OpenAPI UI                  |
| `GET /v3/api-docs`                | OpenAPI JSON                |

## Common commands

```sh
./mvnw spotless:apply       # format
./mvnw spotless:check       # CI-style format check
./mvnw test                 # unit tests
./mvnw verify               # tests + JaCoCo coverage gate
./mvnw spring-boot:run      # run app against your local Postgres/Redis
```

## Project layout

```text
src/main/java/com/virtualpet/
  auth/        # authentication and account management
  cart/
  catalog/
  common/      # cross-cutting code (filters, exceptions, etc.)
  health/
  orders/
  shipments/
src/main/resources/
  application.properties
  db/migration/  # Flyway V* and R__* scripts
  logback-spring.xml
```

## Database migrations

- Versioned migrations (`V*`) are immutable once merged.
- Repeatable (`R__*`) must be idempotent.
- Schemas: `auth`, `catalog`, `orders`, `logistics`.

## Contributing

See [`CONTRIBUTING.md`](CONTRIBUTING.md). Security issues: [`SECURITY.md`](SECURITY.md).
