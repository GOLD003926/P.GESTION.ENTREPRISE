package com.gntfh.sirh_api.recrutement.controller;

import com.gntfh.sirh_api.common.dto.PageResponse;
import com.gntfh.sirh_api.common.security.CurrentUser;
import com.gntfh.sirh_api.recrutement.dto.CandidatureRequest;
import com.gntfh.sirh_api.recrutement.dto.CandidatureResponse;
import com.gntfh.sirh_api.recrutement.dto.DecisionFinaleRequest;
import com.gntfh.sirh_api.recrutement.dto.PreselectionRequest;
import com.gntfh.sirh_api.recrutement.enums.StatutCandidature;
import com.gntfh.sirh_api.recrutement.service.CandidatureService;
import com.gntfh.sirh_api.recrutement.service.SelectionWorkflowService;
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
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

/**
 * Contrôleur pour la gestion des candidatures.
 */
@RestController
@RequestMapping("/api/candidatures")
@Tag(name = "Candidatures", description = "Gestion des candidatures aux offres")
@RequiredArgsConstructor
public class CandidatureController {

    private final CandidatureService candidatureService;
    private final SelectionWorkflowService selectionWorkflowService;

    @GetMapping
    @PreAuthorize("hasAnyRole('RH', 'MANAGER')")
    @Operation(summary = "Lister les candidatures")
    public ResponseEntity<PageResponse<CandidatureResponse>> rechercher(
            @RequestParam(required = false) Long offreId,
            @RequestParam(required = false) StatutCandidature statut,
            @RequestParam(required = false) Long candidatId,
            @PageableDefault(size = 20) Pageable pageable,
            @AuthenticationPrincipal org.springframework.security.oauth2.jwt.Jwt jwt) {
        CurrentUser currentUser = CurrentUser.from(jwt);
        return ResponseEntity.ok(candidatureService.rechercher(offreId, statut, candidatId, pageable, currentUser));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('RH', 'MANAGER')")
    @Operation(summary = "Détail d'une candidature")
    public ResponseEntity<CandidatureResponse> obtenir(
            @PathVariable Long id,
            @AuthenticationPrincipal org.springframework.security.oauth2.jwt.Jwt jwt) {
        CurrentUser currentUser = CurrentUser.from(jwt);
        return ResponseEntity.ok(candidatureService.obtenir(id, currentUser));
    }

    @PostMapping
    @PreAuthorize("hasRole('RH')")
    @Operation(summary = "Enregistrer une candidature")
    public ResponseEntity<CandidatureResponse> creer(
            @Valid @RequestBody CandidatureRequest request,
            @AuthenticationPrincipal org.springframework.security.oauth2.jwt.Jwt jwt) {
        CurrentUser currentUser = CurrentUser.from(jwt);
        return ResponseEntity.status(HttpStatus.CREATED).body(candidatureService.creer(request, currentUser));
    }

    @PostMapping("/{id}/cv")
    @PreAuthorize("hasRole('RH')")
    @Operation(summary = "Déposer le CV d'une candidature")
    public ResponseEntity<Void> televerserCv(
            @PathVariable Long id,
            @RequestParam("fichier") MultipartFile fichier,
            @AuthenticationPrincipal org.springframework.security.oauth2.jwt.Jwt jwt) {
        CurrentUser currentUser = CurrentUser.from(jwt);
        candidatureService.televerserCv(id, fichier, currentUser);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/preselection")
    @PreAuthorize("hasRole('RH')")
    @Operation(summary = "Présélectionner une candidature")
    public ResponseEntity<CandidatureResponse> preselectionner(
            @PathVariable Long id,
            @Valid @RequestBody PreselectionRequest request,
            @AuthenticationPrincipal org.springframework.security.oauth2.jwt.Jwt jwt) {
        CurrentUser currentUser = CurrentUser.from(jwt);
        return ResponseEntity.ok(selectionWorkflowService.preselectionner(id, request, currentUser));
    }

    @PostMapping("/{id}/decision-finale")
    @PreAuthorize("hasAnyRole('RH', 'MANAGER')")
    @Operation(summary = "Décision finale sur une candidature")
    public ResponseEntity<CandidatureResponse> decisionFinale(
            @PathVariable Long id,
            @Valid @RequestBody DecisionFinaleRequest request,
            @AuthenticationPrincipal org.springframework.security.oauth2.jwt.Jwt jwt) {
        CurrentUser currentUser = CurrentUser.from(jwt);
        return ResponseEntity.ok(selectionWorkflowService.decisionFinale(id, request, currentUser));
    }

    @PostMapping("/{id}/promouvoir-liste-attente")
    @PreAuthorize("hasRole('RH')")
    @Operation(summary = "Promouvoir depuis la liste d'attente")
    public ResponseEntity<CandidatureResponse> promouvoirListeAttente(
            @PathVariable Long id,
            @AuthenticationPrincipal org.springframework.security.oauth2.jwt.Jwt jwt) {
        CurrentUser currentUser = CurrentUser.from(jwt);
        return ResponseEntity.ok(candidatureService.promouvoirListeAttente(id, currentUser));
    }

    @PostMapping("/{id}/desister")
    @PreAuthorize("hasRole('RH')")
    @Operation(summary = "Désistement d'une candidature")
    public ResponseEntity<CandidatureResponse> desister(
            @PathVariable Long id,
            @AuthenticationPrincipal org.springframework.security.oauth2.jwt.Jwt jwt) {
        CurrentUser currentUser = CurrentUser.from(jwt);
        return ResponseEntity.ok(candidatureService.desister(id, currentUser));
    }

    @GetMapping("/{id}/historique")
    @PreAuthorize("hasAnyRole('RH', 'MANAGER')")
    @Operation(summary = "Historique des statuts d'une candidature")
    public ResponseEntity<?> historique(
            @PathVariable Long id,
            @AuthenticationPrincipal org.springframework.security.oauth2.jwt.Jwt jwt) {
        CurrentUser currentUser = CurrentUser.from(jwt);
        return ResponseEntity.ok(candidatureService.historique(id, currentUser));
    }
}
