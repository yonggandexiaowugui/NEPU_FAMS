package xyz.wolegelei.nepu_fams.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import xyz.wolegelei.nepu_fams.annotation.OperationLog;
import xyz.wolegelei.nepu_fams.common.PageResult;
import xyz.wolegelei.nepu_fams.common.Result;
import xyz.wolegelei.nepu_fams.dto.inventory.InventoryTaskCreateDTO;
import xyz.wolegelei.nepu_fams.dto.inventory.InventoryTaskQueryDTO;
import xyz.wolegelei.nepu_fams.dto.inventory.InventoryTaskUpdateDTO;
import xyz.wolegelei.nepu_fams.service.InventoryTaskService;
import xyz.wolegelei.nepu_fams.vo.inventory.InventoryTaskDetailVO;
import xyz.wolegelei.nepu_fams.vo.inventory.InventoryTaskVO;

@RestController
@RequestMapping("/api/inventory/task")
@RequiredArgsConstructor
public class InventoryTaskController {

    private final InventoryTaskService inventoryTaskService;

    @OperationLog(value = "创建盘点任务", type = "ADD")
    @PostMapping
    public Result<Void> create(@RequestBody InventoryTaskCreateDTO dto) {
        inventoryTaskService.create(dto);
        return Result.success();
    }

    @GetMapping("/page")
    public Result<PageResult<InventoryTaskVO>> page(InventoryTaskQueryDTO dto) {
        IPage<InventoryTaskVO> page = inventoryTaskService.page(dto);
        return Result.success(PageResult.of(page));
    }

    @GetMapping("/{id}")
    public Result<InventoryTaskDetailVO> getDetail(@PathVariable Long id) {
        InventoryTaskDetailVO detail = inventoryTaskService.getDetail(id);
        return Result.success(detail);
    }

    @PutMapping("/{id}")
    public Result<Void> update(@PathVariable Long id, @RequestBody InventoryTaskUpdateDTO dto) {
        inventoryTaskService.update(id, dto);
        return Result.success();
    }

    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        inventoryTaskService.delete(id);
        return Result.success();
    }

    @OperationLog(value = "完成盘点任务", type = "UPDATE")
    @PostMapping("/complete/{id}")
    public Result<Void> complete(@PathVariable Long id) {
        inventoryTaskService.complete(id);
        return Result.success();
    }

    @PostMapping("/start/{id}")
    public Result<Void> start(@PathVariable Long id) {
        // TODO: 待实现 - InventoryTaskService 暂未提供 start 方法
        return Result.success();
    }

    @PostMapping("/archive/{id}")
    public Result<Void> archive(@PathVariable Long id) {
        inventoryTaskService.archive(id);
        return Result.success();
    }
}
