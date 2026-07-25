package app.school.administration.auth.application.service;

import app.school.administration.auth.infrastructure.persistence.entity.TenantEntity;
import app.school.administration.auth.infrastructure.persistence.entity.embeddable.TenantId;
import app.school.administration.auth.infrastructure.persistence.projection.TenantProjectionDTO;

public interface TenantService {

    TenantProjectionDTO findByIdProjection(TenantId tenantId);

    TenantEntity deActivate(TenantId tenantId);

    TenantEntity activate(TenantId tenantId);

    TenantEntity save(TenantEntity tenantEntity);

    TenantEntity update(TenantProjectionDTO dto, TenantId tenantId);

}
