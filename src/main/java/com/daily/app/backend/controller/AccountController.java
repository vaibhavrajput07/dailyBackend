package com.daily.app.backend.controller;

import com.daily.app.backend.dto.account.AccountResponse;
import com.daily.app.backend.dto.account.EmailOtpRequest;
import com.daily.app.backend.dto.account.UpdateEmailRequest;
import com.daily.app.backend.dto.auth.OtpSentResponse;
import com.daily.app.backend.security.AuthPrincipal;
import com.daily.app.backend.service.AccountService;
import jakarta.validation.Valid;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/account")
public class AccountController {

    private final AccountService accountService;

    public AccountController(AccountService accountService) {
        this.accountService = accountService;
    }

    @GetMapping
    public AccountResponse get(@AuthenticationPrincipal AuthPrincipal principal) {
        return accountService.get(principal.userId());
    }

    @PostMapping("/email/otp")
    public OtpSentResponse sendEmailOtp(@AuthenticationPrincipal AuthPrincipal principal,
                                        @Valid @RequestBody EmailOtpRequest request) {
        return accountService.sendEmailOtp(principal.userId(), request);
    }

    @PutMapping("/email")
    public AccountResponse updateEmail(@AuthenticationPrincipal AuthPrincipal principal,
                                       @Valid @RequestBody UpdateEmailRequest request) {
        return accountService.updateEmail(principal.userId(), request);
    }
}
