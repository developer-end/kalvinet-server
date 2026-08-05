package app.school.administration.auth.infrastructure.persistence.repository;

import app.school.administration.auth.infrastructure.persistence.entity.RoleAssignmentAuditEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface RoleAssignmentAuditRepository extends JpaRepository<RoleAssignmentAuditEntity, UUID> {
}
