package app.school.administration.modules.school.persistance.repository;

import app.school.administration.common.domain.repository.AppBaseRepository;
import app.school.administration.modules.school.persistance.entity.SchoolEntity;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface SchoolRepository extends AppBaseRepository<SchoolEntity, UUID> {

}
