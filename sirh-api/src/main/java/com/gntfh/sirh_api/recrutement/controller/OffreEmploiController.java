package com.gntfh.sirh_api.recrutement.controller;

import com.gntfh.sirh_api.common.dto.PageResponse;
import com.gntfh.sirh_api.common.security.CurrentUser;
import com.gntfh.sirh_api.recrutement.dto.OffreEmploiRequest;
import com.gntfh.sirh_api.recrutement.dto.OffreEmploiResponse;
import com.gntfh.sirh_api.recrutement.dto.OffreEmploiUpdateRequest;
import com.gntfh.sirh_api.recrutement.enums.StatutOffre;
import com.gntfh.sirh_api.recrutement.service.OffreEmploiService;
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
 * Contrôleur pour la gestion des offres d'emploi.
 */
@RestController
@RequestMapping("/api/offres")
@Tag(name = "Offres d'emploi", description = "Gestion des offres de recrutement")
@RequiredArgsConstructor
public class OffreEmploiController {

    private final OffreEmploiService offreEmploiService;

    @GetMapping
    @PreAuthorize("hasAnyRole('RH', 'MANAGER', 'DG')")
    @Operation(summary = "Lister les offres", description = "Liste paginée des offres avec filtres")
    public ResponseEntity<PageResponse<OffreEmploiResponse>> rechercher(
            @RequestParam(required = false) String q,
            @RequestParam(required = false) StatutOffre statut,
            @RequestParam(required = false) Long demandeId,
            @PageableDefault(size = 20) Pageable pageable) {
        return ResponseEntity.ok(offreEmploiService.rechercher(q, statut, demandeId, pageable));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('RH', 'MANAGER', 'DG')")
    @Operation(summary = "Détail d'une offre")
    public ResponseEntity<OffreEmploiResponse> obtenir(@PathVariable Long id) {
        return ResponseEntity.ok(offreEmploiService.obtenir(id));
    }

    @PostMapping
    @PreAuthorize("hasRole('RH')")
    @Operation(summary = "Créer une offre")
    public ResponseEntity<OffreEmploiResponse> creer(
            @Valid @RequestBody OffreEmploiRequest request,
            @AuthenticationPrincipal org.springframework.security.oauth2.jwt.Jwt jwt) {
        CurrentUser currentUser = CurrentUser.from(jwt);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(offreEmploiService.creer(request, currentUser));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('RH')")
    @Operation(summary = "Modifier une offre (brouillon uniquement)")
    public ResponseEntity<OffreEmploiResponse> modifier(
            @PathVariable Long id,
            @Valid @RequestBody OffreEmploiUpdateRequest request,
            @AuthenticationPrincipal org.springframework.security.oauth2.jwt.Jwt jwt) {
        CurrentUser currentUser = CurrentUser.from(jwt);
        return ResponseEntity.ok(offreEmploiService.modifier(id, request, currentUser));
    }

    @PostMapping("/{id}/publier")
    @PreAuthorize("hasRole('RH')")
    @Operation(summary = "Publier une offre")
    public ResponseEntity<OffreEmploiResponse> publier(
            @PathVariable Long id,
            @AuthenticationPrincipal org.springframework.security.oauth2.jwt.Jwt jwt) {
        CurrentUser currentUser = CurrentUser.from(jwt);
        return ResponseEntity.ok(offreEmploiService.publier(id, currentUser));
    }

    @PostMapping("/{id}/cloturer")
    @PreAuthorize("hasRole('RH')")
    @Operation(summary = "Clôturer une offre")
    public ResponseEntity<OffreEmploiResponse> cloturer(
            @PathVariable Long id,
            @AuthenticationPrincipal org.springframework.security.oauth2.jwt.Jwt jwt) {
        CurrentUser currentUser = CurrentUser.from(jwt);
        return ResponseEntity.ok(offreEmploiService.cloturer(id, currentUser));
    }

    @PostMapping("/{id}/annuler")
    @PreAuthorize("hasRole('RH')")
    @Operation(summary = "Annuler une offre")
    public ResponseEntity<OffreEmploiResponse> annuler(
            @PathVariable Long id,
            @AuthenticationPrincipal org.springframework.security.oauth2.jwt.Jwt jwt) {
        CurrentUser currentUser = CurrentUser.from(jwt);
        return ResponseEntity.ok(offreEmploiService.annuler(id, currentUser));
    }
}
