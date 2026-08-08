package app.school.administration.modules.event.api.response;

import java.time.Instant;
import java.util.UUID;

public record EventDTO(
        UUID id,
        String scope,
        UUID institutionId,
        UUID schoolId,
        String title,
        String description,
        String location,
        Instant startsAt,
        Instant endsAt,
        boolean allDay,
        boolean active
) {
}
