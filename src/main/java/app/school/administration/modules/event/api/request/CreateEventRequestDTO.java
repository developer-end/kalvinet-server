package app.school.administration.modules.event.api.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.Instant;
import java.util.UUID;

public record CreateEventRequestDTO(
        @NotBlank String title,
        String description,
        String location,
        @NotNull Instant startsAt,
        Instant endsAt,
        boolean allDay,
        UUID schoolId
) {
}
