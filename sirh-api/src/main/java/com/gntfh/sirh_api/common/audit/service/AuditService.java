package com.gntfh.sirh_api.common.audit.service;

import com.gntfh.sirh_api.common.audit.dto.JournalAuditResponse;
import com.gntfh.sirh_api.common.audit.entity.JournalAudit;
import com.gntfh.sirh_api.common.audit.enums.ActionAudit;
import com.gntfh.sirh_api.common.audit.enums.TypeEntite;
import com.gntfh.sirh_api.common.audit.repository.JournalAuditRepository;
import com.gntfh.sirh_api.common.dto.PageResponse;
import jakarta.persistence.criteria.Predicate;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.json.JsonMapper;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Journal d'audit : enregistre l'état « avant » et « après » d'un objet métier au format JSON.
 * Les objets passés sont des Response DTO (jamais des entités, pour éviter les proxys Hibernate).
 * Une panne de sérialisation ne doit jamais faire échouer l'opération métier appelante.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AuditService {

    private static final String MARQUEUR_ERREUR = "{\"erreur\":\"serialisation\"}";

    private final JournalAuditRepository journalAuditRepository;
    // Bean auto-configuré par Spring Boot 4 (Jackson 3, package tools.jackson).
    private final JsonMapper jsonMapper;

    @Transactional
    public void journaliser(ActionAudit action, TypeEntite type, Long entiteId,
                            Object avant, Object apres, String auteur) {
        JournalAudit journal = JournalAudit.builder()
                .entiteType(type.name())
                .entiteId(entiteId)
                .action(action.name())
                .valeursAvant(serialiser(avant, "avant", action, type, entiteId))
                .valeursApres(serialiser(apres, "apres", action, type, entiteId))
                .auteur(auteur)
                .dateAction(Instant.now())
                .build();
        journalAuditRepository.save(journal);
    }

    @Transactional(readOnly = true)
    public PageResponse<JournalAuditResponse> rechercher(TypeEntite entiteType, Long entiteId,
                                                         String auteur, Pageable pageable) {
        Specification<JournalAudit> spec = (root, query, cb) -> {
            List<Predicate> filtres = new ArrayList<>();
            if (entiteType != null) {
                filtres.add(cb.equal(root.get("entiteType"), entiteType.name()));
            }
            if (entiteId != null) {
                filtres.add(cb.equal(root.get("entiteId"), entiteId));
            }
            if (auteur != null && !auteur.isBlank()) {
                filtres.add(cb.like(cb.lower(root.get("auteur")),
                        "%" + auteur.trim().toLowerCase(Locale.ROOT) + "%"));
            }
            return cb.and(filtres.toArray(new Predicate[0]));
        };
        return PageResponse.from(journalAuditRepository.findAll(spec, pageable), this::toResponse);
    }

    private String serialiser(Object valeur, String champ, ActionAudit action, TypeEntite type, Long entiteId) {
        if (valeur == null) {
            return null;
        }
        try {
            return jsonMapper.writeValueAsString(valeur);
        } catch (JacksonException e) {
            // Le message de l'exception n'est pas loggé : il pourrait contenir des données personnelles.
            log.warn("Sérialisation JSON impossible pour le journal d'audit ({} sur {} {}, champ {})",
                    action, type, entiteId, champ);
            return MARQUEUR_ERREUR;
        }
    }

    private JournalAuditResponse toResponse(JournalAudit journal) {
        return new JournalAuditResponse(
                journal.getId(),
                journal.getEntiteType(),
                journal.getEntiteId(),
                journal.getAction(),
                journal.getValeursAvant(),
                journal.getValeursApres(),
                journal.getAuteur(),
                journal.getDateAction()
        );
    }
}
