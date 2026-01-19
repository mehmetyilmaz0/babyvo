package com.babyvo.babyvo.service.mail;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class SmtpEmailSender implements EmailSender {

    private final JavaMailSender mailSender;

    @Value("${spring.mail.username}")
    private String from;

    @Override
    public void sendOtp(String toEmail, String otp, int expiresMinutes) {
        SimpleMailMessage msg = new SimpleMailMessage();
        msg.setFrom(from);
        msg.setTo(toEmail);
        msg.setSubject("BabyVo - Login Code");
        msg.setText("""
                Your BabyVo login code is: %s

                This code expires in %d minutes.
                """.formatted(otp, expiresMinutes));

        mailSender.send(msg);
    }
}