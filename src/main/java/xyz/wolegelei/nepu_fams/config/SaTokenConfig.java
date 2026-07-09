package xyz.wolegelei.nepu_fams.config;

import cn.dev33.satoken.interceptor.SaInterceptor;
import cn.dev33.satoken.stp.StpInterface;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import xyz.wolegelei.nepu_fams.entity.SysUser;
import xyz.wolegelei.nepu_fams.mapper.SysUserMapper;

import java.util.ArrayList;
import java.util.List;

@Configuration
@RequiredArgsConstructor
public class SaTokenConfig implements WebMvcConfigurer {

    private final SysUserMapper sysUserMapper;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(new SaInterceptor(handle -> {
        }))
                .addPathPatterns("/**")
                .excludePathPatterns(
                        "/api/auth/login",
                        "/api/auth/register",
                        "/api/auth/email-code",
                        "/api/auth/logout",
                        "/error",
                        "/favicon.ico",
                        "/spring/ai/loom/**",
                        "/spring/ai/loom"
                );
    }

    @Bean
    public StpInterface stpInterface() {
        return new StpInterface() {
            @Override
            public List<String> getPermissionList(Object loginId, String loginType) {
                return new ArrayList<>();
            }

            @Override
            public List<String> getRoleList(Object loginId, String loginType) {
                List<String> roles = new ArrayList<>();
                if (loginId != null) {
                    Long userId = Long.valueOf(loginId.toString());
                    SysUser user = sysUserMapper.selectById(userId);
                    if (user != null && user.getRole() != null) {
                        roles.add(user.getRole());
                    }
                }
                return roles;
            }
        };
    }
}
