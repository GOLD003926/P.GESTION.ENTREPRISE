package com.gntfh.sirh_api.common.audit.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.gntfh.sirh_api.common.audit.dto.JournalAuditResponse;
import com.gntfh.sirh_api.common.audit.entity.JournalAudit;
import com.gntfh.sirh_api.common.audit.enums.ActionAudit;
import com.gntfh.sirh_api.common.audit.enums.TypeEntite;
import com.gntfh.sirh_api.common.audit.repository.JournalAuditRepository;
import com.gntfh.sirh_api.common.dto.PageResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.persistence.criteria.Predicate;

import java.time.Instant;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuditService {

    private final JournalAuditRepository journalAuditRepository;
    private final JsonMapper jsonMapper;

    @Transactional
    public void journaliser(ActionAudit action, TypeEntite type, Long entiteId, Object avant, Object apres, String auteur) {
        String valeursAvant = null;
        String valeursApres = null;

        try {
            valeursAvant = avant != null ? jsonMapper.writeValueAsString(avant) : null;
            valeursApres = apres != null ? jsonMapper.writeValueAsString(apres) : null;
        } catch (JsonProcessingException e) {
            log.warn("Erreur lors de la sérialisation JSON pour l'audit (action: {}, entite: {}, entiteId: {}, auteur: {})",
                    action, type, entiteId, auteur);
            valeursAvant = "{\"erreur\":\"serialisation\"}";
            valeursApres = "{\"erreur\":\"serialisation\"}";
        }

        JournalAudit journal = JournalAudit.builder()
                .entiteType(type.name())
                .entiteId(entiteId)
                .action(action.name())
                .valeursAvant(valeursAvant)
                .valeursApres(valeursApres)
                .auteur(auteur)
                .dateAction(Instant.now())
                .build();

        journalAuditRepository.save(journal);
    }

    @Transactional(readOnly = true)
    public PageResponse<JournalAuditResponse> rechercher(TypeEntite entiteType, Long entiteId, String auteur, Pageable pageable) {
        Specification<JournalAudit> spec = (root, query, cb) -> {
            Predicate predicate = cb.conjunction();

            if (entiteType != null) {
                predicate = cb.and(predicate, cb.equal(root.get("entiteType"), entiteType.name()));
            }

            if (entiteId != null) {
                predicate = cb.and(predicate, cb.equal(root.get("entiteId"), entiteId));
            }

            if (auteur != null && !auteur.isBlank()) {
                predicate = cb.and(predicate, cb.like(cb.lower(root.get("auteur")), "%" + auteur.toLowerCase() + "%"));
            }

            return predicate;
        };

        Page<JournalAudit> page = journalAuditRepository.findAll(spec, pageable);

        return new PageResponse<>(
                page.getContent().stream().map(this::toResponse).toList(),
                page.getNumber(),
                page.getSize(),
                page.getTotalElements(),
                page.getTotalPages(),
                page.isFirst(),
                page.isLast()
        );
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
