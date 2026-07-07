package xyz.wolegelei.nepu_fams.vo.inventory;

import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
public class InventoryRecordVO implements Serializable {

    private Long id;

    private Long taskId;

    private Long assetId;

    private String assetNo;

    private String assetName;

    private Integer actualQuantity;

    private Integer actualCount;

    private Boolean isChecked;

    private Integer bookCount;

    private String categoryName;

    private String collegeName;

    private String location;

    private String remark;

    private Long recorderId;

    private String recorderName;

    private LocalDateTime createTime;
}
