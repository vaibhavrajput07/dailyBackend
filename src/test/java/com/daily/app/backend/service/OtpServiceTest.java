package com.daily.app.backend.service;

import com.daily.app.backend.entity.OtpCode;
import com.daily.app.backend.entity.OtpPurpose;
import com.daily.app.backend.exception.ApiException;
import com.daily.app.backend.repository.OtpCodeRepository;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class OtpServiceTest {

    private static final String EMAIL = "rajesh@gmail.com";
    private static final Instant NOW = Instant.parse("2026-10-22T10:00:00Z");

    private final OtpCodeRepository repository = mock(OtpCodeRepository.class);
    private final PasswordEncoder encoder = new BCryptPasswordEncoder(4);
    private final OtpMailer mailer = mock(OtpMailer.class);
    private final OtpService service = new OtpService(repository, encoder, mailer, Clock.fixed(NOW, ZoneOffset.UTC));

    private OtpCode code(String plain, Instant createdAt) {
        return OtpCode.builder()
                .email(EMAIL)
                .purpose(OtpPurpose.PASSWORD_RESET)
                .codeHash(encoder.encode(plain))
                .createdAt(createdAt)
                .expiresAt(createdAt.plus(OtpService.VALIDITY))
                .build();
    }

    private void latestIs(OtpCode otp) {
        when(repository.findFirstByEmailAndPurposeOrderByCreatedAtDescIdDesc(EMAIL, OtpPurpose.PASSWORD_RESET))
                .thenReturn(Optional.ofNullable(otp));
    }

    @Test
    void sendStoresOnlyAHashAndEmailsTheSixDigitCode() {
        latestIs(null);

        service.send(EMAIL, OtpPurpose.PASSWORD_RESET);

        ArgumentCaptor<String> sentCode = ArgumentCaptor.forClass(String.class);
        verify(mailer).send(eq(EMAIL), sentCode.capture(), eq(OtpPurpose.PASSWORD_RESET), eq(10L));
        ArgumentCaptor<OtpCode> saved = ArgumentCaptor.forClass(OtpCode.class);
        verify(repository).save(saved.capture());
        assertThat(sentCode.getValue()).matches("\\d{6}");
        assertThat(saved.getValue().getCodeHash()).isNotEqualTo(sentCode.getValue());
        assertThat(encoder.matches(sentCode.getValue(), saved.getValue().getCodeHash())).isTrue();
        assertThat(saved.getValue().getExpiresAt()).isEqualTo(NOW.plus(OtpService.VALIDITY));
    }

    @Test
    void sendRefusesWithinResendCooldown() {
        latestIs(code("123456", NOW.minusSeconds(20)));

        assertThatThrownBy(() -> service.send(EMAIL, OtpPurpose.PASSWORD_RESET))
                .isInstanceOf(ApiException.class)
                .satisfies(e -> assertThat(((ApiException) e).getStatus()).isEqualTo(HttpStatus.TOO_MANY_REQUESTS));
        verify(mailer, never()).send(anyString(), anyString(), any(), anyLong());
    }

    @Test
    void verifyConsumesAMatchingCodeSoItCannotBeReused() {
        OtpCode otp = code("123456", NOW.minusSeconds(60));
        latestIs(otp);

        service.verify(EMAIL, OtpPurpose.PASSWORD_RESET, "123456");

        assertThat(otp.isConsumed()).isTrue();
        assertThatThrownBy(() -> service.verify(EMAIL, OtpPurpose.PASSWORD_RESET, "123456"))
                .hasMessageContaining("request a new one");
    }

    @Test
    void wrongCodeCountsAttemptsAndLocksAfterMax() {
        OtpCode otp = code("123456", NOW.minusSeconds(60));
        latestIs(otp);

        assertThatThrownBy(() -> service.verify(EMAIL, OtpPurpose.PASSWORD_RESET, "000000"))
                .hasMessage("Incorrect code. 4 attempts left");
        otp.setAttempts(OtpService.MAX_ATTEMPTS);

        assertThatThrownBy(() -> service.verify(EMAIL, OtpPurpose.PASSWORD_RESET, "123456"))
                .hasMessageContaining("Too many wrong attempts");
        assertThat(otp.isConsumed()).isFalse();
    }

    @Test
    void expiredCodeIsRejectedEvenIfCorrect() {
        latestIs(code("123456", NOW.minus(OtpService.VALIDITY).minusSeconds(1)));

        assertThatThrownBy(() -> service.verify(EMAIL, OtpPurpose.PASSWORD_RESET, "123456"))
                .hasMessageContaining("expired");
    }

    @Test
    void maskKeepsDomainAndFirstTwoLetters() {
        assertThat(OtpService.mask("rajesh@gmail.com")).isEqualTo("ra****@gmail.com");
        assertThat(OtpService.mask("a@b.in")).isEqualTo("a****@b.in");
    }
}
