package com.daily.app.backend.service;

import com.daily.app.backend.dto.account.AccountResponse;
import com.daily.app.backend.dto.account.EmailOtpRequest;
import com.daily.app.backend.dto.account.UpdateEmailRequest;
import com.daily.app.backend.dto.auth.OtpSentResponse;
import com.daily.app.backend.entity.OtpPurpose;
import com.daily.app.backend.entity.User;
import com.daily.app.backend.exception.DuplicateResourceException;
import com.daily.app.backend.exception.ResourceNotFoundException;
import com.daily.app.backend.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * The logged-in owner's own account. Adding a recovery email here (proved by an OTP sent
 * to it) is the only way an account created before email verification can use forgot
 * password — the forgot-password flow itself never accepts a new email, since anyone who
 * knows a shop's mobile number could then take the account over.
 */
@Service
public class AccountService {

    private final UserRepository userRepository;
    private final OtpService otpService;

    public AccountService(UserRepository userRepository, OtpService otpService) {
        this.userRepository = userRepository;
        this.otpService = otpService;
    }

    public AccountResponse get(Long userId) {
        return toResponse(find(userId));
    }

    public OtpSentResponse sendEmailOtp(Long userId, EmailOtpRequest request) {
        String email = request.normalizedEmail();
        ensureEmailFree(find(userId), email);
        otpService.send(email, OtpPurpose.EMAIL_CHANGE);
        return new OtpSentResponse(OtpService.mask(email),
                OtpService.VALIDITY.toSeconds(), OtpService.RESEND_COOLDOWN.toSeconds());
    }

    @Transactional
    public AccountResponse updateEmail(Long userId, UpdateEmailRequest request) {
        User user = find(userId);
        String email = request.normalizedEmail();
        ensureEmailFree(user, email);
        otpService.verify(email, OtpPurpose.EMAIL_CHANGE, request.otp());
        user.setEmail(email);
        return toResponse(userRepository.save(user));
    }

    private void ensureEmailFree(User user, String email) {
        if (email.equals(user.getEmail())) {
            throw new DuplicateResourceException("This is already your email");
        }
        if (userRepository.existsByEmail(email)) {
            throw new DuplicateResourceException("Email is already registered to another account");
        }
    }

    private User find(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Account not found"));
    }

    private static AccountResponse toResponse(User user) {
        return new AccountResponse(user.getOwnerName(), user.getMobileNumber(), user.getEmail());
    }
}
