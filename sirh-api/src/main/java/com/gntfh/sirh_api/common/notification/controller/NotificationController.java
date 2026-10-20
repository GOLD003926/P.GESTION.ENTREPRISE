package com.gntfh.sirh_api.common.notification.controller;

import com.gntfh.sirh_api.common.dto.PageResponse;
import com.gntfh.sirh_api.common.notification.dto.NotificationCompteurResponse;
import com.gntfh.sirh_api.common.notification.dto.NotificationResponse;
import com.gntfh.sirh_api.common.notification.service.NotificationService;
import com.gntfh.sirh_api.common.security.CurrentUser;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
@Tag(name = "Notifications", description = "Notifications internes")
public class NotificationController {

    private final NotificationService notificationService;

    @GetMapping
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Mes notifications",
            description = "Liste les notifications de l'utilisateur connecté, avec filtre de lecture.")
    public PageResponse<NotificationResponse> mesNotifications(
            @RequestParam(value = "lue", required = false) Boolean lue,
            @ParameterObject @PageableDefault(size = 20, sort = "createdAt") Pageable pageable,
            @AuthenticationPrincipal Jwt jwt) {
        CurrentUser currentUser = CurrentUser.from(jwt);
        return notificationService.mesNotifications(currentUser, lue, pageable);
    }

    @GetMapping("/compteur")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Compteur de notifications non lues")
    public NotificationCompteurResponse compter(@AuthenticationPrincipal Jwt jwt) {
        CurrentUser currentUser = CurrentUser.from(jwt);
        return notificationService.compter(currentUser);
    }

    @PutMapping("/{id}/lue")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Marquer une notification comme lue")
    public NotificationResponse marquerLue(
            @PathVariable Long id,
            @AuthenticationPrincipal Jwt jwt) {
        CurrentUser currentUser = CurrentUser.from(jwt);
        return notificationService.marquerLue(id, currentUser);
    }

    @PutMapping("/lues")
    @PreAuthorize("isAuthenticated()")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Marquer toutes les notifications comme lues")
    public void toutMarquerLues(@AuthenticationPrincipal Jwt jwt) {
        CurrentUser currentUser = CurrentUser.from(jwt);
        notificationService.toutMarquerLues(currentUser);
    }
}
