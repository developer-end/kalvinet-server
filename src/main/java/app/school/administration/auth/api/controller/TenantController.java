package app.school.administration.auth.api.controller;

import app.school.administration.auth.application.serviceimpl.TenantServiceImpl;
import app.school.administration.auth.infrastructure.persistence.entity.TenantEntity;
import app.school.administration.auth.infrastructure.persistence.entity.embeddable.TenantId;
import app.school.administration.auth.infrastructure.persistence.projection.TenantProjectionDTO;
import app.school.administration.common.utils.AppCommonEndPoint;
import app.school.administration.common.utils.AppModuleApi;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping(AppModuleApi.TENANT)
public class TenantController {

    private final TenantServiceImpl tenantService;

    @GetMapping(AppCommonEndPoint.FIND_BY_ID)
    public ResponseEntity<TenantProjectionDTO> findById(@RequestBody TenantId tenantId) {
        return ResponseEntity.ok(tenantService.findByIdProjection(tenantId));
    }

    @PostMapping(AppCommonEndPoint.CREATE)
    public ResponseEntity<TenantEntity> create(@Validated @RequestBody TenantEntity tenantEntity) {
        return ResponseEntity.ok(tenantService.save(tenantEntity));
    }

    @PutMapping(AppCommonEndPoint.UPDATE)
    public ResponseEntity<TenantEntity> update(@Validated @RequestBody TenantEntity tenantEntity) {
        return ResponseEntity.ok(tenantService.save(tenantEntity));
    }

    @PutMapping(AppCommonEndPoint.DE_ACTIVATE)
    public ResponseEntity<TenantEntity> deActivate(@RequestBody TenantId tenantId) {
        return ResponseEntity.ok(tenantService.deActivate(tenantId));
    }

}
