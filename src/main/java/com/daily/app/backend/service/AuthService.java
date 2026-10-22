package com.daily.app.backend.service;

import com.daily.app.backend.dto.auth.AuthResponse;
import com.daily.app.backend.dto.auth.ForgotPasswordRequest;
import com.daily.app.backend.dto.auth.LoginRequest;
import com.daily.app.backend.dto.auth.OtpSentResponse;
import com.daily.app.backend.dto.auth.ResetPasswordRequest;
import com.daily.app.backend.dto.auth.SignupOtpRequest;
import com.daily.app.backend.dto.auth.SignupRequest;
import com.daily.app.backend.entity.Business;
import com.daily.app.backend.entity.OtpPurpose;
import com.daily.app.backend.entity.User;
import com.daily.app.backend.exception.ApiException;
import com.daily.app.backend.exception.DuplicateResourceException;
import com.daily.app.backend.exception.ResourceNotFoundException;
import com.daily.app.backend.mapper.BusinessMapper;
import com.daily.app.backend.mapper.UserMapper;
import com.daily.app.backend.repository.BusinessRepository;
import com.daily.app.backend.repository.UserRepository;
import com.daily.app.backend.security.JwtService;
import com.daily.app.backend.security.SecurityUserDetails;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final BusinessRepository businessRepository;
    private final UserMapper userMapper;
    private final BusinessMapper businessMapper;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;
    private final OtpService otpService;

    public AuthService(UserRepository userRepository,
                        BusinessRepository businessRepository,
                        UserMapper userMapper,
                        BusinessMapper businessMapper,
                        PasswordEncoder passwordEncoder,
                        JwtService jwtService,
                        AuthenticationManager authenticationManager,
                        OtpService otpService) {
        this.userRepository = userRepository;
        this.businessRepository = businessRepository;
        this.userMapper = userMapper;
        this.businessMapper = businessMapper;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
        this.authenticationManager = authenticationManager;
        this.otpService = otpService;
    }

    /** Signup step 1: fail fast on a taken mobile/email, then email a verification code. */
    public OtpSentResponse sendSignupOtp(SignupOtpRequest request) {
        String email = request.normalizedEmail();
        ensureAvailable(request.mobileNumber(), email);
        otpService.send(email, OtpPurpose.SIGNUP);
        return otpSent(email);
    }

    @Transactional
    public AuthResponse signup(SignupRequest request) {
        String email = request.normalizedEmail();
        ensureAvailable(request.mobileNumber(), email);
        otpService.verify(email, OtpPurpose.SIGNUP, request.otp());

        User user = userMapper.toEntity(request, passwordEncoder.encode(request.password()));
        User savedUser = userRepository.save(user);

        Business business = businessMapper.toEntity(request, savedUser);
        Business savedBusiness = businessRepository.save(business);

        String token = jwtService.generateToken(savedUser.getId(), savedBusiness.getId(), savedUser.getMobileNumber());

        return new AuthResponse(token, savedUser.getId(), savedBusiness.getId(),
                savedBusiness.getBusinessName(), savedUser.getOwnerName());
    }

    public AuthResponse login(LoginRequest request) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.mobileNumber(), request.password()));

        SecurityUserDetails principal = (SecurityUserDetails) authentication.getPrincipal();
        return issueToken(principal.getUser());
    }

    /** Forgot password step 1: email a reset code to the address saved on the account. */
    public OtpSentResponse sendPasswordResetOtp(ForgotPasswordRequest request) {
        String email = resetEmailOf(findByMobile(request.mobileNumber()));
        otpService.send(email, OtpPurpose.PASSWORD_RESET);
        return otpSent(email);
    }

    /** Forgot password step 2: check the code, set the new password, and sign straight in. */
    @Transactional
    public AuthResponse resetPassword(ResetPasswordRequest request) {
        User user = findByMobile(request.mobileNumber());
        otpService.verify(resetEmailOf(user), OtpPurpose.PASSWORD_RESET, request.otp());
        user.setPasswordHash(passwordEncoder.encode(request.newPassword()));
        userRepository.save(user);
        return issueToken(user);
    }

    private AuthResponse issueToken(User user) {
        // The token's business is the default scope for requests without an
        // X-Business-Id header — the oldest active business keeps legacy
        // single-business clients pointed at the business they always used.
        Business business = businessRepository.findFirstByUserIdAndArchivedFalseOrderByIdAsc(user.getId())
                .or(() -> businessRepository.findFirstByUserIdOrderByIdAsc(user.getId()))
                .orElseThrow(() -> new IllegalStateException("User has no linked business: " + user.getId()));

        String token = jwtService.generateToken(user.getId(), business.getId(), user.getMobileNumber());

        return new AuthResponse(token, user.getId(), business.getId(),
                business.getBusinessName(), user.getOwnerName());
    }

    private User findByMobile(String mobileNumber) {
        return userRepository.findByMobileNumber(mobileNumber)
                .orElseThrow(() -> new ResourceNotFoundException("No account found with this mobile number"));
    }

    private static String resetEmailOf(User user) {
        if (user.getEmail() == null) {
            throw new ApiException(HttpStatus.UNPROCESSABLE_ENTITY,
                    "No email is saved on this account. Log in on a device where you're still signed in and add one "
                            + "in Settings > Recovery email, or contact support.");
        }
        return user.getEmail();
    }

    private void ensureAvailable(String mobileNumber, String email) {
        if (userRepository.existsByMobileNumber(mobileNumber)) {
            throw new DuplicateResourceException("Mobile number is already registered");
        }
        if (userRepository.existsByEmail(email)) {
            throw new DuplicateResourceException("Email is already registered");
        }
    }

    private static OtpSentResponse otpSent(String email) {
        return new OtpSentResponse(OtpService.mask(email),
                OtpService.VALIDITY.toSeconds(), OtpService.RESEND_COOLDOWN.toSeconds());
    }
}
