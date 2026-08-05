package app.school.administration.auth.application.service;

import app.school.administration.auth.api.request.CreateRoleRequestDTO;
import app.school.administration.auth.api.response.RoleListItemDTO;
import app.school.administration.auth.infrastructure.persistence.entity.RoleEntity;
import app.school.administration.auth.infrastructure.persistence.projection.RoleProjectionDTO;

import java.util.List;
import java.util.UUID;

public interface RoleService {

    RoleProjectionDTO findByIdProjection(UUID uuid);

    RoleEntity save(RoleEntity roleEntity);

    RoleListItemDTO createRole(CreateRoleRequestDTO request);

    List<RoleListItemDTO> listRolesForCurrentUser();

    RoleEntity update(RoleProjectionDTO dto, UUID uuid);

    RoleEntity activate(UUID uuid);

    RoleEntity deActivate(UUID uuid);

}
