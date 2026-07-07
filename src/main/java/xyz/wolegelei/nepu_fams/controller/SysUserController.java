package xyz.wolegelei.nepu_fams.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import xyz.wolegelei.nepu_fams.annotation.OperationLog;
import xyz.wolegelei.nepu_fams.common.PageResult;
import xyz.wolegelei.nepu_fams.common.Result;
import xyz.wolegelei.nepu_fams.dto.AdminResetPasswordDTO;
import xyz.wolegelei.nepu_fams.dto.ChangePasswordDTO;
import xyz.wolegelei.nepu_fams.dto.UserPageQueryDTO;
import xyz.wolegelei.nepu_fams.dto.UserUpdateDTO;
import xyz.wolegelei.nepu_fams.entity.SysUser;
import xyz.wolegelei.nepu_fams.service.SysUserService;
import xyz.wolegelei.nepu_fams.vo.UserVO;

@RestController
@RequestMapping("/api/user")
@RequiredArgsConstructor
public class SysUserController {

    private final SysUserService sysUserService;

    @GetMapping("/info")
    public Result<UserVO> getCurrentUser() {
        UserVO userVO = sysUserService.getCurrentUser();
        return Result.success(userVO);
    }

    @PutMapping("/info")
    public Result<Void> updateUser(@Valid @RequestBody UserUpdateDTO dto) {
        sysUserService.updateUser(dto);
        return Result.success();
    }

    @PutMapping("/password")
    public Result<Void> changePassword(@Valid @RequestBody ChangePasswordDTO dto) {
        sysUserService.changePassword(dto);
        return Result.success();
    }

    @GetMapping("/list")
    public Result<PageResult<UserVO>> pageUser(UserPageQueryDTO dto) {
        IPage<UserVO> page = sysUserService.pageUser(dto);
        return Result.success(PageResult.of(page));
    }

    @OperationLog(value = "管理员重置用户密码", type = "UPDATE")
    @PutMapping("/{id}/password")
    public Result<Void> resetPassword(@PathVariable Long id, @Valid @RequestBody AdminResetPasswordDTO dto) {
        sysUserService.adminResetPassword(id, dto);
        return Result.success();
    }

    @GetMapping("/{id}")
    public Result<UserVO> getUserById(@PathVariable Long id) {
        UserVO userVO = sysUserService.getUserById(id);
        return Result.success(userVO);
    }

    @OperationLog(value = "新增用户", type = "ADD")
    @PostMapping
    public Result<Void> addUser(@RequestBody SysUser sysUser) {
        sysUserService.addUser(sysUser);
        return Result.success();
    }

    @OperationLog(value = "编辑用户", type = "UPDATE")
    @PutMapping("/{id}")
    public Result<Void> updateUserById(@PathVariable Long id, @RequestBody SysUser sysUser) {
        sysUserService.updateUserById(id, sysUser);
        return Result.success();
    }

    @OperationLog(value = "删除用户", type = "DELETE")
    @DeleteMapping("/{id}")
    public Result<Void> deleteUser(@PathVariable Long id) {
        sysUserService.deleteUser(id);
        return Result.success();
    }
}
