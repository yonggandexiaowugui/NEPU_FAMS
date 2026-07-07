package xyz.wolegelei.nepu_fams.vo.repair;

import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
public class RepairOrderVO implements Serializable {

    private Long id;

    private Long assetId;

    private String assetNo;

    private String assetName;

    private Long userId;

    private String userName;

    private String reporterName;

    private Long collegeId;

    private String collegeName;

    private String faultDescription;

    private String remark;

    private String attachmentUrls;

    private String priority;

    private String priorityName;

    private String status;

    private String statusName;

    private Long assigneeId;

    private String assigneeName;

    private String repairerName;

    private String repairResult;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;
}
