package app.school.administration.auth.infrastructure.persistence.projection;

import app.school.administration.common.infrastucture.persistence.projection.AuditableProjectionDTO;

import java.util.Set;

public interface UserProjectionDTO extends AuditableProjectionDTO {

    String getFirstName();

    String getLastName();

    String getEmail();

    String getUsername();

    String getMobileNo();

    Set<UserRoleProjectionDTO> getRoles();

    Set<OAuthAccountProjectionDTO> getOAuthAccounts();

    interface OAuthAccountProjectionDTO extends AuditableProjectionDTO {
        String getProvider();

        String getProviderUserId();
    }

}
