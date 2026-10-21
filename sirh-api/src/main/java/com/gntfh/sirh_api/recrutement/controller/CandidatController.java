package com.gntfh.sirh_api.recrutement.controller;

import com.gntfh.sirh_api.common.dto.PageResponse;
import com.gntfh.sirh_api.common.security.CurrentUser;
import com.gntfh.sirh_api.recrutement.dto.CandidatRequest;
import com.gntfh.sirh_api.recrutement.dto.CandidatResponse;
import com.gntfh.sirh_api.recrutement.service.CandidatService;
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
 * Contrôleur pour la gestion des candidats.
 */
@RestController
@RequestMapping("/api/candidats")
@Tag(name = "Candidats", description = "Gestion des candidats externes")
@RequiredArgsConstructor
public class CandidatController {

    private final CandidatService candidatService;

    @GetMapping
    @PreAuthorize("hasRole('RH')")
    @Operation(summary = "Lister les candidats")
    public ResponseEntity<PageResponse<CandidatResponse>> rechercher(
            @RequestParam(required = false) String q,
            @PageableDefault(size = 20) Pageable pageable) {
        return ResponseEntity.ok(candidatService.rechercher(q, pageable));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('RH')")
    @Operation(summary = "Détail d'un candidat")
    public ResponseEntity<CandidatResponse> obtenir(@PathVariable Long id) {
        return ResponseEntity.ok(candidatService.obtenir(id));
    }

    @PostMapping
    @PreAuthorize("hasRole('RH')")
    @Operation(summary = "Créer un candidat")
    public ResponseEntity<CandidatResponse> creer(@Valid @RequestBody CandidatRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(candidatService.creer(request));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('RH')")
    @Operation(summary = "Modifier un candidat")
    public ResponseEntity<CandidatResponse> modifier(
            @PathVariable Long id,
            @Valid @RequestBody CandidatRequest request,
            @AuthenticationPrincipal org.springframework.security.oauth2.jwt.Jwt jwt) {
        CurrentUser currentUser = CurrentUser.from(jwt);
        return ResponseEntity.ok(candidatService.modifier(id, request, currentUser));
    }
}
