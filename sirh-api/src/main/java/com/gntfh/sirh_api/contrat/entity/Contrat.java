package com.gntfh.sirh_api.contrat.entity;

import com.gntfh.sirh_api.common.document.entity.Document;
import com.gntfh.sirh_api.common.entity.AuditableEntity;
import com.gntfh.sirh_api.contrat.enums.MotifFinContrat;
import com.gntfh.sirh_api.contrat.enums.StatutContrat;
import com.gntfh.sirh_api.contrat.enums.TypeContrat;
import com.gntfh.sirh_api.employe.entity.Employe;
import com.gntfh.sirh_api.organisation.entity.Poste;
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
import java.time.LocalDate;

/**
 * Entité représentant un contrat de travail.
 */
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
@Table(name = "contrat")
public class Contrat extends AuditableEntity {

    @Column(name = "numero", nullable = false, unique = true, length = 30)
    private String numero;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "employe_id", nullable = false)
    private Employe employe;

    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false, length = 10)
    private TypeContrat type;

    @Column(name = "numero_renouvellement", nullable = false)
    private Integer numeroRenouvellement;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "contrat_precedent_id")
    private Contrat contratPrecedent;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "poste_id", nullable = false)
    private Poste poste;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "manager_id")
    private Employe manager;

    @Column(name = "date_debut", nullable = false)
    private LocalDate dateDebut;

    @Column(name = "date_fin_prevue")
    private LocalDate dateFinPrevue;

    @Column(name = "date_fin_reelle")
    private LocalDate dateFinReelle;

    @Column(name = "salaire_base", nullable = false, precision = 14, scale = 2)
    private BigDecimal salaireBase;

    @Column(name = "devise", nullable = false, length = 3)
    private String devise = "MGA";

    @Column(name = "horaires_travail", nullable = false, length = 255)
    private String horairesTravail;

    @Column(name = "lieu_travail", nullable = false, length = 255)
    private String lieuTravail;

    @Enumerated(EnumType.STRING)
    @Column(name = "statut", nullable = false, length = 20)
    private StatutContrat statut = StatutContrat.BROUILLON;

    @Enumerated(EnumType.STRING)
    @Column(name = "motif_fin", length = 30)
    private MotifFinContrat motifFin;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "document_signe_id")
    private Document documentSigne;

    @Column(name = "date_signature")
    private LocalDate dateSignature;

    @Column(name = "alerte_fin_envoyee", nullable = false)
    private Boolean alerteFinEnvoyee = false;

    @Builder
    private Contrat(
            String numero,
            Employe employe,
            TypeContrat type,
            Integer numeroRenouvellement,
            Contrat contratPrecedent,
            Poste poste,
            Employe manager,
            LocalDate dateDebut,
            LocalDate dateFinPrevue,
            LocalDate dateFinReelle,
            BigDecimal salaireBase,
            String devise,
            String horairesTravail,
            String lieuTravail,
            StatutContrat statut,
            MotifFinContrat motifFin,
            Document documentSigne,
            LocalDate dateSignature,
            Boolean alerteFinEnvoyee
    ) {
        this.numero = numero;
        this.employe = employe;
        this.type = type;
        this.numeroRenouvellement = numeroRenouvellement == null ? 0 : numeroRenouvellement;
        this.contratPrecedent = contratPrecedent;
        this.poste = poste;
        this.manager = manager;
        this.dateDebut = dateDebut;
        this.dateFinPrevue = dateFinPrevue;
        this.dateFinReelle = dateFinReelle;
        this.salaireBase = salaireBase;
        this.devise = devise == null ? "MGA" : devise;
        this.horairesTravail = horairesTravail;
        this.lieuTravail = lieuTravail;
        this.statut = statut == null ? StatutContrat.BROUILLON : statut;
        this.motifFin = motifFin;
        this.documentSigne = documentSigne;
        this.dateSignature = dateSignature;
        this.alerteFinEnvoyee = alerteFinEnvoyee != null && alerteFinEnvoyee;
    }
}
