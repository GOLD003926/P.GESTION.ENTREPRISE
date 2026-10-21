package com.gntfh.sirh_api.demande.controller;

import com.gntfh.sirh_api.common.dto.PageResponse;
import com.gntfh.sirh_api.common.security.CurrentUser;
import com.gntfh.sirh_api.demande.dto.AnnulationRequest;
import com.gntfh.sirh_api.demande.dto.DemandeRecrutementRequest;
import com.gntfh.sirh_api.demande.dto.DemandeRecrutementResponse;
import com.gntfh.sirh_api.demande.enums.StatutDemande;
import com.gntfh.sirh_api.demande.service.DemandeRecrutementService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Contrôleur pour la gestion des demandes de recrutement.
 */
@RestController
@RequestMapping("/api/demandes")
@RequiredArgsConstructor
@Tag(name = "Demandes de recrutement", description = "Gestion des demandes de recrutement")
public class DemandeRecrutementController {

    private final DemandeRecrutementService service;

    @PostMapping
    @PreAuthorize("hasRole('MANAGER')")
    @Operation(summary = "Créer une demande de recrutement")
    public ResponseEntity<DemandeRecrutementResponse> creer(
            @Valid @RequestBody DemandeRecrutementRequest request,
            @AuthenticationPrincipal Jwt jwt) {
        CurrentUser currentUser = CurrentUser.from(jwt);
        DemandeRecrutementResponse response = service.creer(request, currentUser);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('MANAGER')")
    @Operation(summary = "Modifier une demande de recrutement")
    public ResponseEntity<DemandeRecrutementResponse> modifier(
            @PathVariable Long id,
            @Valid @RequestBody DemandeRecrutementRequest request,
            @AuthenticationPrincipal Jwt jwt) {
        CurrentUser currentUser = CurrentUser.from(jwt);
        DemandeRecrutementResponse response = service.modifier(id, request, currentUser);
        return ResponseEntity.ok(response);
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('MANAGER', 'RH', 'FINANCE', 'DG')")
    @Operation(summary = "Lister les demandes de recrutement")
    public ResponseEntity<PageResponse<DemandeRecrutementResponse>> rechercher(
            @RequestParam(required = false) String q,
            @RequestParam(required = false) StatutDemande statut,
            @RequestParam(required = false) Long posteId,
            @RequestParam(required = false) Long departementId,
            @PageableDefault(size = 20) Pageable pageable,
            @AuthenticationPrincipal Jwt jwt) {
        CurrentUser currentUser = CurrentUser.from(jwt);
        PageResponse<DemandeRecrutementResponse> response = service.rechercher(q, statut, posteId, departementId, pageable, currentUser);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('MANAGER', 'RH', 'FINANCE', 'DG')")
    @Operation(summary = "Obtenir une demande de recrutement")
    public ResponseEntity<DemandeRecrutementResponse> obtenir(
            @PathVariable Long id,
            @AuthenticationPrincipal Jwt jwt) {
        CurrentUser currentUser = CurrentUser.from(jwt);
        DemandeRecrutementResponse response = service.obtenir(id, currentUser);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/{id}/soumettre")
    @PreAuthorize("hasRole('MANAGER')")
    @Operation(summary = "Soumettre une demande à validation")
    public ResponseEntity<DemandeRecrutementResponse> soumettre(
            @PathVariable Long id,
            @AuthenticationPrincipal Jwt jwt) {
        CurrentUser currentUser = CurrentUser.from(jwt);
        DemandeRecrutementResponse response = service.soumettre(id, currentUser);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/{id}/annuler")
    @PreAuthorize("hasAnyRole('MANAGER', 'RH')")
    @Operation(summary = "Annuler une demande de recrutement")
    public ResponseEntity<DemandeRecrutementResponse> annuler(
            @PathVariable Long id,
            @Valid @RequestBody AnnulationRequest request,
            @AuthenticationPrincipal Jwt jwt) {
        CurrentUser currentUser = CurrentUser.from(jwt);
        DemandeRecrutementResponse response = service.annuler(id, request, currentUser);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}/historique")
    @PreAuthorize("hasAnyRole('MANAGER', 'RH', 'FINANCE', 'DG')")
    @Operation(summary = "Obtenir l'historique des statuts d'une demande")
    public ResponseEntity<List<Object>> historique(
            @PathVariable Long id,
            @AuthenticationPrincipal Jwt jwt) {
        CurrentUser currentUser = CurrentUser.from(jwt);
        List<Object> response = service.historique(id, currentUser);
        return ResponseEntity.ok(response);
    }
}
