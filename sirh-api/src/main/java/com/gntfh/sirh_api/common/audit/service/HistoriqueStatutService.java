package com.gntfh.sirh_api.common.audit.service;

import com.gntfh.sirh_api.common.audit.dto.HistoriqueStatutResponse;
import com.gntfh.sirh_api.common.audit.entity.HistoriqueStatut;
import com.gntfh.sirh_api.common.audit.enums.TypeEntite;
import com.gntfh.sirh_api.common.audit.repository.HistoriqueStatutRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class HistoriqueStatutService {

    private final HistoriqueStatutRepository historiqueStatutRepository;

    @Transactional
    public void enregistrer(TypeEntite type, Long entiteId, String ancienStatut, String nouveauStatut, String auteur, String commentaire) {
        HistoriqueStatut historique = HistoriqueStatut.builder()
                .entiteType(type.name())
                .entiteId(entiteId)
                .ancienStatut(ancienStatut)
                .nouveauStatut(nouveauStatut)
                .auteur(auteur)
                .dateChangement(Instant.now())
                .commentaire(commentaire)
                .build();

        historiqueStatutRepository.save(historique);
    }

    @Transactional(readOnly = true)
    public List<HistoriqueStatutResponse> lister(TypeEntite entiteType, Long entiteId) {
        List<HistoriqueStatut> historiques = historiqueStatutRepository
                .findByEntiteTypeAndEntiteIdOrderByDateChangementAsc(entiteType.name(), entiteId);

        return historiques.stream()
                .map(this::toResponse)
                .toList();
    }

    private HistoriqueStatutResponse toResponse(HistoriqueStatut historique) {
        return new HistoriqueStatutResponse(
                historique.getId(),
                historique.getEntiteType(),
                historique.getEntiteId(),
                historique.getAncienStatut(),
                historique.getNouveauStatut(),
                historique.getAuteur(),
                historique.getDateChangement(),
                historique.getCommentaire()
        );
    }
}
