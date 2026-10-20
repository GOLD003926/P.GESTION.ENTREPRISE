package com.gntfh.sirh_api.demande.entity;

import com.gntfh.sirh_api.auth.entity.Utilisateur;
import com.gntfh.sirh_api.common.entity.AuditableEntity;
import com.gntfh.sirh_api.demande.enums.MotifDemande;
import com.gntfh.sirh_api.demande.enums.StatutDemande;
import com.gntfh.sirh_api.demande.enums.TypeDemande;
import com.gntfh.sirh_api.organisation.entity.Departement;
import com.gntfh.sirh_api.organisation.entity.Poste;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "demande_recrutement", indexes = {
    @Index(name = "idx_demande_statut", columnList = "statut"),
    @Index(name = "idx_demande_poste", columnList = "poste_id"),
    @Index(name = "idx_demande_demandeur", columnList = "demandeur_id")
})
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class DemandeRecrutement extends AuditableEntity {

    @Column(name = "reference", nullable = false, unique = true, length = 30)
    private String reference;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "poste_id", nullable = false)
    private Poste poste;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "departement_id", nullable = false)
    private Departement departement;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "demandeur_id", nullable = false)
    private Utilisateur demandeur;

    @Enumerated(EnumType.STRING)
    @Column(name = "type_demande", nullable = false, length = 20)
    private TypeDemande typeDemande;

    @Column(name = "employe_remplace_id")
    private Long employeRemplaceId;

    @Column(name = "effectif_demande", nullable = false)
    private Integer effectifDemande;

    @Column(name = "effectif_pourvu", nullable = false)
    private Integer effectifPourvu = 0;

    @Enumerated(EnumType.STRING)
    @Column(name = "motif", nullable = false, length = 30)
    private MotifDemande motif;

    @Column(name = "justification", columnDefinition = "TEXT")
    private String justification;

    @Column(name = "budget_estime", nullable = false, precision = 14, scale = 2)
    private BigDecimal budgetEstime;

    @Column(name = "date_souhaitee", nullable = false)
    private LocalDate dateSouhaitee;

    @Enumerated(EnumType.STRING)
    @Column(name = "statut", nullable = false, length = 30)
    private StatutDemande statut = StatutDemande.BROUILLON;

    @Column(name = "cycle_courant", nullable = false)
    private Integer cycleCourant = 0;

    @Builder
    @SuppressWarnings("java:S2637")
    private DemandeRecrutement(String reference, Poste poste, Departement departement, Utilisateur demandeur,
                               TypeDemande typeDemande, Long employeRemplaceId, Integer effectifDemande,
                               MotifDemande motif, String justification, BigDecimal budgetEstime,
                               LocalDate dateSouhaitee, StatutDemande statut, Integer cycleCourant) {
        this.reference = reference;
        this.poste = poste;
        this.departement = departement;
        this.demandeur = demandeur;
        this.typeDemande = typeDemande;
        this.employeRemplaceId = employeRemplaceId;
        this.effectifDemande = effectifDemande;
        this.motif = motif;
        this.justification = justification;
        this.budgetEstime = budgetEstime;
        this.dateSouhaitee = dateSouhaitee;
        this.statut = statut;
        this.cycleCourant = cycleCourant;
    }
}
