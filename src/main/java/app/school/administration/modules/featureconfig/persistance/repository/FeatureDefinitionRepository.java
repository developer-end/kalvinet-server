package app.school.administration.modules.featureconfig.persistance.repository;

import app.school.administration.modules.featureconfig.persistance.entity.FeatureDefinitionEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface FeatureDefinitionRepository extends JpaRepository<FeatureDefinitionEntity, String> {

    List<FeatureDefinitionEntity> findByActiveTrueOrderByFeatureCodeAsc();
}
