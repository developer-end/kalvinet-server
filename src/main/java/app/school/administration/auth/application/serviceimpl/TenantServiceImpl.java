package app.school.administration.auth.application.serviceimpl;

import app.school.administration.auth.application.service.TenantService;
import app.school.administration.auth.infrastructure.persistence.entity.TenantEntity;
import app.school.administration.auth.infrastructure.persistence.entity.embeddable.TenantId;
import app.school.administration.auth.infrastructure.persistence.projection.TenantProjectionDTO;
import app.school.administration.auth.infrastructure.persistence.repository.TenantRepository;
import app.school.administration.common.application.serviceimpl.AppBaseService;
import app.school.administration.common.domain.repository.AppBaseRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class TenantServiceImpl extends AppBaseService<TenantEntity, TenantId> implements TenantService {

    private final TenantRepository tenantRepository;

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
    public TenantEntity save(TenantEntity tenant) {
        return appSave(tenant);
    }

    @Override
    public TenantEntity update(TenantProjectionDTO dto, TenantId tenantId) {
        TenantEntity tenantEntity = appFindByIdWithDirtyCheck(tenantId);
        BeanUtils.copyProperties(dto, tenantEntity);
        return tenantEntity;
    }

}
