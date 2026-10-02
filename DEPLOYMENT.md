# Deployment checklist

Before deploying:

- Use a unique JWT secret of at least 32 random bytes.
- Use a dedicated PostgreSQL account with a strong password.
- Restrict `CORS_ALLOWED_ORIGINS` to the deployed frontend origins.
- Terminate TLS at a trusted reverse proxy or load balancer.
- Keep `/actuator/health` reachable by the platform health checker.
- Back up PostgreSQL before applying new Flyway migrations.
- Run backend tests and the frontend production build.

After deploying:

- Confirm the health endpoint returns `UP`.
- Register a disposable account and exercise one project workflow.
- Verify logs do not contain credentials, tokens, or request bodies.
