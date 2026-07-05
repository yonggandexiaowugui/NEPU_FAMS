package xyz.wolegelei.nepu_fams.vo.scrap;

import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
public class ScrapplicationVO implements Serializable {

    private Long id;

    private Long assetId;

    private String assetNo;

    private String assetName;

    private Long collegeId;

    private String collegeName;

    private Long proposerId;

    private String proposerName;

    private String reason;

    private String status;

    private String statusName;

    private Long approverId;

    private String approverName;

    private String approvalOpinion;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;
}
