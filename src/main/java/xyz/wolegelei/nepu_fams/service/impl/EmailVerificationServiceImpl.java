package xyz.wolegelei.nepu_fams.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import xyz.wolegelei.nepu_fams.common.BusinessException;
import xyz.wolegelei.nepu_fams.entity.EmailVerificationCode;
import xyz.wolegelei.nepu_fams.mapper.EmailVerificationCodeMapper;
import xyz.wolegelei.nepu_fams.service.EmailVerificationService;

import java.time.LocalDateTime;
import java.util.Random;

@Service
@RequiredArgsConstructor
public class EmailVerificationServiceImpl implements EmailVerificationService {

    private static final String SCENE_REGISTER = "REGISTER";

    private final EmailVerificationCodeMapper emailVerificationCodeMapper;
    private final JavaMailSender mailSender;

    @Value("${spring.mail.username:}")
    private String mailUsername;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void sendRegisterCode(String email) {
        if (!StringUtils.hasText(mailUsername)) {
            throw new BusinessException("邮箱服务未配置，请先配置 MAIL_HOST、MAIL_USERNAME、MAIL_PASSWORD");
        }

        EmailVerificationCode lastCode = emailVerificationCodeMapper.selectOne(new LambdaQueryWrapper<EmailVerificationCode>()
                .eq(EmailVerificationCode::getEmail, email)
                .eq(EmailVerificationCode::getScene, SCENE_REGISTER)
                .orderByDesc(EmailVerificationCode::getCreateTime)
                .last("LIMIT 1"));
        if (lastCode != null && lastCode.getCreateTime().plusSeconds(60).isAfter(LocalDateTime.now())) {
            throw new BusinessException("验证码发送过于频繁，请稍后再试");
        }

        String code = String.format("%06d", new Random().nextInt(1_000_000));
        EmailVerificationCode record = new EmailVerificationCode();
        record.setEmail(email);
        record.setCode(code);
        record.setScene(SCENE_REGISTER);
        record.setExpireTime(LocalDateTime.now().plusMinutes(5));
        record.setUsed(0);
        record.setCreateTime(LocalDateTime.now());
        emailVerificationCodeMapper.insert(record);

        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(mailUsername);
        message.setTo(email);
        message.setSubject("NEPU-FAMS 注册验证码");
        message.setText("你的注册验证码是：" + code + "，5分钟内有效。若非本人操作，请忽略此邮件。");
        mailSender.send(message);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void verifyRegisterCode(String email, String code) {
        EmailVerificationCode record = emailVerificationCodeMapper.selectOne(new LambdaQueryWrapper<EmailVerificationCode>()
                .eq(EmailVerificationCode::getEmail, email)
                .eq(EmailVerificationCode::getCode, code)
                .eq(EmailVerificationCode::getScene, SCENE_REGISTER)
                .eq(EmailVerificationCode::getUsed, 0)
                .orderByDesc(EmailVerificationCode::getCreateTime)
                .last("LIMIT 1"));
        if (record == null) {
            throw new BusinessException("邮箱验证码错误");
        }
        if (record.getExpireTime().isBefore(LocalDateTime.now())) {
            throw new BusinessException("邮箱验证码已过期");
        }
        record.setUsed(1);
        emailVerificationCodeMapper.updateById(record);
    }
}
