package app.school.administration.auth.infrastructure.persistence.repository;

import app.school.administration.auth.infrastructure.persistence.entity.RoleEntity;
import app.school.administration.common.domain.repository.AppBaseRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface RoleRepository extends AppBaseRepository<RoleEntity, UUID> {

    @Transactional(readOnly = true)
    Optional<RoleEntity> findByRoleCodeIgnoreCase(String roleCode);

}
