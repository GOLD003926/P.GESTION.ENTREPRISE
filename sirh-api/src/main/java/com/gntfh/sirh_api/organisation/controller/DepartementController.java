package com.gntfh.sirh_api.organisation.controller;

import com.gntfh.sirh_api.common.dto.PageResponse;
import com.gntfh.sirh_api.organisation.dto.DepartementRequest;
import com.gntfh.sirh_api.organisation.dto.DepartementResponse;
import com.gntfh.sirh_api.organisation.dto.DepartementUpdateRequest;
import com.gntfh.sirh_api.organisation.service.DepartementService;
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
 * Contrôleur pour la gestion des départements.
 */
@RestController
@RequestMapping("/api/departements")
@RequiredArgsConstructor
@Tag(name = "Départements", description = "Gestion des départements de l'entreprise")
public class DepartementController {

    private final DepartementService service;

    @GetMapping
    @PreAuthorize("hasAnyRole('RH', 'MANAGER', 'FINANCE', 'DG', 'ADMIN')")
    @Operation(summary = "Lister les départements")
    public ResponseEntity<PageResponse<DepartementResponse>> rechercher(
            @RequestParam(required = false) String q,
            @RequestParam(required = false) Boolean actif,
            @RequestParam(required = false) Long parentId,
            @PageableDefault(size = 20) Pageable pageable) {
        PageResponse<DepartementResponse> response = service.rechercher(q, actif, parentId, pageable);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('RH', 'MANAGER', 'FINANCE', 'DG', 'ADMIN')")
    @Operation(summary = "Obtenir un département")
    public ResponseEntity<DepartementResponse> obtenir(@PathVariable Long id) {
        DepartementResponse response = service.obtenir(id);
        return ResponseEntity.ok(response);
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('RH', 'ADMIN')")
    @Operation(summary = "Créer un département")
    public ResponseEntity<DepartementResponse> creer(@Valid @RequestBody DepartementRequest request) {
        DepartementResponse response = service.creer(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('RH', 'ADMIN')")
    @Operation(summary = "Modifier un département")
    public ResponseEntity<DepartementResponse> modifier(
            @PathVariable Long id,
            @Valid @RequestBody DepartementUpdateRequest request) {
        DepartementResponse response = service.modifier(id, request);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/{id}/activer")
    @PreAuthorize("hasAnyRole('RH', 'ADMIN')")
    @Operation(summary = "Activer un département")
    public ResponseEntity<DepartementResponse> activer(@PathVariable Long id) {
        DepartementResponse response = service.activer(id);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/{id}/desactiver")
    @PreAuthorize("hasAnyRole('RH', 'ADMIN')")
    @Operation(summary = "Désactiver un département")
    public ResponseEntity<DepartementResponse> desactiver(@PathVariable Long id) {
        DepartementResponse response = service.desactiver(id);
        return ResponseEntity.ok(response);
    }
}
