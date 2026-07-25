package app.school.administration.auth.infrastructure.persistence.projection;

import app.school.administration.common.infrastucture.persistence.projection.AuditableProjectionDTO;

import java.time.Instant;

public interface TenantProjectionDTO extends AuditableProjectionDTO {

    String getTenantName();

    Instant getOpenedDate();

    Instant getClosedDate();

    String getDescription();

}
