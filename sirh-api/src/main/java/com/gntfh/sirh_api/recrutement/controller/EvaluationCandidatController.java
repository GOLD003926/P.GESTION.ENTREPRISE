package com.gntfh.sirh_api.recrutement.controller;

import com.gntfh.sirh_api.common.dto.PageResponse;
import com.gntfh.sirh_api.common.security.CurrentUser;
import com.gntfh.sirh_api.recrutement.dto.EvaluationCandidatRequest;
import com.gntfh.sirh_api.recrutement.dto.EvaluationCandidatResponse;
import com.gntfh.sirh_api.recrutement.dto.EvaluationCandidatUpdateRequest;
import com.gntfh.sirh_api.recrutement.service.EvaluationCandidatService;
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

/**
 * Contrôleur pour la gestion des évaluations de candidats.
 */
@RestController
@RequestMapping("/api/evaluations")
@Tag(name = "Évaluations", description = "Gestion des évaluations lors des entretiens")
@RequiredArgsConstructor
public class EvaluationCandidatController {

    private final EvaluationCandidatService evaluationCandidatService;

    @GetMapping
    @PreAuthorize("hasAnyRole('RH', 'MANAGER')")
    @Operation(summary = "Lister les évaluations")
    public ResponseEntity<PageResponse<EvaluationCandidatResponse>> rechercher(
            @RequestParam Long entretienId,
            @PageableDefault(size = 20) Pageable pageable,
            @AuthenticationPrincipal org.springframework.security.oauth2.jwt.Jwt jwt) {
        CurrentUser currentUser = CurrentUser.from(jwt);
        return ResponseEntity.ok(evaluationCandidatService.lister(entretienId, pageable, currentUser));
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('RH', 'MANAGER')")
    @Operation(summary = "Créer une évaluation")
    public ResponseEntity<EvaluationCandidatResponse> creer(
            @Valid @RequestBody EvaluationCandidatRequest request,
            @AuthenticationPrincipal org.springframework.security.oauth2.jwt.Jwt jwt) {
        CurrentUser currentUser = CurrentUser.from(jwt);
        return ResponseEntity.status(HttpStatus.CREATED).body(evaluationCandidatService.creer(request, currentUser));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('RH', 'MANAGER')")
    @Operation(summary = "Modifier une évaluation")
    public ResponseEntity<EvaluationCandidatResponse> modifier(
            @PathVariable Long id,
            @Valid @RequestBody EvaluationCandidatUpdateRequest request,
            @AuthenticationPrincipal org.springframework.security.oauth2.jwt.Jwt jwt) {
        CurrentUser currentUser = CurrentUser.from(jwt);
        return ResponseEntity.ok(evaluationCandidatService.modifier(id, request, currentUser));
    }

    @PostMapping("/{id}/verrouiller")
    @PreAuthorize("hasAnyRole('RH', 'MANAGER')")
    @Operation(summary = "Verrouiller une évaluation")
    public ResponseEntity<EvaluationCandidatResponse> verrouiller(
            @PathVariable Long id,
            @AuthenticationPrincipal org.springframework.security.oauth2.jwt.Jwt jwt) {
        CurrentUser currentUser = CurrentUser.from(jwt);
        return ResponseEntity.ok(evaluationCandidatService.verrouiller(id, currentUser));
    }

    @PostMapping("/{id}/corriger")
    @PreAuthorize("hasRole('RH')")
    @Operation(summary = "Corriger une évaluation (créer une nouvelle version)")
    public ResponseEntity<EvaluationCandidatResponse> corriger(
            @PathVariable Long id,
            @Valid @RequestBody EvaluationCandidatUpdateRequest request,
            @AuthenticationPrincipal org.springframework.security.oauth2.jwt.Jwt jwt) {
        CurrentUser currentUser = CurrentUser.from(jwt);
        return ResponseEntity.ok(evaluationCandidatService.corriger(id, request, currentUser));
    }
}
