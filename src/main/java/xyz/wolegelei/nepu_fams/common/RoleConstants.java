package xyz.wolegelei.nepu_fams.common;

public class RoleConstants {

    public static final String SUPER_ADMIN = "SUPER_ADMIN";

    public static final String COLLEGE_ADMIN = "COLLEGE_ADMIN";

    public static final String USER = "USER";

    public static String getRoleName(String role) {
        if (SUPER_ADMIN.equals(role)) {
            return "系统管理员";
        } else if (COLLEGE_ADMIN.equals(role)) {
            return "学院管理员";
        } else if (USER.equals(role)) {
            return "普通用户";
        }
        return role;
    }
}
