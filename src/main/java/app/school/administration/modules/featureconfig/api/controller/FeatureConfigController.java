package app.school.administration.modules.featureconfig.api.controller;

import app.school.administration.common.utils.AppModuleApi;
import app.school.administration.modules.featureconfig.api.request.UpsertFeatureConfigRequestDTO;
import app.school.administration.modules.featureconfig.api.response.FeatureConfigDTO;
import app.school.administration.modules.featureconfig.application.service.FeatureAccessService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping(AppModuleApi.FEATURE_CONFIG)
public class FeatureConfigController {

    private final FeatureAccessService featureAccessService;

    @GetMapping("/list")
    public ResponseEntity<List<FeatureConfigDTO>> list(
            @RequestParam String scope,
            @RequestParam(required = false) UUID schoolId) {
        return ResponseEntity.ok(featureAccessService.listForScope(scope, schoolId));
    }

    @PostMapping("/upsert")
    public ResponseEntity<FeatureConfigDTO> upsert(@Validated @RequestBody UpsertFeatureConfigRequestDTO request) {
        return ResponseEntity.ok(featureAccessService.upsert(request));
    }
}
