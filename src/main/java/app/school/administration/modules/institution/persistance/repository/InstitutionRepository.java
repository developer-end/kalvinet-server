package app.school.administration.modules.institution.persistance.repository;

import app.school.administration.common.domain.repository.AppBaseRepository;
import app.school.administration.modules.institution.persistance.entity.InstitutionEntity;

import java.util.Optional;
import java.util.UUID;

public interface InstitutionRepository extends AppBaseRepository<InstitutionEntity, UUID> {

    Optional<InstitutionEntity> findBySingletonKey(short singletonKey);
}
