# Testing

Run backend tests from the repository root:

```powershell
./mvnw.cmd test
```

Run frontend checks from `frontend/`:

```powershell
npm ci
npm run typecheck
npm run lint
npm run build
```

Backend tests use the `test` profile and an in-memory H2 database. A local
PostgreSQL instance is not required. GitHub Actions repeats these checks for
pull requests that affect the corresponding application.
