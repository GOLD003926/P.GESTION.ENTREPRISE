package com.gntfh.sirh_api.embauche.mapper;

import com.gntfh.sirh_api.embauche.dto.PropositionEmbaucheRequest;
import com.gntfh.sirh_api.embauche.dto.PropositionEmbaucheResponse;
import com.gntfh.sirh_api.embauche.dto.PropositionEmbaucheUpdateRequest;
import com.gntfh.sirh_api.embauche.entity.PropositionEmbauche;
import com.gntfh.sirh_api.organisation.entity.GrilleSalariale;
import com.gntfh.sirh_api.organisation.entity.Poste;
import com.gntfh.sirh_api.recrutement.entity.Candidature;
import org.springframework.stereotype.Component;

@Component
public class PropositionEmbaucheMapper {

    public PropositionEmbauche toEntity(
            PropositionEmbaucheRequest request,
            Candidature candidature,
            Poste poste
    ) {
        return PropositionEmbauche.builder()
                .candidature(candidature)
                .poste(poste)
                .typeContratPropose(request.typeContratPropose())
                .salairePropose(request.salairePropose())
                .devise(request.devise())
                .horairesTravail(request.horairesTravail())
                .lieuTravail(request.lieuTravail())
                .dateDebutPrevue(request.dateDebutPrevue())
                .dureeMois(request.dureeMois())
                .dateLimiteReponse(request.dateLimiteReponse())
                .build();
    }

    public void updateEntity(PropositionEmbauche proposition, PropositionEmbaucheUpdateRequest request) {
        proposition.setTypeContratPropose(request.typeContratPropose());
        proposition.setSalairePropose(request.salairePropose());
        proposition.setDevise(request.devise());
        proposition.setHorairesTravail(request.horairesTravail());
        proposition.setLieuTravail(request.lieuTravail());
        proposition.setDateDebutPrevue(request.dateDebutPrevue());
        proposition.setDureeMois(request.dureeMois());
        if (request.dateLimiteReponse() != null) {
            proposition.setDateLimiteReponse(request.dateLimiteReponse());
        }
    }

    public PropositionEmbaucheResponse toResponse(PropositionEmbauche proposition) {
        GrilleSalariale grille = proposition.getPoste().getGrilleSalariale();
        String nomCandidat = proposition.getCandidature().getCandidat().getPrenom()
                + " " + proposition.getCandidature().getCandidat().getNom();

        return new PropositionEmbaucheResponse(
                proposition.getId(),
                proposition.getCandidature().getId(),
                nomCandidat,
                proposition.getPoste().getId(),
                proposition.getPoste().getIntitule(),
                proposition.getTypeContratPropose(),
                proposition.getSalairePropose(),
                proposition.getDevise(),
                grille.getSalaireMin(),
                grille.getSalaireMax(),
                proposition.getHorairesTravail(),
                proposition.getLieuTravail(),
                proposition.getDateDebutPrevue(),
                proposition.getDureeMois(),
                proposition.getDateLimiteReponse(),
                proposition.getNbProlongationsDelai(),
                proposition.getStatut(),
                proposition.getDateEnvoi(),
                proposition.getDateReponse(),
                proposition.getMotifRefus(),
                proposition.getDerogationRequise(),
                proposition.getDerogationValideePar() != null,
                proposition.getDocument() == null ? null : proposition.getDocument().getId(),
                proposition.getEmploye() == null ? null : proposition.getEmploye().getId(),
                proposition.getCreatedAt()
        );
    }
}
