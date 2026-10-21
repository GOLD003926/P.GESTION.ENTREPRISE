package com.gntfh.sirh_api.embauche.entity;

import com.gntfh.sirh_api.auth.entity.Utilisateur;
import com.gntfh.sirh_api.common.document.entity.Document;
import com.gntfh.sirh_api.common.entity.AuditableEntity;
import com.gntfh.sirh_api.contrat.enums.TypeContrat;
import com.gntfh.sirh_api.employe.entity.Employe;
import com.gntfh.sirh_api.embauche.enums.StatutProposition;
import com.gntfh.sirh_api.organisation.entity.Poste;
import com.gntfh.sirh_api.recrutement.entity.Candidature;
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

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
@Table(name = "proposition_embauche")
public class PropositionEmbauche extends AuditableEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "candidature_id", nullable = false)
    private Candidature candidature;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "poste_id", nullable = false)
    private Poste poste;

    @Enumerated(EnumType.STRING)
    @Column(name = "type_contrat_propose", nullable = false, length = 10)
    private TypeContrat typeContratPropose = TypeContrat.ESSAI;

    @Column(name = "salaire_propose", nullable = false, precision = 14, scale = 2)
    private BigDecimal salairePropose;

    @Column(name = "devise", nullable = false, length = 3)
    private String devise = "MGA";

    @Column(name = "horaires_travail", nullable = false, length = 255)
    private String horairesTravail;

    @Column(name = "lieu_travail", nullable = false, length = 255)
    private String lieuTravail;

    @Column(name = "date_debut_prevue", nullable = false)
    private LocalDate dateDebutPrevue;

    @Column(name = "duree_mois")
    private Integer dureeMois;

    @Column(name = "date_limite_reponse", nullable = false)
    private LocalDate dateLimiteReponse;

    @Column(name = "nb_prolongations_delai", nullable = false)
    private Integer nbProlongationsDelai = 0;

    @Enumerated(EnumType.STRING)
    @Column(name = "statut", nullable = false, length = 20)
    private StatutProposition statut = StatutProposition.BROUILLON;

    @Column(name = "date_envoi")
    private Instant dateEnvoi;

    @Column(name = "date_reponse")
    private Instant dateReponse;

    @Column(name = "motif_refus", length = 500)
    private String motifRefus;

    @Column(name = "derogation_requise", nullable = false)
    private Boolean derogationRequise = false;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "derogation_validee_par_id")
    private Utilisateur derogationValideePar;

    @Column(name = "date_derogation")
    private Instant dateDerogation;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "document_id")
    private Document document;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "employe_id")
    private Employe employe;

    @Builder
    private PropositionEmbauche(
            Candidature candidature,
            Poste poste,
            TypeContrat typeContratPropose,
            BigDecimal salairePropose,
            String devise,
            String horairesTravail,
            String lieuTravail,
            LocalDate dateDebutPrevue,
            Integer dureeMois,
            LocalDate dateLimiteReponse,
            Integer nbProlongationsDelai,
            StatutProposition statut,
            Instant dateEnvoi,
            Instant dateReponse,
            String motifRefus,
            Boolean derogationRequise,
            Utilisateur derogationValideePar,
            Instant dateDerogation,
            Document document,
            Employe employe
    ) {
        this.candidature = candidature;
        this.poste = poste;
        this.typeContratPropose = typeContratPropose == null ? TypeContrat.ESSAI : typeContratPropose;
        this.salairePropose = salairePropose;
        this.devise = devise == null ? "MGA" : devise;
        this.horairesTravail = horairesTravail;
        this.lieuTravail = lieuTravail;
        this.dateDebutPrevue = dateDebutPrevue;
        this.dureeMois = dureeMois;
        this.dateLimiteReponse = dateLimiteReponse;
        this.nbProlongationsDelai = nbProlongationsDelai == null ? 0 : nbProlongationsDelai;
        this.statut = statut == null ? StatutProposition.BROUILLON : statut;
        this.dateEnvoi = dateEnvoi;
        this.dateReponse = dateReponse;
        this.motifRefus = motifRefus;
        this.derogationRequise = derogationRequise != null && derogationRequise;
        this.derogationValideePar = derogationValideePar;
        this.dateDerogation = dateDerogation;
        this.document = document;
        this.employe = employe;
    }
}
