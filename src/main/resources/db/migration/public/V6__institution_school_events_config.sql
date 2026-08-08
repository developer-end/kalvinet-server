-- ====================================================================================
-- V6: Institution singleton, school profile columns, events, feature configuration
-- ====================================================================================
-- One institution row only (singleton_key = 1).
-- Schools belong to that institution.
-- Events are INSTITUTION- or SCHOOL-scoped.
-- Feature configuration gates access by role grants when a config row exists.
-- ====================================================================================

-- ------------------------------------------------------------------------------------
-- institution_table (exactly one active catalog record)
-- ------------------------------------------------------------------------------------
CREATE TABLE public.institution_table
(
    institution_id      UUID PRIMARY KEY     NOT NULL DEFAULT gen_random_uuid(),
    singleton_key       SMALLINT             NOT NULL DEFAULT 1,
    institution_name    CITEXT               NOT NULL,
    legal_name          TEXT,
    registration_number TEXT,
    email               CITEXT,
    phone               TEXT,
    address_line1       TEXT,
    address_line2       TEXT,
    city                TEXT,
    state               TEXT,
    country             TEXT,
    postal_code         TEXT,
    website             TEXT,
    logo_url            TEXT,
    description         TEXT,
    version             BIGINT               NOT NULL DEFAULT 0,
    is_active           BOOLEAN              NOT NULL DEFAULT TRUE,
    created_at          TIMESTAMPTZ          NOT NULL DEFAULT now(),
    updated_at          TIMESTAMPTZ          NOT NULL DEFAULT now(),
    CONSTRAINT uq_institution_singleton CHECK (singleton_key = 1),
    CONSTRAINT uq_institution_singleton_key UNIQUE (singleton_key)
);
CREATE INDEX idx_institution_table_active ON public.institution_table (is_active);

INSERT INTO public.institution_table (
    institution_id,
    singleton_key,
    institution_name,
    legal_name,
    description,
    version,
    is_active
) VALUES (
    'a0000000-0000-4000-8000-000000000001',
    1,
    'KalviNet Institution',
    'KalviNet Institutional Services',
    'Default singleton institution catalog record.',
    0,
    TRUE
);

-- ------------------------------------------------------------------------------------
-- school_table — profile columns + FK to institution
-- ------------------------------------------------------------------------------------
ALTER TABLE public.school_table
    ADD COLUMN IF NOT EXISTS institution_id UUID,
    ADD COLUMN IF NOT EXISTS school_code CITEXT,
    ADD COLUMN IF NOT EXISTS email CITEXT,
    ADD COLUMN IF NOT EXISTS phone TEXT,
    ADD COLUMN IF NOT EXISTS address_line1 TEXT,
    ADD COLUMN IF NOT EXISTS address_line2 TEXT,
    ADD COLUMN IF NOT EXISTS city TEXT,
    ADD COLUMN IF NOT EXISTS state TEXT,
    ADD COLUMN IF NOT EXISTS country TEXT,
    ADD COLUMN IF NOT EXISTS postal_code TEXT,
    ADD COLUMN IF NOT EXISTS principal_name TEXT,
    ADD COLUMN IF NOT EXISTS board_affiliation TEXT,
    ADD COLUMN IF NOT EXISTS timezone TEXT,
    ADD COLUMN IF NOT EXISTS description TEXT;

UPDATE public.school_table
SET institution_id = 'a0000000-0000-4000-8000-000000000001'
WHERE institution_id IS NULL;

ALTER TABLE public.school_table
    ALTER COLUMN institution_id SET NOT NULL;

ALTER TABLE public.school_table
    DROP CONSTRAINT IF EXISTS fk_school_institution;

ALTER TABLE public.school_table
    ADD CONSTRAINT fk_school_institution
        FOREIGN KEY (institution_id) REFERENCES public.institution_table (institution_id);

CREATE INDEX IF NOT EXISTS idx_school_institution ON public.school_table (institution_id);
CREATE UNIQUE INDEX IF NOT EXISTS uq_school_code_active
    ON public.school_table (school_code)
    WHERE school_code IS NOT NULL AND is_active = TRUE;

-- ------------------------------------------------------------------------------------
-- calendar_event_table
-- ------------------------------------------------------------------------------------
CREATE TABLE public.calendar_event_table
(
    event_id       UUID PRIMARY KEY NOT NULL DEFAULT gen_random_uuid(),
    scope          VARCHAR(20)      NOT NULL,
    institution_id UUID             NOT NULL,
    school_id      UUID,
    title          TEXT             NOT NULL,
    description    TEXT,
    location       TEXT,
    starts_at      TIMESTAMPTZ      NOT NULL,
    ends_at        TIMESTAMPTZ,
    all_day        BOOLEAN          NOT NULL DEFAULT FALSE,
    version        BIGINT           NOT NULL DEFAULT 0,
    is_active      BOOLEAN          NOT NULL DEFAULT TRUE,
    created_at     TIMESTAMPTZ      NOT NULL DEFAULT now(),
    updated_at     TIMESTAMPTZ      NOT NULL DEFAULT now(),
    CONSTRAINT ck_event_scope CHECK (scope IN ('INSTITUTION', 'SCHOOL')),
    CONSTRAINT ck_event_school_scope CHECK (
        (scope = 'INSTITUTION' AND school_id IS NULL)
            OR (scope = 'SCHOOL' AND school_id IS NOT NULL)
        ),
    CONSTRAINT fk_event_institution
        FOREIGN KEY (institution_id) REFERENCES public.institution_table (institution_id),
    CONSTRAINT fk_event_school
        FOREIGN KEY (school_id) REFERENCES public.school_table (school_id)
);
CREATE INDEX idx_event_scope_active ON public.calendar_event_table (scope, is_active);
CREATE INDEX idx_event_institution ON public.calendar_event_table (institution_id, starts_at);
CREATE INDEX idx_event_school ON public.calendar_event_table (school_id, starts_at);

-- ------------------------------------------------------------------------------------
-- feature_definition_table (catalog of configurable capabilities)
-- ------------------------------------------------------------------------------------
CREATE TABLE public.feature_definition_table
(
    feature_code    CITEXT PRIMARY KEY NOT NULL,
    feature_name    TEXT               NOT NULL,
    description     TEXT,
    default_enabled BOOLEAN            NOT NULL DEFAULT TRUE,
    version         BIGINT             NOT NULL DEFAULT 0,
    is_active       BOOLEAN            NOT NULL DEFAULT TRUE,
    created_at      TIMESTAMPTZ        NOT NULL DEFAULT now(),
    updated_at      TIMESTAMPTZ        NOT NULL DEFAULT now()
);

INSERT INTO public.feature_definition_table (feature_code, feature_name, description) VALUES
    ('INSTITUTION_SETTINGS', 'Institution settings', 'Edit the singleton institution profile'),
    ('INSTITUTION_EVENTS', 'Institution events', 'Create and manage institution-level events'),
    ('SCHOOLS_LIST', 'Schools list', 'View and create schools under the institution'),
    ('SCHOOL_SETTINGS', 'School settings', 'Edit school profile data'),
    ('SCHOOL_EVENTS', 'School events', 'Create and manage school-level events'),
    ('FEATURE_CONFIGURATION', 'Feature configuration', 'Configure feature enablement and role grants');

-- ------------------------------------------------------------------------------------
-- feature_configuration_table (scope enablement)
-- ------------------------------------------------------------------------------------
CREATE TABLE public.feature_configuration_table
(
    config_id      UUID PRIMARY KEY NOT NULL DEFAULT gen_random_uuid(),
    feature_code   CITEXT           NOT NULL,
    scope          VARCHAR(20)      NOT NULL,
    institution_id UUID             NOT NULL,
    school_id      UUID,
    enabled        BOOLEAN          NOT NULL DEFAULT TRUE,
    version        BIGINT           NOT NULL DEFAULT 0,
    is_active      BOOLEAN          NOT NULL DEFAULT TRUE,
    created_at     TIMESTAMPTZ      NOT NULL DEFAULT now(),
    updated_at     TIMESTAMPTZ      NOT NULL DEFAULT now(),
    CONSTRAINT ck_feature_config_scope CHECK (scope IN ('INSTITUTION', 'SCHOOL')),
    CONSTRAINT ck_feature_config_school CHECK (
        (scope = 'INSTITUTION' AND school_id IS NULL)
            OR (scope = 'SCHOOL' AND school_id IS NOT NULL)
        ),
    CONSTRAINT fk_feature_config_definition
        FOREIGN KEY (feature_code) REFERENCES public.feature_definition_table (feature_code),
    CONSTRAINT fk_feature_config_institution
        FOREIGN KEY (institution_id) REFERENCES public.institution_table (institution_id),
    CONSTRAINT fk_feature_config_school
        FOREIGN KEY (school_id) REFERENCES public.school_table (school_id)
);

CREATE UNIQUE INDEX uq_feature_config_institution
    ON public.feature_configuration_table (feature_code, scope, institution_id)
    WHERE scope = 'INSTITUTION' AND school_id IS NULL AND is_active = TRUE;

CREATE UNIQUE INDEX uq_feature_config_school
    ON public.feature_configuration_table (feature_code, scope, school_id)
    WHERE scope = 'SCHOOL' AND school_id IS NOT NULL AND is_active = TRUE;

-- ------------------------------------------------------------------------------------
-- feature_role_grant_table (roles allowed when a feature is configured)
-- ------------------------------------------------------------------------------------
CREATE TABLE public.feature_role_grant_table
(
    grant_id   UUID PRIMARY KEY NOT NULL DEFAULT gen_random_uuid(),
    config_id  UUID             NOT NULL,
    role_code  CITEXT           NOT NULL,
    version    BIGINT           NOT NULL DEFAULT 0,
    is_active  BOOLEAN          NOT NULL DEFAULT TRUE,
    created_at TIMESTAMPTZ      NOT NULL DEFAULT now(),
    updated_at TIMESTAMPTZ      NOT NULL DEFAULT now(),
    CONSTRAINT fk_feature_grant_config
        FOREIGN KEY (config_id) REFERENCES public.feature_configuration_table (config_id) ON DELETE CASCADE,
    CONSTRAINT uq_feature_grant UNIQUE (config_id, role_code)
);
CREATE INDEX idx_feature_grant_config ON public.feature_role_grant_table (config_id);
