package app.school.administration.auth.api.request;

import jakarta.validation.constraints.NotBlank;

public record AssignRoleRequestDTO(
        @NotBlank String roleCode
) {
}
