package xyz.wolegelei.nepu_fams.common;

import lombok.Getter;

@Getter
public enum ScrapStatus {

    PENDING("PENDING", "待审核"),
    APPROVED("APPROVED", "已通过"),
    REJECTED("REJECTED", "已驳回");

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
