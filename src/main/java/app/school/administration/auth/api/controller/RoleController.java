package app.school.administration.auth.api.controller;

import app.school.administration.auth.api.request.CreateRoleRequestDTO;
import app.school.administration.auth.api.response.RoleListItemDTO;
import app.school.administration.auth.application.serviceimpl.RoleServiceImpl;
import app.school.administration.auth.infrastructure.persistence.projection.RoleProjectionDTO;
import app.school.administration.common.utils.AppCommonEndPoint;
import app.school.administration.common.utils.AppModuleApi;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping({AppModuleApi.ROLE, AppModuleApi.ROLE_LEGACY})
public class RoleController {

    private final RoleServiceImpl roleService;

    @GetMapping(AppCommonEndPoint.FIND_BY_ID)
    public ResponseEntity<RoleProjectionDTO> findById(@PathVariable(name = "uuid") UUID uuid) {
        return ResponseEntity.ok(roleService.findByIdProjection(uuid));
    }

    @GetMapping(AppCommonEndPoint.LIST)
    public ResponseEntity<List<RoleListItemDTO>> list() {
        return ResponseEntity.ok(roleService.listRolesForCurrentUser());
    }

    @PostMapping(AppCommonEndPoint.CREATE)
    public ResponseEntity<RoleListItemDTO> create(@Validated @RequestBody CreateRoleRequestDTO request) {
        return ResponseEntity.ok(roleService.createRole(request));
    }

    @PutMapping(AppCommonEndPoint.DE_ACTIVATE)
    public ResponseEntity<?> deActivate(@PathVariable(name = "uuid") UUID uuid) {
        return ResponseEntity.ok(roleService.deActivate(uuid));
    }

}
