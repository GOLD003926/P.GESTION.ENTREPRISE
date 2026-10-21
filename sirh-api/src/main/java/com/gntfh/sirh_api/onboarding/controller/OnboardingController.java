package com.gntfh.sirh_api.onboarding.controller;

import com.gntfh.sirh_api.common.audit.dto.HistoriqueStatutResponse;
import com.gntfh.sirh_api.common.dto.PageResponse;
import com.gntfh.sirh_api.common.security.CurrentUser;
import com.gntfh.sirh_api.onboarding.dto.OnboardingRequest;
import com.gntfh.sirh_api.onboarding.dto.OnboardingResponse;
import com.gntfh.sirh_api.onboarding.enums.StatutOnboarding;
import com.gntfh.sirh_api.onboarding.service.OnboardingService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/onboardings")
@Tag(name = "Onboardings", description = "Suivi de l'intégration des employés")
@RequiredArgsConstructor
public class OnboardingController {

    private final OnboardingService service;

    @GetMapping
    @PreAuthorize("hasAnyRole('RH', 'MANAGER')")
    @Operation(summary = "Rechercher les onboardings")
    public ResponseEntity<PageResponse<OnboardingResponse>> rechercher(
            @RequestParam(required = false) Long employeId,
            @RequestParam(required = false) StatutOnboarding statut,
            @ParameterObject @PageableDefault(size = 20, sort = "dateDebut") Pageable pageable,
            @AuthenticationPrincipal Jwt jwt
    ) {
        return ResponseEntity.ok(service.rechercher(employeId, statut, pageable, CurrentUser.from(jwt)));
    }

    @GetMapping("/mon-onboarding")
    @PreAuthorize("hasRole('SALARIE')")
    @Operation(summary = "Consulter mon onboarding")
    public ResponseEntity<OnboardingResponse> monOnboarding(@AuthenticationPrincipal Jwt jwt) {
        return ResponseEntity.ok(service.monOnboarding(CurrentUser.from(jwt)));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('RH', 'MANAGER')")
    @Operation(summary = "Obtenir un onboarding")
    public ResponseEntity<OnboardingResponse> obtenir(@PathVariable Long id, @AuthenticationPrincipal Jwt jwt) {
        return ResponseEntity.ok(service.obtenir(id, CurrentUser.from(jwt)));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('RH')")
    @Operation(summary = "Désigner le responsable RH d'un onboarding")
    public ResponseEntity<OnboardingResponse> modifier(@PathVariable Long id,
                                                        @Valid @RequestBody OnboardingRequest request,
                                                        @AuthenticationPrincipal Jwt jwt) {
        return ResponseEntity.ok(service.modifier(id, request, CurrentUser.from(jwt)));
    }

    @GetMapping("/{id}/historique")
    @PreAuthorize("hasRole('RH')")
    @Operation(summary = "Consulter l'historique d'un onboarding")
    public ResponseEntity<List<HistoriqueStatutResponse>> historique(@PathVariable Long id,
                                                                      @AuthenticationPrincipal Jwt jwt) {
        return ResponseEntity.ok(service.historique(id, CurrentUser.from(jwt)));
    }
}
