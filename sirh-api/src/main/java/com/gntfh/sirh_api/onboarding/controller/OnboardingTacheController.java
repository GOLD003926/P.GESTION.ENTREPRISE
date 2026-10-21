package com.gntfh.sirh_api.onboarding.controller;

import com.gntfh.sirh_api.common.dto.PageResponse;
import com.gntfh.sirh_api.common.security.CurrentUser;
import com.gntfh.sirh_api.onboarding.dto.OnboardingTacheRequest;
import com.gntfh.sirh_api.onboarding.dto.OnboardingTacheResponse;
import com.gntfh.sirh_api.onboarding.enums.StatutTache;
import com.gntfh.sirh_api.onboarding.service.OnboardingTacheService;
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
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/onboarding-taches")
@Tag(name = "Tâches d'onboarding", description = "Traitement et affectation des tâches")
@RequiredArgsConstructor
public class OnboardingTacheController {

    private final OnboardingTacheService service;

    @GetMapping("/mes-taches")
    @PreAuthorize("hasAnyRole('RH', 'MANAGER', 'IT_SERVICES', 'PAIE')")
    @Operation(summary = "Lister les tâches à traiter")
    public ResponseEntity<PageResponse<OnboardingTacheResponse>> mesTaches(
            @RequestParam(required = false) StatutTache statut,
            @ParameterObject @PageableDefault(size = 20, sort = "echeance") Pageable pageable,
            @AuthenticationPrincipal Jwt jwt
    ) {
        return ResponseEntity.ok(service.mesTaches(statut, pageable, CurrentUser.from(jwt)));
    }

    @PostMapping("/{id}/demarrer")
    @PreAuthorize("hasAnyRole('RH', 'MANAGER', 'IT_SERVICES', 'PAIE')")
    @Operation(summary = "Démarrer une tâche")
    public ResponseEntity<OnboardingTacheResponse> demarrer(@PathVariable Long id,
                                                             @Valid @RequestBody OnboardingTacheRequest request,
                                                             @AuthenticationPrincipal Jwt jwt) {
        return ResponseEntity.ok(service.demarrer(id, request, CurrentUser.from(jwt)));
    }

    @PostMapping("/{id}/terminer")
    @PreAuthorize("hasAnyRole('RH', 'MANAGER', 'IT_SERVICES', 'PAIE')")
    @Operation(summary = "Terminer une tâche")
    public ResponseEntity<OnboardingTacheResponse> terminer(@PathVariable Long id,
                                                             @Valid @RequestBody OnboardingTacheRequest request,
                                                             @AuthenticationPrincipal Jwt jwt) {
        return ResponseEntity.ok(service.terminer(id, request, CurrentUser.from(jwt)));
    }

    @PostMapping("/{id}/non-applicable")
    @PreAuthorize("hasRole('RH')")
    @Operation(summary = "Marquer une tâche facultative comme non applicable")
    public ResponseEntity<OnboardingTacheResponse> marquerNonApplicable(@PathVariable Long id,
                                                                         @Valid @RequestBody OnboardingTacheRequest request,
                                                                         @AuthenticationPrincipal Jwt jwt) {
        return ResponseEntity.ok(service.marquerNonApplicable(id, request, CurrentUser.from(jwt)));
    }

    @PostMapping("/{id}/assigner")
    @PreAuthorize("hasRole('RH')")
    @Operation(summary = "Assigner une tâche à un utilisateur")
    public ResponseEntity<OnboardingTacheResponse> assigner(@PathVariable Long id,
                                                             @Valid @RequestBody OnboardingTacheRequest request,
                                                             @AuthenticationPrincipal Jwt jwt) {
        return ResponseEntity.ok(service.assigner(id, request, CurrentUser.from(jwt)));
    }
}
