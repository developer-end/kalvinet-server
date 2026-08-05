-- ====================================================================================
-- MIGRATION: V5__role_assignment_audit.sql
-- ====================================================================================
-- Privilege-escalation audit trail for role (re)assignment.
-- Depends on: V2__user_role_table.sql (public.user_table).
-- Written by UserServiceImpl.assignRole on every successful assignment.
-- ====================================================================================

CREATE TABLE public.role_assignment_audit
(
    audit_id           UUID PRIMARY KEY      DEFAULT gen_random_uuid(),
    actor_user_id      UUID         NOT NULL,
    target_user_id     UUID         NOT NULL,
    previous_role_codes TEXT,
    assigned_role_code CITEXT       NOT NULL,
    created_at         TIMESTAMPTZ  NOT NULL DEFAULT now(),

    CONSTRAINT fk_role_assign_audit_actor
        FOREIGN KEY (actor_user_id) REFERENCES public.user_table (user_id),

    CONSTRAINT fk_role_assign_audit_target
        FOREIGN KEY (target_user_id) REFERENCES public.user_table (user_id)
);

CREATE INDEX idx_role_assign_audit_target
    ON public.role_assignment_audit (target_user_id);

CREATE INDEX idx_role_assign_audit_actor
    ON public.role_assignment_audit (actor_user_id);

CREATE INDEX idx_role_assign_audit_created
    ON public.role_assignment_audit (created_at);

CREATE INDEX idx_role_assign_audit_assigned_role
    ON public.role_assignment_audit (assigned_role_code);
