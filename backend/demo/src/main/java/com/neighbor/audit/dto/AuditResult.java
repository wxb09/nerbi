package com.neighbor.audit.dto;

public record AuditResult(
    boolean passed,
    String result,
    String reason,
    String sensitiveWords
) {
    public static AuditResult pass() {
        return new AuditResult(true, "PASS", null, null);
    }

    public static AuditResult block(String reason, String sensitiveWords) {
        return new AuditResult(false, "BLOCK", reason, sensitiveWords);
    }

    public static AuditResult pending(String reason, String sensitiveWords) {
        return new AuditResult(false, "PENDING", reason, sensitiveWords);
    }
}
