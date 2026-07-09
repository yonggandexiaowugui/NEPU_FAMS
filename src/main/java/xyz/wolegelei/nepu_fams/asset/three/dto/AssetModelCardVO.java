package xyz.wolegelei.nepu_fams.asset.three.dto;

import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * 首页 3D 资产展厅卡片专用 DTO。
 * 仅包含商品展示场景所需字段，避开 AssetVO 的 ExcelProperty 与冗余字段。
 */
@Data
public class AssetModelCardVO implements Serializable {

    private Long id;

    private String assetNo;

    private String name;

    private String categoryName;

    private String collegeName;

    private String status;

    private String statusName;

    private String location;

    private BigDecimal purchasePrice;

    private String modelUrl;
}
