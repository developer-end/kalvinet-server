package app.school.administration.auth.infrastructure.persistence.repository;

import app.school.administration.auth.infrastructure.persistence.entity.TenantEntity;
import app.school.administration.auth.infrastructure.persistence.entity.embeddable.TenantId;
import app.school.administration.common.domain.repository.AppBaseRepository;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface TenantRepository extends AppBaseRepository<TenantEntity, TenantId> {

    Optional<TenantEntity> findById(UUID id);

    List<TenantEntity> findByActiveAndOpenedDateGreaterThanEqualAndClosedDateLessThanEqual(
            boolean active, Instant openedDate, Instant closedDate);

    List<TenantEntity> findByActiveAndOpenedDateBetweenAndClosedDateBetween(
            boolean active, Instant openStart, Instant openEnd, Instant closeStart, Instant closeEnd);

}
