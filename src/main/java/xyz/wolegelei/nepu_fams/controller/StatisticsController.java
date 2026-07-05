package xyz.wolegelei.nepu_fams.controller;

import com.alibaba.excel.EasyExcel;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import xyz.wolegelei.nepu_fams.common.Result;
import xyz.wolegelei.nepu_fams.service.StatisticsService;
import xyz.wolegelei.nepu_fams.vo.statistics.*;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/api/statistics")
@RequiredArgsConstructor
public class StatisticsController {

    private final StatisticsService statisticsService;

    @GetMapping("/overview")
    public Result<OverviewStatsVO> getOverview() {
        OverviewStatsVO vo = statisticsService.getOverview();
        return Result.success(vo);
    }

    @GetMapping("/college")
    public Result<List<CollegeStatsVO>> getByCollege() {
        List<CollegeStatsVO> list = statisticsService.getByCollege();
        return Result.success(list);
    }

    @GetMapping("/category")
    public Result<List<CategoryStatsVO>> getByCategory() {
        List<CategoryStatsVO> list = statisticsService.getByCategory();
        return Result.success(list);
    }

    @GetMapping("/status")
    public Result<List<StatusStatsVO>> getByStatus() {
        List<StatusStatsVO> list = statisticsService.getByStatus();
        return Result.success(list);
    }

    @GetMapping("/inventory")
    public Result<InventoryStatsVO> getInventoryStats() {
        InventoryStatsVO vo = statisticsService.getInventoryStats(null);
        return Result.success(vo);
    }

    @GetMapping("/trend")
    public Result<List<Object>> getTrend() {
        // TODO: 待实现 - StatisticsService 暂未提供趋势统计方法，暂时返回空列表
        return Result.success(new ArrayList<>());
    }

    @GetMapping("/inventory/{taskId}")
    public Result<InventoryStatsVO> getInventoryStats(@PathVariable Long taskId) {
        InventoryStatsVO vo = statisticsService.getInventoryStats(taskId);
        return Result.success(vo);
    }

    @GetMapping("/export/{type}")
    public void exportStats(@PathVariable String type, HttpServletResponse response) throws IOException {
        List<?> list = statisticsService.exportStats(type);

        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        response.setCharacterEncoding("utf-8");

        String fileName;
        String sheetName;
        Class<?> clazz;

        if ("college".equalsIgnoreCase(type)) {
            fileName = URLEncoder.encode("按学院统计报表", StandardCharsets.UTF_8).replaceAll("\\+", "%20");
            sheetName = "学院统计";
            clazz = CollegeStatsVO.class;
        } else if ("category".equalsIgnoreCase(type)) {
            fileName = URLEncoder.encode("按分类统计报表", StandardCharsets.UTF_8).replaceAll("\\+", "%20");
            sheetName = "分类统计";
            clazz = CategoryStatsVO.class;
        } else if ("status".equalsIgnoreCase(type)) {
            fileName = URLEncoder.encode("按状态统计报表", StandardCharsets.UTF_8).replaceAll("\\+", "%20");
            sheetName = "状态统计";
            clazz = StatusStatsVO.class;
        } else {
            throw new IllegalArgumentException("不支持的导出类型：" + type);
        }

        response.setHeader("Content-disposition", "attachment;filename*=utf-8''" + fileName + ".xlsx");

        EasyExcel.write(response.getOutputStream(), clazz)
                .sheet(sheetName)
                .doWrite(list);
    }
}
