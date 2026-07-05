package xyz.wolegelei.nepu_fams.common;

import lombok.Getter;

@Getter
public enum AssetStatus {

    IDLE("IDLE", "闲置"),
    IN_USE("IN_USE", "在用"),
    REPAIRING("REPAIRING", "维修中"),
    SCRAPPED("SCRAPPED", "已报废"),
    LOSS("LOSS", "盘亏");

    private final String code;
    private final String label;

    AssetStatus(String code, String label) {
        this.code = code;
        this.label = label;
    }

    public static String getLabelByCode(String code) {
        if (code == null) {
            return "";
        }
        for (AssetStatus status : values()) {
            if (status.getCode().equals(code)) {
                return status.getLabel();
            }
        }
        return code;
    }
}
