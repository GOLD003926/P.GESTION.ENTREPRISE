package com.gntfh.sirh_api.auth.mapper;

import com.gntfh.sirh_api.auth.dto.UtilisateurResponse;
import com.gntfh.sirh_api.auth.entity.Utilisateur;
import org.springframework.stereotype.Component;

/** Conversion entité ↔ DTO, rien d'autre : aucune règle métier ici. */
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
}
