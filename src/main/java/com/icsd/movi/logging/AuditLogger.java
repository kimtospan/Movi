package com.icsd.movi.logging;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class AuditLogger {

    public void logAction(String action, String details, String username) {
        log.info("[AUDIT] User: {}, Action: {}, Details: {}", username, action, details);
    }

    public void logError(String action, String error, String username) {
        log.error("[AUDIT-ERROR] User: {}, Action: {}, Error: {}", username, action, error);
    }
}