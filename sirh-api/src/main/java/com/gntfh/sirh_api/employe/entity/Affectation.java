package com.gntfh.sirh_api.employe.entity;

import com.gntfh.sirh_api.common.entity.AuditableEntity;
import com.gntfh.sirh_api.employe.enums.MotifAffectation;
import com.gntfh.sirh_api.organisation.entity.Poste;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Entité représentant une affectation d'un employé à un poste.
 */
@Entity
@Table(name = "affectation")
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Affectation extends AuditableEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "employe_id", nullable = false)
    private Employe employe;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "poste_id", nullable = false)
    private Poste poste;

    @Column(name = "contrat_id")
    private Long contratId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "manager_id")
    private Employe manager;

    @Column(name = "date_debut", nullable = false)
    private LocalDate dateDebut;

    @Column(name = "date_fin")
    private LocalDate dateFin;

    @Column(name = "taux_occupation", nullable = false, precision = 5, scale = 2)
    private BigDecimal tauxOccupation = BigDecimal.valueOf(100);

    @Enumerated(EnumType.STRING)
    @Column(name = "motif", nullable = false, length = 20)
    private MotifAffectation motif;

    @Builder
    private Affectation(
            Employe employe,
            Poste poste,
            Long contratId,
            Employe manager,
            LocalDate dateDebut,
            LocalDate dateFin,
            BigDecimal tauxOccupation,
            MotifAffectation motif
    ) {
        this.employe = employe;
        this.poste = poste;
        this.contratId = contratId;
        this.manager = manager;
        this.dateDebut = dateDebut;
        this.dateFin = dateFin;
        this.tauxOccupation = tauxOccupation != null ? tauxOccupation : BigDecimal.valueOf(100);
        this.motif = motif;
    }
}
