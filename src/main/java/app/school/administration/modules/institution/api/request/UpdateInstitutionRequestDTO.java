package app.school.administration.modules.institution.api.request;

import jakarta.validation.constraints.NotBlank;

public record UpdateInstitutionRequestDTO(
        @NotBlank String institutionName,
        String legalName,
        String registrationNumber,
        String email,
        String phone,
        String addressLine1,
        String addressLine2,
        String city,
        String state,
        String country,
        String postalCode,
        String website,
        String logoUrl,
        String description
) {
}
