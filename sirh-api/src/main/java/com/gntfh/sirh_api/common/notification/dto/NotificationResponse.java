package com.gntfh.sirh_api.common.notification.dto;

import com.gntfh.sirh_api.common.audit.enums.TypeEntite;
import com.gntfh.sirh_api.common.notification.enums.TypeNotification;

import java.time.Instant;

public record NotificationResponse(
        Long id,
        TypeNotification type,
        String titre,
        String message,
        TypeEntite entiteType,
        Long entiteId,
        boolean lue,
        Instant dateLecture,
        Instant createdAt
) {
}
