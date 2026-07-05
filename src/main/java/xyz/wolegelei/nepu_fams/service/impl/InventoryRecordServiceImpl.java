package xyz.wolegelei.nepu_fams.service.impl;

import cn.dev33.satoken.stp.StpUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import xyz.wolegelei.nepu_fams.common.*;
import xyz.wolegelei.nepu_fams.dto.inventory.InventoryRecordBatchDTO;
import xyz.wolegelei.nepu_fams.dto.inventory.InventoryRecordDTO;
import xyz.wolegelei.nepu_fams.entity.*;
import xyz.wolegelei.nepu_fams.mapper.*;
import xyz.wolegelei.nepu_fams.service.InventoryRecordService;
import xyz.wolegelei.nepu_fams.vo.inventory.InventoryDiffVO;
import xyz.wolegelei.nepu_fams.vo.inventory.InventoryRecordVO;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class InventoryRecordServiceImpl implements InventoryRecordService {

    private final InventoryRecordMapper inventoryRecordMapper;
    private final InventoryTaskMapper inventoryTaskMapper;
    private final AssetMapper assetMapper;
    private final AssetCategoryMapper assetCategoryMapper;
    private final SysUserMapper sysUserMapper;

    @Override
    public List<InventoryRecordVO> listByTaskId(Long taskId) {
        InventoryTask task = inventoryTaskMapper.selectById(taskId);
        if (task == null) {
            throw new BusinessException(ResultCode.INVENTORY_NOT_FOUND);
        }

        checkDataPermission(task);

        LambdaQueryWrapper<InventoryRecord> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(InventoryRecord::getTaskId, taskId);
        wrapper.orderByDesc(InventoryRecord::getCreateTime);

        List<InventoryRecord> records = inventoryRecordMapper.selectList(wrapper);
        return records.stream()
                .map(this::convertToVO)
                .collect(Collectors.toList());
    }

    @Override
    public void addRecord(InventoryRecordDTO dto) {
        InventoryTask task = inventoryTaskMapper.selectById(dto.getTaskId());
        if (task == null) {
            throw new BusinessException(ResultCode.INVENTORY_NOT_FOUND);
        }

        checkDataPermission(task);

        if (!InventoryStatus.IN_PROGRESS.getCode().equals(task.getStatus())) {
            throw new BusinessException("盘点任务已完成，不能再录入数据");
        }

        Asset asset = assetMapper.selectById(dto.getAssetId());
        if (asset == null) {
            throw new BusinessException(ResultCode.ASSET_NOT_FOUND);
        }

        LambdaQueryWrapper<InventoryRecord> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(InventoryRecord::getTaskId, dto.getTaskId());
        wrapper.eq(InventoryRecord::getAssetId, dto.getAssetId());
        InventoryRecord existRecord = inventoryRecordMapper.selectOne(wrapper);

        if (existRecord != null) {
            existRecord.setActualQuantity(dto.getActualQuantity());
            existRecord.setRemark(dto.getRemark());
            existRecord.setRecorderId(StpUtil.getLoginIdAsLong());
            inventoryRecordMapper.updateById(existRecord);
        } else {
            InventoryRecord record = new InventoryRecord();
            BeanUtils.copyProperties(dto, record);
            record.setAssetNo(asset.getAssetNo());
            record.setAssetName(asset.getName());
            record.setRecorderId(StpUtil.getLoginIdAsLong());
            inventoryRecordMapper.insert(record);
        }
    }

    @Override
    public void batchAddRecords(InventoryRecordBatchDTO dto) {
        InventoryTask task = inventoryTaskMapper.selectById(dto.getTaskId());
        if (task == null) {
            throw new BusinessException(ResultCode.INVENTORY_NOT_FOUND);
        }

        checkDataPermission(task);

        if (!InventoryStatus.IN_PROGRESS.getCode().equals(task.getStatus())) {
            throw new BusinessException("盘点任务已完成，不能再录入数据");
        }

        Long currentUserId = StpUtil.getLoginIdAsLong();

        if (dto.getRecords() != null) {
            for (InventoryRecordDTO recordDTO : dto.getRecords()) {
                recordDTO.setTaskId(dto.getTaskId());

                Asset asset = assetMapper.selectById(recordDTO.getAssetId());
                if (asset == null) {
                    continue;
                }

                LambdaQueryWrapper<InventoryRecord> wrapper = new LambdaQueryWrapper<>();
                wrapper.eq(InventoryRecord::getTaskId, dto.getTaskId());
                wrapper.eq(InventoryRecord::getAssetId, recordDTO.getAssetId());
                InventoryRecord existRecord = inventoryRecordMapper.selectOne(wrapper);

                if (existRecord != null) {
                    existRecord.setActualQuantity(recordDTO.getActualQuantity());
                    existRecord.setRemark(recordDTO.getRemark());
                    existRecord.setRecorderId(currentUserId);
                    inventoryRecordMapper.updateById(existRecord);
                } else {
                    InventoryRecord record = new InventoryRecord();
                    BeanUtils.copyProperties(recordDTO, record);
                    record.setAssetNo(asset.getAssetNo());
                    record.setAssetName(asset.getName());
                    record.setRecorderId(currentUserId);
                    inventoryRecordMapper.insert(record);
                }
            }
        }
    }

    @Override
    public List<InventoryDiffVO> getDiffList(Long taskId) {
        InventoryTask task = inventoryTaskMapper.selectById(taskId);
        if (task == null) {
            throw new BusinessException(ResultCode.INVENTORY_NOT_FOUND);
        }

        checkDataPermission(task);

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

        List<InventoryDiffVO> diffList = new ArrayList<>();

        for (Asset asset : bookAssets) {
            InventoryDiffVO diffVO = new InventoryDiffVO();
            diffVO.setAssetId(asset.getId());
            diffVO.setAssetNo(asset.getAssetNo());
            diffVO.setAssetName(asset.getName());
            diffVO.setLocation(asset.getLocation());
            diffVO.setBookQuantity(1);

            if (asset.getCategoryId() != null) {
                AssetCategory category = assetCategoryMapper.selectById(asset.getCategoryId());
                if (category != null) {
                    diffVO.setCategoryName(category.getName());
                }
            }

            InventoryRecord record = recordMap.get(asset.getId());
            if (record != null) {
                diffVO.setActualQuantity(record.getActualQuantity());
                diffVO.setRemark(record.getRemark());
                int diff = record.getActualQuantity() - 1;
                diffVO.setDiffQuantity(diff);
                if (diff == 0) {
                    diffVO.setDiffType("MATCH");
                } else if (diff > 0) {
                    diffVO.setDiffType("PROFIT");
                } else {
                    diffVO.setDiffType("LOSS");
                }
            } else {
                diffVO.setActualQuantity(0);
                diffVO.setDiffQuantity(-1);
                diffVO.setDiffType("LOSS");
            }

            diffList.add(diffVO);
        }

        for (InventoryRecord record : records) {
            if (!bookAssetMap.containsKey(record.getAssetId())) {
                InventoryDiffVO diffVO = new InventoryDiffVO();
                diffVO.setAssetId(record.getAssetId());
                diffVO.setAssetNo(record.getAssetNo());
                diffVO.setAssetName(record.getAssetName());
                diffVO.setBookQuantity(0);
                diffVO.setActualQuantity(record.getActualQuantity());
                diffVO.setDiffQuantity(record.getActualQuantity());
                diffVO.setDiffType("PROFIT");
                diffVO.setRemark(record.getRemark() != null ? record.getRemark() + "（资产不在盘点范围内，可能录入错误）" : "资产不在盘点范围内，可能录入错误");
                diffList.add(diffVO);
            }
        }

        return diffList;
    }

    @Override
    public void confirmDiff(Long taskId, List<Long> assetIds) {
        checkAdminPermission();

        InventoryTask task = inventoryTaskMapper.selectById(taskId);
        if (task == null) {
            throw new BusinessException(ResultCode.INVENTORY_NOT_FOUND);
        }

        checkDataPermission(task);

        if (!InventoryStatus.COMPLETED.getCode().equals(task.getStatus())) {
            throw new BusinessException("只有已完成的盘点任务才能确认差异");
        }

        List<InventoryDiffVO> diffList = getDiffList(taskId);
        Map<Long, InventoryDiffVO> diffMap = diffList.stream()
                .collect(Collectors.toMap(InventoryDiffVO::getAssetId, d -> d));

        if (assetIds != null) {
            for (Long assetId : assetIds) {
                InventoryDiffVO diff = diffMap.get(assetId);
                if (diff == null) {
                    continue;
                }

                if ("LOSS".equals(diff.getDiffType())) {
                    Asset asset = assetMapper.selectById(assetId);
                    if (asset != null) {
                        asset.setStatus(AssetStatus.LOSS.getCode());
                        assetMapper.updateById(asset);
                    }
                }
            }
        }
    }

    private InventoryRecordVO convertToVO(InventoryRecord record) {
        InventoryRecordVO vo = new InventoryRecordVO();
        BeanUtils.copyProperties(record, vo);

        if (record.getRecorderId() != null) {
            SysUser recorder = sysUserMapper.selectById(record.getRecorderId());
            if (recorder != null) {
                vo.setRecorderName(recorder.getRealName());
            }
        }

        return vo;
    }

    private void checkAdminPermission() {
        Long userId = StpUtil.getLoginIdAsLong();
        SysUser user = sysUserMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException(ResultCode.USER_NOT_FOUND);
        }
        String role = user.getRole();
        if (!RoleConstants.SUPER_ADMIN.equals(role) && !RoleConstants.COLLEGE_ADMIN.equals(role)) {
            throw new BusinessException(ResultCode.FORBIDDEN);
        }
    }

    private void checkDataPermission(InventoryTask task) {
        Long currentUserId = StpUtil.getLoginIdAsLong();
        SysUser currentUser = sysUserMapper.selectById(currentUserId);
        if (currentUser == null) {
            throw new BusinessException(ResultCode.USER_NOT_FOUND);
        }
        if (RoleConstants.COLLEGE_ADMIN.equals(currentUser.getRole()) || RoleConstants.USER.equals(currentUser.getRole())) {
            if (task.getCollegeId() == null || !task.getCollegeId().equals(currentUser.getCollegeId())) {
                throw new BusinessException(ResultCode.FORBIDDEN);
            }
        }
    }
}
