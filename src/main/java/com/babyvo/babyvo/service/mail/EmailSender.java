package com.babyvo.babyvo.service.mail;

public interface EmailSender {
    void sendOtp(String toEmail, String otp, int expiresMinutes);
    void sendInvite(
            String toEmail,
            String babyName,
            String inviteToken,
            long expiresHours
    );
}
