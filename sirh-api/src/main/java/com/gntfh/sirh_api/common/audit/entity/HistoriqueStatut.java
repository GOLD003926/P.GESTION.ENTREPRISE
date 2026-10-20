package com.gntfh.sirh_api.common.audit.entity;

import com.gntfh.sirh_api.common.entity.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
@Table(name = "historique_statut")
public class HistoriqueStatut extends BaseEntity {

    @Column(name = "entite_type", nullable = false, length = 50)
    private String entiteType;

    @Column(name = "entite_id", nullable = false)
    private Long entiteId;

    @Column(name = "ancien_statut", length = 40)
    private String ancienStatut;

    @Column(name = "nouveau_statut", nullable = false, length = 40)
    private String nouveauStatut;

    @Column(nullable = false, length = 150)
    private String auteur;

    @Column(name = "date_changement", nullable = false)
    private Instant dateChangement;

    @Column(columnDefinition = "TEXT")
    private String commentaire;

    @Builder
    private HistoriqueStatut(String entiteType, Long entiteId, String ancienStatut,
                              String nouveauStatut, String auteur, Instant dateChangement,
                              String commentaire) {
        this.entiteType = entiteType;
        this.entiteId = entiteId;
        this.ancienStatut = ancienStatut;
        this.nouveauStatut = nouveauStatut;
        this.auteur = auteur;
        this.dateChangement = dateChangement;
        this.commentaire = commentaire;
    }
}
