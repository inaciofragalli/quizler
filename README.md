# Web Quiz Engine — API

A RESTful quiz API built with Spring Boot, Spring Security, JWT-based authentication (HttpOnly cookies), CSRF protection, and per-client rate limiting.

## Tech Stack

- Java / Spring Boot
- Spring Security
- Spring Data JPA + PostgreSQL
- JJWT (JSON Web Tokens)
- Bucket4j (rate limiting)
- Gradle

## Authentication

Authentication uses JWTs stored in an **HttpOnly cookie** (not returned in the response body), protecting the token from being read or stolen via XSS.

- Cookie name: `jwt`
- `HttpOnly` always. `Secure`, `SameSite`, and `Partitioned` differ by environment — see below.
- Sent automatically by the browser on every request once set.

### Local vs. production cookie config

This project currently maintains **two separate `SecurityConfig` files** (one for local dev, one for production), because the correct cookie attributes genuinely differ by environment:

| Attribute | Local (`localhost:5173` → `localhost:8080`) | Production (Vercel → Render) |
|---|---|---|
| `Secure` | `false` | `true` |
| `SameSite` | `Lax` | `None` |
| `Partitioned` | not set | `true` |

Production requires `Secure` + `SameSite=None` because the frontend and backend are on different domains. As of late 2025, cross-site cookies also require the `Partitioned` attribute (CHIPS) or modern browsers will reject them. `Partitioned` cookies additionally can't be read via `document.cookie` from a different origin — see the frontend README for how this is worked around via a Vercel rewrite proxy that makes the two appear same-origin in production.

> These two config files are hand-maintained in parallel. Any fix to shared logic (e.g. the CSRF filter, rate limiting) needs to be applied to both — this is a known source of merge friction and a candidate for future consolidation into a single environment-variable-driven config.

### `POST /api/auth/login`

Authenticates a user and sets the `jwt` cookie on success.

**Request body:** `{ "username": "string", "password": "string" }`

| Status | Meaning |
|---|---|
| `200 OK` | Login successful. `Set-Cookie: jwt=...` present. |
| `400 Bad Request` | Validation failure. |
| `401 Unauthorized` | Invalid username or password. |

### `POST /api/register`

Registers a new user. Same request/response shape as login, minus the cookie.

### `POST /api/auth/logout`

Clears the `jwt` cookie. Requires a valid CSRF token (unlike login/register — logout is a state-changing action on an authenticated session, so it's intentionally *not* CSRF-exempt).

### `GET /api/auth/me`

Returns the current authenticated user's username, based on the JWT cookie. Used by the frontend to determine login state on page load, since the cookie itself isn't readable by JS.

### `GET /api/auth/csrf`

Returns the current CSRF token in the response body (`{"token": "..."}"`) *and* sets the `XSRF-TOKEN` cookie. Explicitly resolves the token server-side (`CsrfToken.getToken()`) to force the cookie to be written — without this, Spring Security's deferred token loading means the cookie may never be issued at all for GET requests that don't otherwise touch the token.

## CSRF Protection

Uses the double-submit cookie pattern via `CookieCsrfTokenRepository`:

- A separate, JS-readable `XSRF-TOKEN` cookie is issued alongside the JWT.
- Clients must echo its value back as an `X-XSRF-TOKEN` header on all state-changing requests (POST/PUT/DELETE).
- `/api/register` and `/api/auth/login` are exempt (no authenticated session exists yet to protect). Everything else — including logout — requires a valid, matching token.

**`CsrfCookieFilter`** is registered in the filter chain specifically to force `CsrfToken.getToken()` resolution on every request. Without it, certain authenticated requests (notably the first one after login) can trigger Spring Security to *delete* the CSRF cookie rather than reissue it, as part of its session-fixation protection tied to authentication events. This was a real, subtle production bug — see git history around `CsrfCookieFilter`'s introduction for context.

## Rate Limiting

`RateLimitFilter` applies a per-client token bucket (Bucket4j), currently 20 requests/minute (adjust `REQUESTS_PER_MINUTE` — this is intentionally low for testing and should be raised before real usage). Exceeding the limit returns `429` with `{"error":"Too many requests"}`.

Client identification uses `X-Forwarded-For` (falling back to `getRemoteAddr()` for local dev) — required because Render sits behind a reverse proxy, and `getRemoteAddr()` alone would return the proxy's IP, causing all users to share a single bucket.

> Frontend note: a `429` must never be treated as "not authenticated." Only a genuine `401` from `/me` should clear client-side auth state — rate limiting is a transient condition, not proof of an invalid session.

## Quiz Endpoints

All require a valid `jwt` cookie (`401` if missing/invalid). Mutating endpoints (POST/DELETE) additionally require a valid CSRF token (`403` if missing/mismatched).

| Method | Path | Description |
|---|---|---|
| `GET` | `/api/quizzes` | Paginated list of all quizzes (`Page<QuizResponse>`) |
| `GET` | `/api/quizzes/{id}` | Single quiz detail, including the current user's prior submission if they've already solved it |
| `POST` | `/api/quizzes` | Create a quiz (creator is set from the authenticated user) |
| `DELETE` | `/api/quizzes/{id}` | Delete a quiz — creator only, `403` otherwise |
| `POST` | `/api/quizzes/{id}/solve` | Submit an answer. Records a `QuizCompletion`. A quiz can only be solved once per user — resubmission is rejected once a completion exists. |

`QuizResponse` never includes the correct answer — only `options` and (once solved) the user's own prior selection, so the answer can't be read from the network tab before solving.

## Environment Variables

| Variable | Required | Description |
|---|---|---|
| `JWT_SECRET` | Yes | HMAC signing key. App fails to start if missing — no insecure default. |
| `JWT_EXPIRATION` | No (default `86400000`) | Token lifetime in ms. |
| `DB_HOST` / `DB_PORT` / `DB_NAME` / `DB_USERNAME` / `DB_PASSWORD` | Yes | Substituted into `spring.datasource.*` in `application.properties`. |

## Running Locally

```bash
./gradlew clean build
./gradlew bootRun
```

Requires PostgreSQL running locally with matching env vars set (via IntelliJ run config or `application.properties`). Hibernate creates/updates tables automatically with `ddl-auto=update` in dev.

## Known Gotchas (learned the hard way)

- **Two `SecurityConfig` files exist** for local/prod — see above. Any CSRF/filter-chain fix needs manual porting between them.
- **CSRF cookie can be silently deleted on the first authenticated GET after login** if `CsrfCookieFilter` isn't in the chain — see the CSRF section above.
- **Rate limiting must key off `X-Forwarded-For` in production**, or all users behind Render's proxy share one bucket.
