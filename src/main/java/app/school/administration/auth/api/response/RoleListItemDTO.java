package app.school.administration.auth.api.response;

import java.util.UUID;

public record RoleListItemDTO(
        UUID id,
        String roleCode,
        String roleName,
        String description,
        boolean active
) {
}
