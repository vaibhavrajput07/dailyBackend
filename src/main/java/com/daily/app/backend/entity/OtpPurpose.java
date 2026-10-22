package com.daily.app.backend.entity;

public enum OtpPurpose {
    SIGNUP,
    PASSWORD_RESET,
    /** Adding or changing the recovery email while logged in. */
    EMAIL_CHANGE
}
