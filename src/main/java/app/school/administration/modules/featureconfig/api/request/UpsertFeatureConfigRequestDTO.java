package app.school.administration.modules.featureconfig.api.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.List;
import java.util.UUID;

public record UpsertFeatureConfigRequestDTO(
        @NotBlank String featureCode,
        @NotBlank String scope,
        UUID schoolId,
        @NotNull Boolean enabled,
        List<String> roleCodes
) {
}
