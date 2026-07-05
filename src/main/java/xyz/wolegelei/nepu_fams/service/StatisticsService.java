package xyz.wolegelei.nepu_fams.service;

import xyz.wolegelei.nepu_fams.vo.statistics.*;

import java.util.List;

public interface StatisticsService {

    OverviewStatsVO getOverview();

    List<CollegeStatsVO> getByCollege();

    List<CategoryStatsVO> getByCategory();

    List<StatusStatsVO> getByStatus();

    InventoryStatsVO getInventoryStats(Long taskId);

    List<?> exportStats(String type);
}
