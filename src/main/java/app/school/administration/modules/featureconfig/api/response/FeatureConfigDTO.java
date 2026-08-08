package app.school.administration.modules.featureconfig.api.response;

import java.util.List;
import java.util.UUID;

public record FeatureConfigDTO(
        UUID id,
        String featureCode,
        String featureName,
        String scope,
        UUID institutionId,
        UUID schoolId,
        boolean enabled,
        List<String> roleCodes,
        boolean configured
) {
}
