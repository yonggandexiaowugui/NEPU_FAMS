package xyz.wolegelei.nepu_fams.dto.inventory;

import java.io.Serializable;
import lombok.Data;

@Data
public class InventoryRecordDTO implements Serializable {

    private Long taskId;

    private Long assetId;

    private String assetNo;

    private String assetName;

    private Integer actualQuantity;

    /**
     * 兼容前端类表格字段命名。
     */
    private Integer actualCount;

    private Boolean isChecked;

    private String remark;
}
