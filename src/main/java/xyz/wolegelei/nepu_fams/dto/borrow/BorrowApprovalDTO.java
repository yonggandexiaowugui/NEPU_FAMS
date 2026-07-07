package xyz.wolegelei.nepu_fams.dto.borrow;

import java.io.Serializable;
import lombok.Data;

@Data
public class BorrowApprovalDTO implements Serializable {

    private Long applicationId;

    private Boolean approved;

    private String opinion;

    private String remark;

    public String getApprovalOpinion() {
        return opinion != null ? opinion : remark;
    }
}