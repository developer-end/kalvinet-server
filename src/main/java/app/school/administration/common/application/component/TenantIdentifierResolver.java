package app.school.administration.common.application.component;

import app.school.administration.auth.application.context.TenantContext;
import org.hibernate.context.spi.CurrentTenantIdentifierResolver;
import org.springframework.stereotype.Component;

import java.util.Objects;

/**
 * ====================================================================================
 * COMPONENT: TenantIdentifierResolver
 * ====================================================================================
 *
 * <h2>FUNCTIONALITY</h2>
 * Implements Hibernate's {@link CurrentTenantIdentifierResolver} interface to determine the target
 * PostgreSQL database schema for every JPA query execution.
 *
 * <h2>WHY IMPLEMENTED</h2>
 * In a schema-based multi-tenant architecture, Hibernate requires a resolver component to identify
 * which schema should receive the incoming query. This class inspects {@link TenantContext#getTenant()}
 * (populated during request authentication by `JWTAuthFilter`). If no tenant is explicitly bound to the current thread
 * (e.g. unauthenticated public auth routes or startup tasks), it safely falls back to `"master, public"`.
 *
 * <h2>WHAT HAPPENS IF NOT IMPLEMENTED</h2>
 * - Hibernate will throw a `TenantIdentifierMismatchException` or fail to execute queries because no active schema identifier can be resolved.
 * - Multi-tenant isolation will break, risking queries leaking across tenant schemas or falling into an undefined database state.
 * ====================================================================================
 */
@Component
public class TenantIdentifierResolver implements CurrentTenantIdentifierResolver {

    /**
     * Default schema fallback when no tenant context is bound to the executing thread.
     */
    private static final String DEFAULT_SCHEMA = "master, public";

    /**
     * Resolves the current tenant's database schema identifier.
     *
     * @return current tenant schema name if present in {@link TenantContext}; otherwise {@link #DEFAULT_SCHEMA}.
     */
    @Override
    public Object resolveCurrentTenantIdentifier() {
        String tenant = TenantContext.getTenant();
        return Objects.nonNull(tenant) ? tenant : DEFAULT_SCHEMA;
    }

    /**
     * Controls whether existing sessions should be validated against the current tenant identifier.
     *
     * @return true to validate existing current sessions.
     */
    @Override
    public boolean validateExistingCurrentSessions() {
        return true;
    }

}

