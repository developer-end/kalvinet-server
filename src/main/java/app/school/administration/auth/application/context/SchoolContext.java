package app.school.administration.auth.application.context;

import app.school.administration.modules.school.persistance.projection.SchoolProjectionDTO;

/**
 * ====================================================================================
 * CONTEXT HOLDER: SchoolContext
 * ====================================================================================
 *
 * <h2>FUNCTIONALITY</h2>
 * Holds the active school projection details ({@link SchoolProjectionDTO}) bound to the current executing request thread.
 *
 * <h2>WHY IMPLEMENTED</h2>
 * Allows downstream services to access school metadata (e.g. school ID, name) without hitting the database repeatedly during request processing.
 *
 * <h2>WHAT HAPPENS IF NOT IMPLEMENTED</h2>
 * Services requiring school metadata must execute duplicate SQL queries for every request step.
 * ====================================================================================
 */
public final class SchoolContext {

    private static final ThreadLocal<SchoolProjectionDTO> CURRENT_SCHOOL = new ThreadLocal<>();

    private SchoolContext() {
    }

    /**
     * Gets current thread's bound school projection.
     *
     * @return active school projection DTO or null
     */
    public static SchoolProjectionDTO getSchool() {
        return CURRENT_SCHOOL.get();
    }

    /**
     * Binds a school projection DTO to the current thread.
     *
     * @param school active school projection
     */
    public static void setSchool(SchoolProjectionDTO school) {
        CURRENT_SCHOOL.set(school);
    }

    /**
     * Clears school projection context from thread.
     */
    public static void clear() {
        CURRENT_SCHOOL.remove();
    }

}

