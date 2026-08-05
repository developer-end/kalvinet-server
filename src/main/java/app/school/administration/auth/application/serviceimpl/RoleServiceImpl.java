package app.school.administration.auth.application.serviceimpl;

import app.school.administration.auth.api.request.CreateRoleRequestDTO;
import app.school.administration.auth.api.response.RoleListItemDTO;
import app.school.administration.auth.application.constant.RoleAssignmentPolicy;
import app.school.administration.auth.application.service.RoleService;
import app.school.administration.auth.infrastructure.persistence.entity.RoleEntity;
import app.school.administration.auth.infrastructure.persistence.projection.RoleProjectionDTO;
import app.school.administration.auth.infrastructure.persistence.repository.RoleRepository;
import app.school.administration.auth.domain.model.CustomUserDetails;
import app.school.administration.common.application.serviceimpl.AppBaseService;
import app.school.administration.common.domain.repository.AppBaseRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class RoleServiceImpl extends AppBaseService<RoleEntity, UUID> implements RoleService {

    private final RoleRepository roleRepository;

    @Override
    protected AppBaseRepository<RoleEntity, UUID> getJpaRepository() {
        return this.roleRepository;
    }

    @Override
    public RoleProjectionDTO findByIdProjection(UUID id) {
        return appFindByIdProjection(id, RoleProjectionDTO.class);
    }

    @Override
    public RoleEntity save(RoleEntity roleEntity) {
        throw new UnsupportedOperationException("Use createRole(CreateRoleRequestDTO) instead");
    }

    @Override
    @Transactional
    public RoleListItemDTO createRole(CreateRoleRequestDTO request) {
        CustomUserDetails actor = requireActor();
        Set<String> callerRoles = actor.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.toSet());
        RoleAssignmentPolicy.assertCanRegisterRoles(callerRoles);

        String roleCode = RoleAssignmentPolicy.normalize(request.roleCode());
        if (RoleAssignmentPolicy.ROLE_IT.equals(roleCode)) {
            throw new AccessDeniedException("ROLE_IT cannot be created through the application");
        }
        if (RoleAssignmentPolicy.BASELINE_ROLES.contains(roleCode)) {
            throw new IllegalArgumentException(
                    roleCode + " is a baseline system role and cannot be registered again");
        }
        if (roleRepository.existsByRoleCodeIgnoreCase(roleCode)) {
            throw new IllegalArgumentException("Role already exists: " + roleCode);
        }

        String roleName = request.roleName() == null ? "" : request.roleName().trim();
        if (roleName.isBlank()) {
            throw new IllegalArgumentException("Role name is required");
        }

        RoleEntity entity = new RoleEntity(roleCode, roleName,
                request.description() == null ? null : request.description().trim());
        entity.setActive(true);

        RoleEntity saved = appSave(entity);
        return toListItem(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public List<RoleListItemDTO> listRolesForCurrentUser() {
        CustomUserDetails actor = requireActor();
        Set<String> callerRoles = actor.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.toSet());
        RoleAssignmentPolicy.assertCanRegisterRoles(callerRoles);

        return roleRepository.findByActiveTrueOrderByRoleCodeAsc().stream()
                .map(this::toListItem)
                .toList();
    }

    @Override
    public RoleEntity update(RoleProjectionDTO dto, UUID uuid) {
        RoleEntity roleEntity = appFindByIdWithDirtyCheck(uuid);
        BeanUtils.copyProperties(dto, roleEntity);
        return roleEntity;
    }

    @Override
    public RoleEntity activate(UUID uuid) {
        return appActivate(uuid);
    }

    @Override
    public RoleEntity deActivate(UUID uuid) {
        return appDeActivate(uuid);
    }

    private RoleListItemDTO toListItem(RoleEntity role) {
        return new RoleListItemDTO(
                role.getId(),
                role.getRoleCode(),
                role.getRoleName(),
                role.getDescription(),
                role.isActive()
        );
    }

    private CustomUserDetails requireActor() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !(authentication.getPrincipal() instanceof CustomUserDetails details)) {
            throw new AccessDeniedException("Authenticated user required");
        }
        return details;
    }
}
