package com.gntfh.sirh_api.embauche.controller;

import com.gntfh.sirh_api.common.audit.dto.HistoriqueStatutResponse;
import com.gntfh.sirh_api.common.dto.PageResponse;
import com.gntfh.sirh_api.common.security.CurrentUser;
import com.gntfh.sirh_api.embauche.dto.DerogationRequest;
import com.gntfh.sirh_api.embauche.dto.PropositionEmbaucheRequest;
import com.gntfh.sirh_api.embauche.dto.PropositionEmbaucheResponse;
import com.gntfh.sirh_api.embauche.dto.PropositionEmbaucheUpdateRequest;
import com.gntfh.sirh_api.embauche.dto.ReponsePropositionRequest;
import com.gntfh.sirh_api.embauche.dto.ReponsePropositionResultat;
import com.gntfh.sirh_api.embauche.enums.StatutProposition;
import com.gntfh.sirh_api.embauche.service.EmbaucheService;
import com.gntfh.sirh_api.embauche.service.PropositionEmbaucheService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;
import java.util.List;

@Validated
@RestController
@RequestMapping("/api/propositions")
@Tag(name = "Propositions d'embauche", description = "Préparation, réponse et suivi des propositions d'embauche")
public class PropositionEmbaucheController {

    private final PropositionEmbaucheService propositionService;
    private final EmbaucheService embaucheService;

    public PropositionEmbaucheController(
            PropositionEmbaucheService propositionService,
            EmbaucheService embaucheService
    ) {
        this.propositionService = propositionService;
        this.embaucheService = embaucheService;
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('RH', 'MANAGER', 'DG')")
    @Operation(summary = "Rechercher les propositions d'embauche")
    public PageResponse<PropositionEmbaucheResponse> rechercher(
            @RequestParam(required = false) Long candidatureId,
            @RequestParam(required = false) StatutProposition statut,
            @ParameterObject @PageableDefault(size = 20, sort = "id") Pageable pageable,
            @AuthenticationPrincipal Jwt jwt
    ) {
        return propositionService.rechercher(candidatureId, statut, pageable, CurrentUser.from(jwt));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('RH', 'MANAGER', 'DG')")
    @Operation(summary = "Obtenir une proposition d'embauche")
    public PropositionEmbaucheResponse obtenir(
            @PathVariable Long id,
            @AuthenticationPrincipal Jwt jwt
    ) {
        return propositionService.obtenir(id, CurrentUser.from(jwt));
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasRole('RH')")
    @Operation(summary = "Créer un brouillon de proposition")
    public PropositionEmbaucheResponse creer(
            @Valid @RequestBody PropositionEmbaucheRequest request,
            @AuthenticationPrincipal Jwt jwt
    ) {
        return propositionService.creer(request, CurrentUser.from(jwt));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('RH')")
    @Operation(summary = "Modifier un brouillon de proposition")
    public PropositionEmbaucheResponse modifier(
            @PathVariable Long id,
            @Valid @RequestBody PropositionEmbaucheUpdateRequest request,
            @AuthenticationPrincipal Jwt jwt
    ) {
        return propositionService.modifier(id, request, CurrentUser.from(jwt));
    }

    @PostMapping("/{id}/derogation")
    @PreAuthorize("hasRole('DG')")
    @Operation(summary = "Valider une dérogation de salaire")
    public PropositionEmbaucheResponse validerDerogation(
            @PathVariable Long id,
            @Valid @RequestBody DerogationRequest request,
            @AuthenticationPrincipal Jwt jwt
    ) {
        return propositionService.validerDerogation(id, request, CurrentUser.from(jwt));
    }

    @PostMapping("/{id}/envoyer")
    @PreAuthorize("hasRole('RH')")
    @Operation(summary = "Envoyer une proposition au candidat")
    public PropositionEmbaucheResponse envoyer(
            @PathVariable Long id,
            @AuthenticationPrincipal Jwt jwt
    ) {
        return propositionService.envoyer(id, CurrentUser.from(jwt));
    }

    @PostMapping("/{id}/reponse")
    @PreAuthorize("hasRole('RH')")
    @Operation(summary = "Enregistrer la réponse du candidat")
    public ReponsePropositionResultat repondre(
            @PathVariable Long id,
            @Valid @RequestBody ReponsePropositionRequest request,
            @AuthenticationPrincipal Jwt jwt
    ) {
        return embaucheService.repondre(id, request, CurrentUser.from(jwt));
    }

    @PostMapping("/{id}/retirer")
    @PreAuthorize("hasRole('RH')")
    @Operation(summary = "Retirer une proposition")
    public PropositionEmbaucheResponse retirer(
            @PathVariable Long id,
            @AuthenticationPrincipal Jwt jwt
    ) {
        return propositionService.retirer(id, CurrentUser.from(jwt));
    }

    @GetMapping("/{id}/historique")
    @PreAuthorize("hasAnyRole('RH', 'MANAGER', 'DG')")
    @Operation(summary = "Consulter l'historique d'une proposition")
    public List<HistoriqueStatutResponse> historique(
            @PathVariable Long id,
            @AuthenticationPrincipal Jwt jwt
    ) {
        return propositionService.historique(id, CurrentUser.from(jwt));
    }
}
