package com.gntfh.sirh_api.employe.entity;

import com.gntfh.sirh_api.common.entity.AuditableEntity;
import com.gntfh.sirh_api.employe.enums.StatutEmploye;
import com.gntfh.sirh_api.recrutement.entity.Candidature;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

/**
 * Entité représentant un employé.
 */
@Entity
@Table(name = "employe")
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Employe extends AuditableEntity {

    @Column(name = "matricule", nullable = false, unique = true, length = 30)
    private String matricule;

    @Column(name = "nom", nullable = false, length = 100)
    private String nom;

    @Column(name = "prenom", nullable = false, length = 100)
    private String prenom;

    @Column(name = "email_pro", length = 150)
    private String emailPro;

    @Column(name = "email_perso", length = 150)
    private String emailPerso;

    @Column(name = "telephone", length = 30)
    private String telephone;

    @Column(name = "date_naissance")
    private LocalDate dateNaissance;

    @Column(name = "adresse", length = 255)
    private String adresse;

    @Column(name = "numero_securite_sociale", length = 30)
    private String numeroSecuriteSociale;

    @Column(name = "date_embauche")
    private LocalDate dateEmbauche;

    @Enumerated(EnumType.STRING)
    @Column(name = "statut", nullable = false, length = 20)
    private StatutEmploye statut = StatutEmploye.PRE_EMBAUCHE;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "candidature_origine_id", unique = true)
    private Candidature candidatureOrigine;

    @Builder
    private Employe(
            String matricule,
            String nom,
            String prenom,
            String emailPro,
            String emailPerso,
            String telephone,
            LocalDate dateNaissance,
            String adresse,
            String numeroSecuriteSociale,
            LocalDate dateEmbauche,
            StatutEmploye statut,
            Candidature candidatureOrigine
    ) {
        this.matricule = matricule;
        this.nom = nom;
        this.prenom = prenom;
        this.emailPro = emailPro;
        this.emailPerso = emailPerso;
        this.telephone = telephone;
        this.dateNaissance = dateNaissance;
        this.adresse = adresse;
        this.numeroSecuriteSociale = numeroSecuriteSociale;
        this.dateEmbauche = dateEmbauche;
        this.statut = statut != null ? statut : StatutEmploye.PRE_EMBAUCHE;
        this.candidatureOrigine = candidatureOrigine;
    }
}
