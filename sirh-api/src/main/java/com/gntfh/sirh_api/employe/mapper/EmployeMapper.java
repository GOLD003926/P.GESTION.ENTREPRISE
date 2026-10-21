package com.gntfh.sirh_api.employe.mapper;

import com.gntfh.sirh_api.employe.dto.EmployeRequest;
import com.gntfh.sirh_api.employe.dto.EmployeResponse;
import com.gntfh.sirh_api.employe.dto.EmployeResumeResponse;
import com.gntfh.sirh_api.employe.dto.EmployeUpdateRequest;
import com.gntfh.sirh_api.employe.entity.Employe;
import org.springframework.stereotype.Component;

/**
 * Mapper pour l'entité Employe.
 */
@Component
public class EmployeMapper {

    public Employe toEntity(EmployeRequest request) {
        return Employe.builder()
                .nom(request.nom())
                .prenom(request.prenom())
                .emailPro(request.emailPro())
                .emailPerso(request.emailPerso())
                .telephone(request.telephone())
                .dateNaissance(request.dateNaissance())
                .adresse(request.adresse())
                .numeroSecuriteSociale(request.numeroSecuriteSociale())
                .build();
    }

    public void updateEntity(EmployeUpdateRequest request, Employe employe) {
        employe.setNom(request.nom());
        employe.setPrenom(request.prenom());
        employe.setEmailPro(request.emailPro());
        employe.setEmailPerso(request.emailPerso());
        employe.setTelephone(request.telephone());
        employe.setDateNaissance(request.dateNaissance());
        employe.setAdresse(request.adresse());
        employe.setNumeroSecuriteSociale(request.numeroSecuriteSociale());
    }

    public EmployeResponse toResponse(Employe employe) {
        return new EmployeResponse(
                employe.getId(),
                employe.getMatricule(),
                employe.getNom(),
                employe.getPrenom(),
                employe.getEmailPro(),
                employe.getEmailPerso(),
                employe.getTelephone(),
                employe.getDateNaissance(),
                employe.getAdresse(),
                employe.getNumeroSecuriteSociale(),
                employe.getDateEmbauche(),
                employe.getStatut(),
                employe.getCandidatureOrigine() != null ? employe.getCandidatureOrigine().getId() : null,
                null, // posteActuelId (calculé par le service)
                null, // posteActuelIntitule (calculé par le service)
                null, // departementActuelNom (calculé par le service)
                null, // managerActuelId (calculé par le service)
                null, // managerActuelNom (calculé par le service)
                employe.getCreatedAt()
        );
    }

    public EmployeResumeResponse toResumeResponse(Employe employe) {
        return new EmployeResumeResponse(
                employe.getId(),
                employe.getMatricule(),
                employe.getNom(),
                employe.getPrenom(),
                employe.getStatut(),
                employe.getDateEmbauche(),
                null // posteActuelIntitule (calculé par le service)
        );
    }
}
