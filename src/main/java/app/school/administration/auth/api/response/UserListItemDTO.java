package app.school.administration.auth.api.response;

import java.util.List;
import java.util.UUID;

public record UserListItemDTO(
        UUID id,
        String firstName,
        String lastName,
        String email,
        String username,
        List<String> roleCodes,
        boolean active
) {
}
