# Issue Tracker frontend

React and TypeScript dashboard for the Issue Tracker API.

## Run locally

```powershell
npm install
npm run dev
```

Vite serves the application at `http://localhost:5173`. During development,
requests under `/api` are proxied to `http://localhost:8080`.

To use a different backend, create `.env.local`:

```text
VITE_API_URL=http://localhost:8080/api
```

## Quality checks

```powershell
npm run lint
npm run build
```

The dashboard supports authentication, project creation, issue creation,
filtering, status transitions, and session-expiry recovery. The API module also
contains typed clients for project members, issue comments, and issue history.
