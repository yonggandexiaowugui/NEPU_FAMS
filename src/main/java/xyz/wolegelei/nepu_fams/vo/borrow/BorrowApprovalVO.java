package xyz.wolegelei.nepu_fams.vo.borrow;

import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
public class BorrowApprovalVO implements Serializable {

    private Long id;

    private Long applicationId;

    private Long approverId;

    private String approverName;

    private String approvalStatus;

    private String statusName;

    private String opinion;

    private LocalDateTime createTime;
}
