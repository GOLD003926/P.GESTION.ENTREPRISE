package com.gntfh.sirh_api.auth.config;

import com.gntfh.sirh_api.auth.entity.Utilisateur;
import com.gntfh.sirh_api.auth.enums.Role;
import com.gntfh.sirh_api.auth.repository.UtilisateurRepository;
import com.gntfh.sirh_api.common.config.SirhProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

/**
 * Au démarrage, crée le premier administrateur s'il n'existe aucun compte ADMIN.
 * Sans lui, personne ne pourrait se connecter pour créer les autres comptes.
 * Idempotent : redémarrer l'application ne crée pas de doublon.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class AdminInitializer implements ApplicationRunner {

    private final UtilisateurRepository utilisateurRepository;
    private final PasswordEncoder passwordEncoder;
    private final SirhProperties properties;

    @Override
    public void run(ApplicationArguments args) {
        if (utilisateurRepository.existsByRole(Role.ADMIN)) {
            return;
        }

        SirhProperties.Admin admin = properties.admin();
        Utilisateur utilisateur = Utilisateur.builder()
                .email(admin.email())
                .motDePasse(passwordEncoder.encode(admin.password()))
                .nom(admin.nom())
                .prenom(admin.prenom())
                .role(Role.ADMIN)
                .actif(true)
                .build();
        utilisateurRepository.save(utilisateur);

        // On ne logge jamais le mot de passe.
        log.warn("Administrateur initial créé : {}. Changez son mot de passe dès la première connexion.",
                admin.email());
    }
}
