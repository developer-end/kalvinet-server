-- ====================================================================================
-- MIGRATION SCRIPT: V4__user_role_table.sql
-- ====================================================================================

-- ------------------------------------------------------------------------------------
-- TABLE 1: master.user_table
-- ------------------------------------------------------------------------------------
-- [FUNCTIONALITY]:
--   Stores user credentials, contact details, and account status in the central master schema.
--
-- [WHY IMPLEMENTED]:
--   Provides a centralized identity store for all system users across all schools/tenants.
--   Email and username are CITEXT columns with unique indexes to enforce unique login accounts.
--
-- [WHAT HAPPENS IF NOT IMPLEMENTED]:
--   - System authentication (`AuthServiceImpl`, `CustomUserDetailsServiceImpl`) will fail.
--   - Users will be unable to log in, register, or store account profile details.
--
-- [DELETION IMPACT & CASCADE CASES]:
--   - CASE 1 (User Deletion -> `master.user_roles`): FK `fk_user_roles_user` has `ON DELETE CASCADE`.
--     Deleting a user row in `master.user_table` automatically removes all associated user-role join entries in `master.user_roles`.
--   - CASE 2 (User Deletion -> `master.oauth_accounts`): FK `fk_oauth_user` has `ON DELETE CASCADE`.
--     Deleting a user row in `master.user_table` automatically removes all linked third-party OAuth accounts (Google, Microsoft) in `master.oauth_accounts`.
--   - CASE 3 (User Deletion -> `master.role_table`): `master.role_table` entries ARE NOT DELETED.
--     Roles are aggregate master entities shared across all system users. Deleting a user only disassociates the user from roles by deleting `master.user_roles` records.
-- ------------------------------------------------------------------------------------
CREATE TABLE public.user_table
(
    user_id    UUID PRIMARY KEY NOT NULL DEFAULT gen_random_uuid(),
    first_name VARCHAR(100)     NOT NULL,
    last_name  VARCHAR(100)     NOT NULL,
    email      CITEXT           NOT NULL UNIQUE,
    username   CITEXT           NOT NULL UNIQUE,
    password   TEXT             NOT NULL,
    mobile_no  VARCHAR(15),
    version    BIGINT           NOT NULL,
    is_active  BOOLEAN          NOT NULL DEFAULT TRUE,
    created_at TIMESTAMPTZ      NOT NULL DEFAULT now(),
    updated_at TIMESTAMPTZ      NOT NULL DEFAULT now()
);
CREATE INDEX idx_users_email ON public.user_table (email);
CREATE INDEX idx_users_username ON public.user_table (username);
CREATE INDEX idx_user_active ON public.user_table (is_active);


-- ------------------------------------------------------------------------------------
-- TABLE 2: master.oauth_accounts
-- ------------------------------------------------------------------------------------
-- [FUNCTIONALITY]:
--   Stores linked third-party OAuth2 account provider identity tokens (e.g. Google, Microsoft).
--
-- [WHY IMPLEMENTED]:
--   Enables Single Sign-On (SSO) authentication. Maps external provider user IDs to internal system `user_id`.
--
-- [WHAT HAPPENS IF NOT IMPLEMENTED]:
--   - OAuth2 login callbacks (`OAuth2SuccessHandler`, `OAuthUserServiceImpl`) will fail to persist linked accounts.
--   - Social SSO users will be unable to authenticate.
--
-- [DELETION IMPACT & CASCADE CASES]:
--   - CASE 1 (OAuth Account Deletion -> `master.user_table`): Deleting an entry in `master.oauth_accounts` ONLY unlinks the external SSO provider.
--     The primary user account record in `master.user_table` remains INTACT and UNDELETED.
--   - CASE 2 (User Deletion -> `master.oauth_accounts`): When a parent user is deleted from `master.user_table`,
--     `ON DELETE CASCADE` automatically removes all associated OAuth provider entries in `master.oauth_accounts`.
-- ------------------------------------------------------------------------------------
CREATE TABLE public.oauth_accounts
(
    oauth_id         UUID PRIMARY KEY      DEFAULT gen_random_uuid(),
    user_id          UUID         NOT NULL,
    provider         VARCHAR(30)  NOT NULL,
    provider_user_id VARCHAR(255) NOT NULL,
    version          BIGINT       NOT NULL,
    is_active        BOOLEAN      NOT NULL DEFAULT TRUE,
    created_at       TIMESTAMPTZ  NOT NULL DEFAULT now(),
    updated_at       TIMESTAMPTZ  NOT NULL DEFAULT now(),
    CONSTRAINT fk_oauth_user
        FOREIGN KEY (user_id) REFERENCES public.user_table (user_id)
            ON DELETE CASCADE
);
CREATE UNIQUE INDEX idx_oauth_provider_user
    ON public.oauth_accounts (provider, provider_user_id);
CREATE UNIQUE INDEX idx_oauth_provider_user_id
    ON public.oauth_accounts (provider, user_id);
CREATE INDEX idx_oauth_provider_active ON public.oauth_accounts (is_active);


-- ------------------------------------------------------------------------------------
-- TABLE 3 & SEED DATA: master.role_table
-- ------------------------------------------------------------------------------------
-- [FUNCTIONALITY]:
--   Defines system roles (`ROLE_SUPER_ADMIN`, `ROLE_ADMIN`, `ROLE_TEACHER`, `ROLE_STUDENT`, `ROLE_ACCOUNTANT`).
--
-- [WHY IMPLEMENTED]:
--   Enforces Role-Based Access Control (RBAC) across Spring Security `@PreAuthorize` methods and API endpoints.
--
-- [WHAT HAPPENS IF NOT IMPLEMENTED]:
--   - Role assignment during user registration will fail.
--   - Spring Security cannot authorize authenticated users based on GrantedAuthorities.
--
-- [DELETION IMPACT & CASCADE CASES]:
--   - CASE 1 (Role Deletion -> `master.user_roles`): FK `fk_user_roles_role` has `ON DELETE CASCADE`.
--     Deleting a role definition in `master.role_table` automatically removes all user-role assignment entries in `master.user_roles` referencing this `role_id`.
--   - CASE 2 (Role Deletion -> `master.user_table`): Users in `master.user_table` ARE NOT DELETED.
--     However, assigned users lose this role's authorities, which will revoke access in Spring Security (`@PreAuthorize`).
--   - CASE 3 (Role Aggregation / System Role Deletion Restriction): Pre-seeded system roles (`ROLE_SUPER_ADMIN`, `ROLE_ADMIN`, `ROLE_TEACHER`, `ROLE_STUDENT`, `ROLE_ACCOUNTANT`, `ROLE_USER`)
--     are aggregated into system logic and security authorities. Deleting system roles is strongly discouraged as active user authorizations will break.
-- ------------------------------------------------------------------------------------
CREATE TABLE public.role_table
(
    role_id     UUID PRIMARY KEY      DEFAULT gen_random_uuid(),
    role_code   CITEXT       NOT NULL UNIQUE,
    role_name   VARCHAR(250) NOT NULL,
    description TEXT,
    version     BIGINT       NOT NULL,
    is_active   BOOLEAN      NOT NULL DEFAULT TRUE,
    created_at  TIMESTAMPTZ  NOT NULL DEFAULT now(),
    updated_at  TIMESTAMPTZ  NOT NULL DEFAULT now()
);
CREATE INDEX idx_role_table_active ON public.role_table (is_active);

INSERT INTO public.role_table (role_code, role_name, version)
VALUES ('ROLE_SUPER_ADMIN', 'Super Administrator', 1),
       ('ROLE_ADMIN', 'Administrator', 1),
       ('ROLE_TEACHER', 'Teacher', 1),
       ('ROLE_STUDENT', 'Student', 1),
       ('ROLE_ACCOUNTANT', 'Accountant', 1),
       ('ROLE_USER', 'User', 1)
;

-- ------------------------------------------------------------------------------------
-- TABLE 4: master.user_roles
-- ------------------------------------------------------------------------------------
-- [FUNCTIONALITY]:
--   Join table mapping users (`user_id`) to assigned roles (`role_id`).
--
-- [WHY IMPLEMENTED]:
--   Supports multi-role user accounts (e.g. a user being both an `ADMIN` and a `TEACHER`).
--
-- [WHAT HAPPENS IF NOT IMPLEMENTED]:
--   - The system cannot resolve which roles belong to a user during login context setup (`CustomUserDetailsServiceImpl`).
--
-- [DELETION IMPACT & CASCADE CASES]:
--   - CASE 1 (Mapping Row Deletion): Deleting a row from `master.user_roles` removes the specific role assignment from a user.
--     Neither the user (`master.user_table`) nor the role (`master.role_table`) is deleted.
--   - CASE 2 (User or Role Deletion): Deleting either a parent user or a parent role triggers `ON DELETE CASCADE`,
--     automatically cleaning up the corresponding join records in `master.user_roles`.
-- ------------------------------------------------------------------------------------
CREATE TABLE public.user_roles
(
    user_id     UUID        NOT NULL,
    role_id     UUID        NOT NULL,
    description TEXT,
    version     BIGINT      NOT NULL,
    is_active   BOOLEAN     NOT NULL DEFAULT TRUE,
    assigned_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_at  TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at  TIMESTAMPTZ NOT NULL DEFAULT now(),
    PRIMARY KEY (user_id, role_id),

    CONSTRAINT fk_user_roles_user
        FOREIGN KEY (user_id) REFERENCES public.user_table (user_id)
            ON DELETE CASCADE,

    CONSTRAINT fk_user_roles_role
        FOREIGN KEY (role_id) REFERENCES public.role_table (role_id)
            ON DELETE CASCADE
);
CREATE INDEX idx_user_roles_active ON public.user_roles (is_active);
