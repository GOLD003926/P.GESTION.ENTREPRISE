package com.gntfh.sirh_api.onboarding.entity;

import com.gntfh.sirh_api.common.entity.AuditableEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
@Table(name = "modele_onboarding")
public class ModeleOnboarding extends AuditableEntity {

    @Column(name = "nom", nullable = false, length = 100)
    private String nom;

    @Column(name = "type_contrat", length = 10)
    private String typeContrat;

    @Column(name = "actif", nullable = false)
    private Boolean actif = true;

    @Builder
    private ModeleOnboarding(String nom, String typeContrat, Boolean actif) {
        this.nom = nom;
        this.typeContrat = typeContrat;
        this.actif = actif == null || actif;
    }
}
