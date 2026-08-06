package app.school.administration.auth.infrastructure.persistence.projection;

import java.time.Instant;

/**
 * Projection for {@code user_roles} rows. Composite PK (user_id + role_id) — no single {@code id} field.
 */
public interface UserRoleProjectionDTO {

    RoleProjectionDTO getRole();

    Instant getAssignedAt();

    boolean isActive();
}
