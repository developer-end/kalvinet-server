-- ====================================================================================
-- MIGRATION: V4__system_role_catalog.sql
-- ====================================================================================
-- Baseline system role catalog for KalviNet (fresh-install source of truth).
-- Depends on: V2__user_role_table.sql (public.role_table).
--
-- Predefined roles only:
--   ROLE_USER         — default at sign-up (pending institutional assignment)
--   ROLE_OWNER        — platform / institution owner
--   ROLE_MANAGER      — operational manager
--   ROLE_MANAGEMENT   — institution management
--   ROLE_IT           — IT administrator; grant only via manual DB action
--
-- Customer / institutional roles (STUDENT, TEACHER, STAFF, …) are NOT seeded.
-- They are registered at runtime by an IT account via the Role Registry API/UI.
-- ====================================================================================

INSERT INTO public.role_table (role_code, role_name, description, version)
VALUES
    ('ROLE_USER', 'User', 'Default role after sign-up; pending institutional role assignment', 1),
    ('ROLE_OWNER', 'Owner', 'Platform / institution owner', 1),
    ('ROLE_MANAGER', 'Manager', 'Operational manager with elevated role-assignment rights', 1),
    ('ROLE_MANAGEMENT', 'Management', 'Institution management', 1),
    ('ROLE_IT', 'IT', 'IT administrator — granted exclusively via manual database action; may register customer roles', 1)
;
