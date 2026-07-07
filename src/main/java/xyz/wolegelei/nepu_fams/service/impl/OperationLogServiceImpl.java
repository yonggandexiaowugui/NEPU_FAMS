package xyz.wolegelei.nepu_fams.service.impl;

import cn.dev33.satoken.stp.StpUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import xyz.wolegelei.nepu_fams.common.OperationType;
import xyz.wolegelei.nepu_fams.common.RoleConstants;
import xyz.wolegelei.nepu_fams.dto.log.OperationLogQueryDTO;
import xyz.wolegelei.nepu_fams.entity.OperationLog;
import xyz.wolegelei.nepu_fams.entity.SysUser;
import xyz.wolegelei.nepu_fams.mapper.OperationLogMapper;
import xyz.wolegelei.nepu_fams.mapper.SysUserMapper;
import xyz.wolegelei.nepu_fams.service.OperationLogService;
import xyz.wolegelei.nepu_fams.vo.log.OperationLogVO;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class OperationLogServiceImpl implements OperationLogService {

    private final OperationLogMapper operationLogMapper;
    private final SysUserMapper sysUserMapper;

    @Override
    public IPage<OperationLogVO> page(OperationLogQueryDTO dto) {
        Long currentUserId = StpUtil.getLoginIdAsLong();
        SysUser currentUser = sysUserMapper.selectById(currentUserId);
        if (currentUser == null) {
            throw new RuntimeException("用户不存在");
        }

        Page<OperationLog> page = new Page<>(dto.getPageNum(), dto.getPageSize());
        LambdaQueryWrapper<OperationLog> wrapper = buildQueryWrapper(dto, currentUser);

        if (StringUtils.hasText(dto.getOrderBy())) {
            boolean isAsc = "asc".equalsIgnoreCase(dto.getOrderDirection());
            if ("createTime".equals(dto.getOrderBy())) {
                wrapper.orderBy(true, isAsc, OperationLog::getCreateTime);
            } else {
                wrapper.orderByDesc(OperationLog::getCreateTime);
            }
        } else {
            wrapper.orderByDesc(OperationLog::getCreateTime);
        }

        IPage<OperationLog> logPage = operationLogMapper.selectPage(page, wrapper);
        return logPage.convert(this::convertToVO);
    }

    @Override
    public OperationLogVO getById(Long id) {
        OperationLog log = operationLogMapper.selectById(id);
        if (log == null) {
            throw new RuntimeException("日志不存在");
        }
        return convertToVO(log);
    }

    @Override
    public void add(OperationLog operationLog) {
        operationLogMapper.insert(operationLog);
    }

    @Override
    public void delete(Long id) {
        OperationLog log = operationLogMapper.selectById(id);
        if (log == null) {
            throw new RuntimeException("日志不存在");
        }
        operationLogMapper.deleteById(id);
    }

    @Override
    public void clear() {
        Long currentUserId = StpUtil.getLoginIdAsLong();
        SysUser currentUser = sysUserMapper.selectById(currentUserId);
        if (currentUser == null || !RoleConstants.SUPER_ADMIN.equals(currentUser.getRole())) {
            throw new RuntimeException("只有超级管理员可以清空日志");
        }
        operationLogMapper.delete(new LambdaQueryWrapper<OperationLog>().isNotNull(OperationLog::getId));
    }

    @Override
    public List<OperationLogVO> export(OperationLogQueryDTO dto) {
        Long currentUserId = StpUtil.getLoginIdAsLong();
        SysUser currentUser = sysUserMapper.selectById(currentUserId);
        if (currentUser == null) {
            throw new RuntimeException("用户不存在");
        }

        LambdaQueryWrapper<OperationLog> wrapper = buildQueryWrapper(dto, currentUser);
        wrapper.orderByDesc(OperationLog::getCreateTime);
        List<OperationLog> list = operationLogMapper.selectList(wrapper);
        return list.stream().map(this::convertToVO).collect(Collectors.toList());
    }

    private LambdaQueryWrapper<OperationLog> buildQueryWrapper(OperationLogQueryDTO dto, SysUser currentUser) {
        LambdaQueryWrapper<OperationLog> wrapper = new LambdaQueryWrapper<>();

        if (StringUtils.hasText(dto.getUsername())) {
            wrapper.like(OperationLog::getUsername, dto.getUsername());
        }
        if (StringUtils.hasText(dto.getOperation())) {
            wrapper.like(OperationLog::getOperation, dto.getOperation());
        }
        if (StringUtils.hasText(dto.getType())) {
            wrapper.eq(OperationLog::getType, dto.getType());
        }
        if (dto.getStartTime() != null) {
            wrapper.ge(OperationLog::getCreateTime, dto.getStartTime());
        }
        if (dto.getEndTime() != null) {
            wrapper.le(OperationLog::getCreateTime, dto.getEndTime());
        }

        String role = currentUser.getRole();
        if (RoleConstants.COLLEGE_ADMIN.equals(role)) {
            List<Long> collegeUserIds = getCollegeUserIds(currentUser.getCollegeId());
            if (collegeUserIds.isEmpty()) {
                wrapper.eq(OperationLog::getUserId, -1L);
            } else {
                wrapper.in(OperationLog::getUserId, collegeUserIds);
            }
        } else if (RoleConstants.USER.equals(role)) {
            wrapper.eq(OperationLog::getUserId, currentUser.getId());
        }

        return wrapper;
    }

    private void checkLogPermission(OperationLog log, SysUser currentUser) {
        String role = currentUser.getRole();
        if (RoleConstants.SUPER_ADMIN.equals(role)) {
            return;
        }
        if (RoleConstants.USER.equals(role) && currentUser.getId().equals(log.getUserId())) {
            return;
        }
        if (RoleConstants.COLLEGE_ADMIN.equals(role)) {
            List<Long> collegeUserIds = getCollegeUserIds(currentUser.getCollegeId());
            if (collegeUserIds.contains(log.getUserId())) {
                return;
            }
        }
        throw new RuntimeException("无权查看该日志");
    }

    private List<Long> getCollegeUserIds(Long collegeId) {
        LambdaQueryWrapper<SysUser> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SysUser::getCollegeId, collegeId);
        wrapper.select(SysUser::getId);
        List<SysUser> users = sysUserMapper.selectList(wrapper);
        return users.stream().map(SysUser::getId).collect(Collectors.toList());
    }

    private OperationLogVO convertToVO(OperationLog log) {
        OperationLogVO vo = new OperationLogVO();
        BeanUtils.copyProperties(log, vo);
        vo.setTypeName(OperationType.getDescriptionByCode(log.getType()));
        return vo;
    }
}
