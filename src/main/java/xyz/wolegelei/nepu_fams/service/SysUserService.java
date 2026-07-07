package xyz.wolegelei.nepu_fams.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import xyz.wolegelei.nepu_fams.dto.AdminResetPasswordDTO;
import xyz.wolegelei.nepu_fams.dto.ChangePasswordDTO;
import xyz.wolegelei.nepu_fams.dto.LoginDTO;
import xyz.wolegelei.nepu_fams.dto.RegisterDTO;
import xyz.wolegelei.nepu_fams.dto.UserPageQueryDTO;
import xyz.wolegelei.nepu_fams.dto.UserUpdateDTO;
import xyz.wolegelei.nepu_fams.entity.SysUser;
import xyz.wolegelei.nepu_fams.vo.LoginVO;
import xyz.wolegelei.nepu_fams.vo.UserVO;

public interface SysUserService {

    void register(RegisterDTO registerDTO);

    LoginVO login(LoginDTO loginDTO);

    UserVO getUserById(Long id);

    IPage<UserVO> pageUser(UserPageQueryDTO dto);

    void addUser(SysUser sysUser);

    void updateUserById(Long id, SysUser sysUser);

    void deleteUser(Long id);

    void updateUser(UserUpdateDTO dto);

    void changePassword(ChangePasswordDTO dto);

    void adminResetPassword(Long id, AdminResetPasswordDTO dto);

    UserVO getCurrentUser();
}
