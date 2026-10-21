package com.gntfh.sirh_api.recrutement.mapper;

import com.gntfh.sirh_api.recrutement.dto.CandidatRequest;
import com.gntfh.sirh_api.recrutement.dto.CandidatResponse;
import com.gntfh.sirh_api.recrutement.entity.Candidat;
import org.springframework.stereotype.Component;

/**
 * Conversion entité ↔ DTO pour Candidat.
 * Aucune règle métier ici.
 */
@Component
public class CandidatMapper {

    public CandidatResponse toResponse(Candidat candidat) {
        return new CandidatResponse(
                candidat.getId(),
                candidat.getNom(),
                candidat.getPrenom(),
                candidat.getEmail(),
                candidat.getTelephone(),
                candidat.getDateNaissance() != null ? candidat.getDateNaissance().toString() : null,
                candidat.getAdresse(),
                candidat.getNationalite(),
                candidat.isConsentementDonnees(),
                candidat.getCreatedAt()
        );
    }

    public Candidat toEntity(CandidatRequest request) {
        return Candidat.builder()
                .nom(request.nom())
                .prenom(request.prenom())
                .email(request.email())
                .telephone(request.telephone())
                .dateNaissance(request.dateNaissance())
                .adresse(request.adresse())
                .nationalite(request.nationalite())
                .consentementDonnees(request.consentementDonnees())
                .build();
    }

    public void updateEntity(CandidatRequest request, Candidat candidat) {
        candidat.setNom(request.nom());
        candidat.setPrenom(request.prenom());
        candidat.setTelephone(request.telephone());
        candidat.setDateNaissance(request.dateNaissance());
        candidat.setAdresse(request.adresse());
        candidat.setNationalite(request.nationalite());
        candidat.setConsentementDonnees(request.consentementDonnees());
    }
}
