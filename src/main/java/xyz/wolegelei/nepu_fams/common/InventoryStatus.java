package xyz.wolegelei.nepu_fams.common;

import lombok.Getter;

@Getter
public enum InventoryStatus {

    PENDING("PENDING", "待开始"),
    IN_PROGRESS("IN_PROGRESS", "进行中"),
    COMPLETED("COMPLETED", "已完成"),
    ARCHIVED("ARCHIVED", "已归档");

    private final String code;
    private final String label;

    InventoryStatus(String code, String label) {
        this.code = code;
        this.label = label;
    }

    public static String getLabelByCode(String code) {
        if (code == null) {
            return "";
        }
        for (InventoryStatus status : values()) {
            if (status.getCode().equals(code)) {
                return status.getLabel();
            }
        }
        return code;
    }
}
