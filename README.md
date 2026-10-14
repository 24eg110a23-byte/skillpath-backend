# Skillpath API deployment

## Deploy to Railway

1. Create a Railway project from the `skillpath-backend` GitHub repository. Railway uses the included Dockerfile to build and run the Spring Boot API.
2. Add a Railway PostgreSQL service. In the API service's Variables settings, reference the database service's `PGHOST`, `PGPORT`, `PGDATABASE`, `PGUSER`, and `PGPASSWORD` variables. The API starts with its PostgreSQL profile automatically.
3. Generate a public domain for the API service. The health check is `/api/meta/levels`.
4. Set `SKILLPATH_CORS_ALLOWED_ORIGINS` in the API service to the full Vercel frontend origin, for example `https://skillpath-frontend.vercel.app` (no trailing slash). Add any custom frontend domain as a comma-separated additional origin.

The API listens on Railway's `PORT` and uses PostgreSQL's `PG*` connection variables. Hibernate updates the schema on startup.
