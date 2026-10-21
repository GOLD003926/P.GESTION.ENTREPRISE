package com.gntfh.sirh_api.demande.mapper;

import com.gntfh.sirh_api.demande.dto.DemandeRecrutementRequest;
import com.gntfh.sirh_api.demande.dto.DemandeRecrutementResponse;
import com.gntfh.sirh_api.demande.entity.DemandeRecrutement;
import com.gntfh.sirh_api.demande.enums.RoleValidateur;
import com.gntfh.sirh_api.demande.enums.StatutDemande;
import org.springframework.stereotype.Component;

/**
 * Mapper pour l'entité DemandeRecrutement.
 * Note : les relations (poste, departement, demandeur) sont gérées par le service.
 */
@Component
public class DemandeRecrutementMapper {

    public DemandeRecrutement toEntity(DemandeRecrutementRequest request) {
        DemandeRecrutement demande = DemandeRecrutement.builder()
                .typeDemande(request.typeDemande())
                .employeRemplaceId(request.employeRemplaceId())
                .effectifDemande(request.effectifDemande())
                .motif(request.motif())
                .justification(request.justification())
                .dateSouhaitee(request.dateSouhaitee())
                .statut(StatutDemande.BROUILLON)
                .cycleCourant(0)
                .build();
        demande.setEffectifPourvu(0);
        return demande;
    }

    public DemandeRecrutementResponse toResponse(DemandeRecrutement entity) {
        RoleValidateur etapeCourante = calculerEtapeCourante(entity.getStatut());
        return new DemandeRecrutementResponse(
                entity.getId(),
                entity.getReference(),
                entity.getPoste() != null ? entity.getPoste().getId() : null,
                entity.getPoste() != null ? entity.getPoste().getIntitule() : null,
                entity.getDepartement() != null ? entity.getDepartement().getId() : null,
                entity.getDepartement() != null ? entity.getDepartement().getNom() : null,
                entity.getDemandeur() != null ? entity.getDemandeur().getId() : null,
                entity.getDemandeur() != null ? entity.getDemandeur().getNom() + " " + entity.getDemandeur().getPrenom() : null,
                entity.getTypeDemande(),
                entity.getEmployeRemplaceId(),
                entity.getEffectifDemande(),
                entity.getEffectifPourvu(),
                entity.getMotif(),
                entity.getJustification(),
                entity.getBudgetEstime(),
                entity.getDateSouhaitee(),
                entity.getStatut(),
                entity.getCycleCourant(),
                etapeCourante,
                entity.getCreatedAt(),
                entity.getUpdatedAt()
        );
    }

    public void updateEntity(DemandeRecrutementRequest request, DemandeRecrutement entity) {
        entity.setTypeDemande(request.typeDemande());
        entity.setEmployeRemplaceId(request.employeRemplaceId());
        entity.setEffectifDemande(request.effectifDemande());
        entity.setMotif(request.motif());
        entity.setJustification(request.justification());
        entity.setDateSouhaitee(request.dateSouhaitee());
    }

    private RoleValidateur calculerEtapeCourante(StatutDemande statut) {
        return switch (statut) {
            case EN_VALIDATION_RH -> RoleValidateur.RH;
            case EN_VALIDATION_FINANCE -> RoleValidateur.FINANCE;
            case EN_VALIDATION_DG -> RoleValidateur.DG;
            default -> null;
        };
    }
}
