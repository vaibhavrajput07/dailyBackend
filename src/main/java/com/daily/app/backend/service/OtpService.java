package com.daily.app.backend.service;

import com.daily.app.backend.entity.OtpCode;
import com.daily.app.backend.entity.OtpPurpose;
import com.daily.app.backend.exception.ApiException;
import com.daily.app.backend.repository.OtpCodeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;

/**
 * Issues and checks 6-digit email OTPs. Only a BCrypt hash of each code is stored.
 * A code is single-use, expires after {@link #VALIDITY}, dies after {@link #MAX_ATTEMPTS}
 * wrong guesses, and is replaced by any newer code for the same email + purpose.
 */
@Service
public class OtpService {

    static final Duration VALIDITY = Duration.ofMinutes(10);
    static final Duration RESEND_COOLDOWN = Duration.ofSeconds(60);
    static final int MAX_ATTEMPTS = 5;

    private final OtpCodeRepository otpCodeRepository;
    private final PasswordEncoder passwordEncoder;
    private final OtpMailer otpMailer;
    private final Clock clock;
    private final SecureRandom random = new SecureRandom();

    @Autowired
    public OtpService(OtpCodeRepository otpCodeRepository, PasswordEncoder passwordEncoder, OtpMailer otpMailer) {
        this(otpCodeRepository, passwordEncoder, otpMailer, Clock.systemUTC());
    }

    OtpService(OtpCodeRepository otpCodeRepository, PasswordEncoder passwordEncoder, OtpMailer otpMailer, Clock clock) {
        this.otpCodeRepository = otpCodeRepository;
        this.passwordEncoder = passwordEncoder;
        this.otpMailer = otpMailer;
        this.clock = clock;
    }

    @Transactional
    public void send(String email, OtpPurpose purpose) {
        Instant now = clock.instant();
        otpCodeRepository.findFirstByEmailAndPurposeOrderByCreatedAtDescIdDesc(email, purpose).ifPresent(latest -> {
            Duration sinceLast = Duration.between(latest.getCreatedAt(), now);
            if (sinceLast.compareTo(RESEND_COOLDOWN) < 0) {
                long wait = RESEND_COOLDOWN.minus(sinceLast).toSeconds() + 1;
                throw new ApiException(HttpStatus.TOO_MANY_REQUESTS,
                        "Please wait " + wait + " seconds before requesting another code");
            }
        });

        String code = "%06d".formatted(random.nextInt(1_000_000));
        otpCodeRepository.save(OtpCode.builder()
                .email(email)
                .purpose(purpose)
                .codeHash(passwordEncoder.encode(code))
                .expiresAt(now.plus(VALIDITY))
                .createdAt(now)
                .build());
        otpMailer.send(email, code, purpose, VALIDITY.toMinutes());
    }

    /**
     * Consumes the latest code for {@code email} + {@code purpose} if {@code code} matches.
     * Runs in its own transaction so a wrong guess is counted even though the caller's
     * transaction rolls back on the resulting exception.
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW, noRollbackFor = ApiException.class)
    public void verify(String email, OtpPurpose purpose, String code) {
        OtpCode otp = otpCodeRepository.findFirstByEmailAndPurposeOrderByCreatedAtDescIdDesc(email, purpose)
                .filter(o -> !o.isConsumed())
                .orElseThrow(() -> invalid("No active code — please request a new one"));

        if (clock.instant().isAfter(otp.getExpiresAt())) {
            throw invalid("This code has expired — please request a new one");
        }
        if (otp.getAttempts() >= MAX_ATTEMPTS) {
            throw invalid("Too many wrong attempts — please request a new code");
        }
        if (!passwordEncoder.matches(code, otp.getCodeHash())) {
            otp.setAttempts(otp.getAttempts() + 1);
            otpCodeRepository.save(otp);
            int left = MAX_ATTEMPTS - otp.getAttempts();
            throw invalid(left > 0 ? "Incorrect code. " + left + " attempt" + (left == 1 ? "" : "s") + " left"
                    : "Too many wrong attempts — please request a new code");
        }
        otp.setConsumed(true);
        otpCodeRepository.save(otp);
    }

    private static ApiException invalid(String message) {
        return new ApiException(HttpStatus.BAD_REQUEST, message);
    }

    /** "rajesh@gmail.com" → "ra****@gmail.com" — enough to recognise, not enough to harvest. */
    public static String mask(String email) {
        int at = email.indexOf('@');
        if (at <= 0) {
            return "****";
        }
        String local = email.substring(0, at);
        String visible = local.length() <= 2 ? local.substring(0, 1) : local.substring(0, 2);
        return visible + "****" + email.substring(at);
    }
}
