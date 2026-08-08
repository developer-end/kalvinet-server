package app.school.administration.modules.institution.api.controller;

import app.school.administration.common.utils.AppCommonEndPoint;
import app.school.administration.common.utils.AppModuleApi;
import app.school.administration.modules.institution.api.request.UpdateInstitutionRequestDTO;
import app.school.administration.modules.institution.api.response.InstitutionDTO;
import app.school.administration.modules.institution.application.service.InstitutionService;
import app.school.administration.modules.school.api.response.SchoolListItemDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping(AppModuleApi.INSTITUTION)
public class InstitutionController {

    private final InstitutionService institutionService;

    @GetMapping("/current")
    public ResponseEntity<InstitutionDTO> current() {
        return ResponseEntity.ok(institutionService.getSingleton());
    }

    @PutMapping(AppCommonEndPoint.UPDATE)
    public ResponseEntity<InstitutionDTO> update(@Validated @RequestBody UpdateInstitutionRequestDTO request) {
        return ResponseEntity.ok(institutionService.update(request));
    }

    @GetMapping("/schools")
    public ResponseEntity<List<SchoolListItemDTO>> schools() {
        return ResponseEntity.ok(institutionService.listSchools());
    }
}
