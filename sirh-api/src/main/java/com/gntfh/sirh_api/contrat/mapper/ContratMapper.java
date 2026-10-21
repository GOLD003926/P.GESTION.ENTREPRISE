package com.gntfh.sirh_api.contrat.mapper;

import com.gntfh.sirh_api.contrat.dto.ContratResponse;
import com.gntfh.sirh_api.contrat.dto.ContratUpdateRequest;
import com.gntfh.sirh_api.contrat.entity.Contrat;
import com.gntfh.sirh_api.employe.entity.Employe;
import com.gntfh.sirh_api.organisation.entity.Poste;
import org.springframework.stereotype.Component;

import java.time.LocalDate;

@Component
public class ContratMapper {

    public ContratResponse toResponse(Contrat contrat) {
        Integer joursRestants = contrat.getDateFinPrevue() == null
                ? null
                : (int) (contrat.getDateFinPrevue().toEpochDay() - LocalDate.now().toEpochDay());

        String employeNom = contrat.getEmploye().getNom() + " " + contrat.getEmploye().getPrenom();

        return new ContratResponse(
                contrat.getId(),
                contrat.getNumero(),
                contrat.getEmploye().getId(),
                employeNom,
                contrat.getEmploye().getMatricule(),
                contrat.getType(),
                contrat.getNumeroRenouvellement(),
                contrat.getContratPrecedent() == null ? null : contrat.getContratPrecedent().getId(),
                contrat.getDateDebut(),
                contrat.getDateFinPrevue(),
                contrat.getDateFinReelle(),
                contrat.getSalaireBase(),
                contrat.getDevise(),
                contrat.getHorairesTravail(),
                contrat.getLieuTravail(),
                contrat.getPoste().getId(),
                contrat.getPoste().getIntitule(),
                contrat.getManager() == null ? null : contrat.getManager().getId(),
                contrat.getStatut(),
                contrat.getMotifFin(),
                contrat.getDocumentSigne() == null ? null : contrat.getDocumentSigne().getId(),
                contrat.getDateSignature(),
                joursRestants,
                contrat.getCreatedAt()
        );
    }

    public void updateEntity(
            Contrat contrat,
            ContratUpdateRequest request,
            Poste poste,
            Employe manager,
            LocalDate dateFinPrevue
    ) {
        contrat.setType(request.type());
        contrat.setPoste(poste);
        contrat.setManager(manager);
        contrat.setDateDebut(request.dateDebut());
        contrat.setDateFinPrevue(dateFinPrevue);
        contrat.setSalaireBase(request.salaireBase());
        contrat.setDevise(request.devise() == null ? "MGA" : request.devise());
        contrat.setHorairesTravail(request.horairesTravail());
        contrat.setLieuTravail(request.lieuTravail());
    }
}
