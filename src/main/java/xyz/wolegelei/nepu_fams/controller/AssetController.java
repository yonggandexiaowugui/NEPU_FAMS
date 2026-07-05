package xyz.wolegelei.nepu_fams.controller;

import com.alibaba.excel.EasyExcel;
import com.baomidou.mybatisplus.core.metadata.IPage;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import xyz.wolegelei.nepu_fams.annotation.OperationLog;
import xyz.wolegelei.nepu_fams.common.PageResult;
import xyz.wolegelei.nepu_fams.common.Result;
import xyz.wolegelei.nepu_fams.dto.asset.AssetAddDTO;
import xyz.wolegelei.nepu_fams.dto.asset.AssetQueryDTO;
import xyz.wolegelei.nepu_fams.dto.asset.AssetUpdateDTO;
import xyz.wolegelei.nepu_fams.service.AssetService;
import xyz.wolegelei.nepu_fams.vo.asset.AssetVO;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;

@RestController
@RequestMapping("/api/asset")
@RequiredArgsConstructor
public class AssetController {

    private final AssetService assetService;

    @GetMapping("/page")
    public Result<PageResult<AssetVO>> page(AssetQueryDTO dto) {
        IPage<AssetVO> page = assetService.page(dto);
        return Result.success(PageResult.of(page));
    }

    @GetMapping("/{id}")
    public Result<AssetVO> getById(@PathVariable Long id) {
        AssetVO vo = assetService.getById(id);
        return Result.success(vo);
    }

    @OperationLog(value = "新增资产", type = "ADD")
    @PostMapping
    public Result<Void> add(@RequestBody AssetAddDTO dto) {
        assetService.add(dto);
        return Result.success();
    }

    @OperationLog(value = "编辑资产", type = "UPDATE")
    @PutMapping("/{id}")
    public Result<Void> update(@PathVariable Long id, @RequestBody AssetUpdateDTO dto) {
        assetService.update(id, dto);
        return Result.success();
    }

    @OperationLog(value = "删除资产", type = "DELETE")
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        assetService.delete(id);
        return Result.success();
    }

    @OperationLog(value = "导出资产台账", type = "EXPORT")
    @GetMapping("/export")
    public void export(AssetQueryDTO dto, HttpServletResponse response) throws IOException {
        List<AssetVO> list = assetService.export(dto);

        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        response.setCharacterEncoding("utf-8");
        String fileName = URLEncoder.encode("固定资产台账", StandardCharsets.UTF_8).replaceAll("\\+", "%20");
        response.setHeader("Content-disposition", "attachment;filename*=utf-8''" + fileName + ".xlsx");

        EasyExcel.write(response.getOutputStream(), AssetVO.class)
                .sheet("资产列表")
                .doWrite(list);
    }

    @OperationLog(value = "导入资产", type = "IMPORT")
    @PostMapping("/import")
    public Result<Void> importAsset(@RequestParam("file") MultipartFile file) {
        // TODO: 待实现 - AssetService 暂未提供导入方法，需配合 EasyExcel 和导入 DTO 实现
        return Result.success();
    }
}
