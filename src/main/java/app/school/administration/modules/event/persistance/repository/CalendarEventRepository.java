package app.school.administration.modules.event.persistance.repository;

import app.school.administration.common.domain.repository.AppBaseRepository;
import app.school.administration.modules.event.persistance.entity.CalendarEventEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.UUID;

public interface CalendarEventRepository extends AppBaseRepository<CalendarEventEntity, UUID> {

    Page<CalendarEventEntity> findByScopeAndInstitutionIdAndActiveTrueOrderByStartsAtDesc(
            String scope, UUID institutionId, Pageable pageable);

    Page<CalendarEventEntity> findByScopeAndSchoolIdAndActiveTrueOrderByStartsAtDesc(
            String scope, UUID schoolId, Pageable pageable);
}
