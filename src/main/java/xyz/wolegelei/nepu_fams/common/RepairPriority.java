package xyz.wolegelei.nepu_fams.common;

import lombok.Getter;

@Getter
public enum RepairPriority {

    LOW("LOW", "低"),
    NORMAL("NORMAL", "中"),
    HIGH("HIGH", "高"),
    URGENT("URGENT", "紧急");

    private final String code;
    private final String label;

    RepairPriority(String code, String label) {
        this.code = code;
        this.label = label;
    }

    public static String getLabelByCode(String code) {
        if (code == null) {
            return "";
        }
        for (RepairPriority priority : values()) {
            if (priority.getCode().equals(code)) {
                return priority.getLabel();
            }
        }
        return code;
    }
}
