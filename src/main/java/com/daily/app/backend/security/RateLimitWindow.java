package com.daily.app.backend.security;

final class RateLimitWindow {

    private long windowStartMillis;
    private int count;

    RateLimitWindow(long now) {
        this.windowStartMillis = now;
        this.count = 0;
    }

    synchronized boolean tryConsume(long now, long windowMillis, int limit) {
        if (now - windowStartMillis >= windowMillis) {
            windowStartMillis = now;
            count = 0;
        }
        if (count >= limit) {
            return false;
        }
        count++;
        return true;
    }
}
