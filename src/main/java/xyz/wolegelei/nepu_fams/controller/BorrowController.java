package xyz.wolegelei.nepu_fams.controller;

import cn.dev33.satoken.stp.StpUtil;
import com.baomidou.mybatisplus.core.metadata.IPage;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import xyz.wolegelei.nepu_fams.annotation.OperationLog;
import xyz.wolegelei.nepu_fams.common.PageResult;
import xyz.wolegelei.nepu_fams.common.Result;
import xyz.wolegelei.nepu_fams.common.RoleConstants;
import xyz.wolegelei.nepu_fams.dto.borrow.BorrowApplyDTO;
import xyz.wolegelei.nepu_fams.dto.borrow.BorrowApprovalDTO;
import xyz.wolegelei.nepu_fams.dto.borrow.BorrowQueryDTO;
import xyz.wolegelei.nepu_fams.entity.SysUser;
import xyz.wolegelei.nepu_fams.mapper.SysUserMapper;
import xyz.wolegelei.nepu_fams.service.BorrowApplicationService;
import xyz.wolegelei.nepu_fams.vo.borrow.BorrowApplicationDetailVO;
import xyz.wolegelei.nepu_fams.vo.borrow.BorrowApplicationVO;

@RestController
@RequestMapping("/api/borrow")
@RequiredArgsConstructor
public class BorrowController {

    private final BorrowApplicationService borrowApplicationService;

    private final SysUserMapper sysUserMapper;

    @OperationLog(value = "领用申请", type = "ADD")
    @PostMapping("/apply")
    public Result<Void> apply(@RequestBody BorrowApplyDTO dto) {
        borrowApplicationService.apply(dto);
        return Result.success();
    }

    @GetMapping("/my")
    public Result<PageResult<BorrowApplicationVO>> my(BorrowQueryDTO dto) {
        IPage<BorrowApplicationVO> page = borrowApplicationService.myApplicationList(dto);
        return Result.success(PageResult.of(page));
    }

    @GetMapping("/page")
    public Result<PageResult<BorrowApplicationVO>> page(BorrowQueryDTO dto) {
        IPage<BorrowApplicationVO> page = borrowApplicationService.pendingApprovalList(dto);
        return Result.success(PageResult.of(page));
    }

    @GetMapping("/{id}")
    public Result<BorrowApplicationDetailVO> getDetail(@PathVariable Long id) {
        BorrowApplicationDetailVO detail = borrowApplicationService.getDetail(id);
        return Result.success(detail);
    }

    @OperationLog(value = "审批领用", type = "APPROVE")
    @PostMapping("/approve/{id}")
    public Result<Void> approve(@PathVariable Long id, @RequestBody BorrowApprovalDTO dto) {
        dto.setApplicationId(id);
        SysUser currentUser = sysUserMapper.selectById(StpUtil.getLoginIdAsLong());
        if (currentUser != null && RoleConstants.SUPER_ADMIN.equals(currentUser.getRole())) {
            borrowApplicationService.superApprove(dto);
        } else {
            borrowApplicationService.collegeApprove(dto);
        }
        return Result.success();
    }

    @PostMapping("/confirm/{id}")
    public Result<Void> confirmBorrow(@PathVariable Long id) {
        borrowApplicationService.confirmBorrow(id);
        return Result.success();
    }

    @PostMapping("/return/{id}")
    public Result<Void> returnApply(@PathVariable Long id) {
        borrowApplicationService.returnApply(id);
        return Result.success();
    }

    @PostMapping("/return/confirm/{id}")
    public Result<Void> confirmReturn(@PathVariable Long id) {
        borrowApplicationService.confirmReturn(id);
        return Result.success();
    }

    @PostMapping("/cancel/{id}")
    public Result<Void> cancel(@PathVariable Long id) {
        borrowApplicationService.delete(id);
        return Result.success();
    }

    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        borrowApplicationService.delete(id);
        return Result.success();
    }
}
