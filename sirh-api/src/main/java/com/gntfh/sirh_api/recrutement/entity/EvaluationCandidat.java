package com.gntfh.sirh_api.recrutement.entity;

import com.gntfh.sirh_api.common.entity.AuditableEntity;
import com.gntfh.sirh_api.recrutement.enums.Avis;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.Instant;

/**
 * Évaluation d'un candidat par un intervenant lors d'un entretien.
 * Le champ grilleCriteres contient du JSON : liste {critere, note}.
 * Hérite de AuditableEntity pour la traçabilité.
 */
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
@Table(name = "evaluation_candidat")
public class EvaluationCandidat extends AuditableEntity {

    @ManyToOne(fetch = jakarta.persistence.FetchType.LAZY)
    @JoinColumn(name = "entretien_id", nullable = false)
    private Entretien entretien;

    @ManyToOne(fetch = jakarta.persistence.FetchType.LAZY)
    @JoinColumn(name = "evaluateur_id", nullable = false)
    private com.gntfh.sirh_api.auth.entity.Utilisateur evaluateur;

    @Column(name = "note_globale", nullable = false, precision = 4, scale = 2)
    private BigDecimal noteGlobale;

    @Column(name = "grille_criteres", columnDefinition = "TEXT")
    private String grilleCriteres;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Avis avis;

    @Column(columnDefinition = "TEXT")
    private String commentaire;

    @Column(name = "date_evaluation", nullable = false)
    private Instant dateEvaluation;

    @Column(nullable = false)
    private boolean verrouillee = false;

    @Column(nullable = false)
    private boolean active = true;

    @ManyToOne(fetch = jakarta.persistence.FetchType.LAZY)
    @JoinColumn(name = "evaluation_precedente_id")
    private EvaluationCandidat evaluationPrecedente;

    @Builder
    private EvaluationCandidat(Entretien entretien, com.gntfh.sirh_api.auth.entity.Utilisateur evaluateur,
                                BigDecimal noteGlobale, String grilleCriteres, Avis avis,
                                String commentaire, Instant dateEvaluation, Boolean verrouillee,
                                Boolean active, EvaluationCandidat evaluationPrecedente) {
        this.entretien = entretien;
        this.evaluateur = evaluateur;
        this.noteGlobale = noteGlobale;
        this.grilleCriteres = grilleCriteres;
        this.avis = avis;
        this.commentaire = commentaire;
        this.dateEvaluation = dateEvaluation == null ? Instant.now() : dateEvaluation;
        this.verrouillee = verrouillee != null && verrouillee;
        this.active = active == null || active;
        this.evaluationPrecedente = evaluationPrecedente;
    }
}
