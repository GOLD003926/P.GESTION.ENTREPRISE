package com.gntfh.sirh_api.organisation.controller;

import com.gntfh.sirh_api.common.dto.PageResponse;
import com.gntfh.sirh_api.organisation.dto.GrilleSalarialeRequest;
import com.gntfh.sirh_api.organisation.dto.GrilleSalarialeResponse;
import com.gntfh.sirh_api.organisation.dto.GrilleSalarialeUpdateRequest;
import com.gntfh.sirh_api.organisation.service.GrilleSalarialeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

/**
 * Contrôleur pour la gestion des grilles salariales.
 */
@RestController
@RequestMapping("/api/grilles-salariales")
@RequiredArgsConstructor
@Tag(name = "Grilles salariales", description = "Gestion des grilles salariales de l'entreprise")
public class GrilleSalarialeController {

    private final GrilleSalarialeService service;

    @GetMapping
    @PreAuthorize("hasAnyRole('RH', 'MANAGER', 'FINANCE', 'DG', 'ADMIN')")
    @Operation(summary = "Lister les grilles salariales")
    public ResponseEntity<PageResponse<GrilleSalarialeResponse>> rechercher(
            @RequestParam(required = false) String q,
            @RequestParam(required = false) Boolean actif,
            @PageableDefault(size = 20) Pageable pageable) {
        PageResponse<GrilleSalarialeResponse> response = service.rechercher(q, actif, pageable);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('RH', 'MANAGER', 'FINANCE', 'DG', 'ADMIN')")
    @Operation(summary = "Obtenir une grille salariale")
    public ResponseEntity<GrilleSalarialeResponse> obtenir(@PathVariable Long id) {
        GrilleSalarialeResponse response = service.obtenir(id);
        return ResponseEntity.ok(response);
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('RH', 'ADMIN')")
    @Operation(summary = "Créer une grille salariale")
    public ResponseEntity<GrilleSalarialeResponse> creer(@Valid @RequestBody GrilleSalarialeRequest request) {
        GrilleSalarialeResponse response = service.creer(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('RH', 'ADMIN')")
    @Operation(summary = "Modifier une grille salariale")
    public ResponseEntity<GrilleSalarialeResponse> modifier(
            @PathVariable Long id,
            @Valid @RequestBody GrilleSalarialeUpdateRequest request) {
        GrilleSalarialeResponse response = service.modifier(id, request);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/{id}/activer")
    @PreAuthorize("hasAnyRole('RH', 'ADMIN')")
    @Operation(summary = "Activer une grille salariale")
    public ResponseEntity<GrilleSalarialeResponse> activer(@PathVariable Long id) {
        GrilleSalarialeResponse response = service.activer(id);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/{id}/desactiver")
    @PreAuthorize("hasAnyRole('RH', 'ADMIN')")
    @Operation(summary = "Désactiver une grille salariale")
    public ResponseEntity<GrilleSalarialeResponse> desactiver(@PathVariable Long id) {
        GrilleSalarialeResponse response = service.desactiver(id);
        return ResponseEntity.ok(response);
    }
}
