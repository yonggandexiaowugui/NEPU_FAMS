package xyz.wolegelei.nepu_fams.ai;

import com.baomidou.mybatisplus.core.metadata.IPage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Component;
import xyz.wolegelei.nepu_fams.dto.asset.AssetQueryDTO;
import xyz.wolegelei.nepu_fams.dto.borrow.BorrowQueryDTO;
import xyz.wolegelei.nepu_fams.dto.repair.RepairQueryDTO;
import xyz.wolegelei.nepu_fams.dto.UserPageQueryDTO;
import xyz.wolegelei.nepu_fams.service.AssetService;
import xyz.wolegelei.nepu_fams.service.BorrowApplicationService;
import xyz.wolegelei.nepu_fams.service.RepairOrderService;
import xyz.wolegelei.nepu_fams.service.StatisticsService;
import xyz.wolegelei.nepu_fams.service.SysUserService;
import xyz.wolegelei.nepu_fams.vo.asset.AssetVO;
import xyz.wolegelei.nepu_fams.vo.statistics.OverviewStatsVO;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 固定资产管理系统 AI Agent 工具集
 * 让灵梭 Agent 能直接查询和操作项目业务数据
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class FamsAgentTools {

    private final AssetService assetService;
    private final StatisticsService statisticsService;
    private final BorrowApplicationService borrowApplicationService;
    private final RepairOrderService repairOrderService;
    private final SysUserService sysUserService;

    @Tool(description = "查询固定资产总览统计数据，包括资产总数、总价值、在用数量、闲置数量、维修中数量、已报废数量、学院数量、分类数量")
    public OverviewStatsVO getAssetOverview() {
        log.info("AI Agent 调用: 查询资产总览统计");
        return statisticsService.getOverview();
    }

    @Tool(description = "分页查询固定资产列表，支持按关键词、状态、分类、学院筛选。status可选值: IN_USE(在用), IDLE(闲置), REPAIRING(维修中), SCRAPPED(已报废)")
    public Map<String, Object> queryAssets(
            @ToolParam(description = "页码，默认1") Integer pageNum,
            @ToolParam(description = "每页条数，默认10") Integer pageSize,
            @ToolParam(description = "关键词搜索（资产名称/编号）", required = false) String keyword,
            @ToolParam(description = "资产状态: IN_USE, IDLE, REPAIRING, SCRAPPED", required = false) String status,
            @ToolParam(description = "分类ID", required = false) Long categoryId,
            @ToolParam(description = "学院ID", required = false) Long collegeId
    ) {
        log.info("AI Agent 调用: 查询资产列表, keyword={}, status={}", keyword, status);
        AssetQueryDTO dto = new AssetQueryDTO();
        dto.setPageNum(pageNum != null ? pageNum : 1);
        dto.setPageSize(pageSize != null ? pageSize : 10);
        dto.setKeyword(keyword);
        dto.setStatus(status);
        dto.setCategoryId(categoryId);
        dto.setCollegeId(collegeId);

        IPage<AssetVO> page = assetService.page(dto);
        Map<String, Object> result = new HashMap<>();
        result.put("total", page.getTotal());
        result.put("pages", page.getPages());
        result.put("current", page.getCurrent());
        result.put("records", page.getRecords().stream().map(this::assetToMap).collect(Collectors.toList()));
        return result;
    }

    @Tool(description = "根据资产ID查询单个资产详细信息")
    public AssetVO getAssetById(@ToolParam(description = "资产ID") Long id) {
        log.info("AI Agent 调用: 查询资产详情, id={}", id);
        return assetService.getById(id);
    }

    @Tool(description = "查询领用申请列表，支持按状态筛选")
    public Map<String, Object> queryBorrowApplications(
            @ToolParam(description = "页码") Integer pageNum,
            @ToolParam(description = "每页条数") Integer pageSize,
            @ToolParam(description = "状态筛选", required = false) String status
    ) {
        log.info("AI Agent 调用: 查询领用申请列表, status={}", status);
        BorrowQueryDTO dto = new BorrowQueryDTO();
        dto.setPageNum(pageNum != null ? pageNum : 1);
        dto.setPageSize(pageSize != null ? pageSize : 10);
        dto.setStatus(status);

        var page = borrowApplicationService.pendingApprovalList(dto);
        Map<String, Object> result = new HashMap<>();
        result.put("total", page.getTotal());
        result.put("records", page.getRecords());
        return result;
    }

    @Tool(description = "查询维修工单列表，支持按状态筛选")
    public Map<String, Object> queryRepairOrders(
            @ToolParam(description = "页码") Integer pageNum,
            @ToolParam(description = "每页条数") Integer pageSize,
            @ToolParam(description = "状态筛选", required = false) String status
    ) {
        log.info("AI Agent 调用: 查询维修工单列表, status={}", status);
        RepairQueryDTO dto = new RepairQueryDTO();
        dto.setPageNum(pageNum != null ? pageNum : 1);
        dto.setPageSize(pageSize != null ? pageSize : 10);
        dto.setStatus(status);

        var page = repairOrderService.page(dto);
        Map<String, Object> result = new HashMap<>();
        result.put("total", page.getTotal());
        result.put("records", page.getRecords());
        return result;
    }

    @Tool(description = "分页查询系统用户列表")
    public Map<String, Object> queryUsers(
            @ToolParam(description = "页码") Integer pageNum,
            @ToolParam(description = "每页条数") Integer pageSize,
            @ToolParam(description = "用户名搜索", required = false) String username,
            @ToolParam(description = "姓名搜索", required = false) String realName
    ) {
        log.info("AI Agent 调用: 查询用户列表, username={}, realName={}", username, realName);
        UserPageQueryDTO dto = new UserPageQueryDTO();
        dto.setPageNum(pageNum != null ? pageNum : 1);
        dto.setPageSize(pageSize != null ? pageSize : 10);
        dto.setUsername(username);
        dto.setRealName(realName);

        var page = sysUserService.pageUser(dto);
        Map<String, Object> result = new HashMap<>();
        result.put("total", page.getTotal());
        result.put("records", page.getRecords());
        return result;
    }

    @Tool(description = "按学院统计资产分布情况")
    public List<?> getStatsByCollege() {
        log.info("AI Agent 调用: 按学院统计资产");
        return statisticsService.getByCollege();
    }

    @Tool(description = "按分类统计资产分布情况")
    public List<?> getStatsByCategory() {
        log.info("AI Agent 调用: 按分类统计资产");
        return statisticsService.getByCategory();
    }

    @Tool(description = "按状态统计资产分布情况（在用/闲置/维修中/已报废）")
    public List<?> getStatsByStatus() {
        log.info("AI Agent 调用: 按状态统计资产");
        return statisticsService.getByStatus();
    }

    private Map<String, Object> assetToMap(AssetVO vo) {
        Map<String, Object> map = new HashMap<>();
        map.put("id", vo.getId());
        map.put("assetNo", vo.getAssetNo());
        map.put("name", vo.getName());
        map.put("categoryName", vo.getCategoryName());
        map.put("specification", vo.getSpecification());
        map.put("status", vo.getStatus());
        map.put("statusName", vo.getStatusName());
        map.put("location", vo.getLocation());
        map.put("collegeName", vo.getCollegeName());
        map.put("purchasePrice", vo.getPurchasePrice());
        map.put("currentValue", vo.getCurrentValue());
        map.put("responsiblePerson", vo.getResponsiblePerson());
        return map;
    }
}
