package com.daily.app.backend.service;

import com.daily.app.backend.dto.account.AccountResponse;
import com.daily.app.backend.dto.account.EmailOtpRequest;
import com.daily.app.backend.dto.account.UpdateEmailRequest;
import com.daily.app.backend.entity.OtpPurpose;
import com.daily.app.backend.entity.User;
import com.daily.app.backend.exception.ApiException;
import com.daily.app.backend.exception.DuplicateResourceException;
import com.daily.app.backend.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class AccountServiceTest {

    private static final long USER_ID = 7L;

    private final UserRepository userRepository = mock(UserRepository.class);
    private final OtpService otpService = mock(OtpService.class);
    private final AccountService service = new AccountService(userRepository, otpService);
    private final User user = User.builder().id(USER_ID).ownerName("Ravi").mobileNumber("9876543210").build();

    @Test
    void addsEmailOnlyAfterTheCodeSentToItIsVerified() {
        when(userRepository.findById(USER_ID)).thenReturn(Optional.of(user));
        when(userRepository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));

        AccountResponse response = service.updateEmail(USER_ID, new UpdateEmailRequest(" Ravi@Gmail.com ", "123456"));

        verify(otpService).verify("ravi@gmail.com", OtpPurpose.EMAIL_CHANGE, "123456");
        assertThat(response.email()).isEqualTo("ravi@gmail.com");
    }

    @Test
    void wrongCodeLeavesEmailUnchanged() {
        when(userRepository.findById(USER_ID)).thenReturn(Optional.of(user));
        doThrow(new ApiException(HttpStatus.BAD_REQUEST, "Incorrect code"))
                .when(otpService).verify("ravi@gmail.com", OtpPurpose.EMAIL_CHANGE, "000000");

        assertThatThrownBy(() -> service.updateEmail(USER_ID, new UpdateEmailRequest("ravi@gmail.com", "000000")))
                .hasMessage("Incorrect code");
        assertThat(user.getEmail()).isNull();
        verify(userRepository, never()).save(any());
    }

    @Test
    void refusesAnEmailAlreadyUsedByAnotherAccount() {
        when(userRepository.findById(USER_ID)).thenReturn(Optional.of(user));
        when(userRepository.existsByEmail("taken@gmail.com")).thenReturn(true);

        assertThatThrownBy(() -> service.sendEmailOtp(USER_ID, new EmailOtpRequest("taken@gmail.com")))
                .isInstanceOf(DuplicateResourceException.class);
        verify(otpService, never()).send(any(), any());
    }
}
