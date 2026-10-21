package com.gntfh.sirh_api.recrutement.entity;

import com.gntfh.sirh_api.common.entity.AuditableEntity;
import com.gntfh.sirh_api.recrutement.enums.StatutOffre;
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

import java.time.LocalDate;

/**
 * Offre d'emploi publiée à partir d'une demande de recrutement validée.
 * Hérite de AuditableEntity pour la traçabilité.
 */
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
@Table(name = "offre_emploi")
public class OffreEmploi extends AuditableEntity {

    @Column(nullable = false, unique = true, length = 30)
    private String reference;

    @ManyToOne(fetch = jakarta.persistence.FetchType.LAZY)
    @JoinColumn(name = "demande_id", nullable = false)
    private com.gntfh.sirh_api.demande.entity.DemandeRecrutement demande;

    @Column(nullable = false, length = 150)
    private String titre;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String description;

    @Column(columnDefinition = "TEXT")
    private String profilRecherche;

    @Column(nullable = false)
    private Integer nombrePostes;

    @Column(nullable = false)
    private Integer nombreNiveauxSelection = 3;

    @Column(length = 100)
    private String canalPublication;

    @Column
    private LocalDate datePublication;

    @Column
    private LocalDate dateCloture;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private StatutOffre statut = StatutOffre.BROUILLON;

    @Builder
    private OffreEmploi(String reference, com.gntfh.sirh_api.demande.entity.DemandeRecrutement demande,
                        String titre, String description, String profilRecherche,
                        Integer nombrePostes, Integer nombreNiveauxSelection,
                        String canalPublication, LocalDate datePublication, LocalDate dateCloture,
                        StatutOffre statut) {
        this.reference = reference;
        this.demande = demande;
        this.titre = titre;
        this.description = description;
        this.profilRecherche = profilRecherche;
        this.nombrePostes = nombrePostes;
        this.nombreNiveauxSelection = nombreNiveauxSelection == null ? 3 : nombreNiveauxSelection;
        this.canalPublication = canalPublication;
        this.datePublication = datePublication;
        this.dateCloture = dateCloture;
        this.statut = statut == null ? StatutOffre.BROUILLON : statut;
    }
}
