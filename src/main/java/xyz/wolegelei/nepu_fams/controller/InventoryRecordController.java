package xyz.wolegelei.nepu_fams.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import xyz.wolegelei.nepu_fams.common.Result;
import xyz.wolegelei.nepu_fams.dto.inventory.InventoryRecordBatchDTO;
import xyz.wolegelei.nepu_fams.dto.inventory.InventoryRecordDTO;
import xyz.wolegelei.nepu_fams.service.InventoryRecordService;
import xyz.wolegelei.nepu_fams.vo.inventory.InventoryAnalysisVO;
import xyz.wolegelei.nepu_fams.vo.inventory.InventoryDiffVO;
import xyz.wolegelei.nepu_fams.vo.inventory.InventoryRecordVO;

import java.util.List;

@RestController
@RequestMapping("/api/inventory")
@RequiredArgsConstructor
public class InventoryRecordController {

    private final InventoryRecordService inventoryRecordService;

    @GetMapping("/record/page")
    public Result<List<InventoryRecordVO>> recordPage(@RequestParam Long taskId) {
        List<InventoryRecordVO> list = inventoryRecordService.listByTaskId(taskId);
        return Result.success(list);
    }

    @PostMapping("/record")
    public Result<Void> addRecord(@RequestBody InventoryRecordDTO dto) {
        inventoryRecordService.addRecord(dto);
        return Result.success();
    }

    @PostMapping("/record/batch")
    public Result<Void> batchAddRecords(@RequestBody InventoryRecordBatchDTO dto) {
        inventoryRecordService.batchAddRecords(dto);
        return Result.success();
    }

    @GetMapping("/diff")
    public Result<List<InventoryDiffVO>> diffList(@RequestParam Long taskId) {
        List<InventoryDiffVO> list = inventoryRecordService.getDiffList(taskId);
        return Result.success(list);
    }

    @GetMapping("/diff/analyze")
    public Result<InventoryAnalysisVO> analyzeDiff(@RequestParam Long taskId) {
        return Result.success(inventoryRecordService.analyzeDiff(taskId));
    }

    @GetMapping("/diff/ai-analysis")
    public Result<String> aiAnalysis(@RequestParam Long taskId) {
        return Result.success(inventoryRecordService.generateAiDiffAnalysis(taskId));
    }

    @PostMapping("/diff/confirm/{taskId}")
    public Result<Void> confirmDiff(@PathVariable Long taskId, @RequestBody List<Long> assetIds) {
        inventoryRecordService.confirmDiff(taskId, assetIds);
        return Result.success();
    }
}
