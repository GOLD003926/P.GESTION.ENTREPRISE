package com.gntfh.sirh_api.auth.service;

import com.gntfh.sirh_api.auth.dto.LoginRequest;
import com.gntfh.sirh_api.auth.dto.TokenResponse;
import com.gntfh.sirh_api.auth.dto.UtilisateurResponse;
import com.gntfh.sirh_api.auth.entity.Utilisateur;
import com.gntfh.sirh_api.auth.mapper.UtilisateurMapper;
import com.gntfh.sirh_api.auth.repository.UtilisateurRepository;
import com.gntfh.sirh_api.common.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Connexion et profil. Toute la logique est ici : le contrôleur ne décide de rien.
 */
@Service
@RequiredArgsConstructor
public class AuthService {

    private final UtilisateurRepository utilisateurRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final UtilisateurMapper utilisateurMapper;

    @Transactional(readOnly = true)
    public TokenResponse login(LoginRequest request) {
        // Même erreur que l'email soit inconnu ou le mot de passe faux :
        // on ne révèle jamais lequel des deux est en cause.
        Utilisateur utilisateur = utilisateurRepository.findByEmailIgnoreCase(request.email().trim())
                .filter(u -> passwordEncoder.matches(request.motDePasse(), u.getMotDePasse()))
                .orElseThrow(() -> new BadCredentialsException("Identifiants invalides"));

        // Le statut du compte n'est vérifié qu'APRÈS le mot de passe : on ne révèle
        // l'état d'un compte qu'à quelqu'un qui en connaît le mot de passe.
        verifierActif(utilisateur);

        JwtService.Token token = jwtService.generer(utilisateur);
        return new TokenResponse(token.value(), "Bearer", token.expiresAt(), utilisateurMapper.toResponse(utilisateur));
    }

    /** Profil de l'utilisateur connecté, relu en base (pas depuis le jeton) pour refléter l'état actuel. */
    @Transactional(readOnly = true)
    public UtilisateurResponse profil(String email) {
        Utilisateur utilisateur = utilisateurRepository.findByEmailIgnoreCase(email)
                .orElseThrow(() -> ResourceNotFoundException.of("Utilisateur", email));
        verifierActif(utilisateur);
        return utilisateurMapper.toResponse(utilisateur);
    }

    private void verifierActif(Utilisateur utilisateur) {
        if (!utilisateur.isActif()) {
            throw new DisabledException("Compte désactivé");
        }
    }
}
