package xyz.wolegelei.nepu_fams.service.impl;

import cn.dev33.satoken.stp.StpUtil;
import cn.hutool.crypto.digest.BCrypt;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import xyz.wolegelei.nepu_fams.common.BusinessException;
import xyz.wolegelei.nepu_fams.common.ResultCode;
import xyz.wolegelei.nepu_fams.common.RoleConstants;
import xyz.wolegelei.nepu_fams.dto.AdminResetPasswordDTO;
import xyz.wolegelei.nepu_fams.dto.ChangePasswordDTO;
import xyz.wolegelei.nepu_fams.dto.LoginDTO;
import xyz.wolegelei.nepu_fams.dto.RegisterDTO;
import xyz.wolegelei.nepu_fams.dto.UserPageQueryDTO;
import xyz.wolegelei.nepu_fams.dto.UserUpdateDTO;
import xyz.wolegelei.nepu_fams.entity.College;
import xyz.wolegelei.nepu_fams.entity.SysUser;
import xyz.wolegelei.nepu_fams.mapper.CollegeMapper;
import xyz.wolegelei.nepu_fams.mapper.SysUserMapper;
import xyz.wolegelei.nepu_fams.service.EmailVerificationService;
import xyz.wolegelei.nepu_fams.service.SysUserService;
import xyz.wolegelei.nepu_fams.vo.LoginVO;
import xyz.wolegelei.nepu_fams.vo.UserVO;

@Service
@RequiredArgsConstructor
@Slf4j
public class SysUserServiceImpl implements SysUserService {

    private final SysUserMapper sysUserMapper;
    private final CollegeMapper collegeMapper;
    private final EmailVerificationService emailVerificationService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void register(RegisterDTO registerDTO) {
        log.info("开始注册用户: {}", registerDTO.getUsername());
        LambdaQueryWrapper<SysUser> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SysUser::getUsername, registerDTO.getUsername());
        long count = sysUserMapper.selectCount(wrapper);
        log.info("用户名检查结果: 已存在 {} 条记录", count);
        if (count > 0) {
            throw new BusinessException(ResultCode.USER_ALREADY_EXISTS);
        }
        emailVerificationService.verifyRegisterCode(registerDTO.getEmail(), registerDTO.getEmailCode());
        SysUser user = new SysUser();
        BeanUtils.copyProperties(registerDTO, user);
        user.setPassword(BCrypt.hashpw(registerDTO.getPassword()));
        user.setStatus(1);
        user.setRole(RoleConstants.USER);
        user.setIsDeleted(0);
        log.info("准备插入用户数据: username={}, realName={}, collegeId={}, role={}, isDeleted={}", 
                user.getUsername(), user.getRealName(), user.getCollegeId(), user.getRole(), user.getIsDeleted());
        int result = sysUserMapper.insert(user);
        log.info("用户插入结果: result={}, userId={}", result, user.getId());
        if (result <= 0) {
            throw new BusinessException("注册失败，用户数据未保存");
        }
        log.info("用户注册成功: userId={}, username={}", user.getId(), user.getUsername());
    }

    @Override
    public LoginVO login(LoginDTO loginDTO) {
        LambdaQueryWrapper<SysUser> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SysUser::getUsername, loginDTO.getUsername());
        SysUser user = sysUserMapper.selectOne(wrapper);
        if (user == null) {
            throw new BusinessException(ResultCode.USERNAME_OR_PASSWORD_ERROR);
        }
        if (!BCrypt.checkpw(loginDTO.getPassword(), user.getPassword())) {
            throw new BusinessException(ResultCode.USERNAME_OR_PASSWORD_ERROR);
        }
        if (user.getStatus() != null && user.getStatus() == 0) {
            throw new BusinessException(ResultCode.USER_DISABLED);
        }
        StpUtil.login(user.getId());
        StpUtil.getSession().set("username", user.getUsername());
        StpUtil.getSession().set("role", user.getRole());
        String token = StpUtil.getTokenValue();
        UserVO userVO = convertToVO(user);
        LoginVO loginVO = new LoginVO();
        loginVO.setToken(token);
        loginVO.setUserInfo(userVO);
        return loginVO;
    }

    @Override
    public UserVO getUserById(Long id) {
        SysUser user = sysUserMapper.selectById(id);
        if (user == null) {
            throw new BusinessException(ResultCode.USER_NOT_FOUND);
        }
        return convertToVO(user);
    }

    @Override
    public IPage<UserVO> pageUser(UserPageQueryDTO dto) {
        Long currentUserId = StpUtil.getLoginIdAsLong();
        SysUser currentUser = sysUserMapper.selectById(currentUserId);
        if (currentUser == null) {
            throw new BusinessException(ResultCode.USER_NOT_FOUND);
        }
        Page<SysUser> page = new Page<>(dto.getPageNum(), dto.getPageSize());
        LambdaQueryWrapper<SysUser> wrapper = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(dto.getUsername())) {
            wrapper.like(SysUser::getUsername, dto.getUsername());
        }
        if (StringUtils.hasText(dto.getRealName())) {
            wrapper.like(SysUser::getRealName, dto.getRealName());
        }
        if (StringUtils.hasText(dto.getRole())) {
            wrapper.eq(SysUser::getRole, dto.getRole());
        }
        if (dto.getCollegeId() != null) {
            wrapper.eq(SysUser::getCollegeId, dto.getCollegeId());
        }
        String role = currentUser.getRole();
        if (RoleConstants.COLLEGE_ADMIN.equals(role)) {
            wrapper.eq(SysUser::getCollegeId, currentUser.getCollegeId());
        } else if (RoleConstants.USER.equals(role)) {
            wrapper.eq(SysUser::getId, currentUserId);
        }
        if (StringUtils.hasText(dto.getOrderBy())) {
            boolean isAsc = "asc".equalsIgnoreCase(dto.getOrderDirection());
            if ("createTime".equals(dto.getOrderBy())) {
                wrapper.orderBy(true, isAsc, SysUser::getCreateTime);
            } else if ("username".equals(dto.getOrderBy())) {
                wrapper.orderBy(true, isAsc, SysUser::getUsername);
            } else {
                wrapper.orderByDesc(SysUser::getCreateTime);
            }
        } else {
            wrapper.orderByDesc(SysUser::getCreateTime);
        }
        IPage<SysUser> userPage = sysUserMapper.selectPage(page, wrapper);
        return userPage.convert(this::convertToVO);
    }

    @Override
    public void addUser(SysUser sysUser) {
        checkAdminPermission();
        checkDataPermissionForUser(sysUser.getCollegeId());
        LambdaQueryWrapper<SysUser> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SysUser::getUsername, sysUser.getUsername());
        if (sysUserMapper.selectCount(wrapper) > 0) {
            throw new BusinessException(ResultCode.USER_ALREADY_EXISTS);
        }
        sysUser.setPassword(BCrypt.hashpw(sysUser.getPassword()));
        if (sysUser.getStatus() == null) {
            sysUser.setStatus(1);
        }
        sysUser.setIsDeleted(0);
        sysUserMapper.insert(sysUser);
    }

    @Override
    public void updateUserById(Long id, SysUser sysUser) {
        checkAdminPermission();
        SysUser existUser = sysUserMapper.selectById(id);
        if (existUser == null) {
            throw new BusinessException(ResultCode.USER_NOT_FOUND);
        }
        checkDataPermissionForUser(existUser.getCollegeId());
        if (sysUser.getUsername() != null && !sysUser.getUsername().equals(existUser.getUsername())) {
            LambdaQueryWrapper<SysUser> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(SysUser::getUsername, sysUser.getUsername());
            if (sysUserMapper.selectCount(wrapper) > 0) {
                throw new BusinessException(ResultCode.USER_ALREADY_EXISTS);
            }
        }
        sysUser.setId(id);
        if (sysUser.getPassword() != null && !sysUser.getPassword().isEmpty()) {
            sysUser.setPassword(BCrypt.hashpw(sysUser.getPassword()));
        } else {
            sysUser.setPassword(null);
        }
        sysUserMapper.updateById(sysUser);
    }

    @Override
    public void deleteUser(Long id) {
        checkAdminPermission();
        SysUser user = sysUserMapper.selectById(id);
        if (user == null) {
            throw new BusinessException(ResultCode.USER_NOT_FOUND);
        }
        checkDataPermissionForUser(user.getCollegeId());
        sysUserMapper.deleteById(id);
    }

    private void checkDataPermissionForUser(Long targetCollegeId) {
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

    @Override
    public void updateUser(UserUpdateDTO dto) {
        Long userId = StpUtil.getLoginIdAsLong();
        SysUser user = sysUserMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException(ResultCode.USER_NOT_FOUND);
        }
        if (StringUtils.hasText(dto.getRealName())) {
            user.setRealName(dto.getRealName());
        }
        if (dto.getPhone() != null) {
            user.setPhone(dto.getPhone());
        }
        if (dto.getEmail() != null) {
            user.setEmail(dto.getEmail());
        }
        sysUserMapper.updateById(user);
    }

    @Override
    public void changePassword(ChangePasswordDTO dto) {
        Long userId = StpUtil.getLoginIdAsLong();
        SysUser user = sysUserMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException(ResultCode.USER_NOT_FOUND);
        }
        if (!BCrypt.checkpw(dto.getOldPassword(), user.getPassword())) {
            throw new BusinessException("旧密码错误");
        }
        user.setPassword(BCrypt.hashpw(dto.getNewPassword()));
        sysUserMapper.updateById(user);
        StpUtil.logout();
    }

    @Override
    public void adminResetPassword(Long id, AdminResetPasswordDTO dto) {
        checkAdminPermission();
        SysUser user = sysUserMapper.selectById(id);
        if (user == null) {
            throw new BusinessException(ResultCode.USER_NOT_FOUND);
        }
        checkDataPermissionForUser(user.getCollegeId());
        user.setPassword(BCrypt.hashpw(dto.getNewPassword()));
        sysUserMapper.updateById(user);
    }

    @Override
    public UserVO getCurrentUser() {
        Long userId = StpUtil.getLoginIdAsLong();
        SysUser user = sysUserMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException(ResultCode.USER_NOT_FOUND);
        }
        return convertToVO(user);
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

    private UserVO convertToVO(SysUser user) {
        UserVO vo = new UserVO();
        BeanUtils.copyProperties(user, vo);
        vo.setRoleName(RoleConstants.getRoleName(user.getRole()));
        if (user.getCollegeId() != null) {
            College college = collegeMapper.selectById(user.getCollegeId());
            if (college != null) {
                vo.setCollegeName(college.getName());
            }
        }
        return vo;
    }
}
