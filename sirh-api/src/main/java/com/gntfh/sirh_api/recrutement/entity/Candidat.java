package com.gntfh.sirh_api.recrutement.entity;

import com.gntfh.sirh_api.common.entity.AuditableEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.util.Locale;

/**
 * Candidat externe potentiel pour une offre d'emploi.
 * Hérite de AuditableEntity pour la traçabilité.
 */
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
@Table(name = "candidat")
public class Candidat extends AuditableEntity {

    @Column(nullable = false, length = 100)
    private String nom;

    @Column(nullable = false, length = 100)
    private String prenom;

    @Column(nullable = false, length = 150, unique = true)
    private String email;

    @Column(length = 30)
    private String telephone;

    @Column
    private LocalDate dateNaissance;

    @Column(length = 255)
    private String adresse;

    @Column(length = 60)
    private String nationalite;

    @Column(nullable = false)
    private boolean consentementDonnees = false;

    @Builder
    private Candidat(String nom, String prenom, String email, String telephone,
                     LocalDate dateNaissance, String adresse, String nationalite,
                     Boolean consentementDonnees) {
        this.nom = nom;
        this.prenom = prenom;
        this.email = email;
        this.telephone = telephone;
        this.dateNaissance = dateNaissance;
        this.adresse = adresse;
        this.nationalite = nationalite;
        this.consentementDonnees = consentementDonnees != null && consentementDonnees;
    }

    /**
     * Garantit que l'email est toujours enregistré en minuscules.
     */
    @PrePersist
    @PreUpdate
    void normaliserEmail() {
        if (email != null) {
            email = email.trim().toLowerCase(Locale.ROOT);
        }
    }
}
