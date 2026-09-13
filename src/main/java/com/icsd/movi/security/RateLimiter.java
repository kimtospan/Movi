package com.icsd.movi.security;

import org.springframework.stereotype.Component;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.time.Instant;

@Component
public class RateLimiter {
    private final ConcurrentHashMap<String, RateInfo> clientRequests = new ConcurrentHashMap<>();
    private static final int MAX_REQUESTS = 10;
    private static final int TIME_WINDOW_SECONDS = 60;

    public boolean isAllowed(String clientId, String action) {
        String key = clientId + ":" + action;
        RateInfo info = clientRequests.computeIfAbsent(key, k -> new RateInfo());
        
        synchronized(info) {
            long now = Instant.now().getEpochSecond();
            if (now - info.lastResetTime > TIME_WINDOW_SECONDS) {
                info.count.set(0);
                info.lastResetTime = now;
            }
            return info.count.incrementAndGet() <= MAX_REQUESTS;
        }
    }

    private static class RateInfo {
        AtomicInteger count = new AtomicInteger(0);
        long lastResetTime = Instant.now().getEpochSecond();
    }
}