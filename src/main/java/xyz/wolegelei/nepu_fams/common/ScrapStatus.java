package xyz.wolegelei.nepu_fams.common;

import lombok.Getter;

@Getter
public enum ScrapStatus {

    PENDING_COLLEGE("PENDING_COLLEGE", "待学院初审"),
    PENDING_SUPER("PENDING_SUPER", "待校级复审"),
    APPROVED("APPROVED", "已通过"),
    REJECTED("REJECTED", "已驳回"),
    /**
     * 兼容历史数据，旧 PENDING 统一展示为待学院初审。
     */
    PENDING("PENDING", "待学院初审");

    private final String code;
    private final String label;

    ScrapStatus(String code, String label) {
        this.code = code;
        this.label = label;
    }

    public static String getLabelByCode(String code) {
        if (code == null) {
            return "";
        }
        for (ScrapStatus status : values()) {
            if (status.getCode().equals(code)) {
                return status.getLabel();
            }
        }
        return code;
    }
}
