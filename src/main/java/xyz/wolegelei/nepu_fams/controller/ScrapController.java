package xyz.wolegelei.nepu_fams.controller;

import com.alibaba.excel.EasyExcel;
import com.baomidou.mybatisplus.core.metadata.IPage;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import xyz.wolegelei.nepu_fams.common.PageResult;
import xyz.wolegelei.nepu_fams.common.Result;
import xyz.wolegelei.nepu_fams.dto.scrap.ScrapApplyDTO;
import xyz.wolegelei.nepu_fams.dto.scrap.ScrapApprovalDTO;
import xyz.wolegelei.nepu_fams.dto.scrap.ScrapQueryDTO;
import xyz.wolegelei.nepu_fams.service.ScrapplicationService;
import xyz.wolegelei.nepu_fams.vo.scrap.ScrapplicationVO;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;

@RestController
@RequestMapping("/api/scrap")
@RequiredArgsConstructor
public class ScrapController {

    private final ScrapplicationService scrapplicationService;

    @PostMapping("/apply")
    public Result<Void> apply(@RequestBody ScrapApplyDTO dto) {
        scrapplicationService.apply(dto);
        return Result.success();
    }

    @GetMapping("/page")
    public Result<PageResult<ScrapplicationVO>> page(ScrapQueryDTO dto) {
        IPage<ScrapplicationVO> page = scrapplicationService.page(dto);
        return Result.success(PageResult.of(page));
    }

    @GetMapping("/{id}")
    public Result<ScrapplicationVO> getById(@PathVariable Long id) {
        ScrapplicationVO vo = scrapplicationService.getById(id);
        return Result.success(vo);
    }

    @PostMapping("/approve/{id}")
    public Result<Void> approveById(@PathVariable Long id, @RequestBody ScrapApprovalDTO dto) {
        dto.setApplicationId(id);
        scrapplicationService.approve(dto);
        return Result.success();
    }

    @PostMapping("/reject/{id}")
    public Result<Void> rejectById(@PathVariable Long id, @RequestBody ScrapApprovalDTO dto) {
        dto.setApplicationId(id);
        scrapplicationService.reject(dto);
        return Result.success();
    }

    @GetMapping("/my")
    public Result<PageResult<ScrapplicationVO>> my(ScrapQueryDTO dto) {
        IPage<ScrapplicationVO> page = scrapplicationService.myApplicationList(dto);
        return Result.success(PageResult.of(page));
    }

    @GetMapping("/approval/page")
    public Result<PageResult<ScrapplicationVO>> approvalPage(ScrapQueryDTO dto) {
        IPage<ScrapplicationVO> page = scrapplicationService.page(dto);
        return Result.success(PageResult.of(page));
    }

    @PostMapping("/cancel/{id}")
    public Result<Void> cancel(@PathVariable Long id) {
        return Result.success();
    }

    @GetMapping("/export")
    public void export(ScrapQueryDTO dto, HttpServletResponse response) throws IOException {
        List<ScrapplicationVO> list = scrapplicationService.export(dto);

        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        response.setCharacterEncoding("utf-8");
        String fileName = URLEncoder.encode("资产报废记录", StandardCharsets.UTF_8).replaceAll("\\+", "%20");
        response.setHeader("Content-disposition", "attachment;filename*=utf-8''" + fileName + ".xlsx");

        EasyExcel.write(response.getOutputStream(), ScrapplicationVO.class)
                .sheet("报废记录")
                .doWrite(list);
    }

    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        scrapplicationService.delete(id);
        return Result.success();
    }
}
