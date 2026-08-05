package app.school.administration.common.utils;

public final class AppCommonEndPoint {

    public static final String FIND_BY_ID = "/findById/{uuid}";
    public static final String CREATE = "/create";
    public static final String UPDATE = "/update";
    public static final String DE_ACTIVATE = "/deActivate/{uuid}";
    public static final String ACTIVATE = "/activate/{uuid}";
    public static final String LIST = "/list";

    public static final String USER_ROLE_MAPPING_DE_ACTIVATE = "/userRoleMapping/deActivate/{uuid}";
    public static final String USER_ROLE_MAPPING_ACTIVATE = "/userRoleMapping/activate/{uuid}";

    public static final String ASSIGNABLE_ROLES = "/assignableRoles";
    public static final String ASSIGN_ROLE = "/assignRole/{uuid}";
    public static final String SEARCH = "/search";

    private AppCommonEndPoint() {
    }

}
