package com.gntfh.sirh_api.auth.controller;

import com.gntfh.sirh_api.auth.dto.LoginRequest;
import com.gntfh.sirh_api.auth.dto.TokenResponse;
import com.gntfh.sirh_api.auth.dto.UtilisateurResponse;
import com.gntfh.sirh_api.auth.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirements;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** Couche HTTP uniquement : routes, validation, délégation au service. */
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Tag(name = "Authentification", description = "Connexion et profil de l'utilisateur connecté")
public class AuthController {

    private final AuthService authService;

    @PostMapping("/login")
    @SecurityRequirements   // vide = endpoint public (pas de cadenas dans Swagger)
    @Operation(summary = "Se connecter",
            description = "Retourne un jeton JWT à fournir dans l'en-tête Authorization: Bearer <jeton>.")
    public TokenResponse login(@Valid @RequestBody LoginRequest request) {
        return authService.login(request);
    }

    @GetMapping("/me")
    @Operation(summary = "Qui suis-je ?", description = "Profil de l'utilisateur identifié par le jeton.")
    public UtilisateurResponse me(@AuthenticationPrincipal Jwt jwt) {
        return authService.profil(jwt.getSubject());
    }
}
