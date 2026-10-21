package com.gntfh.sirh_api.embauche.dto;

public record ReponsePropositionResultat(
        PropositionEmbaucheResponse proposition,
        Long employeId,
        String matricule,
        Long contratId
) {
}
