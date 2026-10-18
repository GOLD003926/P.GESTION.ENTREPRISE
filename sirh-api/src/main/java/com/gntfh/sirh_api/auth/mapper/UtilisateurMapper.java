package com.gntfh.sirh_api.auth.mapper;

import com.gntfh.sirh_api.auth.dto.UtilisateurRequest;
import com.gntfh.sirh_api.auth.dto.UtilisateurResponse;
import com.gntfh.sirh_api.auth.dto.UtilisateurUpdateRequest;
import com.gntfh.sirh_api.auth.entity.Utilisateur;
import org.springframework.stereotype.Component;

/**
 * Conversion entité ↔ DTO, rien d'autre : aucune règle métier ici.
 * Le mot de passe arrive déjà haché (le hachage est une décision du service).
 */
@Component
public class UtilisateurMapper {

    public UtilisateurResponse toResponse(Utilisateur utilisateur) {
        return new UtilisateurResponse(
                utilisateur.getId(),
                utilisateur.getEmail(),
                utilisateur.getNom(),
                utilisateur.getPrenom(),
                utilisateur.getRole(),
                utilisateur.isActif(),
                utilisateur.getEmployeId(),
                utilisateur.getCreatedAt()
        );
    }

    public Utilisateur toEntity(UtilisateurRequest request, String motDePasseHache) {
        return Utilisateur.builder()
                .email(request.email().trim())
                .motDePasse(motDePasseHache)
                .nom(request.nom().trim())
                .prenom(request.prenom().trim())
                .role(request.role())
                .employeId(request.employeId())
                .actif(true)
                .build();
    }

    /** Applique les modifications sur l'entité existante (seuls les champs modifiables). */
    public void updateEntity(UtilisateurUpdateRequest request, Utilisateur utilisateur) {
        utilisateur.setNom(request.nom().trim());
        utilisateur.setPrenom(request.prenom().trim());
        utilisateur.setRole(request.role());
        utilisateur.setEmployeId(request.employeId());
    }
}
