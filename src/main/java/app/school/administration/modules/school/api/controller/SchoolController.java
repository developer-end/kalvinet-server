package app.school.administration.modules.school.api.controller;

import app.school.administration.common.utils.AppCommonEndPoint;
import app.school.administration.common.utils.AppModuleApi;
import app.school.administration.modules.school.api.request.UpsertSchoolRequestDTO;
import app.school.administration.modules.school.api.response.SchoolDTO;
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

@RestController
@RequiredArgsConstructor
@RequestMapping(AppModuleApi.SCHOOL)
public class SchoolController {

    private final SchoolServiceImpl schoolService;

    @GetMapping(AppCommonEndPoint.FIND_BY_ID)
    public ResponseEntity<SchoolDTO> findById(@PathVariable(name = "uuid") UUID uuid) {
        return ResponseEntity.ok(schoolService.getById(uuid));
    }

    /** @deprecated prefer DTO create */
    @GetMapping("/projection/{uuid}")
    public ResponseEntity<SchoolProjectionDTO> findByIdProjection(@PathVariable UUID uuid) {
        return ResponseEntity.ok(schoolService.findByIdProjection(uuid));
    }

    @PostMapping(AppCommonEndPoint.CREATE)
    public ResponseEntity<SchoolDTO> create(@Validated @RequestBody UpsertSchoolRequestDTO request) {
        return ResponseEntity.ok(schoolService.create(request));
    }

    @PutMapping("/update/{uuid}")
    public ResponseEntity<SchoolDTO> update(
            @PathVariable UUID uuid,
            @Validated @RequestBody UpsertSchoolRequestDTO request) {
        return ResponseEntity.ok(schoolService.updateSettings(uuid, request));
    }

    /** Legacy entity update path retained for compatibility. */
    @PutMapping(AppCommonEndPoint.UPDATE)
    public ResponseEntity<SchoolEntity> updateLegacy(@Validated @RequestBody SchoolEntity schoolEntity) {
        return ResponseEntity.ok(schoolService.save(schoolEntity));
    }

    @PutMapping(AppCommonEndPoint.DE_ACTIVATE)
    public ResponseEntity<SchoolEntity> deActivate(@PathVariable(name = "uuid") UUID uuid) {
        return ResponseEntity.ok(schoolService.deActivate(uuid));
    }
}
