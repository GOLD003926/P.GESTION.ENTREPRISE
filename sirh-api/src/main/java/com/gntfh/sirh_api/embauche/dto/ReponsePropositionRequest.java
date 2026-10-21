package com.gntfh.sirh_api.embauche.dto;

import com.gntfh.sirh_api.embauche.enums.ReponseCandidat;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record ReponsePropositionRequest(
        @NotNull(message = "La réponse du candidat est obligatoire")
        ReponseCandidat reponse,
        @Size(max = 500, message = "Le motif de refus ne doit pas dépasser 500 caractères")
        String motifRefus,
        @Size(max = 500, message = "Le commentaire ne doit pas dépasser 500 caractères")
        String commentaire
) {
    @AssertTrue(message = "Le motif est obligatoire lorsque la réponse est un refus")
    public boolean motifRefusObligatoirePourRefus() {
        return reponse != ReponseCandidat.REFUSER
                || (motifRefus != null && !motifRefus.isBlank());
    }
}
