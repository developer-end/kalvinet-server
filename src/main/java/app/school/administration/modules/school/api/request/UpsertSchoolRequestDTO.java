package app.school.administration.modules.school.api.request;

import jakarta.validation.constraints.NotBlank;

import java.time.Instant;

public record UpsertSchoolRequestDTO(
        @NotBlank String schoolName,
        String schoolCode,
        String email,
        String phone,
        String addressLine1,
        String addressLine2,
        String city,
        String state,
        String country,
        String postalCode,
        String principalName,
        String boardAffiliation,
        Instant startedDate,
        String timezone,
        String description
) {
}
