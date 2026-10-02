# Operations

## Health

Use `/actuator/health` for readiness and liveness checks. A healthy response
must report `UP`.

## Logging

Every request receives an `X-Request-Id`. Search logs by that value when
investigating a failed client call. Request bodies and authorization headers
must not be logged.

## Shutdown

The backend uses graceful shutdown with a bounded drain period. Stop containers
with `docker compose down` so active requests can finish.

## Recovery

Restore PostgreSQL from the latest verified backup, deploy the matching
application revision, and allow Flyway to apply only migrations newer than the
restored schema version.
