package app.school.administration.auth.application.serviceimpl;

import app.school.administration.auth.application.service.TenantService;
import app.school.administration.auth.infrastructure.persistence.entity.TenantEntity;
import app.school.administration.auth.infrastructure.persistence.entity.embeddable.TenantId;
import app.school.administration.auth.infrastructure.persistence.projection.TenantProjectionDTO;
import app.school.administration.auth.infrastructure.persistence.repository.TenantRepository;
import app.school.administration.common.application.serviceimpl.AppBaseService;
import app.school.administration.common.application.serviceimpl.TenantMigrationService;
import app.school.administration.common.domain.repository.AppBaseRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class TenantServiceImpl extends AppBaseService<TenantEntity, TenantId> implements TenantService {

    private final TenantRepository tenantRepository;
    private final TenantMigrationService tenantMigrationService;

    @Override
    protected AppBaseRepository<TenantEntity, TenantId> getJpaRepository() {
        return this.tenantRepository;
    }

    @Override
    public TenantProjectionDTO findByIdProjection(TenantId tenantId) {
        return appFindByIdProjection(tenantId, TenantProjectionDTO.class);
    }

    @Override
    public TenantEntity deActivate(TenantId tenantId) {
        return appDeActivate(tenantId);
    }

    @Override
    public TenantEntity activate(TenantId tenantId) {
        return appActivate(tenantId);
    }

    @Override
    @Transactional
    public TenantEntity save(TenantEntity tenant) {
        if (tenant.getId() == null) {
            tenant.setId(UUID.randomUUID());
        }
        TenantEntity savedTenant = appSave(tenant);
        if (savedTenant != null && savedTenant.getTenantName() != null && !savedTenant.getTenantName().isBlank()) {
            tenantMigrationService.migrateTenant(savedTenant.getTenantName());
        }
        return savedTenant;
    }

    @Override
    public TenantEntity update(TenantProjectionDTO dto, TenantId tenantId) {
        TenantEntity tenantEntity = appFindByIdWithDirtyCheck(tenantId);
        BeanUtils.copyProperties(dto, tenantEntity);
        return tenantEntity;
    }

}
