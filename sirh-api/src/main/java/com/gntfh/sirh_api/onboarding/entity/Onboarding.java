package com.gntfh.sirh_api.onboarding.entity;

import com.gntfh.sirh_api.auth.entity.Utilisateur;
import com.gntfh.sirh_api.common.entity.AuditableEntity;
import com.gntfh.sirh_api.employe.entity.Employe;
import com.gntfh.sirh_api.onboarding.enums.StatutOnboarding;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;
import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
@Table(name = "onboarding")
public class Onboarding extends AuditableEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "employe_id", nullable = false)
    private Employe employe;

    @Column(name = "contrat_id", nullable = false, unique = true)
    private Long contratId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "modele_id", nullable = false)
    private ModeleOnboarding modele;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "responsable_rh_id")
    private Utilisateur responsableRh;

    @Column(name = "date_debut", nullable = false)
    private LocalDate dateDebut;

    @Column(name = "date_cible", nullable = false)
    private LocalDate dateCible;

    @Enumerated(EnumType.STRING)
    @Column(name = "statut", nullable = false, length = 20)
    private StatutOnboarding statut = StatutOnboarding.A_DEMARRER;

    @Column(name = "date_cloture")
    private Instant dateCloture;

    @Builder
    private Onboarding(Employe employe, Long contratId, ModeleOnboarding modele, Utilisateur responsableRh,
                       LocalDate dateDebut, LocalDate dateCible, StatutOnboarding statut, Instant dateCloture) {
        this.employe = employe;
        this.contratId = contratId;
        this.modele = modele;
        this.responsableRh = responsableRh;
        this.dateDebut = dateDebut;
        this.dateCible = dateCible;
        this.statut = statut == null ? StatutOnboarding.A_DEMARRER : statut;
        this.dateCloture = dateCloture;
    }
}
