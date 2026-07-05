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

    private Integer bookQuantity;

    private Integer actualQuantity;

    private String diffType;

    private Integer diffQuantity;

    private String remark;
}
