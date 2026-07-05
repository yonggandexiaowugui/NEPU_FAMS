package xyz.wolegelei.nepu_fams.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import xyz.wolegelei.nepu_fams.common.PageResult;
import xyz.wolegelei.nepu_fams.common.Result;
import xyz.wolegelei.nepu_fams.dto.repair.RepairAssignDTO;
import xyz.wolegelei.nepu_fams.dto.repair.RepairProgressDTO;
import xyz.wolegelei.nepu_fams.dto.repair.RepairQueryDTO;
import xyz.wolegelei.nepu_fams.dto.repair.RepairSubmitDTO;
import xyz.wolegelei.nepu_fams.service.RepairOrderService;
import xyz.wolegelei.nepu_fams.vo.repair.RepairOrderDetailVO;
import xyz.wolegelei.nepu_fams.vo.repair.RepairOrderVO;

@RestController
@RequestMapping("/api/repair")
@RequiredArgsConstructor
public class RepairController {

    private final RepairOrderService repairOrderService;

    @PostMapping("/submit")
    public Result<Void> submit(@RequestBody RepairSubmitDTO dto) {
        repairOrderService.submit(dto);
        return Result.success();
    }

    @GetMapping("/page")
    public Result<PageResult<RepairOrderVO>> page(RepairQueryDTO dto) {
        IPage<RepairOrderVO> page = repairOrderService.page(dto);
        return Result.success(PageResult.of(page));
    }

    @GetMapping("/{id}")
    public Result<RepairOrderDetailVO> getById(@PathVariable Long id) {
        RepairOrderDetailVO detail = repairOrderService.getById(id);
        return Result.success(detail);
    }

    @PostMapping("/assign/{id}")
    public Result<Void> assign(@PathVariable Long id, @RequestBody RepairAssignDTO dto) {
        dto.setOrderId(id);
        repairOrderService.assign(dto);
        return Result.success();
    }

    @PostMapping("/progress/{id}")
    public Result<Void> updateProgress(@PathVariable Long id, @RequestBody RepairProgressDTO dto) {
        dto.setOrderId(id);
        repairOrderService.updateProgress(dto);
        return Result.success();
    }

    @GetMapping("/my")
    public Result<PageResult<RepairOrderVO>> my(RepairQueryDTO dto) {
        IPage<RepairOrderVO> page = repairOrderService.myAssignedList(dto);
        return Result.success(PageResult.of(page));
    }

    @PostMapping("/complete/{id}")
    public Result<Void> complete(@PathVariable Long id) {
        return Result.success();
    }

    @PostMapping("/cancel/{id}")
    public Result<Void> cancel(@PathVariable Long id) {
        return Result.success();
    }

    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        repairOrderService.delete(id);
        return Result.success();
    }
}
