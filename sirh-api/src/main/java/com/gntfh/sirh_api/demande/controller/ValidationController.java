package com.gntfh.sirh_api.demande.controller;

import com.gntfh.sirh_api.common.dto.PageResponse;
import com.gntfh.sirh_api.common.security.CurrentUser;
import com.gntfh.sirh_api.demande.dto.DemandeRecrutementResponse;
import com.gntfh.sirh_api.demande.dto.ValidationRequest;
import com.gntfh.sirh_api.demande.dto.ValidationResponse;
import com.gntfh.sirh_api.demande.service.ValidationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Contrôleur pour la gestion des validations de demandes de recrutement.
 */
@RestController
@RequestMapping("/api/validations")
@RequiredArgsConstructor
@Tag(name = "Validations", description = "Gestion des validations de demandes de recrutement")
public class ValidationController {

    private final ValidationService service;

    @GetMapping("/en-attente")
    @PreAuthorize("hasAnyRole('RH', 'FINANCE', 'DG')")
    @Operation(summary = "Lister les demandes en attente de validation")
    public ResponseEntity<PageResponse<DemandeRecrutementResponse>> enAttentePour(
            @PageableDefault(size = 20) Pageable pageable,
            @AuthenticationPrincipal Jwt jwt) {
        CurrentUser currentUser = CurrentUser.from(jwt);
        PageResponse<DemandeRecrutementResponse> response = service.enAttentePour(currentUser, pageable);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/demandes/{demandeId}/decision")
    @PreAuthorize("hasAnyRole('RH', 'FINANCE', 'DG')")
    @Operation(summary = "Prendre une décision sur une demande")
    public ResponseEntity<DemandeRecrutementResponse> decider(
            @PathVariable Long demandeId,
            @Valid @RequestBody ValidationRequest request,
            @AuthenticationPrincipal Jwt jwt) {
        CurrentUser currentUser = CurrentUser.from(jwt);
        DemandeRecrutementResponse response = service.decider(demandeId, request, currentUser);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/demandes/{demandeId}")
    @PreAuthorize("hasAnyRole('MANAGER', 'RH', 'FINANCE', 'DG')")
    @Operation(summary = "Obtenir l'historique des validations d'une demande")
    public ResponseEntity<List<ValidationResponse>> historiqueValidations(
            @PathVariable Long demandeId,
            @AuthenticationPrincipal Jwt jwt) {
        CurrentUser currentUser = CurrentUser.from(jwt);
        List<ValidationResponse> response = service.historiqueValidations(demandeId, currentUser);
        return ResponseEntity.ok(response);
    }
}
