package com.gntfh.sirh_api.auth.controller;

import com.gntfh.sirh_api.auth.dto.ResetPasswordRequest;
import com.gntfh.sirh_api.auth.dto.UtilisateurRequest;
import com.gntfh.sirh_api.auth.dto.UtilisateurResponse;
import com.gntfh.sirh_api.auth.dto.UtilisateurUpdateRequest;
import com.gntfh.sirh_api.auth.enums.Role;
import com.gntfh.sirh_api.auth.service.UtilisateurService;
import com.gntfh.sirh_api.common.dto.PageResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
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
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

/**
 * Gestion des comptes, réservée à l'ADMIN.
 * Convention : @PreAuthorize au niveau de la classe quand tout le contrôleur a la même règle,
 * au niveau de chaque méthode quand les règles diffèrent.
 */
@RestController
@RequestMapping("/api/utilisateurs")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
@Tag(name = "Utilisateurs", description = "Gestion des comptes (administrateur uniquement)")
public class UtilisateurController {

    private final UtilisateurService utilisateurService;

    @GetMapping
    @Operation(summary = "Lister les utilisateurs",
            description = "Recherche par texte (email, nom, prénom), filtres rôle et actif, pagination et tri "
                    + "(ex. ?q=rako&role=RH&actif=true&sort=nom,asc&page=0&size=20).")
    public PageResponse<UtilisateurResponse> lister(
            @RequestParam(required = false) String q,
            @RequestParam(required = false) Role role,
            @RequestParam(required = false) Boolean actif,
            @ParameterObject @PageableDefault(size = 20, sort = "nom") Pageable pageable) {
        return utilisateurService.rechercher(q, role, actif, pageable);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Obtenir un utilisateur")
    public UtilisateurResponse obtenir(@PathVariable Long id) {
        return utilisateurService.obtenir(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Créer un utilisateur")
    public UtilisateurResponse creer(@Valid @RequestBody UtilisateurRequest request) {
        return utilisateurService.creer(request);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Modifier un utilisateur",
            description = "Modifie nom, prénom, rôle et employé lié. L'email est immuable.")
    public UtilisateurResponse modifier(@PathVariable Long id,
                                        @Valid @RequestBody UtilisateurUpdateRequest request,
                                        @AuthenticationPrincipal Jwt jwt) {
        return utilisateurService.modifier(id, request, jwt.getSubject());
    }

    @PostMapping("/{id}/activer")
    @Operation(summary = "Activer un compte")
    public UtilisateurResponse activer(@PathVariable Long id) {
        return utilisateurService.activer(id);
    }

    @PostMapping("/{id}/desactiver")
    @Operation(summary = "Désactiver un compte",
            description = "Interdit pour son propre compte. Il n'y a pas de suppression : on désactive.")
    public UtilisateurResponse desactiver(@PathVariable Long id, @AuthenticationPrincipal Jwt jwt) {
        return utilisateurService.desactiver(id, jwt.getSubject());
    }

    @PutMapping("/{id}/mot-de-passe")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Réinitialiser le mot de passe d'un utilisateur")
    public void reinitialiserMotDePasse(@PathVariable Long id, @Valid @RequestBody ResetPasswordRequest request) {
        utilisateurService.reinitialiserMotDePasse(id, request);
    }
}
