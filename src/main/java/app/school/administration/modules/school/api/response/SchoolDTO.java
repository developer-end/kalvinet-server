package app.school.administration.modules.school.api.response;

import java.time.Instant;
import java.util.UUID;

public record SchoolDTO(
        UUID id,
        UUID institutionId,
        String schoolName,
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
        String description,
        boolean active
) {
}
