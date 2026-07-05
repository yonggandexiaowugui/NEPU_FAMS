package xyz.wolegelei.nepu_fams.aspect;

import cn.dev33.satoken.stp.StpUtil;
import cn.hutool.json.JSONUtil;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import xyz.wolegelei.nepu_fams.annotation.OperationLog;
import xyz.wolegelei.nepu_fams.entity.SysUser;
import xyz.wolegelei.nepu_fams.mapper.SysUserMapper;
import xyz.wolegelei.nepu_fams.service.OperationLogService;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;

@Aspect
@Component
@RequiredArgsConstructor
public class OperationLogAspect {

    private static final List<String> SENSITIVE_FIELDS = Arrays.asList("password", "oldPassword", "newPassword");

    private final OperationLogService operationLogService;

    private final SysUserMapper sysUserMapper;

    @Around("@annotation(xyz.wolegelei.nepu_fams.annotation.OperationLog)")
    public Object around(ProceedingJoinPoint joinPoint) throws Throwable {
        Object result = joinPoint.proceed();
        try {
            saveLog(joinPoint);
        } catch (Exception e) {
            // 日志记录失败不影响业务
        }
        return result;
    }

    private void saveLog(ProceedingJoinPoint joinPoint) {
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Method method = signature.getMethod();
        OperationLog annotation = method.getAnnotation(OperationLog.class);
        if (annotation == null) {
            return;
        }

        xyz.wolegelei.nepu_fams.entity.OperationLog log = new xyz.wolegelei.nepu_fams.entity.OperationLog();
        log.setOperation(annotation.value());
        log.setType(annotation.type());
        log.setMethod(method.getDeclaringClass().getName() + "." + method.getName());

        if (annotation.recordParams()) {
            Object[] args = joinPoint.getArgs();
            String[] paramNames = signature.getParameterNames();
            if (args != null && args.length > 0 && paramNames != null && paramNames.length > 0) {
                StringBuilder paramsBuilder = new StringBuilder("{");
                for (int i = 0; i < args.length; i++) {
                    if (i > 0) {
                        paramsBuilder.append(", ");
                    }
                    String paramName = paramNames[i];
                    Object arg = args[i];
                    if (SENSITIVE_FIELDS.contains(paramName)) {
                        paramsBuilder.append("\"").append(paramName).append("\": \"******\"");
                    } else if (isSimpleType(arg)) {
                        paramsBuilder.append("\"").append(paramName).append("\": ").append(JSONUtil.toJsonStr(arg));
                    } else if (arg != null && !isExcludedType(arg)) {
                        paramsBuilder.append("\"").append(paramName).append("\": ").append(JSONUtil.toJsonStr(arg));
                    }
                }
                paramsBuilder.append("}");
                log.setParams(paramsBuilder.toString());
            }
        }

        try {
            Long userId = StpUtil.getLoginIdAsLong();
            log.setUserId(userId);
            String username = (String) StpUtil.getSession().get("username");
            if (username == null || username.isEmpty()) {
                SysUser user = sysUserMapper.selectById(userId);
                if (user != null) {
                    username = user.getUsername();
                    StpUtil.getSession().set("username", username);
                }
            }
            log.setUsername(username);
        } catch (Exception e) {
            // 未登录的情况，如登录接口
        }

        HttpServletRequest request = getRequest();
        if (request != null) {
            log.setIp(getClientIp(request));
        }

        operationLogService.add(log);
    }

    private HttpServletRequest getRequest() {
        try {
            ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            if (attributes != null) {
                return attributes.getRequest();
            }
        } catch (Exception e) {
            // ignore
        }
        return null;
    }

    private String getClientIp(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("Proxy-Client-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("WL-Proxy-Client-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("X-Real-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        if (ip != null && ip.contains(",")) {
            ip = ip.split(",")[0].trim();
        }
        return ip;
    }

    private boolean isSimpleType(Object obj) {
        if (obj == null) {
            return true;
        }
        return obj instanceof String || obj instanceof Number || obj instanceof Boolean
                || obj instanceof Character || obj.getClass().isPrimitive();
    }

    private boolean isExcludedType(Object obj) {
        if (obj == null) {
            return true;
        }
        return obj instanceof jakarta.servlet.ServletRequest
                || obj instanceof jakarta.servlet.ServletResponse
                || obj instanceof org.springframework.web.multipart.MultipartFile;
    }
}
