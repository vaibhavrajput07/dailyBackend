package com.daily.app.backend.dto.auth;

/**
 * @param sentTo           masked address the code went to, e.g. "ra****@gmail.com"
 * @param expiresInSeconds how long the code stays valid
 * @param resendInSeconds  how long before another code can be requested
 */
public record OtpSentResponse(String sentTo, long expiresInSeconds, long resendInSeconds) {
}
