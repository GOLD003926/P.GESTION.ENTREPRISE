package com.gntfh.sirh_api.contrat.controller;

import com.gntfh.sirh_api.common.audit.dto.HistoriqueStatutResponse;
import com.gntfh.sirh_api.common.dto.PageResponse;
import com.gntfh.sirh_api.common.security.CurrentUser;
import com.gntfh.sirh_api.contrat.dto.ContratRequest;
import com.gntfh.sirh_api.contrat.dto.ContratResponse;
import com.gntfh.sirh_api.contrat.dto.ContratUpdateRequest;
import com.gntfh.sirh_api.contrat.dto.FinContratRequest;
import com.gntfh.sirh_api.contrat.dto.RenouvellementRequest;
import com.gntfh.sirh_api.contrat.dto.TransformationRequest;
import com.gntfh.sirh_api.contrat.enums.StatutContrat;
import com.gntfh.sirh_api.contrat.enums.TypeContrat;
import com.gntfh.sirh_api.contrat.service.ContratService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/contrats")
@Tag(name = "Contrats", description = "Gestion des contrats")
@RequiredArgsConstructor
public class ContratController {

    private final ContratService contratService;

    @GetMapping
    @PreAuthorize("hasAnyRole('RH', 'MANAGER', 'PAIE')")
    @Operation(summary = "Lister les contrats")
    public ResponseEntity<PageResponse<ContratResponse>> lister(
            @RequestParam(required = false) Long employeId,
            @RequestParam(required = false) TypeContrat type,
            @RequestParam(required = false) StatutContrat statut,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate finAvant,
            @ParameterObject @PageableDefault(size = 20, sort = "dateDebut") Pageable pageable,
            @AuthenticationPrincipal Jwt jwt
    ) {
        CurrentUser currentUser = CurrentUser.from(jwt);
        return ResponseEntity.ok(contratService.rechercher(employeId, type, statut, finAvant, pageable, currentUser));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('RH', 'MANAGER', 'PAIE')")
    @Operation(summary = "Détail d'un contrat")
    public ResponseEntity<ContratResponse> obtenir(@PathVariable Long id, @AuthenticationPrincipal Jwt jwt) {
        CurrentUser currentUser = CurrentUser.from(jwt);
        return ResponseEntity.ok(contratService.obtenir(id, currentUser));
    }

    @GetMapping("/mes-contrats")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Mes contrats")
    public ResponseEntity<PageResponse<ContratResponse>> mesContrats(
            @ParameterObject @PageableDefault(size = 20, sort = "dateDebut") Pageable pageable,
            @AuthenticationPrincipal Jwt jwt
    ) {
        CurrentUser currentUser = CurrentUser.from(jwt);
        return ResponseEntity.ok(contratService.mesContrats(currentUser));
    }

    @PostMapping
    @PreAuthorize("hasRole('RH')")
    @Operation(summary = "Créer un contrat")
    public ResponseEntity<ContratResponse> creer(
            @Valid @RequestBody ContratRequest request,
            @AuthenticationPrincipal Jwt jwt
    ) {
        CurrentUser currentUser = CurrentUser.from(jwt);
        ContratResponse response = contratService.creer(request, currentUser);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('RH')")
    @Operation(summary = "Modifier un contrat")
    public ResponseEntity<ContratResponse> modifier(
            @PathVariable Long id,
            @Valid @RequestBody ContratUpdateRequest request,
            @AuthenticationPrincipal Jwt jwt
    ) {
        CurrentUser currentUser = CurrentUser.from(jwt);
        return ResponseEntity.ok(contratService.modifier(id, request, currentUser));
    }

    @PostMapping("/{id}/envoyer-signature")
    @PreAuthorize("hasRole('RH')")
    @Operation(summary = "Envoyer un contrat pour signature")
    public ResponseEntity<Void> envoyerPourSignature(@PathVariable Long id, @AuthenticationPrincipal Jwt jwt) {
        CurrentUser currentUser = CurrentUser.from(jwt);
        contratService.envoyerPourSignature(id, currentUser);
        return ResponseEntity.noContent().build();
    }

    @PostMapping(value = "/{id}/signature", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasRole('RH')")
    @Operation(summary = "Enregistrer un contrat signé")
    public ResponseEntity<Void> enregistrerSignature(
            @PathVariable Long id,
            @RequestPart("fichier") MultipartFile fichier,
            @RequestParam("dateSignature") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateSignature,
            @AuthenticationPrincipal Jwt jwt
    ) {
        CurrentUser currentUser = CurrentUser.from(jwt);
        contratService.enregistrerSignature(id, fichier, dateSignature, currentUser);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/renouveler")
    @PreAuthorize("hasRole('RH')")
    @Operation(summary = "Renouveler un contrat")
    public ResponseEntity<ContratResponse> renouveler(
            @PathVariable Long id,
            @Valid @RequestBody RenouvellementRequest request,
            @AuthenticationPrincipal Jwt jwt
    ) {
        CurrentUser currentUser = CurrentUser.from(jwt);
        return ResponseEntity.status(HttpStatus.CREATED).body(contratService.renouveler(id, request, currentUser));
    }

    @PostMapping("/{id}/transformer")
    @PreAuthorize("hasRole('RH')")
    @Operation(summary = "Transformer un contrat")
    public ResponseEntity<ContratResponse> transformer(
            @PathVariable Long id,
            @Valid @RequestBody TransformationRequest request,
            @AuthenticationPrincipal Jwt jwt
    ) {
        CurrentUser currentUser = CurrentUser.from(jwt);
        return ResponseEntity.status(HttpStatus.CREATED).body(contratService.transformer(id, request, currentUser));
    }

    @PostMapping("/{id}/terminer")
    @PreAuthorize("hasRole('RH')")
    @Operation(summary = "Terminer un contrat")
    public ResponseEntity<Void> terminer(
            @PathVariable Long id,
            @Valid @RequestBody FinContratRequest request,
            @AuthenticationPrincipal Jwt jwt
    ) {
        CurrentUser currentUser = CurrentUser.from(jwt);
        contratService.terminer(id, request, currentUser);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/annuler")
    @PreAuthorize("hasRole('RH')")
    @Operation(summary = "Annuler un contrat")
    public ResponseEntity<Void> annuler(@PathVariable Long id, @AuthenticationPrincipal Jwt jwt) {
        CurrentUser currentUser = CurrentUser.from(jwt);
        contratService.annuler(id, currentUser);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}/historique")
    @PreAuthorize("hasRole('RH')")
    @Operation(summary = "Historique des statuts du contrat")
    public ResponseEntity<List<HistoriqueStatutResponse>> historique(
            @PathVariable Long id,
            @AuthenticationPrincipal Jwt jwt
    ) {
        CurrentUser currentUser = CurrentUser.from(jwt);
        return ResponseEntity.ok(contratService.historique(id, currentUser));
    }
}
