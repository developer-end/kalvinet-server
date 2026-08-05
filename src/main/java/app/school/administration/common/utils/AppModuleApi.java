package app.school.administration.common.utils;

public final class AppModuleApi {

    public static final String AUTH = AppApiVersion.API_VERSION + "/auth";
    public static final String O_AUTH = AppApiVersion.API_VERSION + "/oAuth";
    public static final String SCHOOL = AppApiVersion.API_VERSION + "/school";
    public static final String TENANT = AppApiVersion.API_VERSION + "/tenant";
    public static final String USER = AppApiVersion.API_VERSION + "/user";
    /** Canonical role catalog path. */
    public static final String ROLE = AppApiVersion.API_VERSION + "/role";
    /** Temporary alias for the historical typo path `/rloe`. */
    public static final String ROLE_LEGACY = AppApiVersion.API_VERSION + "/rloe";

    private AppModuleApi() {
    }

}
