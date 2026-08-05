package app.school.administration.auth.infrastructure.persistence.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.UuidGenerator;

import java.time.Instant;
import java.util.UUID;

@Entity
@Getter
@Setter
@Table(name = "role_assignment_audit", schema = "public")
public class RoleAssignmentAuditEntity {

    @Id
    @GeneratedValue
    @UuidGenerator
    @Column(name = "audit_id", nullable = false, updatable = false)
    private UUID id;

    @Column(name = "actor_user_id", nullable = false, updatable = false)
    private UUID actorUserId;

    @Column(name = "target_user_id", nullable = false, updatable = false)
    private UUID targetUserId;

    @Column(name = "previous_role_codes")
    private String previousRoleCodes;

    @Column(name = "assigned_role_code", nullable = false)
    private String assignedRoleCode;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt = Instant.now();

    protected RoleAssignmentAuditEntity() {
    }

    public RoleAssignmentAuditEntity(UUID actorUserId, UUID targetUserId, String previousRoleCodes, String assignedRoleCode) {
        this.actorUserId = actorUserId;
        this.targetUserId = targetUserId;
        this.previousRoleCodes = previousRoleCodes;
        this.assignedRoleCode = assignedRoleCode;
        this.createdAt = Instant.now();
    }
}
