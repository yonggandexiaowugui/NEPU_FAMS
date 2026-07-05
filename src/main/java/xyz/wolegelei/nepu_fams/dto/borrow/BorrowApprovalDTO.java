package xyz.wolegelei.nepu_fams.dto.borrow;

import java.io.Serializable;
import lombok.Data;

@Data
public class BorrowApprovalDTO implements Serializable {

    private Long applicationId;

    private String opinion;
}