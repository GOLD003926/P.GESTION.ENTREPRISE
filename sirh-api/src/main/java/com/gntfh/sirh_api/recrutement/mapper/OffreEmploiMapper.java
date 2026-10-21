package com.gntfh.sirh_api.recrutement.mapper;

import com.gntfh.sirh_api.recrutement.dto.OffreEmploiRequest;
import com.gntfh.sirh_api.recrutement.dto.OffreEmploiResponse;
import com.gntfh.sirh_api.recrutement.dto.OffreEmploiUpdateRequest;
import com.gntfh.sirh_api.recrutement.entity.OffreEmploi;
import org.springframework.stereotype.Component;

/**
 * Conversion entité ↔ DTO pour OffreEmploi.
 * Aucune règle métier ici.
 */
@Component
public class OffreEmploiMapper {

    public OffreEmploiResponse toResponse(OffreEmploi offre) {
        return new OffreEmploiResponse(
                offre.getId(),
                offre.getReference(),
                offre.getDemande() != null ? offre.getDemande().getId() : null,
                offre.getDemande() != null ? offre.getDemande().getReference() : null,
                offre.getDemande() != null && offre.getDemande().getPoste() != null
                        ? offre.getDemande().getPoste().getIntitule() : null,
                offre.getTitre(),
                offre.getDescription(),
                offre.getProfilRecherche(),
                offre.getNombrePostes(),
                offre.getNombreNiveauxSelection(),
                offre.getCanalPublication(),
                offre.getDatePublication(),
                offre.getDateCloture(),
                offre.getStatut() != null ? offre.getStatut().name() : null,
                null, // nombreCandidatures : calculé par le service
                offre.getCreatedAt()
        );
    }

    public OffreEmploi toEntity(OffreEmploiRequest request, com.gntfh.sirh_api.demande.entity.DemandeRecrutement demande,
                                  String reference) {
        return OffreEmploi.builder()
                .reference(reference)
                .demande(demande)
                .titre(request.titre())
                .description(request.description())
                .profilRecherche(request.profilRecherche())
                .nombrePostes(request.nombrePostes())
                .nombreNiveauxSelection(request.nombreNiveauxSelection())
                .canalPublication(request.canalPublication())
                .datePublication(null)
                .dateCloture(request.dateCloture())
                .statut(com.gntfh.sirh_api.recrutement.enums.StatutOffre.BROUILLON)
                .build();
    }

    public void updateEntity(OffreEmploiUpdateRequest request, OffreEmploi offre) {
        offre.setTitre(request.titre());
        offre.setDescription(request.description());
        offre.setProfilRecherche(request.profilRecherche());
        offre.setNombrePostes(request.nombrePostes());
        offre.setNombreNiveauxSelection(request.nombreNiveauxSelection());
        offre.setCanalPublication(request.canalPublication());
        offre.setDateCloture(request.dateCloture());
    }
}
