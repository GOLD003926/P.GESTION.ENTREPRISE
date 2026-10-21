package com.gntfh.sirh_api.onboarding.entity;

import com.gntfh.sirh_api.auth.entity.Utilisateur;
import com.gntfh.sirh_api.auth.enums.Role;
import com.gntfh.sirh_api.common.document.entity.Document;
import com.gntfh.sirh_api.common.entity.AuditableEntity;
import com.gntfh.sirh_api.onboarding.enums.StatutTache;
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
@Table(name = "onboarding_tache")
public class OnboardingTache extends AuditableEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "onboarding_id", nullable = false)
    private Onboarding onboarding;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "modele_tache_id")
    private ModeleTache modeleTache;

    @Column(name = "code", nullable = false, length = 40)
    private String code;

    @Column(name = "libelle", nullable = false, length = 200)
    private String libelle;

    @Column(name = "obligatoire", nullable = false)
    private Boolean obligatoire;

    @Enumerated(EnumType.STRING)
    @Column(name = "role_responsable", nullable = false, length = 30)
    private Role roleResponsable;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "assigne_a_id")
    private Utilisateur assigneA;

    @Column(name = "echeance", nullable = false)
    private LocalDate echeance;

    @Enumerated(EnumType.STRING)
    @Column(name = "statut", nullable = false, length = 20)
    private StatutTache statut = StatutTache.A_FAIRE;

    @Column(name = "date_realisation")
    private Instant dateRealisation;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "preuve_document_id")
    private Document preuveDocument;

    @Column(name = "commentaire", columnDefinition = "TEXT")
    private String commentaire;

    @Builder
    private OnboardingTache(Onboarding onboarding, ModeleTache modeleTache, String code, String libelle,
                            Boolean obligatoire, Role roleResponsable, Utilisateur assigneA, LocalDate echeance,
                            StatutTache statut, Instant dateRealisation, Document preuveDocument, String commentaire) {
        this.onboarding = onboarding;
        this.modeleTache = modeleTache;
        this.code = code;
        this.libelle = libelle;
        this.obligatoire = obligatoire;
        this.roleResponsable = roleResponsable;
        this.assigneA = assigneA;
        this.echeance = echeance;
        this.statut = statut == null ? StatutTache.A_FAIRE : statut;
        this.dateRealisation = dateRealisation;
        this.preuveDocument = preuveDocument;
        this.commentaire = commentaire;
    }
}
