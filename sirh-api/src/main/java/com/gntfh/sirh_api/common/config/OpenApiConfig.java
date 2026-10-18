package com.gntfh.sirh_api.common.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import org.springframework.context.annotation.Configuration;

/**
 * Description de l'API et schéma d'authentification pour Swagger.
 * Tous les endpoints sont protégés par défaut ; les endpoints publics (login)
 * l'indiquent avec @SecurityRequirements vide.
 */
@Configuration
@OpenAPIDefinition(
        info = @Info(
                title = "SIRH API",
                version = "0.1.0",
                description = "API du Système d'Information de Ressources Humaines : "
                        + "besoin, recrutement, embauche, contrat et onboarding."
        ),
        security = @SecurityRequirement(name = OpenApiConfig.BEARER_AUTH)
)
@SecurityScheme(
        name = OpenApiConfig.BEARER_AUTH,
        type = SecuritySchemeType.HTTP,
        scheme = "bearer",
        bearerFormat = "JWT",
        description = "Collez le accessToken obtenu via POST /api/auth/login (sans le mot « Bearer »)."
)
public class OpenApiConfig {

    public static final String BEARER_AUTH = "bearerAuth";
}
