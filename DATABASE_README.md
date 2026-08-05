# KalviNet Server - Database Architecture & Connectivity Documentation

This document serves as the authoritative guide for the **Database Architecture, Multi-Tenant Flow, Schema Structure, Connection Routing, and Flyway Migrations** of the `KalviNet` project.

---

## 1. Database Architecture Overview

The system uses **PostgreSQL** with a **Schema-Based Multi-Tenancy Architecture** managed by **Spring Data JPA (Hibernate)** and **Flyway**.

```
+---------------------------------------------------------------------------------+
|                               POSTGRESQL DATABASE                               |
|                                                                                 |
|  +---------------------------------------------------------------------------+  |
|  | MASTER SCHEMA ('master')                                                  |  |
|  | - Central User Identity Store (user_table, role_table, user_roles)       |  |
|  | - Institutions & Schools Registry (institution_table, school_table)      |  |
|  | - Tenant Registry & Schema Mapping (tenant_table, tenant_school)         |  |
|  | - OAuth Account Bindings (oauth_accounts)                                 |  |
|  +---------------------------------------------------------------------------+  |
|                                                                                 |
|  +---------------------------+  +---------------------------+                   |
|  | TENANT SCHEMA ('tenant_a')|  | TENANT SCHEMA ('tenant_b')|  ...              |
|  | - School-specific Data    |  | - School-specific Data    |                   |
|  | - Student & Teacher Data  |  | - Student & Teacher Data  |                   |
|  | - Academic & ERP Records  |  | - Academic & ERP Records  |                   |
|  +---------------------------+  +---------------------------+                   |
+---------------------------------------------------------------------------------+
```

### Key Highlights
- **Master Schema (`master`)**: Centralized catalog storing system-wide identity, user credentials, roles, institutions, school registrations, and tenant schema mappings.
- **Tenant Schemas (`tenant_<name>`)**: Isolated PostgreSQL schemas provisioned dynamically for each school/client. Data in one tenant schema is physically isolated from other schemas.
- **PostgreSQL Extensions**:
  - `pgcrypto`: Enables `gen_random_uuid()` for database-level primary key UUID generation.
  - `citext`: Provides case-insensitive string columns (`CITEXT`) for emails, usernames, and school codes.

---

## 2. Database Flow & Request Connectivity Lifecycle

Every client request executes through a strictly controlled multi-tenant connectivity pipeline:

```
[ Client Request ]
       |
       v
[ JWTAuthFilter ] -------------------> Extracts JWT & resolves Tenant Schema Name
       |
       v
[ TenantContext ] -------------------> Binds Schema Name to ThreadLocal<String>
       |
       v
[ Spring Data JPA / Hibernate Query ]
       |
       v
[ TenantIdentifierResolver ] --------> Retrieves active Schema Name from TenantContext
       |                               (Defaults to "master, public" if null)
       v
[ SchemaMultiTenantConnectionProvider ] -> Gets pooled Connection from HikariCP Pool
       |                                   Sets PostgreSQL search_path to Target Schema
       v
[ PostgreSQL Execution ] ------------> Executes SQL against Target Tenant Schema
       |
       v
[ AppContextService ] ---------------> Purges ThreadLocal (TenantContext.clear())
```

### Step-by-Step Connectivity Breakdown

1. **Request Interception**: Client sends HTTP request containing `Authorization: Bearer <token>`.
2. **JWT & Tenant Parsing**: [JWTAuthFilter](file:///d:/projects-com/administration/src/main/java/app/school/administration/common/application/component/JWTAuthFilter.java) parses the token and resolves the user's assigned tenant schema name.
3. **Thread Scoping**: [TenantContext](file:///d:/projects-com/administration/src/main/java/app/school/administration/auth/application/context/TenantContext.java) stores the target schema name in a `ThreadLocal<String>`.
4. **Tenant Resolution**: When a repository or service method executes a database query, Hibernate calls [TenantIdentifierResolver](file:///d:/projects-com/administration/src/main/java/app/school/administration/common/application/component/TenantIdentifierResolver.java), which reads the schema name from `TenantContext`.
5. **Connection Selection**: [SchemaMultiTenantConnectionProvider](file:///d:/projects-com/administration/src/main/java/app/school/administration/auth/application/component/SchemaMultiTenantConnectionProvider.java) fetches a database connection from the HikariCP connection pool and routes the SQL execution to the active tenant schema.
6. **Thread Clean-up**: Upon request completion, [AppContextService](file:///d:/projects-com/administration/src/main/java/app/school/administration/common/application/serviceimpl/AppContextService.java) purges `TenantContext` to prevent cross-tenant data contamination across reused thread pool threads.

---

## 3. Database Connection Pool Configuration (HikariCP)

Configured in [application.yaml](file:///d:/projects-com/administration/src/main/resources/application.yaml):

```yaml
spring:
  datasource:
    hikari:
      pool-name: kalvinet-hikari-pool
      maximum-pool-size: 15
      minimum-idle: 5
      connection-timeout: 10000     # 10 seconds max wait for connection
      idle-timeout: 60000           # 60 seconds idle connection retirement
      max-lifetime: 1700000         # 28.3 minutes connection recycle threshold
```

### Rationale
- **Pool Sizing (`15` max, `5` idle)**: Provides high throughput for multi-tenant concurrent requests without overloading PostgreSQL backends.
- **Connection Timeout (`10000ms`)**: Prevents HTTP threads from hanging indefinitely when connection pool exhaustion occurs.

---

## 4. Database Migrations Flow (Flyway)

Database migrations use **Flyway** with a dual-mode initialization strategy:

### A. Startup Master Migrations ([MasterFlywayConfig.java](file:///d:/projects-com/administration/src/main/java/app/school/administration/common/config/flyway/MasterFlywayConfig.java))
At application boot, Flyway runs automatically against the `master` schema to create and update core system tables:
- **`dev` / `local` Profile**: Runs `flyway.repair()` to fix checksum mismatches during active development and uses `validateOnMigrate(false)`.
- **`prod` / `live` Profile**: Enforces strict checksum validation (`validateOnMigrate(true)`) to prevent unauthorized schema script changes.

### B. Dynamic Tenant Schema Provisioning ([TenantMigrationService.java](file:///d:/projects-com/administration/src/main/java/app/school/administration/common/application/serviceimpl/TenantMigrationService.java))
When a new school or tenant registers:
1. System creates a new PostgreSQL schema (e.g. `CREATE SCHEMA tenant_school_99`).
2. `TenantMigrationService.migrateTenant(schemaName)` programmatically triggers Flyway against the new schema, populating it with all standard tenant tables.

---

## 5. Master Database Table Structure & Schemas

All master tables reside in `master` schema and are defined in Flyway SQL scripts (`src/main/resources/db/migration/master/`).

### Table Entity Relationship Diagram

```
 [ master.institution_table ]
             | 1
             |
             | N
     [ master.school_table ]                    [ master.tenant_table ]
                                                
 [ master.user_table ] 1 <---- N [ master.user_roles ] N ----> 1 [ master.role_table ]
             | 1
             |
             | N
 [ master.oauth_accounts ]
```

---

### Table 1: `master.school_table`
* **File**: [V3__school_tenant_table.sql](file:///d:/projects-com/administration/src/main/resources/db/migration/master/V3__school_tenant_table.sql)
* **Purpose**: Stores physical school branches in master catalog.

| Column Name | Data Type | Constraints | Description |
| :--- | :--- | :--- | :--- |
| `school_id` | `UUID` | `PRIMARY KEY, DEFAULT gen_random_uuid()` | Unique school identifier |
| `school_name` | `CITEXT` | `NOT NULL, UNIQUE` | Case-insensitive school name |
| `version` | `BIGINT` | `NOT NULL` | Optimistic locking counter |
| `is_active` | `BOOLEAN` | `NOT NULL, DEFAULT TRUE` | Activation status (Indexed) |
| `created_at` | `TIMESTAMPTZ` | `NOT NULL, DEFAULT now()` | Creation timestamp |
| `updated_at` | `TIMESTAMPTZ` | `NOT NULL, DEFAULT now()` | Last update timestamp |

---

### Table 2: `master.tenant_table`
* **File**: [V3__school_tenant_table.sql](file:///d:/projects-com/administration/src/main/resources/db/migration/master/V3__school_tenant_table.sql)
* **Purpose**: Registry of PostgreSQL tenant database schemas.

| Column Name | Data Type | Constraints | Description |
| :--- | :--- | :--- | :--- |
| `tenant_id` | `UUID` | `PRIMARY KEY (tenant_id, opened_date)` | Unique tenant ID (Composite PK field 1) |
| `tenant_name` | `CITEXT` | `NOT NULL, UNIQUE` | Matching PostgreSQL schema name |
| `opened_date` | `TIMESTAMPTZ` | `PRIMARY KEY (tenant_id, opened_date)` | Tenant schema start timestamp (Composite PK field 2, Indexed) |
| `closed_date` | `TIMESTAMPTZ` | `NOT NULL, DEFAULT now()` | Tenant schema expiration timestamp (Indexed) |
| `description` | `TEXT` | `NULL` | Optional description |
| `version` | `BIGINT` | `NOT NULL` | Optimistic locking counter |
| `is_active` | `BOOLEAN` | `NOT NULL, DEFAULT TRUE` | Activation status (Indexed: `idx_tenant_table_active`, `idx_tenant_table_active_opened_closed`) |
| `created_at` | `TIMESTAMPTZ` | `NOT NULL, DEFAULT now()` | Creation timestamp |
| `updated_at` | `TIMESTAMPTZ` | `NOT NULL, DEFAULT now()` | Last update timestamp |

---

### Table 4: `master.user_table`
* **File**: [V4__user_role_table.sql](file:///d:/projects-com/administration/src/main/resources/db/migration/master/V4__user_role_table.sql)
* **Purpose**: Centralized user account identity store.

| Column Name | Data Type | Constraints | Description |
| :--- | :--- | :--- | :--- |
| `user_id` | `UUID` | `PRIMARY KEY, DEFAULT gen_random_uuid()` | Unique user identifier |
| `first_name` | `VARCHAR(100)` | `NOT NULL` | User first name |
| `last_name` | `VARCHAR(100)` | `NOT NULL` | User last name |
| `email` | `CITEXT` | `NOT NULL, UNIQUE` | Case-insensitive email (Indexed) |
| `username` | `CITEXT` | `NOT NULL, UNIQUE` | Case-insensitive username (Indexed) |
| `password` | `TEXT` | `NOT NULL` | BCrypt hashed password |
| `mobile_no` | `VARCHAR(15)` | `NULL` | Contact mobile number |
| `version` | `BIGINT` | `NOT NULL` | Optimistic locking counter |
| `is_active` | `BOOLEAN` | `NOT NULL, DEFAULT TRUE` | Account activation status (Indexed) |
| `created_at` | `TIMESTAMPTZ` | `NOT NULL, DEFAULT now()` | Creation timestamp |
| `updated_at` | `TIMESTAMPTZ` | `NOT NULL, DEFAULT now()` | Last update timestamp |

---

### Table 5: `master.oauth_accounts`
* **File**: [V4__user_role_table.sql](file:///d:/projects-com/administration/src/main/resources/db/migration/master/V4__user_role_table.sql)
* **Purpose**: Linked third-party SSO accounts (Google, Microsoft).

| Column Name | Data Type | Constraints | Description |
| :--- | :--- | :--- | :--- |
| `oauth_id` | `UUID` | `PRIMARY KEY, DEFAULT gen_random_uuid()` | OAuth entry ID |
| `user_id` | `UUID` | `NOT NULL, FK -> master.user_table (ON DELETE CASCADE)` | Linked internal user ID |
| `provider` | `VARCHAR(30)` | `NOT NULL` | Provider name (e.g., 'GOOGLE') |
| `provider_user_id` | `VARCHAR(255)` | `NOT NULL` | Provider's unique user sub ID |

---

### Table 6: `master.role_table` & Seed Roles
* **File**: [V4__user_role_table.sql](file:///d:/projects-com/administration/src/main/resources/db/migration/master/V4__user_role_table.sql)
* **Purpose**: Stores Role-Based Access Control (RBAC) role definitions.

#### Pre-seeded Roles
- `ROLE_SUPER_ADMIN` - Super Administrator
- `ROLE_ADMIN` - Administrator
- `ROLE_TEACHER` - Teacher
- `ROLE_STUDENT` - Student
- `ROLE_ACCOUNTANT` - Accountant

---

### Table 7: `master.user_roles`
* **File**: [V4__user_role_table.sql](file:///d:/projects-com/administration/src/main/resources/db/migration/master/V4__user_role_table.sql)
* **Purpose**: Join table establishing user-to-role assignments (`user_id`, `role_id`).

---

## 6. Table Foreign Key Cascades & Deletion Impact Cases

| Target Entity | Related Entity | FK Constraint Name | Constraint Strategy | Deletion Impact Behavior & Rules |
| :--- | :--- | :--- | :--- | :--- |
| `master.user_table` | `master.user_roles` | `fk_user_roles_user` | `ON DELETE CASCADE` | Deleting a user row automatically removes all corresponding user-role assignment records in `master.user_roles`. |
| `master.user_table` | `master.oauth_accounts` | `fk_oauth_user` | `ON DELETE CASCADE` | Deleting a user row automatically removes all linked SSO OAuth credentials (Google, Microsoft). |
| `master.user_table` | `master.role_table` | *(via user_roles)* | *(Disassociation)* | Roles in `master.role_table` are **NOT** deleted when a user is deleted. Roles are aggregate master definitions shared across users. |
| `master.role_table` | `master.user_roles` | `fk_user_roles_role` | `ON DELETE CASCADE` | Deleting a role definition removes all user-role mappings for that role. Users are NOT deleted, but lose that role's authority. |
| `master.role_table` | System Core Security | N/A | *(System Guardrail)* | Pre-seeded roles (`ROLE_SUPER_ADMIN`, `ROLE_ADMIN`, `ROLE_TEACHER`, etc.) are aggregated into code logic and security annotations. Deleting system roles is strongly discouraged. |

---

## 7. Audit & Concurrency Strategy

Every JPA entity mapped to these tables extends [AuditableBaseEntity](file:///d:/projects-com/administration/src/main/java/app/school/administration/common/infrastucture/persistence/entity/AuditableBaseEntity.java):
1. **Optimistic Locking (`version` column)**: Incremented automatically on every SQL `UPDATE`. If two transactions attempt to update the same record concurrently, Hibernate throws `OptimisticLockException`.
2. **Automated Timestamps (`created_at`, `updated_at`)**: Managed by `@EntityListeners(AuditingEntityListener.class)`.

---

## 8. Developer Maintenance & Documentation Update Guidelines

> [!IMPORTANT]
> **MANDATORY RULE FOR ALL DEVELOPERS & AGENTS**:
> Whenever database schema changes, new Flyway scripts, table alterations, or connection routing components are added or modified:
> 1. Update the Flyway version SQL script in `src/main/resources/db/migration/`.
> 2. Update the corresponding JPA entity class and projection DTO.
> 3. **Immediately update this `DATABASE_README.md` file** to document the new/modified tables, columns, indexes, foreign keys, and connectivity flows.

---

## 9. Local PostgreSQL Dockerization Guide

The repository includes a dedicated [docker-compose.yml](file:///d:/projects-com/administration/docker-compose.yml) file to run PostgreSQL 16 containerized locally while keeping the Spring Boot application running natively on your host machine.

### Quick Start Commands

```bash
# 1. Start PostgreSQL Container in background
docker compose up -d

# 2. Check Container Health & Status
docker compose ps

# 3. View Real-time PostgreSQL Container Logs
docker compose logs -f postgres

# 4. Stop PostgreSQL Container (Data is preserved in volume)
docker compose down

# 5. Stop Container & Wipe Persistent Database Storage Volume (Fresh Start)
docker compose down -v
```

---

### Implementation Breakdown & Technical Rationale

| Component | Setting / Parameter | Technical Rationale & Impact |
| :--- | :--- | :--- |
| **Image** | `postgres:16-alpine` | Minimal vulnerability footprint with `pgcrypto` / `citext` support. |
| **Container Name** | `kalvinet-local-dev-db` | Predictable name for `docker exec` / `docker logs`. |
| **Restart Policy** | `restart: unless-stopped` | Auto-restarts after crash or host reboot. |
| **Database Name** | `POSTGRES_DB: kalvinet-local-dev` | Matches `application-local.yaml` / `.env.example`. |
| **Credentials** | `POSTGRES_USER: postgres`<br>`POSTGRES_PASSWORD: root` | Same defaults everywhere; override via `.env`. |
| **Port Binding** | `5454:5432` | Host **5454** → container 5432 (avoids clash with a native Postgres on 5432). |
| **Data Volume** | `kalvinet_local_dev_pgdata` | Persists data across `docker compose down` (use `-v` to wipe). |
| **Healthcheck** | `pg_isready -U postgres -d kalvinet-local-dev` | Readiness every 5s before Flyway / Spring Boot. |

---

### Spring Boot Native Connectivity (`application-local.yaml`)

When running Spring Boot natively, ensure active profile is `local`. Credentials match Docker Compose:

```yaml
spring:
  datasource:
    driver-class-name: org.postgresql.Driver
    url: jdbc:postgresql://localhost:5454/kalvinet-local-dev
    username: postgres
    password: root
```

Flyway migrations (`MasterFlywayConfig`) run on boot against the **`public`** schema (`db/migration/public` V1–V5).
