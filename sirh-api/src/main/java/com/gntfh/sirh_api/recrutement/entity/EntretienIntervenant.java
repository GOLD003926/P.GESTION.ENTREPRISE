package com.gntfh.sirh_api.recrutement.entity;

import com.gntfh.sirh_api.common.entity.AuditableEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Lien entre un entretien et un intervenant (utilisateur interne).
 * Hérite de AuditableEntity pour la traçabilité.
 */
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
@Table(name = "entretien_intervenant")
public class EntretienIntervenant extends AuditableEntity {

    @ManyToOne(fetch = jakarta.persistence.FetchType.LAZY)
    @JoinColumn(name = "entretien_id", nullable = false)
    private Entretien entretien;

    @ManyToOne(fetch = jakarta.persistence.FetchType.LAZY)
    @JoinColumn(name = "utilisateur_id", nullable = false)
    private com.gntfh.sirh_api.auth.entity.Utilisateur utilisateur;

    @Column(name = "role_intervenant", length = 50)
    private String roleIntervenant;

    @Builder
    private EntretienIntervenant(Entretien entretien, com.gntfh.sirh_api.auth.entity.Utilisateur utilisateur,
                                 String roleIntervenant) {
        this.entretien = entretien;
        this.utilisateur = utilisateur;
        this.roleIntervenant = roleIntervenant;
    }
}
