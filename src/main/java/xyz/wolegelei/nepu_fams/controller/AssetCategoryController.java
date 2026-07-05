package xyz.wolegelei.nepu_fams.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import xyz.wolegelei.nepu_fams.annotation.OperationLog;
import xyz.wolegelei.nepu_fams.common.Result;
import xyz.wolegelei.nepu_fams.dto.asset.AssetCategoryDTO;
import xyz.wolegelei.nepu_fams.service.AssetCategoryService;
import xyz.wolegelei.nepu_fams.vo.asset.AssetCategoryVO;

import java.util.List;

@RestController
@RequestMapping("/api/category")
@RequiredArgsConstructor
public class AssetCategoryController {

    private final AssetCategoryService assetCategoryService;

    @GetMapping("/tree")
    public Result<List<AssetCategoryVO>> listTree() {
        List<AssetCategoryVO> tree = assetCategoryService.listTree();
        return Result.success(tree);
    }

    @GetMapping("/list")
    public Result<List<AssetCategoryVO>> listAll() {
        List<AssetCategoryVO> list = assetCategoryService.listAll();
        return Result.success(list);
    }

    @GetMapping("/{id}")
    public Result<AssetCategoryVO> getById(@PathVariable Long id) {
        AssetCategoryVO vo = assetCategoryService.getById(id);
        return Result.success(vo);
    }

    @OperationLog(value = "新增资产分类", type = "ADD")
    @PostMapping
    public Result<Void> add(@RequestBody AssetCategoryDTO dto) {
        assetCategoryService.add(dto);
        return Result.success();
    }

    @OperationLog(value = "编辑资产分类", type = "UPDATE")
    @PutMapping("/{id}")
    public Result<Void> update(@PathVariable Long id, @RequestBody AssetCategoryDTO dto) {
        assetCategoryService.update(id, dto);
        return Result.success();
    }

    @OperationLog(value = "删除资产分类", type = "DELETE")
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        assetCategoryService.delete(id);
        return Result.success();
    }
}
