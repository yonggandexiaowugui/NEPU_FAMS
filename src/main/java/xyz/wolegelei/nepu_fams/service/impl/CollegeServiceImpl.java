package xyz.wolegelei.nepu_fams.service.impl;

import cn.dev33.satoken.stp.StpUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import xyz.wolegelei.nepu_fams.common.BusinessException;
import xyz.wolegelei.nepu_fams.common.ResultCode;
import xyz.wolegelei.nepu_fams.common.RoleConstants;
import xyz.wolegelei.nepu_fams.entity.College;
import xyz.wolegelei.nepu_fams.entity.SysUser;
import xyz.wolegelei.nepu_fams.mapper.CollegeMapper;
import xyz.wolegelei.nepu_fams.mapper.SysUserMapper;
import xyz.wolegelei.nepu_fams.service.CollegeService;
import xyz.wolegelei.nepu_fams.vo.CollegeVO;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CollegeServiceImpl implements CollegeService {

    private final CollegeMapper collegeMapper;
    private final SysUserMapper sysUserMapper;

    @Override
    public List<CollegeVO> listAll() {
        List<College> colleges = collegeMapper.selectList(null);
        return colleges.stream().map(this::convertToVO).collect(Collectors.toList());
    }

    @Override
    public CollegeVO getById(Long id) {
        College college = collegeMapper.selectById(id);
        if (college == null) {
            throw new BusinessException(ResultCode.COLLEGE_NOT_FOUND);
        }
        return convertToVO(college);
    }

    @Override
    public void addCollege(College college) {
        checkSuperAdmin();
        LambdaQueryWrapper<College> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(College::getCode, college.getCode());
        if (collegeMapper.selectCount(wrapper) > 0) {
            throw new BusinessException(ResultCode.COLLEGE_CODE_EXISTS);
        }
        collegeMapper.insert(college);
    }

    @Override
    public void updateCollege(Long id, College college) {
        checkSuperAdmin();
        College exist = collegeMapper.selectById(id);
        if (exist == null) {
            throw new BusinessException(ResultCode.COLLEGE_NOT_FOUND);
        }
        if (!exist.getCode().equals(college.getCode())) {
            LambdaQueryWrapper<College> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(College::getCode, college.getCode());
            if (collegeMapper.selectCount(wrapper) > 0) {
                throw new BusinessException(ResultCode.COLLEGE_CODE_EXISTS);
            }
        }
        college.setId(id);
        collegeMapper.updateById(college);
    }

    @Override
    public void deleteCollege(Long id) {
        checkSuperAdmin();
        College college = collegeMapper.selectById(id);
        if (college == null) {
            throw new BusinessException(ResultCode.COLLEGE_NOT_FOUND);
        }
        LambdaQueryWrapper<SysUser> userWrapper = new LambdaQueryWrapper<>();
        userWrapper.eq(SysUser::getCollegeId, id);
        if (sysUserMapper.selectCount(userWrapper) > 0) {
            throw new BusinessException("该学院下存在用户，无法删除");
        }
        collegeMapper.deleteById(id);
    }

    private void checkSuperAdmin() {
        Long userId = StpUtil.getLoginIdAsLong();
        SysUser user = sysUserMapper.selectById(userId);
        if (user == null || !RoleConstants.SUPER_ADMIN.equals(user.getRole())) {
            throw new BusinessException(ResultCode.FORBIDDEN);
        }
    }

    private CollegeVO convertToVO(College college) {
        CollegeVO vo = new CollegeVO();
        BeanUtils.copyProperties(college, vo);
        return vo;
    }
}
