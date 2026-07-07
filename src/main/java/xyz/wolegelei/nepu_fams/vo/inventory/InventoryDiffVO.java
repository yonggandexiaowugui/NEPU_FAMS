package xyz.wolegelei.nepu_fams.vo.inventory;

import lombok.Data;

import java.io.Serializable;

@Data
public class InventoryDiffVO implements Serializable {

    private Long assetId;

    private String assetNo;

    private String assetName;

    private String categoryName;

    private String location;

    private String collegeName;

    private Integer bookQuantity;

    private Integer bookCount;

    private Integer actualQuantity;

    private Integer actualCount;

    private String diffType;

    private Integer diffQuantity;

    private String riskLevel;

    private String analysis;

    private String suggestion;

    private String remark;
}
