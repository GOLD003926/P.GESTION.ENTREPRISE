package com.gntfh.sirh_api.auth.service;

import com.gntfh.sirh_api.auth.dto.ChangePasswordRequest;
import com.gntfh.sirh_api.auth.dto.LoginRequest;
import com.gntfh.sirh_api.auth.dto.TokenResponse;
import com.gntfh.sirh_api.auth.dto.UtilisateurResponse;
import com.gntfh.sirh_api.auth.entity.Utilisateur;
import com.gntfh.sirh_api.auth.mapper.UtilisateurMapper;
import com.gntfh.sirh_api.auth.repository.UtilisateurRepository;
import com.gntfh.sirh_api.common.exception.BusinessRuleException;
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
        return utilisateurMapper.toResponse(trouverActif(email));
    }

    /**
     * Changement de son propre mot de passe.
     * Un mauvais mot de passe actuel renvoie 422 (règle métier) et surtout PAS 401 :
     * le front déconnecte l'utilisateur sur toute réponse 401.
     */
    @Transactional
    public void changerMotDePasse(String email, ChangePasswordRequest request) {
        Utilisateur utilisateur = trouverActif(email);

        if (!passwordEncoder.matches(request.motDePasseActuel(), utilisateur.getMotDePasse())) {
            throw new BusinessRuleException("Le mot de passe actuel est incorrect.");
        }
        if (request.motDePasseActuel().equals(request.nouveauMotDePasse())) {
            throw new BusinessRuleException("Le nouveau mot de passe doit être différent de l'ancien.");
        }
        utilisateur.setMotDePasse(passwordEncoder.encode(request.nouveauMotDePasse()));
    }

    private Utilisateur trouverActif(String email) {
        Utilisateur utilisateur = utilisateurRepository.findByEmailIgnoreCase(email)
                .orElseThrow(() -> ResourceNotFoundException.of("Utilisateur", email));
        verifierActif(utilisateur);
        return utilisateur;
    }

    private void verifierActif(Utilisateur utilisateur) {
        if (!utilisateur.isActif()) {
            throw new DisabledException("Compte désactivé");
        }
    }
}
