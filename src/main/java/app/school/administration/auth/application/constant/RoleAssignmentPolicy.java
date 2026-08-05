package app.school.administration.auth.application.constant;

import org.springframework.security.access.AccessDeniedException;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Locale;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Who may assign / register roles.
 * Catalog roles come from {@code role_table}; only baseline roles are seeded at install.
 * Institutional roles are registered by IT at runtime.
 */
public final class RoleAssignmentPolicy {

    public static final String ROLE_USER = "ROLE_USER";
    public static final String ROLE_MANAGER = "ROLE_MANAGER";
    public static final String ROLE_MANAGEMENT = "ROLE_MANAGEMENT";
    public static final String ROLE_OWNER = "ROLE_OWNER";
    public static final String ROLE_IT = "ROLE_IT";

    /** Seeded at install — never creatable via Role Registry API. */
    public static final Set<String> BASELINE_ROLES = Set.of(
            ROLE_USER, ROLE_OWNER, ROLE_MANAGER, ROLE_MANAGEMENT, ROLE_IT
    );

    private RoleAssignmentPolicy() {
    }

    public static String normalize(String roleCode) {
        if (roleCode == null || roleCode.isBlank()) {
            throw new IllegalArgumentException("Role code is required");
        }
        String trimmed = roleCode.trim().toUpperCase(Locale.ROOT);
        if (!trimmed.startsWith("ROLE_")) {
            trimmed = "ROLE_" + trimmed;
        }
        return trimmed;
    }

    public static Set<String> normalizeAll(Collection<String> roleCodes) {
        if (roleCodes == null || roleCodes.isEmpty()) {
            return Set.of();
        }
        return roleCodes.stream()
                .filter(r -> r != null && !r.isBlank())
                .map(RoleAssignmentPolicy::normalize)
                .collect(Collectors.toCollection(LinkedHashSet::new));
    }

    public static boolean isIt(Collection<String> callerRoleCodes) {
        return normalizeAll(callerRoleCodes).contains(ROLE_IT);
    }

    /** Callers who may use assign / search-user APIs. */
    public static boolean canAssignRoles(Collection<String> callerRoleCodes) {
        Set<String> caller = normalizeAll(callerRoleCodes);
        return caller.contains(ROLE_IT)
                || caller.contains(ROLE_MANAGER)
                || caller.contains(ROLE_MANAGEMENT)
                || caller.contains("ROLE_TEACHER");
    }

    public static void assertCanRegisterRoles(Collection<String> callerRoleCodes) {
        if (!isIt(callerRoleCodes)) {
            throw new AccessDeniedException("Only ROLE_IT may register new roles");
        }
    }

    /**
     * Filter catalog role codes to those the caller may assign.
     * Never includes ROLE_IT.
     */
    public static Set<String> assignableRolesFor(
            Collection<String> callerRoleCodes,
            Collection<String> catalogRoleCodes) {
        Set<String> caller = normalizeAll(callerRoleCodes);
        Set<String> catalog = normalizeAll(catalogRoleCodes);
        catalog.remove(ROLE_IT);

        if (caller.contains(ROLE_IT)) {
            return Collections.unmodifiableSet(catalog);
        }

        if (caller.contains(ROLE_MANAGER)) {
            Set<String> denied = Set.of(ROLE_IT, ROLE_OWNER, ROLE_MANAGER, ROLE_USER);
            return catalog.stream()
                    .filter(code -> !denied.contains(code))
                    .collect(Collectors.toCollection(LinkedHashSet::new));
        }

        // MANAGEMENT / TEACHER: institutional (non-baseline) roles only
        if (caller.contains(ROLE_MANAGEMENT) || caller.contains("ROLE_TEACHER")) {
            return catalog.stream()
                    .filter(code -> !BASELINE_ROLES.contains(code))
                    .collect(Collectors.toCollection(LinkedHashSet::new));
        }

        return Set.of();
    }

    /** @deprecated use {@link #assignableRolesFor(Collection, Collection)} with catalog */
    public static Set<String> assignableRolesFor(Collection<String> callerRoleCodes) {
        return assignableRolesFor(callerRoleCodes, BASELINE_ROLES);
    }

    public static void assertCanAssign(
            Collection<String> callerRoleCodes,
            String targetRoleCode,
            Collection<String> catalogRoleCodes) {
        String target = normalize(targetRoleCode);
        if (ROLE_IT.equals(target)) {
            throw new AccessDeniedException("ROLE_IT cannot be assigned through the application");
        }
        Set<String> allowed = assignableRolesFor(callerRoleCodes, catalogRoleCodes);
        if (!allowed.contains(target)) {
            throw new AccessDeniedException("You are not permitted to assign " + target);
        }
    }

    public static void assertCanAssign(Collection<String> callerRoleCodes, String targetRoleCode) {
        assertCanAssign(callerRoleCodes, targetRoleCode, BASELINE_ROLES);
    }
}
