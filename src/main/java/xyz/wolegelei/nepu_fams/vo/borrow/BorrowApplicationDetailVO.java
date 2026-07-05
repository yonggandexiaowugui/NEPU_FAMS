package xyz.wolegelei.nepu_fams.vo.borrow;

import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;
import java.util.List;

@Data
@EqualsAndHashCode(callSuper = true)
public class BorrowApplicationDetailVO extends BorrowApplicationVO implements Serializable {

    private List<BorrowApprovalVO> approvalHistory;
}
