package app.school.administration.auth.application.context;

import java.util.UUID;

/**
 * ====================================================================================
 * CONTEXT HOLDER: UserContext
 * ====================================================================================
 *
 * <h2>FUNCTIONALITY</h2>
 * Holds the authenticated user's unique identifier (`user_id` UUID) for the executing thread using {@link ThreadLocal}.
 *
 * <h2>WHY IMPLEMENTED</h2>
 * Allows any domain service, audit listener, or repository layer to access the current authenticated user's ID
 * without dependency on web controller request scopes or redundant security context parsing.
 *
 * <h2>WHAT HAPPENS IF NOT IMPLEMENTED</h2>
 * - Automatic entity auditing (e.g., `created_by` / `updated_by` tracking) will be unable to identify the active user.
 * - Business logic requiring user ownership checks will fail or require manual parameter passing.
 * ====================================================================================
 */
public final class UserContext {

    private static final ThreadLocal<UUID> USER_ID = new ThreadLocal<>();

    private UserContext() {
    }

    /**
     * Gets the authenticated user's UUID bound to the current thread.
     *
     * @return current user UUID or null
     */
    public static UUID getUser() {
        return USER_ID.get();
    }

    /**
     * Binds an authenticated user's UUID to the current executing thread.
     *
     * @param userId authenticated user UUID
     */
    public static void setUser(UUID userId) {
        USER_ID.set(userId);
    }

    /**
     * Clears the user UUID from the current thread.
     */
    public static void clear() {
        USER_ID.remove();
    }

}

