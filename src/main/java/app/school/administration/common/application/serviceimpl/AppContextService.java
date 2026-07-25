package app.school.administration.common.application.serviceimpl;

import app.school.administration.auth.application.context.SchoolContext;
import app.school.administration.auth.application.context.TenantContext;
import app.school.administration.auth.application.context.UserContext;
import app.school.administration.auth.domain.model.CustomUserDetails;
import org.springframework.stereotype.Service;

/**
 * ====================================================================================
 * SERVICE: AppContextService
 * ====================================================================================
 *
 * <h2>FUNCTIONALITY</h2>
 * Manages the lifecycle of request-scoped context holders ({@link UserContext}, {@link TenantContext},
 * {@link SchoolContext}).
 *
 * <h2>WHY IMPLEMENTED</h2>
 * Encapsulates thread context management into a single centralized Spring service. Called by `JWTAuthFilter`
 * during token validation to hydrate contexts and upon request completion to purge all `ThreadLocal` references.
 *
 * <h2>WHAT HAPPENS IF NOT IMPLEMENTED</h2>
 * - Context initialization and thread cleanup logic will be scattered across filters, controllers, and services.
 * - Incomplete thread cleanup will lead to **ThreadLocal memory leaks** and cross-tenant security vulnerabilities.
 * ====================================================================================
 */
@Service
public class AppContextService {

    /**
     * Initializes authentication and context state for the authenticated user thread.
     *
     * @param customUserDetails authenticated user security principal
     */
    public void createAuthContexts(CustomUserDetails customUserDetails) {
        UserContext.setUser(customUserDetails.toAuthUser().id());
    }

    /**
     * Purges all ThreadLocal context holders for the current executing request thread.
     */
    public void clearAuthContext() {
        SchoolContext.clear();
        UserContext.clear();
        TenantContext.clear();
    }

}
