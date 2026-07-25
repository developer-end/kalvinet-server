package app.school.administration.modules.school.application.serviceimpl;

import app.school.administration.common.application.serviceimpl.AppBaseService;
import app.school.administration.common.domain.repository.AppBaseRepository;
import app.school.administration.modules.school.application.service.SchoolService;
import app.school.administration.modules.school.persistance.entity.SchoolEntity;
import app.school.administration.modules.school.persistance.projection.SchoolProjectionDTO;
import app.school.administration.modules.school.persistance.repository.SchoolRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.util.UUID;

/**
 * ====================================================================================
 * SERVICE IMPLEMENTATION: SchoolServiceImpl
 * ====================================================================================
 *
 * <h2>FUNCTIONALITY</h2>
 * Business service handling CRUD operations, activation state toggles, and projection lookups for {@link SchoolEntity}.
 * Extends {@link AppBaseService} to leverage generic database handling.
 *
 * <h2>WHY IMPLEMENTED</h2>
 * Business logic layer for school administration:
 * - Manages school entity updates with property mapping via {@link BeanUtils#copyProperties}.
 * - Controls school active/inactive status via {@link #activate(UUID)} and {@link #deActivate(UUID)}.
 * - Serves read-only projection DTOs via {@link #findByIdProjection(UUID)}.
 *
 * <h2>WHAT HAPPENS IF NOT IMPLEMENTED</h2>
 * School administration endpoints (`/school/...`) will fail to register, modify, or query school branch data.
 * ====================================================================================
 */
@Service
@RequiredArgsConstructor
public class SchoolServiceImpl extends AppBaseService<SchoolEntity, UUID> implements SchoolService {

    private final SchoolRepository schoolRepository;

    @Override
    protected AppBaseRepository<SchoolEntity, UUID> getJpaRepository() {
        return schoolRepository;
    }

    /**
     * Deactivates a school entity by ID (`is_active = false`).
     *
     * @param uuid primary key of school
     * @return updated school entity
     */
    @Override
    public SchoolEntity deActivate(UUID uuid) {
        return appDeActivate(uuid);
    }

    /**
     * Activates a school entity by ID (`is_active = true`).
     *
     * @param uuid primary key of school
     * @return updated school entity
     */
    @Override
    public SchoolEntity activate(UUID uuid) {
        return appActivate(uuid);
    }

    /**
     * Saves a new school record in database.
     *
     * @param schoolEntity school entity to create
     * @return saved school entity
     */
    @Override
    public SchoolEntity save(SchoolEntity schoolEntity) {
        return appSave(schoolEntity);
    }

    /**
     * Updates an existing school entity using projection DTO values.
     *
     * @param dto projection DTO containing updated fields
     * @param uuid target school ID
     * @return updated school entity
     */
    @Override
    public SchoolEntity update(SchoolProjectionDTO dto, UUID uuid) {
        SchoolEntity school = appFindByIdWithDirtyCheck(uuid);
        BeanUtils.copyProperties(dto, school);
        return school;
    }

    /**
     * Retrieves a school projection DTO by ID.
     *
     * @param uuid primary key of school
     * @return projection DTO
     */
    @Override
    public SchoolProjectionDTO findByIdProjection(UUID uuid) {
        return appFindByIdProjection(uuid, SchoolProjectionDTO.class);
    }

}

