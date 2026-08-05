# Database & Flyway

Authoritative long-form guide: [`DATABASE_README.md`](../../DATABASE_README.md)  
Living status / gaps: [`APPLICATION.md`](../APPLICATION.md)

## Source of truth

| Concern | Path |
|---|---|
| Local JDBC | `src/main/resources/application-local.yaml` |
| Hikari / JPA / Flyway flags | `src/main/resources/application.yaml` |
| Public migrations | `src/main/resources/db/migration/public/` |
| Tenant migrations | `src/main/resources/db/migration/tenant/` (empty) |
| Master Flyway runner | `common/config/flyway/MasterFlywayConfig.java` |
| Tenant Flyway runner | `common/application/serviceimpl/TenantMigrationService.java` |
| Docker Postgres | `docker-compose.yml` (host **5454**, DB `kalvinet-local-dev`) |
| Entities | `auth/infrastructure/persistence/entity/`, `modules/school/persistance/entity/` |
| Audit columns | `common/infrastucture/persistence/entity/AuditableBaseEntity.java` |

## Migration layout

| Script | Contents |
|---|---|
| `public/V1__extentions.sql` | `pgcrypto`, `citext` |
| `public/V2__user_role_table.sql` | `user_table`, `oauth_accounts`, `role_table`, `user_roles` (schema only; no role seed) |
| `public/V3__school_tenant_table.sql` | `school_table`, `tenant_table` |
| `public/V4__system_role_catalog.sql` | Baseline seed: USER, OWNER, MANAGER, MANAGEMENT, IT |
| `public/V5__role_assignment_audit.sql` | `role_assignment_audit` + indexes |
| `public/R__init.sql` | empty repeatable |
| `tenant/*` | none yet |

`spring.flyway.enabled: false` — only custom beans migrate.

## Local database

Canonical values (same in `docker-compose.yml`, `.env.example`, `application-local.yaml`):

| Key | Value |
|---|---|
| Container | `kalvinet-local-dev-db` |
| Database | `kalvinet-local-dev` |
| User / password | `postgres` / `root` |
| Host port | `5454` → container `5432` |
| Volume | `kalvinet_local_dev_pgdata` |
| JDBC | `jdbc:postgresql://localhost:5454/kalvinet-local-dev` |

```bash
cp .env.example .env          # optional overrides
docker compose up -d postgres
docker compose down -v        # wipe for a fresh Flyway migrate
```

## Entity notes

- Catalog tables are mapped with schema/`public` as used in migrations.
- Soft delete / audit via `AuditableBaseEntity` (`is_active`, timestamps, version).
- `TenantEntity` uses a composite key (`TenantId` embeddable).

## Agent rule

Prefer adding DDL via Flyway scripts over `ddl-auto` changes. Never enable Hibernate auto-DDL for shared environments.
