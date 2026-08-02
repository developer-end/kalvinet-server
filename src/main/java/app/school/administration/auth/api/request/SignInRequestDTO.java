package app.school.administration.auth.api.request;

import jakarta.validation.constraints.NotBlank;

public record SignInRequestDTO(
        @NotBlank String username,
        @NotBlank String password,
        String role
) {
}
