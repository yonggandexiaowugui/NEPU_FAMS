package xyz.wolegelei.nepu_fams.common;

public enum OperationType {

    LOGIN("登录"),
    LOGOUT("登出"),
    ADD("新增"),
    UPDATE("修改"),
    DELETE("删除"),
    QUERY("查询"),
    APPROVE("审批"),
    EXPORT("导出"),
    OTHER("其他");

    private final String description;

    OperationType(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }

    public static String getDescriptionByCode(String code) {
        if (code == null) {
            return OTHER.getDescription();
        }
        for (OperationType type : values()) {
            if (type.name().equals(code)) {
                return type.getDescription();
            }
        }
        return OTHER.getDescription();
    }
}
