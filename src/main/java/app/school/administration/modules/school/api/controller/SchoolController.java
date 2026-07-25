package app.school.administration.modules.school.api.controller;

import app.school.administration.common.utils.AppCommonEndPoint;
import app.school.administration.common.utils.AppModuleApi;
import app.school.administration.modules.school.application.serviceimpl.SchoolServiceImpl;
import app.school.administration.modules.school.persistance.entity.SchoolEntity;
import app.school.administration.modules.school.persistance.projection.SchoolProjectionDTO;
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

import java.util.UUID;

/**
 * ====================================================================================
 * REST CONTROLLER: SchoolController
 * ====================================================================================
 *
 * <h2>FUNCTIONALITY</h2>
 * REST API controller exposing endpoints for school administration (`/api/v1/school/...`).
 *
 * <h2>WHY IMPLEMENTED</h2>
 * Exposes web endpoints for managing school branches:
 * - Read school projection by UUID (`/find-by-id/{uuid}`).
 * - Create new school records (`/create`).
 * - Update existing school records (`/update`).
 * - Deactivate school branches (`/deactivate/{uuid}`).
 *
 * <h2>WHAT HAPPENS IF NOT IMPLEMENTED</h2>
 * External clients cannot execute CRUD or state change operations on school entity records.
 * ====================================================================================
 */
@RestController
@RequiredArgsConstructor
@RequestMapping(AppModuleApi.SCHOOL)
public class SchoolController {

    private final SchoolServiceImpl schoolService;

    /**
     * Finds school projection by primary key UUID.
     *
     * @param uuid school UUID
     * @return ResponseEntity with SchoolProjectionDTO
     */
    @GetMapping(AppCommonEndPoint.FIND_BY_ID)
    public ResponseEntity<SchoolProjectionDTO> findById(@PathVariable(name = "uuid") UUID uuid) {
        return ResponseEntity.ok(schoolService.findByIdProjection(uuid));
    }

    /**
     * Creates a new school record.
     *
     * @param schoolEntity school entity payload
     * @return ResponseEntity with created SchoolEntity
     */
    @PostMapping(AppCommonEndPoint.CREATE)
    public ResponseEntity<SchoolEntity> create(@Validated @RequestBody SchoolEntity schoolEntity) {
        return ResponseEntity.ok(schoolService.save(schoolEntity));
    }

    /**
     * Updates an existing school record.
     *
     * @param schoolEntity school entity payload
     * @return ResponseEntity with updated SchoolEntity
     */
    @PutMapping(AppCommonEndPoint.UPDATE)
    public ResponseEntity<SchoolEntity> update(@Validated @RequestBody SchoolEntity schoolEntity) {
        return ResponseEntity.ok(schoolService.save(schoolEntity));
    }

    /**
     * Deactivates a school branch by ID.
     *
     * @param uuid target school UUID
     * @return ResponseEntity with deactivated SchoolEntity
     */
    @PutMapping(AppCommonEndPoint.DE_ACTIVATE)
    public ResponseEntity<SchoolEntity> deActivate(@PathVariable(name = "uuid") UUID uuid) {
        return ResponseEntity.ok(schoolService.deActivate(uuid));
    }

}

