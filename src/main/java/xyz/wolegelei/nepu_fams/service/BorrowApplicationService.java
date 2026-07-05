package xyz.wolegelei.nepu_fams.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import xyz.wolegelei.nepu_fams.dto.borrow.BorrowApplyDTO;
import xyz.wolegelei.nepu_fams.dto.borrow.BorrowApprovalDTO;
import xyz.wolegelei.nepu_fams.dto.borrow.BorrowQueryDTO;
import xyz.wolegelei.nepu_fams.vo.borrow.BorrowApplicationDetailVO;
import xyz.wolegelei.nepu_fams.vo.borrow.BorrowApplicationVO;

public interface BorrowApplicationService {

    void apply(BorrowApplyDTO dto);

    IPage<BorrowApplicationVO> myApplicationList(BorrowQueryDTO dto);

    IPage<BorrowApplicationVO> pendingApprovalList(BorrowQueryDTO dto);

    BorrowApplicationDetailVO getDetail(Long id);

    void collegeApprove(BorrowApprovalDTO dto);

    void superApprove(BorrowApprovalDTO dto);

    void confirmBorrow(Long id);

    void returnApply(Long id);

    void confirmReturn(Long id);

    void delete(Long id);
}
