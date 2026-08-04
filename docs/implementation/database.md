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

| Script area | Contents |
|---|---|
| `public/V1__extentions.sql` | `pgcrypto`, `citext` |
| `public/V2__user_role_table.sql` | users, oauth_accounts, roles (+ seeds), user_roles |
| `public/V3__school_tenant_table.sql` | school_table, tenant_table |
| `public/R__init.sql` | empty repeatable |
| `tenant/*` | none yet |

`spring.flyway.enabled: false` — only custom beans migrate.

## Local database

```bash
docker-compose up -d postgres
# jdbc:postgresql://localhost:5454/kalvinet-local-dev  user=postgres password=root
```

Note: `.env.example` defaults (`school-admin`, port `5432`) **do not match** compose / local YAML — prefer compose + `application-local.yaml` until `.env.example` is aligned.

## Entity notes

- Catalog tables are mapped with schema/`public` as used in migrations.
- Soft delete / audit via `AuditableBaseEntity` (`is_active`, timestamps, version).
- `TenantEntity` uses a composite key (`TenantId` embeddable).

## Agent rule

Prefer adding DDL via Flyway scripts over `ddl-auto` changes. Never enable Hibernate auto-DDL for shared environments.
