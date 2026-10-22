package com.daily.app.backend.service;

import com.daily.app.backend.entity.OtpPurpose;
import com.daily.app.backend.exception.ApiException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

/**
 * Sends OTP emails over SMTP (any free provider — Gmail app password, Brevo, …).
 * When MAIL_HOST or MAIL_USERNAME is not configured (local dev) the code is logged instead, so
 * signup and forgot-password can be tested without an email account.
 */
@Component
public class OtpMailer {

    private static final Logger log = LoggerFactory.getLogger(OtpMailer.class);

    private final ObjectProvider<JavaMailSender> mailSender;
    private final String mailHost;
    private final String mailUsername;
    private final String from;

    public OtpMailer(ObjectProvider<JavaMailSender> mailSender,
                     @Value("${spring.mail.host:}") String mailHost,
                     @Value("${spring.mail.username:}") String mailUsername,
                     @Value("${app.mail.from:}") String from) {
        this.mailSender = mailSender;
        this.mailHost = mailHost;
        this.mailUsername = mailUsername;
        this.from = from;
    }

    public void send(String email, String code, OtpPurpose purpose, long validMinutes) {
        if (!StringUtils.hasText(mailHost) || !StringUtils.hasText(mailUsername)) {
            log.warn("MAIL_HOST/MAIL_USERNAME not set — not emailing. {} OTP for {} is {}", purpose, email, code);
            return;
        }
        SimpleMailMessage message = new SimpleMailMessage();
        if (StringUtils.hasText(from)) {
            message.setFrom(from);
        }
        message.setTo(email);
        message.setSubject(code + " is your Daily " + label(purpose) + " code");
        message.setText("""
                Your Daily %s code is: %s

                It expires in %d minutes. If you didn't request this, you can ignore this email.
                Never share this code with anyone.
                """.formatted(label(purpose), code, validMinutes));
        try {
            mailSender.getObject().send(message);
        } catch (MailException e) {
            log.error("Failed to send {} OTP email", purpose, e);
            throw new ApiException(HttpStatus.SERVICE_UNAVAILABLE, "Couldn't send the email right now. Please try again.");
        }
    }

    private static String label(OtpPurpose purpose) {
        return switch (purpose) {
            case SIGNUP, EMAIL_CHANGE -> "verification";
            case PASSWORD_RESET -> "password reset";
        };
    }
}
