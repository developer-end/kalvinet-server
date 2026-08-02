-- ====================================================================================
-- MIGRATION SCRIPT: V3__school_tenant_table.sql
-- ====================================================================================

-- ------------------------------------------------------------------------------------
-- TABLE 1: master.school_table
-- ------------------------------------------------------------------------------------
-- [FUNCTIONALITY]:
--   Stores individual school records in master catalog.
--
-- [WHY IMPLEMENTED]:
--   Represents concrete physical or branch schools (e.g. "Springfield High School").
-- ------------------------------------------------------------------------------------
CREATE TABLE public.school_table
(
    school_id      UUID PRIMARY KEY NOT NULL DEFAULT gen_random_uuid(),
    school_name    CITEXT           NOT NULL UNIQUE,
    version        BIGINT           NOT NULL,
    started_date   TIMESTAMPTZ      NOT NULL DEFAULT now(),
    is_active      BOOLEAN          NOT NULL DEFAULT TRUE,
    created_at     TIMESTAMPTZ      NOT NULL DEFAULT now(),
    updated_at     TIMESTAMPTZ      NOT NULL DEFAULT now()
);
CREATE INDEX idx_school_table_active ON public.school_table (is_active);


-- ------------------------------------------------------------------------------------
-- TABLE 2: master.tenant_table
-- ------------------------------------------------------------------------------------
-- [FUNCTIONALITY]:
--   Stores metadata for database tenant schemas created in PostgreSQL.
--
-- [WHY IMPLEMENTED]:
--   In schema-based multi-tenancy, each tenant represents a distinct PostgreSQL schema (e.g., `tenant_school_01`).
--   `tenant_name` matches the physical schema name used by `SchemaMultiTenantConnectionProvider` and `TenantIdentifierResolver`.
-- ------------------------------------------------------------------------------------
CREATE TABLE public.tenant_table
(
    tenant_id   UUID        NOT NULL,
    opened_date TIMESTAMPTZ NOT NULL DEFAULT now(),
    tenant_name CITEXT      NOT NULL UNIQUE,
    closed_date TIMESTAMPTZ NOT NULL DEFAULT now(),
    description TEXT,
    version     BIGINT      NOT NULL,
    is_active   BOOLEAN     NOT NULL DEFAULT TRUE,
    created_at  TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at  TIMESTAMPTZ NOT NULL DEFAULT now(),

    PRIMARY KEY (tenant_id, opened_date)
);
CREATE INDEX idx_tenant_table_active ON public.tenant_table (is_active);
CREATE INDEX idx_tenant_table_active_opened_closed ON public.tenant_table (is_active, opened_date, closed_date);
