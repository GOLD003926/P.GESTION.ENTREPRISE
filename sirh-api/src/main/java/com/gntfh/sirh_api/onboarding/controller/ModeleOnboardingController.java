package com.gntfh.sirh_api.onboarding.controller;

import com.gntfh.sirh_api.common.dto.PageResponse;
import com.gntfh.sirh_api.common.security.CurrentUser;
import com.gntfh.sirh_api.onboarding.dto.ModeleOnboardingRequest;
import com.gntfh.sirh_api.onboarding.dto.ModeleOnboardingResponse;
import com.gntfh.sirh_api.onboarding.service.ModeleOnboardingService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/modeles-onboarding")
@Tag(name = "Modèles d'onboarding", description = "Gestion des modèles et de leurs tâches")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('RH', 'ADMIN')")
public class ModeleOnboardingController {

    private final ModeleOnboardingService service;

    @GetMapping
    @Operation(summary = "Rechercher les modèles d'onboarding")
    public ResponseEntity<PageResponse<ModeleOnboardingResponse>> rechercher(
            @RequestParam(required = false) String q,
            @RequestParam(required = false) Boolean actif,
            @ParameterObject @PageableDefault(size = 20, sort = "nom") Pageable pageable
    ) {
        return ResponseEntity.ok(service.rechercher(q, actif, pageable));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Obtenir un modèle d'onboarding")
    public ResponseEntity<ModeleOnboardingResponse> obtenir(@PathVariable Long id) {
        return ResponseEntity.ok(service.obtenir(id));
    }

    @PostMapping
    @Operation(summary = "Créer un modèle d'onboarding")
    public ResponseEntity<ModeleOnboardingResponse> creer(@Valid @RequestBody ModeleOnboardingRequest request,
                                                           @AuthenticationPrincipal Jwt jwt) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(service.creer(request, CurrentUser.from(jwt)));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Modifier un modèle d'onboarding")
    public ResponseEntity<ModeleOnboardingResponse> modifier(@PathVariable Long id,
                                                               @Valid @RequestBody ModeleOnboardingRequest request,
                                                               @AuthenticationPrincipal Jwt jwt) {
        return ResponseEntity.ok(service.modifier(id, request, CurrentUser.from(jwt)));
    }

    @PostMapping("/{id}/activer")
    @Operation(summary = "Activer un modèle d'onboarding")
    public ResponseEntity<Void> activer(@PathVariable Long id) {
        service.activer(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/desactiver")
    @Operation(summary = "Désactiver un modèle d'onboarding")
    public ResponseEntity<Void> desactiver(@PathVariable Long id) {
        service.desactiver(id);
        return ResponseEntity.noContent().build();
    }
}
