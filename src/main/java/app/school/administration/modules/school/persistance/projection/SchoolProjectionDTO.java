package app.school.administration.modules.school.persistance.projection;

import app.school.administration.common.infrastucture.persistence.projection.AuditableProjectionDTO;

public interface SchoolProjectionDTO extends AuditableProjectionDTO {

    String getSchoolName();

}
