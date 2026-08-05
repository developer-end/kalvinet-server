package app.school.administration.auth.application.serviceimpl;

import app.school.administration.auth.api.request.AssignRoleRequestDTO;
import app.school.administration.auth.api.response.UserListItemDTO;
import app.school.administration.auth.application.constant.RoleAssignmentPolicy;
import app.school.administration.auth.application.service.UserService;
import app.school.administration.auth.domain.model.CustomUserDetails;
import app.school.administration.auth.infrastructure.persistence.entity.RoleAssignmentAuditEntity;
import app.school.administration.auth.infrastructure.persistence.entity.RoleEntity;
import app.school.administration.auth.infrastructure.persistence.entity.UserEntity;
import app.school.administration.auth.infrastructure.persistence.entity.embeddable.UserRoleId;
import app.school.administration.auth.infrastructure.persistence.entity.mapping.UserRoleEntity;
import app.school.administration.auth.infrastructure.persistence.projection.UserProjectionDTO;
import app.school.administration.auth.infrastructure.persistence.repository.RoleAssignmentAuditRepository;
import app.school.administration.auth.infrastructure.persistence.repository.RoleRepository;
import app.school.administration.auth.infrastructure.persistence.repository.UserRepository;
import app.school.administration.auth.infrastructure.persistence.repository.UserRoleRepository;
import app.school.administration.common.application.custom.exception.NoDataFoundException;
import app.school.administration.common.application.serviceimpl.AppBaseService;
import app.school.administration.common.domain.repository.AppBaseRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserServiceImpl extends AppBaseService<UserEntity, UUID> implements UserService {

    private final UserRepository userRepository;
    private final UserRoleRepository userRoleRepository;
    private final RoleRepository roleRepository;
    private final RoleAssignmentAuditRepository roleAssignmentAuditRepository;
    private final CacheManager cacheManager;

    @Override
    protected AppBaseRepository<UserEntity, UUID> getJpaRepository() {
        return this.userRepository;
    }

    @Override
    public UserEntity findByUsernameIgnoreCase(String userName) {
        return userRepository.findByUsernameIgnoreCase(userName)
                .orElseThrow(() -> new UsernameNotFoundException("User not founded"));
    }

    @Override
    public Optional<UserEntity> findByUsernameAndPassword(String username, String password) {
        return userRepository.findByUsernameAndPassword(username, password);
    }

    @Override
    public UserProjectionDTO findByIdProjection(UUID id) {
        return appFindByIdProjection(id, UserProjectionDTO.class);
    }

    @Override
    @Transactional
    public Void userRoleDeActivate(UserRoleId id) {
        UserRoleEntity userRoleEntity = userRoleRepository.findById(id).orElseThrow(NoDataFoundException::new);
        userRoleEntity.setActive(false);
        userRoleRepository.save(userRoleEntity);
        return null;
    }

    @Override
    public UserEntity deActivate(UUID uuid) {
        return appDeActivate(uuid);
    }

    @Override
    public UserEntity activate(UUID uuid) {
        return appActivate(uuid);
    }

    @Override
    public UserEntity save(UserEntity user) {
        return appSave(user);
    }

    @Override
    public UserEntity update(UserProjectionDTO dto, UUID uuid) {
        UserEntity user = appFindByIdWithDirtyCheck(uuid);
        BeanUtils.copyProperties(dto, user);
        return user;
    }

    @Override
    @Transactional(readOnly = true)
    public List<String> getAssignableRolesForCurrentUser() {
        CustomUserDetails actor = requireActor();
        Set<String> callerRoles = actor.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.toSet());
        if (!RoleAssignmentPolicy.canAssignRoles(callerRoles)) {
            throw new AccessDeniedException("You do not have role assignment privileges");
        }
        List<String> catalog = roleRepository.findByActiveTrueOrderByRoleCodeAsc().stream()
                .map(RoleEntity::getRoleCode)
                .toList();
        return List.copyOf(RoleAssignmentPolicy.assignableRolesFor(callerRoles, catalog));
    }

    @Override
    @Transactional
    public UserListItemDTO assignRole(UUID targetUserId, AssignRoleRequestDTO request) {
        CustomUserDetails actor = requireActor();
        Set<String> callerRoles = actor.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.toSet());

        String targetRoleCode = RoleAssignmentPolicy.normalize(request.roleCode());
        List<String> catalog = roleRepository.findByActiveTrueOrderByRoleCodeAsc().stream()
                .map(RoleEntity::getRoleCode)
                .toList();
        RoleAssignmentPolicy.assertCanAssign(callerRoles, targetRoleCode, catalog);

        RoleEntity newRole = roleRepository.findByRoleCodeIgnoreCase(targetRoleCode)
                .orElseThrow(() -> new NoDataFoundException("Role not found: " + targetRoleCode));

        UserEntity target = userRepository.findById(targetUserId)
                .orElseThrow(NoDataFoundException::new);

        List<UserRoleEntity> mappings = userRoleRepository.findByUser_Id(targetUserId);
        String previousRoleCodes = mappings.stream()
                .filter(UserRoleEntity::isActive)
                .map(m -> m.getRole() != null ? m.getRole().getRoleCode() : null)
                .filter(code -> code != null && !code.isBlank())
                .collect(Collectors.joining(","));

        for (UserRoleEntity mapping : mappings) {
            if (mapping.isActive()) {
                mapping.setActive(false);
                userRoleRepository.save(mapping);
            }
        }

        Optional<UserRoleEntity> existingForRole = mappings.stream()
                .filter(m -> m.getRole() != null && newRole.getId().equals(m.getRole().getId()))
                .findFirst();

        if (existingForRole.isPresent()) {
            UserRoleEntity existing = existingForRole.get();
            existing.setActive(true);
            userRoleRepository.save(existing);
        } else {
            userRoleRepository.save(new UserRoleEntity(target, newRole));
        }

        roleAssignmentAuditRepository.save(new RoleAssignmentAuditEntity(
                actor.toAuthUser().id(),
                target.getId(),
                previousRoleCodes.isBlank() ? null : previousRoleCodes,
                targetRoleCode
        ));

        evictAuthCache(target.getUsername());

        // Refresh active roles for response
        List<UserRoleEntity> refreshed = userRoleRepository.findByUser_Id(targetUserId);
        List<String> activeCodes = refreshed.stream()
                .filter(UserRoleEntity::isActive)
                .map(m -> m.getRole().getRoleCode())
                .toList();

        return new UserListItemDTO(
                target.getId(),
                target.getFirstName(),
                target.getLastName(),
                target.getEmail(),
                target.getUsername(),
                activeCodes,
                target.isActive()
        );
    }

    @Override
    @Transactional(readOnly = true)
    public Page<UserListItemDTO> searchUsers(String query, Pageable pageable) {
        CustomUserDetails actor = requireActor();
        Set<String> callerRoles = actor.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.toSet());
        if (!RoleAssignmentPolicy.canAssignRoles(callerRoles)) {
            throw new AccessDeniedException("You do not have role assignment privileges");
        }

        return userRepository.search(query == null ? "" : query.trim(), pageable)
                .map(this::toListItem);
    }

    private UserListItemDTO toListItem(UserEntity user) {
        List<String> roleCodes = user.getRoles().stream()
                .filter(r -> r.getRole() != null)
                .map(r -> r.getRole().getRoleCode())
                .toList();
        return new UserListItemDTO(
                user.getId(),
                user.getFirstName(),
                user.getLastName(),
                user.getEmail(),
                user.getUsername(),
                roleCodes,
                user.isActive()
        );
    }

    private CustomUserDetails requireActor() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !(authentication.getPrincipal() instanceof CustomUserDetails details)) {
            throw new AccessDeniedException("Authenticated user required");
        }
        return details;
    }

    private void evictAuthCache(String username) {
        Cache cache = cacheManager.getCache(CustomUserDetailsServiceImpl.cacheName);
        if (cache != null) {
            cache.evict(username);
        }
    }
}
