package xyz.wolegelei.nepu_fams.service;

public interface EmailVerificationService {

    void sendRegisterCode(String email);

    void verifyRegisterCode(String email, String code);
}
