# KalviNet Server — Agent / IDE Guide

**Audience:** any AI coding agent or IDE assistant (Cursor, VS Code, GitHub Copilot, Claude, Windsurf, JetBrains AI, etc.) and human contributors.  
**Open access:** plain Markdown — read and edit freely. Not locked to Cursor.

**Goal:** Decide the exact file(s) from this map. Do **not** ripgrep the whole repo for routine auth, security, tenancy, or CRUD work.

Base package: `src/main/java/app/school/administration/`  
**Overall product doc (keep current):** [`docs/APPLICATION.md`](docs/APPLICATION.md)  
Doc hub: [`docs/README.md`](docs/README.md)  
Detail docs: `docs/implementation/`  
Database: [`DATABASE_README.md`](DATABASE_README.md)

---

## Task → Edit Here (only)

| User intent | Primary file(s) | Notes |
|---|---|---|
| Overall server / what’s implemented | `docs/APPLICATION.md` | **Update whenever functionality changes** |
| Sign in / sign up API | `auth/api/controller/AuthController.java` + `auth/application/serviceimpl/AuthServiceImpl.java` | Paths via `AppAuthEndPoints` |
| Auth path constants | `common/utils/AppAuthEndPoints.java`, `common/utils/AppModuleApi.java` | Role catalog: `/api/v1/role` (+ legacy `/rloe`) |
| Role assignment policy | `auth/application/constant/RoleAssignmentPolicy.java` | Single matrix for assignableRoles + assignRole |
| Assign role / search users | `auth/api/controller/UserController.java` + `UserServiceImpl` | `/assignableRoles`, `/assignRole/{uuid}`, `/search` |
| Role assignment audit | `auth/infrastructure/persistence/entity/RoleAssignmentAuditEntity.java` | Flyway V4 table |
| Public schema Flyway | `common/config/flyway/MasterFlywayConfig.java` + `db/migration/public/` | **V4** role catalog · **V5** assignment audit |
| Public vs authenticated routes | `auth/application/constant/AuthConstant.java` + `common/config/security/SecurityConfig.java` | |
| JWT issue / validate | `common/application/serviceimpl/JWTService.java` | HS256; props in `application.yaml` → `security.jwt.*` |
| JWT request filter | `common/application/component/JWTAuthFilter.java` | |
| Google OAuth success redirect | `auth/application/component/OAuth2SuccessHandler.java` | No JWT issued yet |
| OAuth account CRUD | `auth/api/controller/OAuthAccountController.java` | |
| User / role / tenant CRUD | `auth/api/controller/{User,Role,Tenant}Controller.java` + matching `*ServiceImpl` | |
| School feature module | `modules/school/api/controller/SchoolController.java` | Pattern for new ERP modules |
| Dashboard config API | `dashboard/api/controller/DashboardController.java` | |
| Health / cancel request | `common/api/HealthController.java`, `common/api/RequestCancellationController.java` | |
| ThreadLocal contexts | `auth/application/context/{Tenant,User,School}Context.java` + `common/application/serviceimpl/AppContextService.java` | `TenantContext.setTenant` unused today |
| Hibernate tenant id | `common/application/component/TenantIdentifierResolver.java` | |
| Multi-tenant connection | `auth/application/component/SchemaMultiTenantConnectionProvider.java` + `common/config/flyway/FlywayConfig.java` | |
| Public schema Flyway | `common/config/flyway/MasterFlywayConfig.java` + `src/main/resources/db/migration/public/` | |
| Tenant schema Flyway | `common/application/serviceimpl/TenantMigrationService.java` + `db/migration/tenant/` | Tenant folder empty |
| Entities (identity) | `auth/infrastructure/persistence/entity/` | |
| Auditing base entity | `common/infrastucture/persistence/entity/AuditableBaseEntity.java` | Package typo `infrastucture` |
| Shared CRUD base | `common/application/serviceimpl/AppBaseService.java`, `common/domain/repository/AppBaseRepository.java` | |
| Global exceptions | `common/application/custom/exception/handler/GlobalExceptionHandler.java` | |
| WebSocket | `common/config/websocket/WebSocketConfig.java` | `/ws` |
| Cache | `common/config/cache/CaffeineCacheConfig.java` | |
| Local DB / OAuth env | `src/main/resources/application-local.yaml`, `.env.example`, `docker-compose.yml` | Port **5454** |
| Server port / context / JWT | `src/main/resources/application.yaml` | `/erp`, port 8081 |

Paths above are under `src/main/java/app/school/administration/` unless noted.

---

## Agent workflow (token-efficient)

1. Match the prompt to **one row** in the table.
2. Open **only** those files (+ linked YAML/SQL if needed).
3. Change the **shared** source (constants, base service, security config)—not every consumer.
4. After shipping a new shared pattern, **update this map** and the matching doc under `docs/implementation/`.
5. **Always** update [`docs/APPLICATION.md`](docs/APPLICATION.md) when APIs, security, or tenancy change (bump Last reviewed).

---

## API quick reference (under `/erp`)

| Intent | Method + path |
|---|---|
| Sign in | `POST /api/v1/auth/signIn` |
| Sign up (forces USER) | `POST /api/v1/auth/signUp` |
| Assignable roles | `GET /api/v1/user/assignableRoles` |
| Assign role | `POST /api/v1/user/assignRole/{uuid}` |
| Search users | `GET /api/v1/user/search?q=` |
| Google OAuth start | `GET /oauth2/authorization/google` |
| Dashboard config | `GET /api/v1/dashboard/config` |
| Health | `GET /api/health` |
| WebSocket | `/ws` |
| Swagger | `/swagger-ui.html` |
