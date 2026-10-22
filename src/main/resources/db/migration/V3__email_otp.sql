-- Email (verified by OTP at signup) — the channel for forgot-password OTPs.
-- Nullable so accounts created before email verification keep working.
ALTER TABLE users
    ADD COLUMN email VARCHAR(255) NULL,
    ADD CONSTRAINT uq_users_email UNIQUE (email);

-- One-time codes for signup email verification and password reset.
-- Only a hash of the code is stored; a code dies when used, expired, or
-- after too many wrong attempts.
CREATE TABLE otp_codes (
    id          BIGINT AUTO_INCREMENT PRIMARY KEY,
    email       VARCHAR(255) NOT NULL,
    purpose     VARCHAR(20)  NOT NULL,
    code_hash   VARCHAR(255) NOT NULL,
    expires_at  TIMESTAMP(6) NOT NULL,
    attempts    INT          NOT NULL DEFAULT 0,
    consumed    BOOLEAN      NOT NULL DEFAULT FALSE,
    created_at  TIMESTAMP(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    INDEX idx_otp_codes_email_purpose (email, purpose, created_at)
);
