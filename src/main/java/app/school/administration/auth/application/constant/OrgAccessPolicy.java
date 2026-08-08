package app.school.administration.auth.application.constant;

import org.springframework.security.access.AccessDeniedException;

import java.util.Collection;
import java.util.Locale;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Baseline RBAC for Institution / School settings, events, and configuration.
 * When a feature_configuration row exists, {@code FeatureAccessService} further
 * restricts access to granted roles only.
 */
public final class OrgAccessPolicy {

    public static final String FEATURE_INSTITUTION_SETTINGS = "INSTITUTION_SETTINGS";
    public static final String FEATURE_INSTITUTION_EVENTS = "INSTITUTION_EVENTS";
    public static final String FEATURE_SCHOOLS_LIST = "SCHOOLS_LIST";
    public static final String FEATURE_SCHOOL_SETTINGS = "SCHOOL_SETTINGS";
    public static final String FEATURE_SCHOOL_EVENTS = "SCHOOL_EVENTS";
    public static final String FEATURE_CONFIGURATION = "FEATURE_CONFIGURATION";

    public static final Set<String> INSTITUTION_SETTINGS_ROLES = Set.of(
            RoleAssignmentPolicy.ROLE_OWNER,
            RoleAssignmentPolicy.ROLE_IT,
            RoleAssignmentPolicy.ROLE_MANAGER
    );

    public static final Set<String> INSTITUTION_EVENTS_ROLES = Set.of(
            RoleAssignmentPolicy.ROLE_OWNER,
            RoleAssignmentPolicy.ROLE_IT,
            RoleAssignmentPolicy.ROLE_MANAGER
    );

    public static final Set<String> SCHOOLS_LIST_ROLES = Set.of(
            RoleAssignmentPolicy.ROLE_OWNER,
            RoleAssignmentPolicy.ROLE_MANAGER,
            RoleAssignmentPolicy.ROLE_IT,
            RoleAssignmentPolicy.ROLE_MANAGEMENT,
            "ROLE_TEACHER"
    );

    /** Who may create a school under the singleton institution. */
    public static final Set<String> SCHOOL_CREATE_ROLES = Set.of(
            RoleAssignmentPolicy.ROLE_OWNER,
            RoleAssignmentPolicy.ROLE_IT,
            RoleAssignmentPolicy.ROLE_MANAGER
    );

    public static final Set<String> SCHOOL_SETTINGS_ROLES = Set.of(
            RoleAssignmentPolicy.ROLE_OWNER,
            RoleAssignmentPolicy.ROLE_IT,
            RoleAssignmentPolicy.ROLE_MANAGER,
            RoleAssignmentPolicy.ROLE_MANAGEMENT
    );

    public static final Set<String> SCHOOL_EVENTS_ROLES = Set.of(
            RoleAssignmentPolicy.ROLE_OWNER,
            RoleAssignmentPolicy.ROLE_IT,
            RoleAssignmentPolicy.ROLE_MANAGER,
            RoleAssignmentPolicy.ROLE_MANAGEMENT,
            "ROLE_TEACHER"
    );

    public static final Set<String> FEATURE_CONFIGURATION_ROLES = Set.of(
            RoleAssignmentPolicy.ROLE_OWNER,
            RoleAssignmentPolicy.ROLE_IT
    );

    private OrgAccessPolicy() {
    }

    public static Set<String> normalizeCallerRoles(Collection<String> callerRoleCodes) {
        return RoleAssignmentPolicy.normalizeAll(callerRoleCodes);
    }

    public static boolean allows(Collection<String> callerRoleCodes, Set<String> allowed) {
        Set<String> caller = normalizeCallerRoles(callerRoleCodes);
        for (String role : allowed) {
            String normalizedAllowed = RoleAssignmentPolicy.normalize(role);
            if (caller.contains(normalizedAllowed)) {
                return true;
            }
            // Also accept bare codes already normalized into caller set
            if (caller.contains(normalizedAllowed.toUpperCase(Locale.ROOT))) {
                return true;
            }
        }
        return false;
    }

    public static void assertAllows(Collection<String> callerRoleCodes, Set<String> allowed, String action) {
        if (!allows(callerRoleCodes, allowed)) {
            throw new AccessDeniedException("You are not permitted to " + action);
        }
    }

    public static Set<String> baselineRolesFor(String featureCode) {
        return switch (featureCode == null ? "" : featureCode.toUpperCase(Locale.ROOT)) {
            case FEATURE_INSTITUTION_SETTINGS -> INSTITUTION_SETTINGS_ROLES;
            case FEATURE_INSTITUTION_EVENTS -> INSTITUTION_EVENTS_ROLES;
            case FEATURE_SCHOOLS_LIST -> SCHOOLS_LIST_ROLES;
            case FEATURE_SCHOOL_SETTINGS -> SCHOOL_SETTINGS_ROLES;
            case FEATURE_SCHOOL_EVENTS -> SCHOOL_EVENTS_ROLES;
            case FEATURE_CONFIGURATION -> FEATURE_CONFIGURATION_ROLES;
            default -> Set.of();
        };
    }
}
