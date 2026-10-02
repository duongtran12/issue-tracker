# Database

PostgreSQL is the production database. H2 is used only by automated tests.

## Schema ownership

Flyway migrations in `src/main/resources/db/migration` are the source of truth.
Never edit a migration that has been applied to a shared environment. Add a new
versioned migration instead.

## Relationships

- Users own projects and may join additional projects.
- Projects contain issues and memberships.
- Issues reference a reporter and optional assignee.
- Comments and history rows are deleted with their issue.

Back up the database before deploying a release that contains new migrations.
