package app.school.administration.modules.school.application.service;

import app.school.administration.modules.school.api.request.UpsertSchoolRequestDTO;
import app.school.administration.modules.school.api.response.SchoolDTO;
import app.school.administration.modules.school.persistance.entity.SchoolEntity;
import app.school.administration.modules.school.persistance.projection.SchoolProjectionDTO;

import java.util.UUID;

public interface SchoolService {

    SchoolDTO getById(UUID uuid);

    SchoolDTO create(UpsertSchoolRequestDTO request);

    SchoolDTO updateSettings(UUID uuid, UpsertSchoolRequestDTO request);

    SchoolEntity deActivate(UUID uuid);

    SchoolEntity activate(UUID uuid);

    /** @deprecated prefer {@link #create(UpsertSchoolRequestDTO)} */
    SchoolEntity save(SchoolEntity schoolEntity);

    SchoolEntity update(SchoolProjectionDTO dto, UUID uuid);

    SchoolProjectionDTO findByIdProjection(UUID uuid);
}
