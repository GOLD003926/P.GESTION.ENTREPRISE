package com.gntfh.sirh_api.employe.controller;

import com.gntfh.sirh_api.common.dto.PageResponse;
import com.gntfh.sirh_api.common.security.CurrentUser;
import com.gntfh.sirh_api.employe.dto.AffectationClotureRequest;
import com.gntfh.sirh_api.employe.dto.AffectationRequest;
import com.gntfh.sirh_api.employe.dto.AffectationResponse;
import com.gntfh.sirh_api.employe.service.AffectationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

/**
 * Contrôleur pour la gestion des affectations.
 */
@RestController
@RequestMapping("/api/affectations")
@Tag(name = "Affectations", description = "Gestion des affectations")
@RequiredArgsConstructor
public class AffectationController {

    private final AffectationService affectationService;

    @GetMapping
    @PreAuthorize("hasAnyRole('RH', 'MANAGER')")
    @Operation(summary = "Lister les affectations")
    public ResponseEntity<PageResponse<AffectationResponse>> lister(
            @RequestParam(required = false) Long employeId,
            @RequestParam(required = false) Long posteId,
            @RequestParam(required = false) Boolean active,
            @ParameterObject @PageableDefault(size = 20, sort = "id") Pageable pageable) {
        return ResponseEntity.ok(affectationService.lister(employeId, posteId, active, pageable));
    }

    @PostMapping
    @PreAuthorize("hasRole('RH')")
    @Operation(summary = "Créer une affectation")
    public ResponseEntity<AffectationResponse> creer(
            @Valid @RequestBody AffectationRequest request,
            @AuthenticationPrincipal org.springframework.security.oauth2.jwt.Jwt jwt) {
        CurrentUser currentUser = CurrentUser.from(jwt);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(affectationService.creer(request, currentUser));
    }

    @PostMapping("/{id}/cloturer")
    @PreAuthorize("hasRole('RH')")
    @Operation(summary = "Clôturer une affectation")
    public ResponseEntity<Void> cloturer(
            @PathVariable Long id,
            @Valid @RequestBody AffectationClotureRequest request,
            @AuthenticationPrincipal org.springframework.security.oauth2.jwt.Jwt jwt) {
        CurrentUser currentUser = CurrentUser.from(jwt);
        affectationService.cloturer(id, request, currentUser);
        return ResponseEntity.ok().build();
    }
}
