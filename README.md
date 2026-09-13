# Issue Tracker

A Spring Boot REST API for managing users, projects, issues, comments, and issue history.

## Stack

- Java 21
- Spring Boot 4.1
- Spring Security with JWT authentication
- Spring Data JPA and Hibernate
- PostgreSQL with Flyway migrations
- H2 for tests

## Requirements

- JDK 21
- PostgreSQL 14 or newer

## Local setup

Create a PostgreSQL database named `issue_tracker`, then run:

```powershell
$env:DB_URL = "jdbc:postgresql://localhost:5432/issue_tracker"
$env:DB_USERNAME = "issue_tracker"
$env:DB_PASSWORD = "issue_tracker"
$env:JWT_SECRET = "replace-with-a-secret-at-least-256-bits-long"
./mvnw.cmd spring-boot:run
```

The API starts on `http://localhost:8080` by default. Flyway applies migrations automatically at startup.

For a local test run without PostgreSQL:

```powershell
./mvnw.cmd test
```

Tests use the H2 test profile and do not require a running database.

## Configuration

| Variable | Default | Purpose |
| --- | --- | --- |
| `DB_URL` | `jdbc:postgresql://localhost:5432/issue_tracker` | PostgreSQL JDBC URL |
| `DB_USERNAME` | `issue_tracker` | Database user |
| `DB_PASSWORD` | `issue_tracker` | Database password |
| `JWT_SECRET` | Development fallback | JWT signing secret |
| `JWT_EXPIRATION` | `3600000` | JWT lifetime in milliseconds |
| `SERVER_PORT` | `8080` | HTTP port |

Never use the development JWT fallback in a deployed environment.
