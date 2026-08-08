package app.school.administration.modules.school.api.response;

import java.util.UUID;

public record SchoolListItemDTO(
        UUID id,
        UUID institutionId,
        String schoolName,
        String schoolCode,
        String email,
        String phone,
        String city,
        String principalName,
        boolean active
) {
}
