package xyz.wolegelei.nepu_fams.common;

public enum ResultCode {

    SUCCESS(200, "操作成功"),
    FAILURE(500, "操作失败"),
    PARAM_ERROR(400, "参数错误"),
    UNAUTHORIZED(401, "未登录或token已过期"),
    FORBIDDEN(403, "没有权限"),
    NOT_FOUND(404, "请求的资源不存在"),
    SYSTEM_ERROR(500, "系统错误"),

    USER_NOT_FOUND(1001, "用户不存在"),
    USERNAME_OR_PASSWORD_ERROR(1002, "用户名或密码错误"),
    USER_ALREADY_EXISTS(1003, "用户已存在"),
    USER_DISABLED(1004, "用户已被禁用"),

    COLLEGE_NOT_FOUND(2001, "学院不存在"),
    COLLEGE_CODE_EXISTS(2002, "学院编码已存在"),

    ASSET_NOT_FOUND(3001, "资产不存在"),
    ASSET_NO_EXISTS(3002, "资产编号已存在"),
    ASSET_STATUS_ERROR(3003, "资产状态异常"),

    BORROW_NOT_FOUND(4001, "领用申请不存在"),
    BORROW_STATUS_ERROR(4002, "领用申请状态异常"),

    REPAIR_NOT_FOUND(5001, "维修工单不存在"),

    SCRAP_NOT_FOUND(6001, "报废申请不存在"),

    INVENTORY_NOT_FOUND(7001, "盘点任务不存在"),

    CATEGORY_NOT_FOUND(8001, "资产分类不存在"),
    CATEGORY_HAS_CHILDREN(8002, "存在子分类，无法删除");

    private final Integer code;
    private final String message;

    ResultCode(Integer code, String message) {
        this.code = code;
        this.message = message;
    }

    public Integer getCode() {
        return code;
    }

    public String getMessage() {
        return message;
    }
}
