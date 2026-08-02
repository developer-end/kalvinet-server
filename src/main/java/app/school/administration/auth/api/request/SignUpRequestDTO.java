package app.school.administration.auth.api.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record SignUpRequestDTO(
        @NotBlank(message = "First name is mandatory")
        @Size(max = 100)
        String firstName,

        @Size(max = 100)
        String lastName,

        @NotBlank(message = "Email address is mandatory")
        @Email(message = "Invalid email format")
        String email,

        @Size(max = 15)
        String mobileNo,

        @NotBlank(message = "Username is mandatory")
        @Size(min = 3, max = 50)
        String username,

        @NotBlank(message = "Password is mandatory")
        @Size(min = 6, message = "Password must be at least 6 characters")
        String password,

        String role
) {
}
