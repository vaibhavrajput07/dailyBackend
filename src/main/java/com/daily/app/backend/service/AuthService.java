package com.daily.app.backend.service;

import com.daily.app.backend.dto.auth.AuthResponse;
import com.daily.app.backend.dto.auth.LoginRequest;
import com.daily.app.backend.dto.auth.SignupRequest;
import com.daily.app.backend.entity.Business;
import com.daily.app.backend.entity.User;
import com.daily.app.backend.exception.DuplicateResourceException;
import com.daily.app.backend.mapper.BusinessMapper;
import com.daily.app.backend.mapper.UserMapper;
import com.daily.app.backend.repository.BusinessRepository;
import com.daily.app.backend.repository.UserRepository;
import com.daily.app.backend.security.JwtService;
import com.daily.app.backend.security.SecurityUserDetails;
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

    public AuthService(UserRepository userRepository,
                        BusinessRepository businessRepository,
                        UserMapper userMapper,
                        BusinessMapper businessMapper,
                        PasswordEncoder passwordEncoder,
                        JwtService jwtService,
                        AuthenticationManager authenticationManager) {
        this.userRepository = userRepository;
        this.businessRepository = businessRepository;
        this.userMapper = userMapper;
        this.businessMapper = businessMapper;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
        this.authenticationManager = authenticationManager;
    }

    @Transactional
    public AuthResponse signup(SignupRequest request) {
        if (userRepository.existsByMobileNumber(request.mobileNumber())) {
            throw new DuplicateResourceException("Mobile number is already registered");
        }

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
        User user = principal.getUser();
        Business business = businessRepository.findByUserId(user.getId())
                .orElseThrow(() -> new IllegalStateException("User has no linked business: " + user.getId()));

        String token = jwtService.generateToken(user.getId(), business.getId(), user.getMobileNumber());

        return new AuthResponse(token, user.getId(), business.getId(),
                business.getBusinessName(), user.getOwnerName());
    }
}
