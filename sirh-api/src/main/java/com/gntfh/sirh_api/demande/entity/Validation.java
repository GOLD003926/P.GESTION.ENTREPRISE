package com.gntfh.sirh_api.demande.entity;

import com.gntfh.sirh_api.auth.entity.Utilisateur;
import com.gntfh.sirh_api.common.entity.AuditableEntity;
import com.gntfh.sirh_api.demande.enums.DecisionValidation;
import com.gntfh.sirh_api.demande.enums.RoleValidateur;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;

@Entity
@Table(name = "validation", uniqueConstraints = {
    @UniqueConstraint(name = "uk_validation_demande_cycle_ordre", columnNames = {"demande_id", "cycle", "ordre"})
})
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Validation extends AuditableEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "demande_id", nullable = false)
    private DemandeRecrutement demande;

    @Column(name = "cycle", nullable = false)
    private Integer cycle;

    @Column(name = "ordre", nullable = false)
    private Integer ordre;

    @Enumerated(EnumType.STRING)
    @Column(name = "role_validateur", nullable = false, length = 20)
    private RoleValidateur roleValidateur;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "validateur_id")
    private Utilisateur validateur;

    @Enumerated(EnumType.STRING)
    @Column(name = "decision", nullable = false, length = 20)
    private DecisionValidation decision = DecisionValidation.EN_ATTENTE;

    @Column(name = "commentaire", columnDefinition = "TEXT")
    private String commentaire;

    @Column(name = "date_decision")
    private Instant dateDecision;

    @Builder
    @SuppressWarnings("java:S2637")
    private Validation(DemandeRecrutement demande, Integer cycle, Integer ordre, RoleValidateur roleValidateur,
                      Utilisateur validateur, DecisionValidation decision, String commentaire, Instant dateDecision) {
        this.demande = demande;
        this.cycle = cycle;
        this.ordre = ordre;
        this.roleValidateur = roleValidateur;
        this.validateur = validateur;
        this.decision = decision;
        this.commentaire = commentaire;
        this.dateDecision = dateDecision;
    }
}
