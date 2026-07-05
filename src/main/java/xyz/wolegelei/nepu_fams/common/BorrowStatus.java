package xyz.wolegelei.nepu_fams.common;

import lombok.Getter;

@Getter
public enum BorrowStatus {

    PENDING_COLLEGE("PENDING_COLLEGE", "待学院审核"),
    PENDING_SUPER("PENDING_SUPER", "待校级审核"),
    APPROVED("APPROVED", "审核通过/待领用"),
    REJECTED("REJECTED", "已驳回"),
    BORROWED("BORROWED", "已领用"),
    RETURNING("RETURNING", "待归还确认"),
    RETURNED("RETURNED", "已归还");

    private final String code;
    private final String label;

    BorrowStatus(String code, String label) {
        this.code = code;
        this.label = label;
    }

    public static String getLabelByCode(String code) {
        if (code == null) {
            return "";
        }
        for (BorrowStatus status : values()) {
            if (status.getCode().equals(code)) {
                return status.getLabel();
            }
        }
        return code;
    }
}
