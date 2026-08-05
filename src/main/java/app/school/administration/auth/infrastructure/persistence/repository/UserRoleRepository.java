package app.school.administration.auth.infrastructure.persistence.repository;

import app.school.administration.auth.infrastructure.persistence.entity.embeddable.UserRoleId;
import app.school.administration.auth.infrastructure.persistence.entity.mapping.UserRoleEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface UserRoleRepository extends JpaRepository<UserRoleEntity, UserRoleId> {

    List<UserRoleEntity> findByUser_Id(UUID userId);
}
