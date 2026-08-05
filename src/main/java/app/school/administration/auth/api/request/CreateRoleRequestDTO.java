package app.school.administration.auth.api.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CreateRoleRequestDTO(
        @NotBlank @Size(max = 64) String roleCode,
        @NotBlank @Size(max = 250) String roleName,
        @Size(max = 2000) String description
) {
}
