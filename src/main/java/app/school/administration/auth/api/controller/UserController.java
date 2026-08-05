package app.school.administration.auth.api.controller;

import app.school.administration.auth.api.request.AssignRoleRequestDTO;
import app.school.administration.auth.api.response.UserListItemDTO;
import app.school.administration.auth.application.serviceimpl.UserServiceImpl;
import app.school.administration.auth.infrastructure.persistence.entity.UserEntity;
import app.school.administration.auth.infrastructure.persistence.entity.embeddable.UserRoleId;
import app.school.administration.auth.infrastructure.persistence.projection.UserProjectionDTO;
import app.school.administration.common.utils.AppCommonEndPoint;
import app.school.administration.common.utils.AppModuleApi;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping(AppModuleApi.USER)
public class UserController {

    private final UserServiceImpl userService;

    @GetMapping(AppCommonEndPoint.FIND_BY_ID)
    public ResponseEntity<UserProjectionDTO> findById(@PathVariable(name = "uuid") UUID uuid) {
        return ResponseEntity.ok(userService.findByIdProjection(uuid));
    }

    @GetMapping(AppCommonEndPoint.SEARCH)
    public ResponseEntity<Page<UserListItemDTO>> search(
            @RequestParam(name = "q", required = false, defaultValue = "") String q,
            @PageableDefault(size = 20) Pageable pageable) {
        return ResponseEntity.ok(userService.searchUsers(q, pageable));
    }

    @GetMapping(AppCommonEndPoint.ASSIGNABLE_ROLES)
    public ResponseEntity<List<String>> assignableRoles() {
        return ResponseEntity.ok(userService.getAssignableRolesForCurrentUser());
    }

    @PostMapping(AppCommonEndPoint.ASSIGN_ROLE)
    public ResponseEntity<UserListItemDTO> assignRole(
            @PathVariable(name = "uuid") UUID uuid,
            @Validated @RequestBody AssignRoleRequestDTO request) {
        return ResponseEntity.ok(userService.assignRole(uuid, request));
    }

    @PostMapping(AppCommonEndPoint.CREATE)
    public ResponseEntity<UserEntity> create(@Validated @RequestBody UserEntity userEntity) {
        return ResponseEntity.ok(userService.save(userEntity));
    }

    @PutMapping(AppCommonEndPoint.UPDATE)
    public ResponseEntity<UserEntity> update(@Validated @RequestBody UserEntity userEntity) {
        return ResponseEntity.ok(userService.save(userEntity));
    }

    @PutMapping(AppCommonEndPoint.DE_ACTIVATE)
    public ResponseEntity<UserEntity> deActivate(@PathVariable(name = "uuid") UUID uuid) {
        return ResponseEntity.ok(userService.deActivate(uuid));
    }

    @PutMapping(AppCommonEndPoint.USER_ROLE_MAPPING_DE_ACTIVATE)
    public ResponseEntity<Void> userRoleMappingDeActivate(@PathVariable(name = "uuid") UserRoleId uuid) {
        return ResponseEntity.ok(userService.userRoleDeActivate(uuid));
    }
}
