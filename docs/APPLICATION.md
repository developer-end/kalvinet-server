# KalviNet Server — Application Overview

> **Living document — open to all.** Plain Markdown under `docs/`.  
> Any AI agent, IDE assistant (Cursor, VS Code/Copilot, Claude, Windsurf, JetBrains, etc.), or human may **read and edit** this file.  
> **Must be updated** whenever APIs, security, tenancy, migrations, or services change.  
> Last reviewed: **2026-08-04**

Canonical agent map (edit paths): [`AGENTS.md`](../AGENTS.md)  
Deep-dive notes: [`docs/implementation/`](./implementation/)  
Doc hub: [`docs/README.md`](./README.md)  
Database detail: [`DATABASE_README.md`](../DATABASE_README.md)

---

## 1. Purpose

KalviNet Server is the **multi-tenant school & institution management API**. It authenticates users (password JWT + Google OAuth2 login), manages identity/catalog data (users, roles, schools, tenants), and exposes ERP endpoints under a shared servlet context for the Angular web client.

Base URL (local): `http://localhost:8081/erp`

---

## 2. Tech stack

| Layer | Choice |
|---|---|
| Runtime | Java **17**, Spring Boot **3.5.10** |
| API | Spring Web MVC, Validation, springdoc-openapi **2.8.5** |
| Security | Spring Security (stateless), OAuth2 Client (Google), JJWT **0.11.5** (HS256) |
| Persistence | Spring Data JPA / Hibernate, PostgreSQL, Flyway (custom multi-schema) |
| Cache | Spring Cache + Caffeine |
| Realtime | Raw WebSocket (`/ws`) |
| Build | Maven (`mvnw` / `mvnw.cmd`) |

Artifact: `app.school:administration:0.0.1-SNAPSHOT`  
Root package: `app.school.administration`

---

## 3. Runtime configuration (local)

| Setting | Value |
|---|---|
| Port | `8081` |
| Context path | `/erp` |
| Active profile | `local` → `application-local.yaml` |
| Datasource | `jdbc:postgresql://localhost:5454/kalvinet-local-dev` |
| Docker Postgres | `docker-compose.yml` — host port **5454** → container 5432 |
| CORS origins | `http://localhost:4200`, `http://localhost:3000` (env override) |
| Frontend URL (OAuth redirect) | `http://localhost:4200` |
| JWT | HS256 HMAC; access **15 days**; refresh **2 months** |
| Swagger UI | `http://localhost:8081/erp/swagger-ui.html` |

---

## 4. Module layout

```
app.school.administration/
├── KalviNetApplication.java
├── auth/          # identity, JWT login, OAuth accounts, users/roles/tenants, TenantContext
├── common/        # security, JWT filter, Flyway, cache, WebSocket, health, base CRUD
├── dashboard/     # dashboard config API
└── modules/
    └── school/    # first feature module (school CRUD)
```

Layering (per module): `api` → `application` → `domain` → `infrastructure` / `persistance`

---

## 5. Functional areas (current)

### 5.1 Authentication — `/api/v1/auth`

| Capability | Status |
|---|---|
| Sign in (username/password → access + refresh JWT) | Implemented (`POST /signIn`) |
| Sign up | Implemented (`POST /signUp`) |
| Sign out | **Gap** — constant exists; no controller method |
| Refresh token endpoint | **Gap** — constant exists; no controller method |
| Password hashing (BCrypt) | Implemented (sign-in also has plaintext equality fallback — see gaps) |
| Google OAuth2 redirect login | Partial — Spring OAuth2 login wired; success handler redirects to frontend **without issuing JWT** |
| OAuth account CRUD | Implemented (`/api/v1/oAuth`) — currently **public** |

OAuth entry: `GET /erp/oauth2/authorization/google`  
Callback: `/erp/login/oauth2/code/google`  
Success redirect: `{frontend-url}/login?oauth_success=true&email=&name=`

### 5.2 Identity catalog

| Resource | Base path | Operations | Notes |
|---|---|---|---|
| User | `/api/v1/user` | findById, create, update, deActivate (+ role mapping deActivate) | |
| Role | `/api/v1/rloe` | same CRUD pattern | **Typo in path** (`rloe` not `role`) |
| Tenant | `/api/v1/tenant` | findById, create, update, deActivate | Create triggers tenant Flyway migrate |
| School | `/api/v1/school` | same CRUD pattern | First ERP feature module |
| OAuth account | `/api/v1/oAuth` | same CRUD pattern | |

Shared subpaths: `/findById/{uuid}`, `/create`, `/update`, `/deActivate/{uuid}` (`AppCommonEndPoint`).

### 5.3 Dashboard — `/api/v1/dashboard`

| Capability | Status |
|---|---|
| `GET /config` | Implemented — mostly hardcoded metrics + `userRepository.count()` |

### 5.4 Cross-cutting APIs

| Endpoint | Status |
|---|---|
| `GET /api/health` | Implemented |
| `DELETE /api/requests/{requestId}/cancel` | Implemented (cancellable task registry) |
| `WS /ws` | Implemented — sends one CONNECTED JSON message on open |

### 5.5 Database & tenancy

| Capability | Status |
|---|---|
| Public schema Flyway (`db/migration/public`) | Implemented via `MasterFlywayConfig` |
| Extensions `pgcrypto`, `citext` | Implemented (V1) |
| Tables: user, oauth_accounts, role, user_roles, school, tenant | Implemented (V2–V3) |
| Tenant schema provisioning on tenant create | Partial — schema migrate path exists; **no tenant SQL scripts** |
| Runtime `TenantContext.setTenant` from JWT / `X-Tenant-ID` | **Gap** — setter never called |
| Hibernate `multiTenancy: SCHEMA` | Configured |

---

## 6. Security model

| Piece | Behavior |
|---|---|
| `SecurityConfig` | CSRF off, STATELESS, CORS, OAuth2 login + `OAuth2SuccessHandler`, JWT filter before username/password filter |
| `AuthConstant.PUBLIC_ENDPOINTS` | auth, oAuth, school, tenant, dashboard, ws, swagger, requests, health — **permitAll** |
| `JWTAuthFilter` | Bearer → validate → `CustomUserDetails` + `AppContextService.createAuthContexts` (user id only today) |
| `JWTService` | Issue/validate HS256 tokens; claims: `sub`=username, `roles` |
| Entry point | Unauthenticated → HTTP 401 |

Headers allowed by CORS include `Authorization`, `X-Tenant-ID`, `X-Request-Id` (`X-Tenant-ID` not consumed yet).

---

## 7. Core services

| Service | Responsibility |
|---|---|
| `AuthService` / `AuthServiceImpl` | Sign in / sign up |
| `UserService`, `RoleService`, `TenantService`, `OAuthUserService` | Catalog CRUD |
| `CustomUserDetailsServiceImpl` | Spring `UserDetailsService` |
| `JWTService` | Token create/parse |
| `AppContextService` | ThreadLocal user context create/clear |
| `TenantMigrationService` | Per-tenant Flyway from `db/migration/tenant` |
| `AppBaseService` | Shared CRUD helpers for feature services |
| `CaffeineCacheService` | App cache |
| `CancellableTaskRegistry` / `CancellableTaskService` | Request cancellation |
| `DashboardService` | Dashboard config payload |
| `SchoolService` | School CRUD |

---

## 8. Seeded roles (Flyway V2)

Typical role codes seeded in `role_table` (see migration): Student, Teacher, Management, Accounts, Owner (and related admin codes as defined in SQL). Align with web client portal slugs: `student`, `teacher`, `management`, `accounts`, `owner`.

---

## 9. Known gaps / stubs (track here)

- `TenantContext.setTenant` never called — schema routing incomplete at request time.
- `db/migration/tenant/` has **no SQL files**; tenant provision creates empty migrations.
- OAuth success does **not** issue JWT or persist/link `OAuthAccountEntity`.
- Spec doc `docs/modules/auth/oauth2-google-jwt-architecture.md` describes RS256 + refresh rotation + `master` schema — **aspirational**, not current code.
- `/signOut` and `/refresh` constants unused.
- Role API path typo: `/api/v1/rloe`.
- Package typos: `infrastucture`, `persistance`.
- Broad `permitAll` on school/tenant/dashboard/oAuth CRUD.
- Sign-in plaintext password equality fallback.
- Hardcoded JWT secret in `application.yaml` (move to env for non-local).
- `.env.example` DB name/port drift vs `docker-compose.yml` / `application-local.yaml`.
- Dashboard metrics largely hardcoded.
- `AppContextService` clear primarily on JWT failure — no guaranteed request-complete cleanup filter.
- DATABASE_README still mentions historical `master` schema; runtime migrations use **`public`**.

---

## 10. Documentation map

| Doc | Role |
|---|---|
| **This file** (`docs/APPLICATION.md`) | Overall server + functionality (keep current) |
| [`README.md`](./README.md) | Documentation hub |
| [`AGENTS.md`](../AGENTS.md) | Task → exact file paths (universal) |
| [`implementation/`](./implementation/) | Deep notes |
| [`DATABASE_README.md`](../DATABASE_README.md) | DB architecture & connectivity |
| [`modules/auth/`](./modules/auth/) | OAuth/JWT architecture notes (check vs code) |
| [`CLAUDE.md`](../CLAUDE.md), [`.github/copilot-instructions.md`](../.github/copilot-instructions.md), [`.cursor/rules/`](../.cursor/rules/) | Optional IDE entrypoints — mirrors, **not** a second source of truth |

## 11. Maintenance contract (required)

**Whenever you change code or behavior, update this file in the same change** (any agent / IDE):

1. New/removed/changed **API endpoint** → update §5.
2. New/changed **security / public paths / JWT / OAuth** → update §5.1 / §6.
3. New/changed **tenancy or Flyway** → update §5.5 (+ implementation/database docs).
4. New/changed **service or module** → update §4 / §7.
5. New stub completed or new gap found → update status tables and §9.
6. Bump **Last reviewed** date at the top.

Do not leave this file stale. Product truth lives in `docs/` + `AGENTS.md`, not in vendor-only config.
