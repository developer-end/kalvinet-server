package app.school.administration.modules.event.application.service;

import app.school.administration.auth.application.constant.OrgAccessPolicy;
import app.school.administration.common.application.custom.exception.NoDataFoundException;
import app.school.administration.modules.event.api.request.CreateEventRequestDTO;
import app.school.administration.modules.event.api.response.EventDTO;
import app.school.administration.modules.event.persistance.entity.CalendarEventEntity;
import app.school.administration.modules.event.persistance.repository.CalendarEventRepository;
import app.school.administration.modules.featureconfig.application.service.FeatureAccessService;
import app.school.administration.modules.institution.persistance.entity.InstitutionEntity;
import app.school.administration.modules.school.persistance.entity.SchoolEntity;
import app.school.administration.modules.school.persistance.repository.SchoolRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CalendarEventService {

    private final CalendarEventRepository calendarEventRepository;
    private final SchoolRepository schoolRepository;
    private final FeatureAccessService featureAccessService;

    @Transactional(readOnly = true)
    public Page<EventDTO> listInstitutionEvents(Pageable pageable) {
        featureAccessService.assertFeatureAccess(
                OrgAccessPolicy.FEATURE_INSTITUTION_EVENTS,
                FeatureAccessService.SCOPE_INSTITUTION,
                null
        );
        InstitutionEntity institution = featureAccessService.requireSingletonInstitution();
        return calendarEventRepository
                .findByScopeAndInstitutionIdAndActiveTrueOrderByStartsAtDesc(
                        FeatureAccessService.SCOPE_INSTITUTION, institution.getId(), pageable)
                .map(this::toDto);
    }

    @Transactional(readOnly = true)
    public Page<EventDTO> listSchoolEvents(UUID schoolId, Pageable pageable) {
        featureAccessService.assertFeatureAccess(
                OrgAccessPolicy.FEATURE_SCHOOL_EVENTS,
                FeatureAccessService.SCOPE_SCHOOL,
                schoolId
        );
        return calendarEventRepository
                .findByScopeAndSchoolIdAndActiveTrueOrderByStartsAtDesc(
                        FeatureAccessService.SCOPE_SCHOOL, schoolId, pageable)
                .map(this::toDto);
    }

    @Transactional
    public EventDTO createInstitutionEvent(CreateEventRequestDTO request) {
        featureAccessService.assertFeatureAccess(
                OrgAccessPolicy.FEATURE_INSTITUTION_EVENTS,
                FeatureAccessService.SCOPE_INSTITUTION,
                null
        );
        InstitutionEntity institution = featureAccessService.requireSingletonInstitution();
        CalendarEventEntity event = new CalendarEventEntity(
                FeatureAccessService.SCOPE_INSTITUTION,
                institution.getId(),
                null,
                request.title().trim()
        );
        applyDetails(event, request);
        return toDto(calendarEventRepository.save(event));
    }

    @Transactional
    public EventDTO createSchoolEvent(UUID schoolId, CreateEventRequestDTO request) {
        featureAccessService.assertFeatureAccess(
                OrgAccessPolicy.FEATURE_SCHOOL_EVENTS,
                FeatureAccessService.SCOPE_SCHOOL,
                schoolId
        );
        SchoolEntity school = schoolRepository.findById(schoolId)
                .orElseThrow(() -> new NoDataFoundException("School not found"));
        CalendarEventEntity event = new CalendarEventEntity(
                FeatureAccessService.SCOPE_SCHOOL,
                school.getInstitutionId(),
                school.getId(),
                request.title().trim()
        );
        applyDetails(event, request);
        return toDto(calendarEventRepository.save(event));
    }

    @Transactional
    public void deactivate(UUID eventId) {
        CalendarEventEntity event = calendarEventRepository.findById(eventId)
                .orElseThrow(() -> new NoDataFoundException("Event not found"));
        if (FeatureAccessService.SCOPE_SCHOOL.equals(event.getScope())) {
            featureAccessService.assertFeatureAccess(
                    OrgAccessPolicy.FEATURE_SCHOOL_EVENTS,
                    FeatureAccessService.SCOPE_SCHOOL,
                    event.getSchoolId()
            );
        } else {
            featureAccessService.assertFeatureAccess(
                    OrgAccessPolicy.FEATURE_INSTITUTION_EVENTS,
                    FeatureAccessService.SCOPE_INSTITUTION,
                    null
            );
        }
        event.setActive(false);
        calendarEventRepository.save(event);
    }

    private void applyDetails(CalendarEventEntity event, CreateEventRequestDTO request) {
        event.setDescription(blankToNull(request.description()));
        event.setLocation(blankToNull(request.location()));
        event.setStartsAt(request.startsAt());
        event.setEndsAt(request.endsAt());
        event.setAllDay(request.allDay());
    }

    private EventDTO toDto(CalendarEventEntity e) {
        return new EventDTO(
                e.getId(),
                e.getScope(),
                e.getInstitutionId(),
                e.getSchoolId(),
                e.getTitle(),
                e.getDescription(),
                e.getLocation(),
                e.getStartsAt(),
                e.getEndsAt(),
                e.isAllDay(),
                e.isActive()
        );
    }

    private static String blankToNull(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return value.trim();
    }
}
