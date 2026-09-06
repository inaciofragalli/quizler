# Quizler — API

A RESTful quiz API built with Spring Boot, Spring Security, and JWT-based authentication (HttpOnly cookies).

## Tech Stack

- Java / Spring Boot
- Spring Security
- Spring Data JPA + PostgreSQL
- JJWT (JSON Web Tokens)
- Gradle

## Authentication

Authentication uses JWTs stored in an **HttpOnly, Secure cookie** (not returned in the response body). This protects the token from being read or stolen via XSS.

- Cookie name: `jwt`
- `HttpOnly`, `Secure`, `SameSite=None`
- Sent automatically by the browser on every request to the API once set — no manual header required from the client.

> Note: Because the token isn't accessible to JavaScript, clients (e.g. a browser frontend) must send requests with `credentials: 'include'` (fetch) or `withCredentials: true` (axios) so the cookie is included cross-origin.

### `POST /api/auth/login`

Authenticates a user and sets the `jwt` cookie on success.

**Request body:**
```json
{
  "username": "string",
  "password": "string"
}
```

**Responses:**
| Status | Meaning |
|---|---|
| `200 OK` | Login successful. `Set-Cookie: jwt=...` header present. |
| `400 Bad Request` | Missing/invalid fields (validation failure). |
| `401 Unauthorized` | Invalid username or password. |

### `POST /api/register`

Registers a new user.

**Request body:**
```json
{
  "username": "string",
  "password": "string"
}
```

**Responses:**
| Status | Meaning |
|---|---|
| `200/201` | User created. |
| `400 Bad Request` | Validation failure (e.g. missing fields). |
| `409 Conflict` | Username already taken (if enforced). |

## Protected Endpoints

All endpoints other than `/api/register` and `/api/auth/login` require a valid `jwt` cookie. Requests without one, or with an invalid/expired token, receive `401 Unauthorized`.

> Fill in the specific quiz endpoints below as they're finalized (e.g. `GET /api/quizzes`, `POST /api/quizzes`, `POST /api/quizzes/{id}/solve`, `GET /api/quizzes/completed`).

| Method | Path | Description | Auth required |
|---|---|---|---|
| `GET` | `/api/quizzes` | List all quizzes | Yes |
| `GET` | `/api/quizzes/{id}` | Get a single quiz | Yes |
| `POST` | `/api/quizzes` | Create a quiz | Yes |
| `DELETE` | `/api/quizzes/{id}` | Delete a quiz (creator only) | Yes |
| `POST` | `/api/quizzes/{id}/solve` | Submit an answer | Yes |
| `GET` | `/api/quizzes/completed` | List quizzes the user has completed | Yes |

## CORS

CORS is configured to allow credentialed requests (cookies) from specific trusted origins only — not `*`, since wildcard origins are incompatible with `allowCredentials`.

Configured origins (update as environments are added):
- `http://localhost:5173` (Vite dev server)
- Production frontend URL (e.g. Vercel deployment)

## Environment Variables

| Variable | Required | Description |
|---|---|---|
| `JWT_SECRET` | Yes | HMAC signing key for JWTs. App fails to start if missing. |
| `JWT_EXPIRATION` | No (default: `86400000`) | Token lifetime in milliseconds. |
| `DB_HOST` | Yes | PostgreSQL host (e.g. `localhost`). |
| `DB_PORT` | Yes | PostgreSQL port (e.g. `5432`). |
| `DB_NAME` | Yes | Database name. |
| `DB_USERNAME` | Yes | Database username. |
| `DB_PASSWORD` | Yes | Database password. |

These are substituted into `spring.datasource.url`, `spring.datasource.username`, and `spring.datasource.password` in `application.properties`:

```properties
spring.datasource.url=jdbc:postgresql://${DB_HOST}:${DB_PORT}/${DB_NAME}
spring.datasource.username=${DB_USERNAME}
spring.datasource.password=${DB_PASSWORD}
spring.datasource.driver-class-name=org.postgresql.Driver
```

## Running Locally

```bash
./gradlew clean build
./gradlew bootRun
```

Ensure PostgreSQL is running and the `users`/`quiz`/`quiz_completion` tables exist (Hibernate will create/update them automatically with `spring.jpa.hibernate.ddl-auto=update` in dev).est
```
