package com.gntfh.sirh_api.auth.service;

import com.gntfh.sirh_api.auth.config.JwtConfig;
import com.gntfh.sirh_api.auth.entity.Utilisateur;
import com.gntfh.sirh_api.common.config.SirhProperties;
import lombok.RequiredArgsConstructor;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;

/** Fabrique les jetons. La vérification est faite par le JwtDecoder de JwtConfig, pas ici. */
@Service
@RequiredArgsConstructor
public class JwtService {

    private final JwtEncoder jwtEncoder;
    private final SirhProperties properties;

    public record Token(String value, Instant expiresAt) {
    }

    public Token generer(Utilisateur utilisateur) {
        Instant now = Instant.now();
        Instant expiresAt = now.plus(Duration.ofMinutes(properties.jwt().expirationMinutes()));

        JwtClaimsSet claims = JwtClaimsSet.builder()
                .issuer(JwtConfig.ISSUER)
                .subject(utilisateur.getEmail())
                .issuedAt(now)
                .expiresAt(expiresAt)
                .claim(JwtConfig.CLAIM_USER_ID, utilisateur.getId())
                .claim(JwtConfig.CLAIM_ROLE, utilisateur.getRole().name())
                .build();

        JwsHeader header = JwsHeader.with(MacAlgorithm.HS256).build();
        String value = jwtEncoder.encode(JwtEncoderParameters.from(header, claims)).getTokenValue();
        return new Token(value, expiresAt);
    }
}
