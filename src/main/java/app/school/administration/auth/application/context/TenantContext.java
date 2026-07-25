package app.school.administration.auth.application.context;

/**
 * ====================================================================================
 * CONTEXT HOLDER: TenantContext
 * ====================================================================================
 *
 * <h2>FUNCTIONALITY</h2>
 * Holds the active database tenant schema name for the currently executing HTTP request thread using {@link ThreadLocal}.
 *
 * <h2>WHY IMPLEMENTED</h2>
 * Multi-tenant routing requires knowing which PostgreSQL database schema to target for each incoming user request.
 * `TenantContext` allows `JWTAuthFilter` to store the active tenant schema name at the start of the request,
 * making it accessible to `TenantIdentifierResolver` and Spring Data JPA repositories without passing `tenantId`
 * parameters through every service method.
 *
 * <h2>WHAT HAPPENS IF NOT IMPLEMENTED</h2>
 * - Services and repositories cannot determine the target tenant schema for the current request.
 * - Without `clear()`, thread reuse in Tomcat thread pools will cause severe **cross-tenant data leaks**,
 *   serving one tenant's data to another tenant's authenticated session.
 * ====================================================================================
 */
public final class TenantContext {

    private static final ThreadLocal<String> TENANT_NAME = new ThreadLocal<>();

    private TenantContext() {
    }

    /**
     * Gets the tenant schema name bound to the current thread.
     *
     * @return current tenant schema name or null
     */
    public static String getTenant() {
        return TENANT_NAME.get();
    }

    /**
     * Binds a tenant schema name to the current executing thread.
     *
     * @param tenant schema identifier name
     */
    public static void setTenant(String tenant) {
        TENANT_NAME.set(tenant);
    }

    /**
     * Clears the tenant schema name from the current thread to prevent ThreadLocal memory leaks.
     */
    public static void clear() {
        TENANT_NAME.remove();
    }

}

