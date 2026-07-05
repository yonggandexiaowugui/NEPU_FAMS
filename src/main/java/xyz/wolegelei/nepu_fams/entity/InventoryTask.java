package xyz.wolegelei.nepu_fams.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("inventory_task")
public class InventoryTask {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String name;

    private Long collegeId;

    private Long categoryId;

    private LocalDateTime startTime;

    private LocalDateTime endTime;

    private String status;

    private Long creatorId;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    @TableLogic
    @TableField(fill = FieldFill.INSERT)
    private Integer isDeleted;
}