package com.gntfh.sirh_api.auth.entity;

import com.gntfh.sirh_api.auth.enums.Role;
import com.gntfh.sirh_api.common.entity.AuditableEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Locale;

/**
 * Compte de connexion. Ne pas confondre avec Employe : un utilisateur peut exister sans employé
 * (l'admin, par exemple) et inversement (un salarié sans accès au logiciel).
 * Le lien se fait par employeId, facultatif.
 *
 * Conventions d'entité (à reproduire dans tous les modules) :
 *  - hérite de AuditableEntity (id, version, dates et auteurs)
 *  - constructeur sans argument protégé (exigé par JPA, interdit aux autres)
 *  - création via le builder, jamais via "new"
 *  - aucune logique métier ici : elle vit dans les services
 */
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
@Table(name = "utilisateur")
public class Utilisateur extends AuditableEntity {

    @Column(nullable = false, length = 150)
    private String email;

    /** Hash BCrypt, jamais le mot de passe en clair. Ne doit jamais sortir dans un DTO. */
    @Column(name = "mot_de_passe", nullable = false, length = 100)
    private String motDePasse;

    @Column(nullable = false, length = 100)
    private String nom;

    @Column(nullable = false, length = 100)
    private String prenom;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private Role role;

    @Column(nullable = false)
    private boolean actif = true;

    /** Identifiant de l'employé lié (facultatif, sans clé étrangère pour l'instant). */
    @Column(name = "employe_id")
    private Long employeId;

    @Builder
    private Utilisateur(String email, String motDePasse, String nom, String prenom,
                        Role role, Boolean actif, Long employeId) {
        this.email = email;
        this.motDePasse = motDePasse;
        this.nom = nom;
        this.prenom = prenom;
        this.role = role;
        this.actif = actif == null || actif;
        this.employeId = employeId;
    }

    /** Garantit que l'email est toujours enregistré en minuscules, quel que soit le chemin d'écriture. */
    @PrePersist
    @PreUpdate
    void normaliserEmail() {
        if (email != null) {
            email = email.trim().toLowerCase(Locale.ROOT);
        }
    }
}
