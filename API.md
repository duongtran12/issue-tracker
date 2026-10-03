# Issue Tracker API

Base URL: `http://localhost:8080`

Protected endpoints require:

```http
Authorization: Bearer <accessToken>
```

Every response includes an `X-Request-Id` header. Clients may send a safe
`X-Request-Id` value of up to 64 characters to correlate API calls with
server logs.

## Authentication

### Register

```http
POST /api/auth/register
Content-Type: application/json
```

```json
{
  "username": "duong",
  "fullName": "Duong Tran",
  "email": "duong@example.com",
  "password": "Password123!"
}
```

### Login

```http
POST /api/auth/login
Content-Type: application/json
```

```json
{
  "username": "duong",
  "password": "Password123!"
}
```

Response contains `accessToken`, `tokenType`, and `expiresIn` seconds.

### Current user

```http
GET /api/auth/me
```

### User profile by username

```http
GET /api/auth/users/{username}
```

Requires authentication.

## Projects

All project endpoints require authentication.

```http
POST   /api/projects
GET    /api/projects
GET    /api/projects/{projectId}
PUT    /api/projects/{projectId}
DELETE /api/projects/{projectId}
```

Project body:

```json
{
  "name": "Issue Tracker",
  "key": "ISSUE",
  "description": "Project management"
}
```

Project members:

```http
POST   /api/projects/{projectId}/members
GET    /api/projects/{projectId}/members
DELETE /api/projects/{projectId}/members/{username}
```

Add member body:

```json
{
  "username": "alice"
}
```

## Issues

```http
POST   /api/projects/{projectId}/issues
GET    /api/projects/{projectId}/issues
GET    /api/projects/{projectId}/issues/{issueId}
PUT    /api/projects/{projectId}/issues/{issueId}
DELETE /api/projects/{projectId}/issues/{issueId}
```

## Labels

```http
GET    /api/projects/{projectId}/labels
POST   /api/projects/{projectId}/labels
PUT    /api/projects/{projectId}/labels/{labelId}
DELETE /api/projects/{projectId}/labels/{labelId}
```

Create and update labels with a name and six-digit hex color, for example
`{"name":"frontend","color":"#2563EB"}`. Label names are unique within a project.

Issue body:

```json
{
  "title": "Fix login",
  "description": "Handle expired token",
  "status": "TODO",
  "priority": "MEDIUM",
  "assigneeUsername": "alice",
  "dueDate": "2026-10-31",
  "labelIds": [1, 3]
}
```

`dueDate` is optional and uses the ISO `YYYY-MM-DD` format.
`labelIds` is optional; every selected label must belong to the issue's project.

The list endpoint supports `status`, `priority`, `assigneeUsername`, `keyword`, `page`, `size`, and `sort` query parameters. The default page size is 20 and default sort is `createdAt,desc`. Page size is capped at 100. Supported sort fields are `createdAt`, `updatedAt`, `title`, `status`, and `priority`.

## Comments and history

```http
POST   /api/projects/{projectId}/issues/{issueId}/comments
GET    /api/projects/{projectId}/issues/{issueId}/comments
PUT    /api/projects/{projectId}/issues/{issueId}/comments/{commentId}
DELETE /api/projects/{projectId}/issues/{issueId}/comments/{commentId}
GET    /api/projects/{projectId}/issues/{issueId}/history
```

Comment body:

```json
{
  "body": "I reproduced this issue."
}
```

## Input behavior

Text fields are trimmed and repeated whitespace is normalized before persistence. Passwords are only trimmed at the beginning and end, so spaces inside a password remain significant.

## Errors

Errors are returned as JSON. Common status codes are `400` for validation, `401` for missing or invalid authentication, `403` for denied access, `404` for missing resources, and `409` for duplicate resources.

Validation errors include an `errors` object keyed by request field:

```json
{
  "status": 400,
  "message": "Validation failed",
  "errors": {
    "title": "Issue title is required"
  }
}
```

Frontend flow: register or login, store `accessToken`, send it as a Bearer token for protected requests, and clear it when a request returns `401`.

## Quick cURL examples

```bash
# Login and copy the accessToken from the response.
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"duong","password":"Password123!"}'

# List the current user's projects.
curl http://localhost:8080/api/projects \
  -H "Authorization: Bearer <accessToken>"

# Search issues in a project.
curl "http://localhost:8080/api/projects/1/issues?keyword=login&page=0&size=20" \
  -H "Authorization: Bearer <accessToken>"
```
