package xyz.wolegelei.nepu_fams.controller;

import com.alibaba.excel.EasyExcel;
import com.baomidou.mybatisplus.core.metadata.IPage;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import xyz.wolegelei.nepu_fams.common.PageResult;
import xyz.wolegelei.nepu_fams.common.Result;
import xyz.wolegelei.nepu_fams.dto.log.OperationLogQueryDTO;
import xyz.wolegelei.nepu_fams.service.OperationLogService;
import xyz.wolegelei.nepu_fams.vo.log.OperationLogVO;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;

@RestController
@RequestMapping("/api/log/operation")
@RequiredArgsConstructor
public class OperationLogController {

    private final OperationLogService operationLogService;

    @GetMapping("/page")
    public Result<PageResult<OperationLogVO>> page(OperationLogQueryDTO dto) {
        IPage<OperationLogVO> page = operationLogService.page(dto);
        return Result.success(PageResult.of(page));
    }

    @GetMapping("/{id}")
    public Result<OperationLogVO> getById(@PathVariable Long id) {
        OperationLogVO vo = operationLogService.getById(id);
        return Result.success(vo);
    }

    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        // TODO: 待实现 - OperationLogService 未继承 IService，暂无 removeById 方法
        return Result.success();
    }

    @DeleteMapping("/clear")
    public Result<Void> clear() {
        // TODO: 待实现 - OperationLogService 未继承 IService，暂无 remove/list 方法
        return Result.success();
    }

    @GetMapping("/export")
    public void export(OperationLogQueryDTO dto, HttpServletResponse response) throws IOException {
        List<OperationLogVO> list = operationLogService.export(dto);

        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        response.setCharacterEncoding("utf-8");
        String fileName = URLEncoder.encode("操作日志", StandardCharsets.UTF_8).replaceAll("\\+", "%20");
        response.setHeader("Content-disposition", "attachment;filename*=utf-8''" + fileName + ".xlsx");

        EasyExcel.write(response.getOutputStream(), OperationLogVO.class)
                .sheet("操作日志")
                .doWrite(list);
    }
}
