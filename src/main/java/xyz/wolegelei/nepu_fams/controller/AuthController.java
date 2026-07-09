package xyz.wolegelei.nepu_fams.controller;

import cn.dev33.satoken.stp.StpUtil;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import xyz.wolegelei.nepu_fams.annotation.OperationLog;
import xyz.wolegelei.nepu_fams.common.OperationType;
import xyz.wolegelei.nepu_fams.common.Result;
import xyz.wolegelei.nepu_fams.dto.EmailCodeDTO;
import xyz.wolegelei.nepu_fams.dto.LoginDTO;
import xyz.wolegelei.nepu_fams.dto.RegisterDTO;
import xyz.wolegelei.nepu_fams.service.EmailVerificationService;
import xyz.wolegelei.nepu_fams.service.SysUserService;
import xyz.wolegelei.nepu_fams.vo.LoginVO;
import xyz.wolegelei.nepu_fams.vo.UserVO;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final SysUserService sysUserService;
    private final EmailVerificationService emailVerificationService;

    @OperationLog(value = "用户登录", type = "LOGIN", recordParams = false)
    @PostMapping("/login")
    public Result<LoginVO> login(@Valid @RequestBody LoginDTO loginDTO) {
        LoginVO loginVO = sysUserService.login(loginDTO);
        return Result.success(loginVO);
    }

    @PostMapping("/email-code")
    public Result<Void> sendEmailCode(@Valid @RequestBody EmailCodeDTO dto) {
        emailVerificationService.sendRegisterCode(dto.getEmail());
        return Result.success();
    }

    @PostMapping("/register")
    public Result<Void> register(@Valid @RequestBody RegisterDTO registerDTO) {
        sysUserService.register(registerDTO);
        return Result.success();
    }

    @OperationLog(value = "用户登出", type = "LOGOUT")
    @PostMapping("/logout")
    public Result<Void> logout() {
        StpUtil.logout();
        return Result.success();
    }

    @GetMapping("/userInfo")
    public Result<UserVO> getCurrentUser() {
        UserVO userVO = sysUserService.getCurrentUser();
        return Result.success(userVO);
    }
}
