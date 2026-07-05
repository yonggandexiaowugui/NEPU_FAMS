package xyz.wolegelei.nepu_fams.service.impl;

import cn.dev33.satoken.stp.StpUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import xyz.wolegelei.nepu_fams.common.*;
import xyz.wolegelei.nepu_fams.dto.inventory.InventoryTaskCreateDTO;
import xyz.wolegelei.nepu_fams.dto.inventory.InventoryTaskQueryDTO;
import xyz.wolegelei.nepu_fams.dto.inventory.InventoryTaskUpdateDTO;
import xyz.wolegelei.nepu_fams.entity.*;
import xyz.wolegelei.nepu_fams.mapper.*;
import xyz.wolegelei.nepu_fams.service.InventoryTaskService;
import xyz.wolegelei.nepu_fams.vo.UserVO;
import xyz.wolegelei.nepu_fams.vo.inventory.InventoryTaskDetailVO;
import xyz.wolegelei.nepu_fams.vo.inventory.InventoryTaskVO;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class InventoryTaskServiceImpl implements InventoryTaskService {

    private final InventoryTaskMapper inventoryTaskMapper;
    private final InventoryRecordMapper inventoryRecordMapper;
    private final AssetMapper assetMapper;
    private final AssetCategoryMapper assetCategoryMapper;
    private final CollegeMapper collegeMapper;
    private final SysUserMapper sysUserMapper;

    @Override
    public IPage<InventoryTaskVO> page(InventoryTaskQueryDTO dto) {
        Long currentUserId = StpUtil.getLoginIdAsLong();
        SysUser currentUser = sysUserMapper.selectById(currentUserId);
        if (currentUser == null) {
            throw new BusinessException(ResultCode.USER_NOT_FOUND);
        }

        Page<InventoryTask> page = new Page<>(dto.getPageNum(), dto.getPageSize());
        LambdaQueryWrapper<InventoryTask> wrapper = buildQueryWrapper(dto, currentUser);
        wrapper.orderByDesc(InventoryTask::getCreateTime);

        IPage<InventoryTask> taskPage = inventoryTaskMapper.selectPage(page, wrapper);
        return taskPage.convert(this::convertToVO);
    }

    private LambdaQueryWrapper<InventoryTask> buildQueryWrapper(InventoryTaskQueryDTO dto, SysUser currentUser) {
        LambdaQueryWrapper<InventoryTask> wrapper = new LambdaQueryWrapper<>();

        if (StringUtils.hasText(dto.getName())) {
            wrapper.like(InventoryTask::getName, dto.getName());
        }
        if (StringUtils.hasText(dto.getStatus())) {
            wrapper.eq(InventoryTask::getStatus, dto.getStatus());
        }
        if (dto.getCollegeId() != null) {
            wrapper.eq(InventoryTask::getCollegeId, dto.getCollegeId());
        }

        String role = currentUser.getRole();
        if (RoleConstants.COLLEGE_ADMIN.equals(role)) {
            wrapper.eq(InventoryTask::getCollegeId, currentUser.getCollegeId());
        } else if (RoleConstants.USER.equals(role)) {
            wrapper.eq(InventoryTask::getCollegeId, currentUser.getCollegeId());
        }

        return wrapper;
    }

    @Override
    public InventoryTaskDetailVO getDetail(Long id) {
        InventoryTask task = inventoryTaskMapper.selectById(id);
        if (task == null) {
            throw new BusinessException(ResultCode.INVENTORY_NOT_FOUND);
        }

        checkDataPermission(task);

        InventoryTaskDetailVO detailVO = new InventoryTaskDetailVO();
        InventoryTaskVO baseVO = convertToVO(task);
        BeanUtils.copyProperties(baseVO, detailVO);

        detailVO.setAssignees(getAssignees(task));

        return detailVO;
    }

    private List<UserVO> getAssignees(InventoryTask task) {
        List<UserVO> assignees = new ArrayList<>();
        LambdaQueryWrapper<SysUser> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SysUser::getCollegeId, task.getCollegeId());
        wrapper.eq(SysUser::getStatus, 1);
        List<SysUser> users = sysUserMapper.selectList(wrapper);
        for (SysUser user : users) {
            UserVO vo = new UserVO();
            BeanUtils.copyProperties(user, vo);
            vo.setRoleName(RoleConstants.getRoleName(user.getRole()));
            if (user.getCollegeId() != null) {
                College college = collegeMapper.selectById(user.getCollegeId());
                if (college != null) {
                    vo.setCollegeName(college.getName());
                }
            }
            assignees.add(vo);
        }
        return assignees;
    }

    @Override
    public void create(InventoryTaskCreateDTO dto) {
        checkAdminPermission();
        checkDataPermissionForCollege(dto.getCollegeId());

        InventoryTask task = new InventoryTask();
        BeanUtils.copyProperties(dto, task);
        task.setStatus(InventoryStatus.IN_PROGRESS.getCode());
        task.setCreatorId(StpUtil.getLoginIdAsLong());

        inventoryTaskMapper.insert(task);
    }

    @Override
    public void update(Long id, InventoryTaskUpdateDTO dto) {
        checkAdminPermission();

        InventoryTask exist = inventoryTaskMapper.selectById(id);
        if (exist == null) {
            throw new BusinessException(ResultCode.INVENTORY_NOT_FOUND);
        }

        checkDataPermission(exist);

        if (!InventoryStatus.IN_PROGRESS.getCode().equals(exist.getStatus())) {
            throw new BusinessException("只有进行中的盘点任务可以编辑");
        }

        InventoryTask task = new InventoryTask();
        BeanUtils.copyProperties(dto, task);
        task.setId(id);

        inventoryTaskMapper.updateById(task);
    }

    @Override
    public void delete(Long id) {
        checkAdminPermission();

        InventoryTask exist = inventoryTaskMapper.selectById(id);
        if (exist == null) {
            throw new BusinessException(ResultCode.INVENTORY_NOT_FOUND);
        }

        checkDataPermission(exist);

        if (!InventoryStatus.IN_PROGRESS.getCode().equals(exist.getStatus())) {
            throw new BusinessException("只有进行中的盘点任务可以删除");
        }

        inventoryTaskMapper.deleteById(id);

        LambdaQueryWrapper<InventoryRecord> recordWrapper = new LambdaQueryWrapper<>();
        recordWrapper.eq(InventoryRecord::getTaskId, id);
        inventoryRecordMapper.delete(recordWrapper);
    }

    @Override
    public void complete(Long id) {
        checkAdminPermission();

        InventoryTask exist = inventoryTaskMapper.selectById(id);
        if (exist == null) {
            throw new BusinessException(ResultCode.INVENTORY_NOT_FOUND);
        }

        checkDataPermission(exist);

        if (!InventoryStatus.IN_PROGRESS.getCode().equals(exist.getStatus())) {
            throw new BusinessException("只有进行中的盘点任务可以完成");
        }

        exist.setStatus(InventoryStatus.COMPLETED.getCode());
        inventoryTaskMapper.updateById(exist);
    }

    @Override
    public void archive(Long id) {
        checkAdminPermission();

        InventoryTask exist = inventoryTaskMapper.selectById(id);
        if (exist == null) {
            throw new BusinessException(ResultCode.INVENTORY_NOT_FOUND);
        }

        checkDataPermission(exist);

        if (!InventoryStatus.COMPLETED.getCode().equals(exist.getStatus())) {
            throw new BusinessException("只有已完成的盘点任务可以归档");
        }

        exist.setStatus(InventoryStatus.ARCHIVED.getCode());
        inventoryTaskMapper.updateById(exist);
    }

    private InventoryTaskVO convertToVO(InventoryTask task) {
        InventoryTaskVO vo = new InventoryTaskVO();
        BeanUtils.copyProperties(task, vo);

        vo.setStatusName(InventoryStatus.getLabelByCode(task.getStatus()));

        if (task.getCollegeId() != null) {
            College college = collegeMapper.selectById(task.getCollegeId());
            if (college != null) {
                vo.setCollegeName(college.getName());
            }
        }

        if (task.getCategoryId() != null) {
            AssetCategory category = assetCategoryMapper.selectById(task.getCategoryId());
            if (category != null) {
                vo.setCategoryName(category.getName());
            }
        }

        if (task.getCreatorId() != null) {
            SysUser creator = sysUserMapper.selectById(task.getCreatorId());
            if (creator != null) {
                vo.setCreatorName(creator.getRealName());
            }
        }

        LambdaQueryWrapper<Asset> assetWrapper = new LambdaQueryWrapper<>();
        assetWrapper.eq(Asset::getCollegeId, task.getCollegeId());
        if (task.getCategoryId() != null) {
            assetWrapper.eq(Asset::getCategoryId, task.getCategoryId());
        }
        Long totalCount = assetMapper.selectCount(assetWrapper);
        vo.setTotalCount(totalCount != null ? totalCount.intValue() : 0);

        LambdaQueryWrapper<InventoryRecord> recordWrapper = new LambdaQueryWrapper<>();
        recordWrapper.eq(InventoryRecord::getTaskId, task.getId());
        Long checkedCount = inventoryRecordMapper.selectCount(recordWrapper);
        vo.setCheckedCount(checkedCount != null ? checkedCount.intValue() : 0);

        LambdaQueryWrapper<SysUser> userWrapper = new LambdaQueryWrapper<>();
        userWrapper.eq(SysUser::getCollegeId, task.getCollegeId());
        userWrapper.eq(SysUser::getStatus, 1);
        Long assigneeCount = sysUserMapper.selectCount(userWrapper);
        vo.setAssigneeCount(assigneeCount != null ? assigneeCount.intValue() : 0);

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

    private void checkDataPermissionForCollege(Long targetCollegeId) {
        Long currentUserId = StpUtil.getLoginIdAsLong();
        SysUser currentUser = sysUserMapper.selectById(currentUserId);
        if (currentUser == null) {
            throw new BusinessException(ResultCode.USER_NOT_FOUND);
        }
        if (RoleConstants.COLLEGE_ADMIN.equals(currentUser.getRole())) {
            if (targetCollegeId == null || !targetCollegeId.equals(currentUser.getCollegeId())) {
                throw new BusinessException(ResultCode.FORBIDDEN);
            }
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
