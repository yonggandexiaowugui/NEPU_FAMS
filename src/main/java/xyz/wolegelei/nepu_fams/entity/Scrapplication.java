package xyz.wolegelei.nepu_fams.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("scrapplication")
public class Scrapplication {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long assetId;

    private Long collegeId;

    private Long proposerId;

    private String reason;

    private String attachmentUrls;

    private String status;

    private Long approverId;

    private String approvalOpinion;

    private Long collegeApproverId;

    private String collegeApprovalOpinion;

    private Long superApproverId;

    private String superApprovalOpinion;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    @TableLogic
    @TableField(fill = FieldFill.INSERT)
    private Integer isDeleted;
}
