package xyz.wolegelei.nepu_fams.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import java.io.Serializable;
import lombok.Data;

@Data
public class RegisterDTO implements Serializable {

    @NotBlank(message = "用户名不能为空")
    private String username;

    @NotBlank(message = "密码不能为空")
    private String password;

    @NotBlank(message = "真实姓名不能为空")
    private String realName;

    @Pattern(regexp = "^(|1[3-9]\\d{9})$", message = "手机号格式不正确")
    private String phone;

    @Pattern(regexp = "^(|[\\w.-]+@[\\w.-]+\\.[a-zA-Z]{2,})$", message = "邮箱格式不正确")
    private String email;

    @NotNull(message = "学院不能为空")
    private Long collegeId;

    private String role = "USER";
}