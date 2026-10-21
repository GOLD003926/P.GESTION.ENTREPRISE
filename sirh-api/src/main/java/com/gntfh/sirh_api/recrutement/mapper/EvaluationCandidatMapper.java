package com.gntfh.sirh_api.recrutement.mapper;

import com.gntfh.sirh_api.recrutement.dto.CritereNote;
import com.gntfh.sirh_api.recrutement.dto.EvaluationCandidatRequest;
import com.gntfh.sirh_api.recrutement.dto.EvaluationCandidatResponse;
import com.gntfh.sirh_api.recrutement.dto.EvaluationCandidatUpdateRequest;
import com.gntfh.sirh_api.recrutement.entity.EvaluationCandidat;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import tools.jackson.databind.json.JsonMapper;

import java.util.List;

/**
 * Conversion entité ↔ DTO pour EvaluationCandidat.
 * La liste criteres est convertie en JSON avec le bean Jackson 3 auto-configuré (JsonMapper).
 * Aucune règle métier ici.
 */
@Component
@RequiredArgsConstructor
public class EvaluationCandidatMapper {

    private final JsonMapper jsonMapper;

    public EvaluationCandidatResponse toResponse(EvaluationCandidat evaluation) {
        List<CritereNote> criteres = null;
        if (evaluation.getGrilleCriteres() != null) {
            criteres = jsonMapper.readValue(evaluation.getGrilleCriteres(),
                    jsonMapper.getTypeFactory().constructCollectionType(List.class, CritereNote.class));
        }

        return new EvaluationCandidatResponse(
                evaluation.getId(),
                evaluation.getEntretien() != null ? evaluation.getEntretien().getId() : null,
                evaluation.getEvaluateur() != null ? evaluation.getEvaluateur().getId() : null,
                evaluation.getEvaluateur() != null
                        ? evaluation.getEvaluateur().getNom() + " " + evaluation.getEvaluateur().getPrenom()
                        : null,
                evaluation.getNoteGlobale(),
                criteres,
                evaluation.getAvis() != null ? evaluation.getAvis().name() : null,
                evaluation.getCommentaire(),
                evaluation.getDateEvaluation(),
                evaluation.isVerrouillee(),
                evaluation.isActive(),
                evaluation.getEvaluationPrecedente() != null ? evaluation.getEvaluationPrecedente().getId() : null
        );
    }

    public EvaluationCandidat toEntity(EvaluationCandidatRequest request,
                                         com.gntfh.sirh_api.recrutement.entity.Entretien entretien,
                                         com.gntfh.sirh_api.auth.entity.Utilisateur evaluateur) {
        String grilleCriteresJson = null;
        if (request.criteres() != null && !request.criteres().isEmpty()) {
            grilleCriteresJson = jsonMapper.writeValueAsString(request.criteres());
        }

        return EvaluationCandidat.builder()
                .entretien(entretien)
                .evaluateur(evaluateur)
                .noteGlobale(request.noteGlobale())
                .grilleCriteres(grilleCriteresJson)
                .avis(com.gntfh.sirh_api.recrutement.enums.Avis.valueOf(request.avis()))
                .commentaire(request.commentaire())
                .dateEvaluation(java.time.Instant.now())
                .verrouillee(false)
                .active(true)
                .evaluationPrecedente(null)
                .build();
    }

    public void updateEntity(EvaluationCandidatUpdateRequest request, EvaluationCandidat evaluation) {
        evaluation.setNoteGlobale(request.noteGlobale());

        if (request.criteres() != null && !request.criteres().isEmpty()) {
            String grilleCriteresJson = jsonMapper.writeValueAsString(request.criteres());
            evaluation.setGrilleCriteres(grilleCriteresJson);
        }

        evaluation.setAvis(com.gntfh.sirh_api.recrutement.enums.Avis.valueOf(request.avis()));
        evaluation.setCommentaire(request.commentaire());
    }
}
