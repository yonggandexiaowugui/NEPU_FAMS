package xyz.wolegelei.nepu_fams.service.impl;

import cn.dev33.satoken.stp.StpUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import xyz.wolegelei.nepu_fams.common.AssetStatus;
import xyz.wolegelei.nepu_fams.common.BusinessException;
import xyz.wolegelei.nepu_fams.common.ResultCode;
import xyz.wolegelei.nepu_fams.common.RoleConstants;
import xyz.wolegelei.nepu_fams.entity.*;
import xyz.wolegelei.nepu_fams.mapper.*;
import xyz.wolegelei.nepu_fams.service.StatisticsService;
import xyz.wolegelei.nepu_fams.vo.statistics.*;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class StatisticsServiceImpl implements StatisticsService {

    private final AssetMapper assetMapper;
    private final CollegeMapper collegeMapper;
    private final AssetCategoryMapper assetCategoryMapper;
    private final SysUserMapper sysUserMapper;
    private final InventoryTaskMapper inventoryTaskMapper;
    private final InventoryRecordMapper inventoryRecordMapper;

    @Override
    public OverviewStatsVO getOverview() {
        SysUser currentUser = getCurrentUser();
        LambdaQueryWrapper<Asset> wrapper = buildDataPermissionWrapper(currentUser);

        List<Asset> assets = assetMapper.selectList(wrapper);

        OverviewStatsVO vo = new OverviewStatsVO();
        vo.setTotalAssetCount(assets.size());

        BigDecimal totalValue = assets.stream()
                .map(Asset::getCurrentValue)
                .filter(Objects::nonNull)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        vo.setTotalAssetValue(totalValue);

        int idleCount = 0;
        int inUseCount = 0;
        int repairingCount = 0;
        int scrappedCount = 0;

        for (Asset asset : assets) {
            String status = asset.getStatus();
            if (AssetStatus.IDLE.getCode().equals(status)) {
                idleCount++;
            } else if (AssetStatus.IN_USE.getCode().equals(status)) {
                inUseCount++;
            } else if (AssetStatus.REPAIRING.getCode().equals(status)) {
                repairingCount++;
            } else if (AssetStatus.SCRAPPED.getCode().equals(status)) {
                scrappedCount++;
            }
        }

        vo.setIdleCount(idleCount);
        vo.setInUseCount(inUseCount);
        vo.setRepairingCount(repairingCount);
        vo.setScrappedCount(scrappedCount);

        LambdaQueryWrapper<College> collegeWrapper = new LambdaQueryWrapper<>();
        if (RoleConstants.COLLEGE_ADMIN.equals(currentUser.getRole())) {
            collegeWrapper.eq(College::getId, currentUser.getCollegeId());
        }
        Long collegeCount = collegeMapper.selectCount(collegeWrapper);
        vo.setCollegeCount(collegeCount != null ? collegeCount.intValue() : 0);

        LambdaQueryWrapper<AssetCategory> categoryWrapper = new LambdaQueryWrapper<>();
        categoryWrapper.eq(AssetCategory::getLevel, 1);
        Long categoryCount = assetCategoryMapper.selectCount(categoryWrapper);
        vo.setCategoryCount(categoryCount != null ? categoryCount.intValue() : 0);

        return vo;
    }

    @Override
    public List<CollegeStatsVO> getByCollege() {
        SysUser currentUser = getCurrentUser();
        LambdaQueryWrapper<Asset> wrapper = buildDataPermissionWrapper(currentUser);

        List<Asset> assets = assetMapper.selectList(wrapper);

        Map<Long, List<Asset>> collegeAssetMap = assets.stream()
                .filter(a -> a.getCollegeId() != null)
                .collect(Collectors.groupingBy(Asset::getCollegeId));

        List<CollegeStatsVO> result = new ArrayList<>();

        LambdaQueryWrapper<College> collegeWrapper = new LambdaQueryWrapper<>();
        if (RoleConstants.COLLEGE_ADMIN.equals(currentUser.getRole()) || RoleConstants.USER.equals(currentUser.getRole())) {
            collegeWrapper.eq(College::getId, currentUser.getCollegeId());
        }
        collegeWrapper.orderByAsc(College::getId);
        List<College> colleges = collegeMapper.selectList(collegeWrapper);

        for (College college : colleges) {
            CollegeStatsVO vo = new CollegeStatsVO();
            vo.setCollegeId(college.getId());
            vo.setCollegeName(college.getName());

            List<Asset> collegeAssets = collegeAssetMap.getOrDefault(college.getId(), Collections.emptyList());

            vo.setAssetCount(collegeAssets.size());

            BigDecimal totalValue = collegeAssets.stream()
                    .map(Asset::getCurrentValue)
                    .filter(Objects::nonNull)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            vo.setTotalValue(totalValue);

            int idleCount = 0;
            int inUseCount = 0;
            int repairingCount = 0;
            int scrappedCount = 0;

            for (Asset asset : collegeAssets) {
                String status = asset.getStatus();
                if (AssetStatus.IDLE.getCode().equals(status)) {
                    idleCount++;
                } else if (AssetStatus.IN_USE.getCode().equals(status)) {
                    inUseCount++;
                } else if (AssetStatus.REPAIRING.getCode().equals(status)) {
                    repairingCount++;
                } else if (AssetStatus.SCRAPPED.getCode().equals(status)) {
                    scrappedCount++;
                }
            }

            vo.setIdleCount(idleCount);
            vo.setInUseCount(inUseCount);
            vo.setRepairingCount(repairingCount);
            vo.setScrappedCount(scrappedCount);

            result.add(vo);
        }

        return result;
    }

    @Override
    public List<CategoryStatsVO> getByCategory() {
        SysUser currentUser = getCurrentUser();
        LambdaQueryWrapper<Asset> wrapper = buildDataPermissionWrapper(currentUser);

        List<Asset> assets = assetMapper.selectList(wrapper);
        int totalCount = assets.size();

        LambdaQueryWrapper<AssetCategory> allCategoryWrapper = new LambdaQueryWrapper<>();
        List<AssetCategory> allCategories = assetCategoryMapper.selectList(allCategoryWrapper);
        Map<Long, AssetCategory> categoryMap = allCategories.stream()
                .collect(Collectors.toMap(AssetCategory::getId, c -> c));

        Map<Long, Long> categoryCountMap = new HashMap<>();
        Map<Long, BigDecimal> categoryValueMap = new HashMap<>();
        for (Asset asset : assets) {
            Long rootCategoryId = getRootCategoryId(asset.getCategoryId(), categoryMap);
            if (rootCategoryId == null) {
                continue;
            }
            categoryCountMap.merge(rootCategoryId, 1L, Long::sum);
            categoryValueMap.merge(rootCategoryId,
                    asset.getCurrentValue() != null ? asset.getCurrentValue() : BigDecimal.ZERO,
                    BigDecimal::add);
        }

        LambdaQueryWrapper<AssetCategory> categoryWrapper = new LambdaQueryWrapper<>();
        categoryWrapper.eq(AssetCategory::getLevel, 1);
        categoryWrapper.orderByAsc(AssetCategory::getSort);
        List<AssetCategory> categories = assetCategoryMapper.selectList(categoryWrapper);

        List<CategoryStatsVO> result = new ArrayList<>();

        for (AssetCategory category : categories) {
            CategoryStatsVO vo = new CategoryStatsVO();
            vo.setCategoryId(category.getId());
            vo.setCategoryName(category.getName());

            int count = categoryCountMap.getOrDefault(category.getId(), 0L).intValue();
            vo.setAssetCount(count);

            BigDecimal value = categoryValueMap.getOrDefault(category.getId(), BigDecimal.ZERO);
            vo.setTotalValue(value);

            BigDecimal percentage = BigDecimal.ZERO;
            if (totalCount > 0) {
                percentage = BigDecimal.valueOf(count)
                        .multiply(BigDecimal.valueOf(100))
                        .divide(BigDecimal.valueOf(totalCount), 1, RoundingMode.HALF_UP);
            }
            vo.setPercentage(percentage);

            result.add(vo);
        }

        return result;
    }

    @Override
    public List<StatusStatsVO> getByStatus() {
        SysUser currentUser = getCurrentUser();
        LambdaQueryWrapper<Asset> wrapper = buildDataPermissionWrapper(currentUser);

        List<Asset> assets = assetMapper.selectList(wrapper);
        int totalCount = assets.size();

        Map<String, Long> statusCountMap = assets.stream()
                .filter(a -> a.getStatus() != null)
                .collect(Collectors.groupingBy(Asset::getStatus, Collectors.counting()));

        List<StatusStatsVO> result = new ArrayList<>();

        AssetStatus[] statuses = {AssetStatus.IDLE, AssetStatus.IN_USE, AssetStatus.REPAIRING, AssetStatus.SCRAPPED};

        for (AssetStatus status : statuses) {
            StatusStatsVO vo = new StatusStatsVO();
            vo.setStatus(status.getCode());
            vo.setStatusName(status.getLabel());

            int count = statusCountMap.getOrDefault(status.getCode(), 0L).intValue();
            vo.setCount(count);

            BigDecimal percentage = BigDecimal.ZERO;
            if (totalCount > 0) {
                percentage = BigDecimal.valueOf(count)
                        .multiply(BigDecimal.valueOf(100))
                        .divide(BigDecimal.valueOf(totalCount), 1, RoundingMode.HALF_UP);
            }
            vo.setPercentage(percentage);

            result.add(vo);
        }

        return result;
    }

    @Override
    public InventoryStatsVO getInventoryStats(Long taskId) {
        if (taskId == null) {
            InventoryTask latestTask = getLatestPermittedInventoryTask();
            if (latestTask == null) {
                InventoryStatsVO vo = new InventoryStatsVO();
                vo.setTotalCount(0);
                vo.setCheckedCount(0);
                vo.setProfitCount(0);
                vo.setLossCount(0);
                vo.setMatchCount(0);
                return vo;
            }
            taskId = latestTask.getId();
        }

        InventoryTask task = inventoryTaskMapper.selectById(taskId);
        if (task == null) {
            throw new BusinessException(ResultCode.INVENTORY_NOT_FOUND);
        }

        SysUser currentUser = getCurrentUser();
        checkDataPermission(task, currentUser);

        LambdaQueryWrapper<Asset> assetWrapper = new LambdaQueryWrapper<>();
        assetWrapper.eq(Asset::getCollegeId, task.getCollegeId());
        if (task.getCategoryId() != null) {
            assetWrapper.eq(Asset::getCategoryId, task.getCategoryId());
        }
        List<Asset> bookAssets = assetMapper.selectList(assetWrapper);
        Map<Long, Asset> bookAssetMap = bookAssets.stream()
                .collect(Collectors.toMap(Asset::getId, a -> a));

        LambdaQueryWrapper<InventoryRecord> recordWrapper = new LambdaQueryWrapper<>();
        recordWrapper.eq(InventoryRecord::getTaskId, taskId);
        List<InventoryRecord> records = inventoryRecordMapper.selectList(recordWrapper);
        Map<Long, InventoryRecord> recordMap = records.stream()
                .collect(Collectors.toMap(InventoryRecord::getAssetId, r -> r));

        InventoryStatsVO vo = new InventoryStatsVO();
        vo.setTotalCount(bookAssets.size());
        vo.setCheckedCount(recordMap.size());

        int profitCount = 0;
        int lossCount = 0;
        int matchCount = 0;

        for (Asset asset : bookAssets) {
            InventoryRecord record = recordMap.get(asset.getId());
            if (record != null) {
                int actualQty = record.getActualQuantity() != null ? record.getActualQuantity() : 0;
                int diff = actualQty - 1;
                if (diff == 0) {
                    matchCount++;
                } else if (diff > 0) {
                    profitCount++;
                } else {
                    lossCount++;
                }
            } else {
                lossCount++;
            }
        }

        for (InventoryRecord record : records) {
            if (!bookAssetMap.containsKey(record.getAssetId())) {
                profitCount++;
            }
        }

        vo.setProfitCount(profitCount);
        vo.setLossCount(lossCount);
        vo.setMatchCount(matchCount);

        return vo;
    }

    @Override
    public List<?> exportStats(String type) {
        getCurrentUser();

        if ("college".equalsIgnoreCase(type)) {
            return getByCollege();
        } else if ("category".equalsIgnoreCase(type)) {
            return getByCategory();
        } else if ("status".equalsIgnoreCase(type)) {
            return getByStatus();
        } else {
            throw new BusinessException("不支持的导出类型：" + type);
        }
    }

    private SysUser getCurrentUser() {
        Long currentUserId = StpUtil.getLoginIdAsLong();
        SysUser currentUser = sysUserMapper.selectById(currentUserId);
        if (currentUser == null) {
            throw new BusinessException(ResultCode.USER_NOT_FOUND);
        }
        return currentUser;
    }

    private LambdaQueryWrapper<Asset> buildDataPermissionWrapper(SysUser currentUser) {
        LambdaQueryWrapper<Asset> wrapper = new LambdaQueryWrapper<>();

        String role = currentUser.getRole();
        if (RoleConstants.COLLEGE_ADMIN.equals(role) || RoleConstants.USER.equals(role)) {
            wrapper.eq(Asset::getCollegeId, currentUser.getCollegeId());
        }

        return wrapper;
    }

    private Long getRootCategoryId(Long categoryId, Map<Long, AssetCategory> categoryMap) {
        if (categoryId == null) {
            return null;
        }
        AssetCategory category = categoryMap.get(categoryId);
        if (category == null) {
            return null;
        }
        if (category.getLevel() != null && category.getLevel() == 1) {
            return category.getId();
        }
        return category.getParentId();
    }

    private InventoryTask getLatestPermittedInventoryTask() {
        SysUser currentUser = getCurrentUser();
        LambdaQueryWrapper<InventoryTask> wrapper = new LambdaQueryWrapper<>();
        if (RoleConstants.COLLEGE_ADMIN.equals(currentUser.getRole()) || RoleConstants.USER.equals(currentUser.getRole())) {
            wrapper.eq(InventoryTask::getCollegeId, currentUser.getCollegeId());
        }
        wrapper.orderByDesc(InventoryTask::getCreateTime).orderByDesc(InventoryTask::getId).last("LIMIT 1");
        return inventoryTaskMapper.selectOne(wrapper);
    }

    private void checkDataPermission(InventoryTask task, SysUser currentUser) {
        if (RoleConstants.COLLEGE_ADMIN.equals(currentUser.getRole()) || RoleConstants.USER.equals(currentUser.getRole())) {
            if (task.getCollegeId() == null || !task.getCollegeId().equals(currentUser.getCollegeId())) {
                throw new BusinessException(ResultCode.FORBIDDEN);
            }
        }
    }
}
