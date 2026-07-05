package xyz.wolegelei.nepu_fams.service.impl;

import cn.dev33.satoken.stp.StpUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import xyz.wolegelei.nepu_fams.common.*;
import xyz.wolegelei.nepu_fams.dto.repair.RepairAssignDTO;
import xyz.wolegelei.nepu_fams.dto.repair.RepairProgressDTO;
import xyz.wolegelei.nepu_fams.dto.repair.RepairQueryDTO;
import xyz.wolegelei.nepu_fams.dto.repair.RepairSubmitDTO;
import xyz.wolegelei.nepu_fams.entity.Asset;
import xyz.wolegelei.nepu_fams.entity.College;
import xyz.wolegelei.nepu_fams.entity.RepairOrder;
import xyz.wolegelei.nepu_fams.entity.SysUser;
import xyz.wolegelei.nepu_fams.mapper.AssetMapper;
import xyz.wolegelei.nepu_fams.mapper.CollegeMapper;
import xyz.wolegelei.nepu_fams.mapper.RepairOrderMapper;
import xyz.wolegelei.nepu_fams.mapper.SysUserMapper;
import xyz.wolegelei.nepu_fams.service.RepairOrderService;
import xyz.wolegelei.nepu_fams.vo.repair.RepairOrderDetailVO;
import xyz.wolegelei.nepu_fams.vo.repair.RepairOrderVO;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class RepairOrderServiceImpl implements RepairOrderService {

    private final RepairOrderMapper repairOrderMapper;
    private final AssetMapper assetMapper;
    private final SysUserMapper sysUserMapper;
    private final CollegeMapper collegeMapper;

    private static final List<String> NON_TERMINAL_STATUSES = Arrays.asList(
            RepairStatus.PENDING.getCode(),
            RepairStatus.IN_PROGRESS.getCode()
    );

    @Override
    @Transactional
    public void submit(RepairSubmitDTO dto) {
        Long currentUserId = StpUtil.getLoginIdAsLong();
        SysUser currentUser = sysUserMapper.selectById(currentUserId);
        if (currentUser == null) {
            throw new BusinessException(ResultCode.USER_NOT_FOUND);
        }

        Asset asset = assetMapper.selectById(dto.getAssetId());
        if (asset == null) {
            throw new BusinessException(ResultCode.ASSET_NOT_FOUND);
        }

        if (AssetStatus.SCRAPPED.getCode().equals(asset.getStatus())) {
            throw new BusinessException("已报废的资产不能报修");
        }

        LambdaQueryWrapper<RepairOrder> existWrapper = new LambdaQueryWrapper<>();
        existWrapper.eq(RepairOrder::getAssetId, dto.getAssetId());
        existWrapper.in(RepairOrder::getStatus, NON_TERMINAL_STATUSES);
        Long count = repairOrderMapper.selectCount(existWrapper);
        if (count > 0) {
            throw new BusinessException("该资产已有未完成的维修工单");
        }

        RepairOrder order = new RepairOrder();
        order.setAssetId(dto.getAssetId());
        order.setUserId(currentUserId);
        order.setCollegeId(currentUser.getCollegeId());
        order.setFaultDescription(dto.getFaultDescription());
        order.setStatus(RepairStatus.PENDING.getCode());
        order.setPriority(RepairPriority.NORMAL.getCode());

        repairOrderMapper.insert(order);

        asset.setStatus(AssetStatus.REPAIRING.getCode());
        assetMapper.updateById(asset);
    }

    @Override
    public IPage<RepairOrderVO> page(RepairQueryDTO dto) {
        Long currentUserId = StpUtil.getLoginIdAsLong();
        SysUser currentUser = sysUserMapper.selectById(currentUserId);
        if (currentUser == null) {
            throw new BusinessException(ResultCode.USER_NOT_FOUND);
        }

        String role = currentUser.getRole();
        Page<RepairOrder> page = new Page<>(dto.getPageNum(), dto.getPageSize());
        LambdaQueryWrapper<RepairOrder> wrapper = buildQueryWrapper(dto);

        if (RoleConstants.USER.equals(role)) {
            wrapper.eq(RepairOrder::getUserId, currentUserId);
        } else if (RoleConstants.COLLEGE_ADMIN.equals(role)) {
            wrapper.eq(RepairOrder::getCollegeId, currentUser.getCollegeId());
        }

        wrapper.orderByDesc(RepairOrder::getCreateTime);

        IPage<RepairOrder> orderPage = repairOrderMapper.selectPage(page, wrapper);
        return orderPage.convert(this::convertToVO);
    }

    @Override
    public RepairOrderDetailVO getById(Long id) {
        Long currentUserId = StpUtil.getLoginIdAsLong();
        SysUser currentUser = sysUserMapper.selectById(currentUserId);
        if (currentUser == null) {
            throw new BusinessException(ResultCode.USER_NOT_FOUND);
        }

        RepairOrder order = repairOrderMapper.selectById(id);
        if (order == null) {
            throw new BusinessException(ResultCode.REPAIR_NOT_FOUND);
        }

        checkDataPermission(order, currentUser);

        RepairOrderDetailVO detailVO = new RepairOrderDetailVO();
        BeanUtils.copyProperties(convertToVO(order), detailVO);

        return detailVO;
    }

    @Override
    @Transactional
    public void assign(RepairAssignDTO dto) {
        Long currentUserId = StpUtil.getLoginIdAsLong();
        SysUser currentUser = sysUserMapper.selectById(currentUserId);
        if (currentUser == null) {
            throw new BusinessException(ResultCode.USER_NOT_FOUND);
        }

        String role = currentUser.getRole();
        if (!RoleConstants.SUPER_ADMIN.equals(role) && !RoleConstants.COLLEGE_ADMIN.equals(role)) {
            throw new BusinessException(ResultCode.FORBIDDEN);
        }

        RepairOrder order = repairOrderMapper.selectById(dto.getOrderId());
        if (order == null) {
            throw new BusinessException(ResultCode.REPAIR_NOT_FOUND);
        }

        if (!RepairStatus.PENDING.getCode().equals(order.getStatus())) {
            throw new BusinessException("只有待处理状态的工单才能派单");
        }

        if (RoleConstants.COLLEGE_ADMIN.equals(role)) {
            if (!order.getCollegeId().equals(currentUser.getCollegeId())) {
                throw new BusinessException("只能派单本学院的工单");
            }
        }

        SysUser assignee = sysUserMapper.selectById(dto.getAssigneeId());
        if (assignee == null) {
            throw new BusinessException(ResultCode.USER_NOT_FOUND);
        }

        order.setAssigneeId(dto.getAssigneeId());
        order.setPriority(dto.getPriority() != null ? dto.getPriority() : RepairPriority.NORMAL.getCode());
        order.setStatus(RepairStatus.IN_PROGRESS.getCode());
        repairOrderMapper.updateById(order);
    }

    @Override
    @Transactional
    public void updateProgress(RepairProgressDTO dto) {
        Long currentUserId = StpUtil.getLoginIdAsLong();
        SysUser currentUser = sysUserMapper.selectById(currentUserId);
        if (currentUser == null) {
            throw new BusinessException(ResultCode.USER_NOT_FOUND);
        }

        RepairOrder order = repairOrderMapper.selectById(dto.getOrderId());
        if (order == null) {
            throw new BusinessException(ResultCode.REPAIR_NOT_FOUND);
        }

        if (!order.getAssigneeId().equals(currentUserId)) {
            throw new BusinessException("只能更新自己负责的工单");
        }

        if (!RepairStatus.IN_PROGRESS.getCode().equals(order.getStatus())) {
            throw new BusinessException("只有维修中状态的工单才能更新进度");
        }

        String newStatus = dto.getStatus();
        if (!RepairStatus.COMPLETED.getCode().equals(newStatus) && !RepairStatus.SCRAPPED.getCode().equals(newStatus)) {
            throw new BusinessException("状态只能更新为已完成或需报废");
        }

        order.setStatus(newStatus);
        order.setRepairResult(dto.getRepairResult());
        repairOrderMapper.updateById(order);

        Asset asset = assetMapper.selectById(order.getAssetId());
        if (asset != null) {
            if (RepairStatus.COMPLETED.getCode().equals(newStatus)) {
                asset.setStatus(AssetStatus.IDLE.getCode());
            }
            assetMapper.updateById(asset);
        }
    }

    @Override
    public IPage<RepairOrderVO> myAssignedList(RepairQueryDTO dto) {
        Long currentUserId = StpUtil.getLoginIdAsLong();
        SysUser currentUser = sysUserMapper.selectById(currentUserId);
        if (currentUser == null) {
            throw new BusinessException(ResultCode.USER_NOT_FOUND);
        }

        Page<RepairOrder> page = new Page<>(dto.getPageNum(), dto.getPageSize());
        LambdaQueryWrapper<RepairOrder> wrapper = buildQueryWrapper(dto);
        wrapper.eq(RepairOrder::getAssigneeId, currentUserId);
        wrapper.orderByDesc(RepairOrder::getCreateTime);

        IPage<RepairOrder> orderPage = repairOrderMapper.selectPage(page, wrapper);
        return orderPage.convert(this::convertToVO);
    }

    @Override
    @Transactional
    public void delete(Long id) {
        Long currentUserId = StpUtil.getLoginIdAsLong();
        SysUser currentUser = sysUserMapper.selectById(currentUserId);
        if (currentUser == null) {
            throw new BusinessException(ResultCode.USER_NOT_FOUND);
        }

        String role = currentUser.getRole();
        if (!RoleConstants.SUPER_ADMIN.equals(role) && !RoleConstants.COLLEGE_ADMIN.equals(role)) {
            throw new BusinessException(ResultCode.FORBIDDEN);
        }

        RepairOrder order = repairOrderMapper.selectById(id);
        if (order == null) {
            throw new BusinessException(ResultCode.REPAIR_NOT_FOUND);
        }

        if (RoleConstants.COLLEGE_ADMIN.equals(role)) {
            if (!order.getCollegeId().equals(currentUser.getCollegeId())) {
                throw new BusinessException("只能删除本学院的工单");
            }
        }

        if (!RepairStatus.PENDING.getCode().equals(order.getStatus())) {
            throw new BusinessException("只有待处理状态的工单才能删除");
        }

        Asset asset = assetMapper.selectById(order.getAssetId());
        if (asset != null && AssetStatus.REPAIRING.getCode().equals(asset.getStatus())) {
            asset.setStatus(AssetStatus.IDLE.getCode());
            assetMapper.updateById(asset);
        }

        repairOrderMapper.deleteById(id);
    }

    private LambdaQueryWrapper<RepairOrder> buildQueryWrapper(RepairQueryDTO dto) {
        LambdaQueryWrapper<RepairOrder> wrapper = new LambdaQueryWrapper<>();

        if (StringUtils.hasText(dto.getStatus())) {
            wrapper.eq(RepairOrder::getStatus, dto.getStatus());
        }
        if (dto.getAssigneeId() != null) {
            wrapper.eq(RepairOrder::getAssigneeId, dto.getAssigneeId());
        }
        if (dto.getCollegeId() != null) {
            wrapper.eq(RepairOrder::getCollegeId, dto.getCollegeId());
        }
        if (StringUtils.hasText(dto.getPriority())) {
            wrapper.eq(RepairOrder::getPriority, dto.getPriority());
        }
        if (StringUtils.hasText(dto.getAssetName())) {
            List<Asset> assets = assetMapper.selectList(
                    new LambdaQueryWrapper<Asset>().like(Asset::getName, dto.getAssetName())
            );
            List<Long> assetIds = assets.stream().map(Asset::getId).collect(Collectors.toList());
            if (!assetIds.isEmpty()) {
                wrapper.in(RepairOrder::getAssetId, assetIds);
            } else {
                wrapper.in(RepairOrder::getAssetId, -1L);
            }
        }

        return wrapper;
    }

    private void checkDataPermission(RepairOrder order, SysUser currentUser) {
        String role = currentUser.getRole();
        if (RoleConstants.USER.equals(role)) {
            boolean isOwner = order.getUserId().equals(currentUser.getId());
            boolean isAssignee = order.getAssigneeId() != null && order.getAssigneeId().equals(currentUser.getId());
            if (!isOwner && !isAssignee) {
                throw new BusinessException(ResultCode.FORBIDDEN);
            }
        } else if (RoleConstants.COLLEGE_ADMIN.equals(role)) {
            if (!order.getCollegeId().equals(currentUser.getCollegeId())) {
                throw new BusinessException(ResultCode.FORBIDDEN);
            }
        }
    }

    private RepairOrderVO convertToVO(RepairOrder order) {
        RepairOrderVO vo = new RepairOrderVO();
        BeanUtils.copyProperties(order, vo);

        vo.setStatusName(RepairStatus.getLabelByCode(order.getStatus()));
        vo.setPriorityName(RepairPriority.getLabelByCode(order.getPriority()));

        Asset asset = assetMapper.selectById(order.getAssetId());
        if (asset != null) {
            vo.setAssetNo(asset.getAssetNo());
            vo.setAssetName(asset.getName());
        }

        SysUser user = sysUserMapper.selectById(order.getUserId());
        if (user != null) {
            vo.setUserName(user.getRealName());
        }

        if (order.getCollegeId() != null) {
            College college = collegeMapper.selectById(order.getCollegeId());
            if (college != null) {
                vo.setCollegeName(college.getName());
            }
        }

        if (order.getAssigneeId() != null) {
            SysUser assignee = sysUserMapper.selectById(order.getAssigneeId());
            if (assignee != null) {
                vo.setAssigneeName(assignee.getRealName());
            }
        }

        return vo;
    }
}
