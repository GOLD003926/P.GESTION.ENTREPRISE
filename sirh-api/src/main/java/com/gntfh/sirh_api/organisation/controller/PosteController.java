package com.gntfh.sirh_api.organisation.controller;

import com.gntfh.sirh_api.common.dto.PageResponse;
import com.gntfh.sirh_api.organisation.dto.PosteRequest;
import com.gntfh.sirh_api.organisation.dto.PosteResponse;
import com.gntfh.sirh_api.organisation.dto.PosteUpdateRequest;
import com.gntfh.sirh_api.organisation.service.PosteService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

/**
 * Contrôleur pour la gestion des postes.
 */
@RestController
@RequestMapping("/api/postes")
@RequiredArgsConstructor
@Tag(name = "Postes", description = "Gestion des postes de travail")
public class PosteController {

    private final PosteService service;

    @GetMapping
    @PreAuthorize("hasAnyAuthority('RH', 'MANAGER', 'FINANCE', 'DG', 'ADMIN')")
    @Operation(summary = "Lister les postes")
    public ResponseEntity<PageResponse<PosteResponse>> rechercher(
            @RequestParam(required = false) String q,
            @RequestParam(required = false) Long departementId,
            @RequestParam(required = false) Long grilleSalarialeId,
            @RequestParam(required = false) Boolean actif,
            @PageableDefault(size = 20) Pageable pageable) {
        PageResponse<PosteResponse> response = service.rechercher(q, departementId, grilleSalarialeId, actif, pageable);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('RH', 'MANAGER', 'FINANCE', 'DG', 'ADMIN')")
    @Operation(summary = "Obtenir un poste avec occupation")
    public ResponseEntity<PosteResponse> obtenir(@PathVariable Long id) {
        PosteResponse response = service.obtenir(id);
        return ResponseEntity.ok(response);
    }

    @PostMapping
    @PreAuthorize("hasAuthority('RH')")
    @Operation(summary = "Créer un poste")
    public ResponseEntity<PosteResponse> creer(@Valid @RequestBody PosteRequest request) {
        PosteResponse response = service.creer(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('RH')")
    @Operation(summary = "Modifier un poste")
    public ResponseEntity<PosteResponse> modifier(
            @PathVariable Long id,
            @Valid @RequestBody PosteUpdateRequest request) {
        PosteResponse response = service.modifier(id, request);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/{id}/activer")
    @PreAuthorize("hasAuthority('RH')")
    @Operation(summary = "Activer un poste")
    public ResponseEntity<PosteResponse> activer(@PathVariable Long id) {
        PosteResponse response = service.activer(id);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/{id}/desactiver")
    @PreAuthorize("hasAuthority('RH')")
    @Operation(summary = "Désactiver un poste")
    public ResponseEntity<PosteResponse> desactiver(@PathVariable Long id) {
        PosteResponse response = service.desactiver(id);
        return ResponseEntity.ok(response);
    }
}
