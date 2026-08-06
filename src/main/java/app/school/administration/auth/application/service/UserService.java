package app.school.administration.auth.application.service;

import app.school.administration.auth.api.request.AssignRoleRequestDTO;
import app.school.administration.auth.api.response.UserListItemDTO;
import app.school.administration.auth.infrastructure.persistence.entity.UserEntity;
import app.school.administration.auth.infrastructure.persistence.entity.embeddable.UserRoleId;
import app.school.administration.auth.infrastructure.persistence.projection.UserProjectionDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface UserService {

    UserEntity findByUsernameIgnoreCase(String userName);

    Optional<UserEntity> findByUsernameAndPassword(String username, String password);

    UserProjectionDTO findByIdProjection(UUID uuid);

    UserListItemDTO findByIdListItem(UUID uuid);

    Void userRoleDeActivate(UserRoleId id);

    UserEntity deActivate(UUID uuid);

    UserEntity activate(UUID uuid);

    UserEntity save(UserEntity user);

    UserEntity update(UserProjectionDTO dto, UUID uuid);

    List<String> getAssignableRolesForCurrentUser();

    UserListItemDTO assignRole(UUID targetUserId, AssignRoleRequestDTO request);

    Page<UserListItemDTO> searchUsers(String query, Pageable pageable);
}
