package xyz.wolegelei.nepu_fams.vo.asset;

import com.alibaba.excel.annotation.ExcelProperty;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
public class AssetVO implements Serializable {

    @ExcelProperty("资产ID")
    private Long id;

    @ExcelProperty("资产编号")
    private String assetNo;

    @ExcelProperty("资产名称")
    private String name;

    @ExcelProperty("分类ID")
    private Long categoryId;

    @ExcelProperty("分类名称")
    private String categoryName;

    @ExcelProperty("规格型号")
    private String specification;

    @ExcelProperty("购置日期")
    private LocalDate purchaseDate;

    @ExcelProperty("购置价格")
    private BigDecimal purchasePrice;

    @ExcelProperty("当前价值")
    private BigDecimal currentValue;

    @ExcelProperty("状态")
    private String status;

    @ExcelProperty("状态名称")
    private String statusName;

    @ExcelProperty("存放位置")
    private String location;

    @ExcelProperty("学院ID")
    private Long collegeId;

    @ExcelProperty("学院名称")
    private String collegeName;

    @ExcelProperty("用户ID")
    private Long userId;

    @ExcelProperty("用户名")
    private String userName;

    @ExcelProperty("责任人")
    private String responsiblePerson;

    @ExcelProperty("备注")
    private String remark;

    @ExcelProperty("创建时间")
    private LocalDateTime createTime;
}
