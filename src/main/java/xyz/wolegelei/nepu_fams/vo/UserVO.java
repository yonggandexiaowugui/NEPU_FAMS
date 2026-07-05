package xyz.wolegelei.nepu_fams.vo;

import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
public class UserVO implements Serializable {

    private Long id;

    private String username;

    private String realName;

    private String role;

    private String roleName;

    private Long collegeId;

    private String collegeName;

    private String phone;

    private String email;

    private String avatar;

    private Integer status;

    private LocalDateTime createTime;
}
