package app.school.administration.modules.featureconfig.persistance.repository;

import app.school.administration.common.domain.repository.AppBaseRepository;
import app.school.administration.modules.featureconfig.persistance.entity.FeatureRoleGrantEntity;

import java.util.List;
import java.util.UUID;

public interface FeatureRoleGrantRepository extends AppBaseRepository<FeatureRoleGrantEntity, UUID> {

    List<FeatureRoleGrantEntity> findByConfigIdAndActiveTrue(UUID configId);

    void deleteByConfigId(UUID configId);
}
