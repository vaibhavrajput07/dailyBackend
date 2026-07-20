# Backend Local Setup

## Prerequisites
- Java 21
- MySQL 8 (Windows service `MySQL80` running locally)

## 1. Create a dedicated MySQL user

The app never uses the MySQL root account. Run once, using your existing root login:

```sql
CREATE USER 'daily'@'localhost' IDENTIFIED BY 'daily';
GRANT ALL PRIVILEGES ON daily.* TO 'daily'@'localhost';
FLUSH PRIVILEGES;
```

The database `daily` itself is auto-created on first run (`createDatabaseIfNotExist=true` in the JDBC URL), so you don't need to `CREATE DATABASE` manually — the `daily` user just needs `CREATE` privilege, which `ALL PRIVILEGES` above already grants.

**Why a dedicated user instead of root**: this account can only touch the `daily` schema — it has no access to any other database and no server-admin privileges. That means the `daily`/`daily` default below is safe to keep as a committed local-dev convenience: even if it leaks, the blast radius is one throwaway local schema, not your whole MySQL instance. Never give the app's DB user root/admin privileges, in dev or prod.

**If you previously ran the app against `root`**: rotate your real MySQL root password now (`ALTER USER 'root'@'localhost' IDENTIFIED BY '<new password>';`) — an earlier version of this file's committed defaults pointed at a real root password, which is now permanently in git history even after being edited out.

## 2. Environment variables

Copy `.env.example` to `.env` (git-ignored) and fill in values — Spring Boot loads it automatically on startup via `spring.config.import` in `application.yml`. A `.env` matching step 1 (`daily`/`daily`/`daily` plus a generated JWT secret) is already present for local dev, so `./gradlew bootRun` works out of the box; nothing sensitive is defaulted inside `application.yml` itself anymore, so `DB_PASSWORD` and `JWT_SECRET` **must** come from `.env` locally or real env vars in every other environment — the app fails to start without them.

| Variable | Default | Purpose |
|---|---|---|
| `DB_HOST` | `localhost` | MySQL host |
| `DB_PORT` | `3306` | MySQL port |
| `DB_NAME` | `daily` | Database name |
| `DB_USERNAME` | `daily` | MySQL user |
| `DB_PASSWORD` | *(required, no default)* | MySQL password |
| `JWT_SECRET` | *(required, no default)* | HMAC signing key for JWTs, at least 32 bytes. Generate one with `openssl rand -base64 48`. |
| `JWT_EXPIRATION_MS` | `2592000000` (30 days) | Token lifetime |
| `SERVER_PORT` | `8080` | HTTP port |
| `CORS_ALLOWED_ORIGINS` | `http://localhost:*,http://127.0.0.1:*` | Comma-separated allowed origin patterns |

## 3. Run

```bash
./gradlew bootRun
```

Flyway runs migrations automatically on startup. API docs (Swagger UI) at `http://localhost:8080/swagger-ui.html`.

If port 8080 is already taken by something else on your machine, override it:

```bash
SERVER_PORT=8081 ./gradlew bootRun
```
