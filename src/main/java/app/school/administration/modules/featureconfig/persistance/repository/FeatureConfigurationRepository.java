package app.school.administration.modules.featureconfig.persistance.repository;

import app.school.administration.common.domain.repository.AppBaseRepository;
import app.school.administration.modules.featureconfig.persistance.entity.FeatureConfigurationEntity;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface FeatureConfigurationRepository extends AppBaseRepository<FeatureConfigurationEntity, UUID> {

    Optional<FeatureConfigurationEntity> findByFeatureCodeAndScopeAndInstitutionIdAndSchoolIdIsNullAndActiveTrue(
            String featureCode, String scope, UUID institutionId);

    Optional<FeatureConfigurationEntity> findByFeatureCodeAndScopeAndSchoolIdAndActiveTrue(
            String featureCode, String scope, UUID schoolId);

    List<FeatureConfigurationEntity> findByInstitutionIdAndSchoolIdIsNullAndActiveTrue(UUID institutionId);

    List<FeatureConfigurationEntity> findBySchoolIdAndActiveTrue(UUID schoolId);
}
