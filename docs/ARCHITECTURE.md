# Architecture

The application is a stateless React and Spring Boot system.

- The React client calls JSON endpoints below `/api`.
- Spring Security authenticates Bearer JWTs before controller execution.
- Controllers validate transport input and delegate to transactional services.
- Services enforce project membership and ownership rules.
- Spring Data repositories persist entities through Hibernate.
- Flyway owns the PostgreSQL schema and applies ordered migrations.
- Nginx serves the production frontend and proxies API requests to Spring Boot.

Issue changes are recorded separately from comments so the activity panel can
show an immutable history alongside user discussion.
