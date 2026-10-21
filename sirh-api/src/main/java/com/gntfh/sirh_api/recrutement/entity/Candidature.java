package com.gntfh.sirh_api.recrutement.entity;

import com.gntfh.sirh_api.common.entity.AuditableEntity;
import com.gntfh.sirh_api.common.document.entity.Document;
import com.gntfh.sirh_api.recrutement.enums.DecisionFinale;
import com.gntfh.sirh_api.recrutement.enums.StatutCandidature;
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

import java.time.Instant;

/**
 * Candidature d'un candidat à une offre d'emploi.
 * Hérite de AuditableEntity pour la traçabilité.
 */
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
@Table(name = "candidature")
public class Candidature extends AuditableEntity {

    @ManyToOne(fetch = jakarta.persistence.FetchType.LAZY)
    @JoinColumn(name = "candidat_id", nullable = false)
    private Candidat candidat;

    @ManyToOne(fetch = jakarta.persistence.FetchType.LAZY)
    @JoinColumn(name = "offre_id", nullable = false)
    private OffreEmploi offre;

    @ManyToOne(fetch = jakarta.persistence.FetchType.LAZY)
    @JoinColumn(name = "cv_document_id")
    private Document cvDocument;

    @Column(columnDefinition = "TEXT")
    private String lettreMotivation;

    @Column(name = "date_depot", nullable = false)
    private Instant dateDepot;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private StatutCandidature statut = StatutCandidature.RECUE;

    @Column(name = "score_preselection")
    private Integer scorePreselection;

    @Column(name = "motif_refus", length = 500)
    private String motifRefus;

    @Enumerated(EnumType.STRING)
    @Column(name = "decision_finale", length = 20)
    private DecisionFinale decisionFinale;

    @Column(name = "rang_liste_attente")
    private Integer rangListeAttente;

    @ManyToOne(fetch = jakarta.persistence.FetchType.LAZY)
    @JoinColumn(name = "decideur_id")
    private com.gntfh.sirh_api.auth.entity.Utilisateur decideur;

    @Column(name = "date_decision")
    private Instant dateDecision;

    @Column(name = "motif_decision", columnDefinition = "TEXT")
    private String motifDecision;

    @Builder
    private Candidature(Candidat candidat, OffreEmploi offre, Document cvDocument,
                       String lettreMotivation, Instant dateDepot, StatutCandidature statut,
                       Integer scorePreselection, String motifRefus, DecisionFinale decisionFinale,
                       Integer rangListeAttente, com.gntfh.sirh_api.auth.entity.Utilisateur decideur,
                       Instant dateDecision, String motifDecision) {
        this.candidat = candidat;
        this.offre = offre;
        this.cvDocument = cvDocument;
        this.lettreMotivation = lettreMotivation;
        this.dateDepot = dateDepot == null ? Instant.now() : dateDepot;
        this.statut = statut == null ? StatutCandidature.RECUE : statut;
        this.scorePreselection = scorePreselection;
        this.motifRefus = motifRefus;
        this.decisionFinale = decisionFinale;
        this.rangListeAttente = rangListeAttente;
        this.decideur = decideur;
        this.dateDecision = dateDecision;
        this.motifDecision = motifDecision;
    }
}
