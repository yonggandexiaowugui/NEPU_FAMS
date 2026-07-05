package xyz.wolegelei.nepu_fams.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@TableName("asset")
public class Asset {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String assetNo;

    private String name;

    private Long categoryId;

    private String specification;

    private LocalDate purchaseDate;

    private BigDecimal purchasePrice;

    private BigDecimal currentValue;

    private String status;

    private String location;

    private Long collegeId;

    private Long userId;

    private String responsiblePerson;

    private String remark;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    @TableLogic
    @TableField(fill = FieldFill.INSERT)
    private Integer isDeleted;
}