package com.gntfh.sirh_api.employe.controller;

import com.gntfh.sirh_api.common.dto.PageResponse;
import com.gntfh.sirh_api.common.security.CurrentUser;
import com.gntfh.sirh_api.employe.dto.EmployeRequest;
import com.gntfh.sirh_api.employe.dto.EmployeResponse;
import com.gntfh.sirh_api.employe.dto.EmployeUpdateRequest;
import com.gntfh.sirh_api.employe.enums.StatutEmploye;
import com.gntfh.sirh_api.employe.service.EmployeService;
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
 * Contrôleur pour la gestion des employés.
 */
@RestController
@RequestMapping("/api/employes")
@Tag(name = "Employés", description = "Gestion des employés")
@RequiredArgsConstructor
public class EmployeController {

    private final EmployeService employeService;

    @GetMapping
    @PreAuthorize("hasAnyRole('RH', 'MANAGER', 'PAIE')")
    @Operation(summary = "Lister les employés")
    public ResponseEntity<PageResponse<?>> rechercher(
            @RequestParam(required = false) String q,
            @RequestParam(required = false) StatutEmploye statut,
            @RequestParam(required = false) Long departementId,
            @RequestParam(required = false) Long posteId,
            @ParameterObject @PageableDefault(size = 20, sort = "id") Pageable pageable,
            @AuthenticationPrincipal org.springframework.security.oauth2.jwt.Jwt jwt) {
        CurrentUser currentUser = CurrentUser.from(jwt);
        return ResponseEntity.ok(employeService.rechercher(q, statut, departementId, posteId, pageable, currentUser));
    }

    @GetMapping("/moi")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Mon dossier")
    public ResponseEntity<EmployeResponse> moi(@AuthenticationPrincipal org.springframework.security.oauth2.jwt.Jwt jwt) {
        CurrentUser currentUser = CurrentUser.from(jwt);
        return ResponseEntity.ok(employeService.moi(currentUser));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('RH', 'MANAGER', 'PAIE')")
    @Operation(summary = "Détail d'un employé")
    public ResponseEntity<?> obtenir(
            @PathVariable Long id,
            @AuthenticationPrincipal org.springframework.security.oauth2.jwt.Jwt jwt) {
        CurrentUser currentUser = CurrentUser.from(jwt);
        return ResponseEntity.ok(employeService.obtenir(id, currentUser));
    }

    @PostMapping
    @PreAuthorize("hasRole('RH')")
    @Operation(summary = "Créer un employé")
    public ResponseEntity<EmployeResponse> creer(
            @Valid @RequestBody EmployeRequest request,
            @AuthenticationPrincipal org.springframework.security.oauth2.jwt.Jwt jwt) {
        CurrentUser currentUser = CurrentUser.from(jwt);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(employeService.creer(request, currentUser));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('RH')")
    @Operation(summary = "Modifier un employé")
    public ResponseEntity<EmployeResponse> modifier(
            @PathVariable Long id,
            @Valid @RequestBody EmployeUpdateRequest request,
            @AuthenticationPrincipal org.springframework.security.oauth2.jwt.Jwt jwt) {
        CurrentUser currentUser = CurrentUser.from(jwt);
        return ResponseEntity.ok(employeService.modifier(id, request, currentUser));
    }

    @PostMapping("/{id}/suspendre")
    @PreAuthorize("hasRole('RH')")
    @Operation(summary = "Suspendre un employé")
    public ResponseEntity<Void> suspendre(
            @PathVariable Long id,
            @AuthenticationPrincipal org.springframework.security.oauth2.jwt.Jwt jwt) {
        CurrentUser currentUser = CurrentUser.from(jwt);
        employeService.suspendre(id, currentUser);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/{id}/reprendre")
    @PreAuthorize("hasRole('RH')")
    @Operation(summary = "Reprendre un employé")
    public ResponseEntity<Void> reprendre(
            @PathVariable Long id,
            @AuthenticationPrincipal org.springframework.security.oauth2.jwt.Jwt jwt) {
        CurrentUser currentUser = CurrentUser.from(jwt);
        employeService.reprendre(id, currentUser);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/{id}/fiche-pdf")
    @PreAuthorize("hasRole('RH')")
    @Operation(summary = "Télécharger la fiche salariale PDF")
    public ResponseEntity<byte[]> ficheSalarie(
            @PathVariable Long id,
            @AuthenticationPrincipal org.springframework.security.oauth2.jwt.Jwt jwt) {
        CurrentUser currentUser = CurrentUser.from(jwt);
        byte[] pdf = employeService.ficheSalarie(id, currentUser);
        return ResponseEntity.ok()
                .header("Content-Disposition", "attachment; filename=fiche-salarie.pdf")
                .body(pdf);
    }

    @GetMapping("/{id}/historique")
    @PreAuthorize("hasRole('RH')")
    @Operation(summary = "Historique des statuts")
    public ResponseEntity<?> historique(
            @PathVariable Long id,
            @AuthenticationPrincipal org.springframework.security.oauth2.jwt.Jwt jwt) {
        CurrentUser currentUser = CurrentUser.from(jwt);
        return ResponseEntity.ok(employeService.historique(id, currentUser));
    }
}
