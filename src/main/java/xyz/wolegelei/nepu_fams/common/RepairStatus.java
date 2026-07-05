package xyz.wolegelei.nepu_fams.common;

import lombok.Getter;

@Getter
public enum RepairStatus {

    PENDING("PENDING", "待处理"),
    IN_PROGRESS("IN_PROGRESS", "维修中"),
    COMPLETED("COMPLETED", "已完成"),
    SCRAPPED("SCRAPPED", "需报废");

    private final String code;
    private final String label;

    RepairStatus(String code, String label) {
        this.code = code;
        this.label = label;
    }

    public static String getLabelByCode(String code) {
        if (code == null) {
            return "";
        }
        for (RepairStatus status : values()) {
            if (status.getCode().equals(code)) {
                return status.getLabel();
            }
        }
        return code;
    }
}
