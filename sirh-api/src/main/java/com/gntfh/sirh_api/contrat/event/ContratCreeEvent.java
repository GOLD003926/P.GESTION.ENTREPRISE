package com.gntfh.sirh_api.contrat.event;

import com.gntfh.sirh_api.contrat.enums.TypeContrat;

import java.time.LocalDate;

public record ContratCreeEvent(
        Long contratId,
        Long employeId,
        TypeContrat type,
        LocalDate dateDebut
) {
}
