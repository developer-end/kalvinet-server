package app.school.administration.modules.event.api.controller;

import app.school.administration.common.utils.AppCommonEndPoint;
import app.school.administration.common.utils.AppModuleApi;
import app.school.administration.modules.event.api.request.CreateEventRequestDTO;
import app.school.administration.modules.event.api.response.EventDTO;
import app.school.administration.modules.event.application.service.CalendarEventService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping(AppModuleApi.EVENT)
public class CalendarEventController {

    private final CalendarEventService calendarEventService;

    @GetMapping("/institution")
    public ResponseEntity<Page<EventDTO>> institutionEvents(@PageableDefault(size = 20) Pageable pageable) {
        return ResponseEntity.ok(calendarEventService.listInstitutionEvents(pageable));
    }

    @PostMapping("/institution")
    public ResponseEntity<EventDTO> createInstitutionEvent(@Validated @RequestBody CreateEventRequestDTO request) {
        return ResponseEntity.ok(calendarEventService.createInstitutionEvent(request));
    }

    @GetMapping("/school/{schoolId}")
    public ResponseEntity<Page<EventDTO>> schoolEvents(
            @PathVariable UUID schoolId,
            @PageableDefault(size = 20) Pageable pageable) {
        return ResponseEntity.ok(calendarEventService.listSchoolEvents(schoolId, pageable));
    }

    @PostMapping("/school/{schoolId}")
    public ResponseEntity<EventDTO> createSchoolEvent(
            @PathVariable UUID schoolId,
            @Validated @RequestBody CreateEventRequestDTO request) {
        return ResponseEntity.ok(calendarEventService.createSchoolEvent(schoolId, request));
    }

    @PutMapping(AppCommonEndPoint.DE_ACTIVATE)
    public ResponseEntity<Void> deActivate(@PathVariable(name = "uuid") UUID uuid) {
        calendarEventService.deactivate(uuid);
        return ResponseEntity.ok().build();
    }
}
