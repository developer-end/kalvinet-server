package app.school.administration.auth.infrastructure.persistence.repository;

import app.school.administration.auth.infrastructure.persistence.entity.RoleEntity;
import app.school.administration.common.domain.repository.AppBaseRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface RoleRepository extends AppBaseRepository<RoleEntity, UUID> {

    @Transactional(readOnly = true)
    Optional<RoleEntity> findByRoleCodeIgnoreCase(String roleCode);

    @Transactional(readOnly = true)
    boolean existsByRoleCodeIgnoreCase(String roleCode);

    @Transactional(readOnly = true)
    List<RoleEntity> findByActiveTrueOrderByRoleCodeAsc();

}
