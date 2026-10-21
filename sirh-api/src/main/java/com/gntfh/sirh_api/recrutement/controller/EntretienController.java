package com.gntfh.sirh_api.recrutement.controller;

import com.gntfh.sirh_api.common.dto.PageResponse;
import com.gntfh.sirh_api.common.security.CurrentUser;
import com.gntfh.sirh_api.recrutement.dto.AnnulationEntretienRequest;
import com.gntfh.sirh_api.recrutement.dto.EntretienRequest;
import com.gntfh.sirh_api.recrutement.dto.EntretienResponse;
import com.gntfh.sirh_api.recrutement.dto.EntretienUpdateRequest;
import com.gntfh.sirh_api.recrutement.enums.StatutEntretien;
import com.gntfh.sirh_api.recrutement.service.EntretienService;
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

import java.time.LocalDate;

/**
 * Contrôleur pour la gestion des entretiens.
 */
@RestController
@RequestMapping("/api/entretiens")
@Tag(name = "Entretiens", description = "Gestion des entretiens de sélection")
@RequiredArgsConstructor
public class EntretienController {

    private final EntretienService entretienService;
    private final SelectionWorkflowService selectionWorkflowService;

    @GetMapping
    @PreAuthorize("hasAnyRole('RH', 'MANAGER')")
    @Operation(summary = "Lister les entretiens")
    public ResponseEntity<PageResponse<EntretienResponse>> rechercher(
            @RequestParam(required = false) Long candidatureId,
            @RequestParam(required = false) StatutEntretien statut,
            @RequestParam(required = false) LocalDate du,
            @RequestParam(required = false) LocalDate au,
            @PageableDefault(size = 20) Pageable pageable,
            @AuthenticationPrincipal org.springframework.security.oauth2.jwt.Jwt jwt) {
        CurrentUser currentUser = CurrentUser.from(jwt);
        return ResponseEntity.ok(entretienService.rechercher(candidatureId, statut, du, au, pageable, currentUser));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('RH', 'MANAGER')")
    @Operation(summary = "Détail d'un entretien")
    public ResponseEntity<EntretienResponse> obtenir(
            @PathVariable Long id,
            @AuthenticationPrincipal org.springframework.security.oauth2.jwt.Jwt jwt) {
        CurrentUser currentUser = CurrentUser.from(jwt);
        return ResponseEntity.ok(entretienService.obtenir(id, currentUser));
    }

    @PostMapping
    @PreAuthorize("hasRole('RH')")
    @Operation(summary = "Planifier un entretien")
    public ResponseEntity<EntretienResponse> creer(
            @Valid @RequestBody EntretienRequest request,
            @AuthenticationPrincipal org.springframework.security.oauth2.jwt.Jwt jwt) {
        CurrentUser currentUser = CurrentUser.from(jwt);
        return ResponseEntity.status(HttpStatus.CREATED).body(selectionWorkflowService.planifierEntretien(request, currentUser));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('RH')")
    @Operation(summary = "Reprogrammer un entretien")
    public ResponseEntity<EntretienResponse> modifier(
            @PathVariable Long id,
            @Valid @RequestBody EntretienUpdateRequest request,
            @AuthenticationPrincipal org.springframework.security.oauth2.jwt.Jwt jwt) {
        CurrentUser currentUser = CurrentUser.from(jwt);
        return ResponseEntity.ok(selectionWorkflowService.reprogrammer(id, request, currentUser));
    }

    @PostMapping("/{id}/cloturer")
    @PreAuthorize("hasRole('RH')")
    @Operation(summary = "Clôturer un entretien")
    public ResponseEntity<EntretienResponse> cloturer(
            @PathVariable Long id,
            @AuthenticationPrincipal org.springframework.security.oauth2.jwt.Jwt jwt) {
        CurrentUser currentUser = CurrentUser.from(jwt);
        return ResponseEntity.ok(selectionWorkflowService.cloturerEntretien(id, currentUser));
    }

    @PostMapping("/{id}/annuler")
    @PreAuthorize("hasRole('RH')")
    @Operation(summary = "Annuler un entretien")
    public ResponseEntity<EntretienResponse> annuler(
            @PathVariable Long id,
            @Valid @RequestBody AnnulationEntretienRequest request,
            @AuthenticationPrincipal org.springframework.security.oauth2.jwt.Jwt jwt) {
        CurrentUser currentUser = CurrentUser.from(jwt);
        return ResponseEntity.ok(selectionWorkflowService.annulerEntretien(id, request, currentUser));
    }

    @PostMapping("/{id}/absent")
    @PreAuthorize("hasRole('RH')")
    @Operation(summary = "Marquer le candidat absent")
    public ResponseEntity<EntretienResponse> marquerAbsent(
            @PathVariable Long id,
            @AuthenticationPrincipal org.springframework.security.oauth2.jwt.Jwt jwt) {
        CurrentUser currentUser = CurrentUser.from(jwt);
        return ResponseEntity.ok(selectionWorkflowService.marquerAbsent(id, currentUser));
    }
}
