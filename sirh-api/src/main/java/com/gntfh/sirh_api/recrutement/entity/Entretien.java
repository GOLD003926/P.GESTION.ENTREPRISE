package com.gntfh.sirh_api.recrutement.entity;

import com.gntfh.sirh_api.common.entity.AuditableEntity;
import com.gntfh.sirh_api.recrutement.enums.StatutEntretien;
import com.gntfh.sirh_api.recrutement.enums.TypeEntretien;
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
 * Entretien de sélection pour une candidature.
 * Hérite de AuditableEntity pour la traçabilité.
 */
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
@Table(name = "entretien")
public class Entretien extends AuditableEntity {

    @ManyToOne(fetch = jakarta.persistence.FetchType.LAZY)
    @JoinColumn(name = "candidature_id", nullable = false)
    private Candidature candidature;

    @Column(nullable = false)
    private Integer niveau;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private TypeEntretien type;

    @Column(name = "date_heure", nullable = false)
    private Instant dateHeure;

    @Column(name = "duree_minutes", nullable = false)
    private Integer dureeMinutes = 60;

    @Column(name = "lieu_ou_lien", length = 255)
    private String lieuOuLien;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private StatutEntretien statut = StatutEntretien.PLANIFIE;

    @Column(name = "motif_annulation", length = 500)
    private String motifAnnulation;

    @Builder
    private Entretien(Candidature candidature, Integer niveau, TypeEntretien type,
                     Instant dateHeure, Integer dureeMinutes, String lieuOuLien,
                     StatutEntretien statut, String motifAnnulation) {
        this.candidature = candidature;
        this.niveau = niveau;
        this.type = type;
        this.dateHeure = dateHeure;
        this.dureeMinutes = dureeMinutes == null ? 60 : dureeMinutes;
        this.lieuOuLien = lieuOuLien;
        this.statut = statut == null ? StatutEntretien.PLANIFIE : statut;
        this.motifAnnulation = motifAnnulation;
    }
}
