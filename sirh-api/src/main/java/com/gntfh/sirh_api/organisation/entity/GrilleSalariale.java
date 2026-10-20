package com.gntfh.sirh_api.organisation.entity;

import com.gntfh.sirh_api.common.entity.AuditableEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

/**
 * Entité représentant une grille salariale de l'entreprise.
 * Définit les fourchettes de salaires par niveau hiérarchique.
 */
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
@Table(name = "grille_salariale")
public class GrilleSalariale extends AuditableEntity {

    @Column(name = "code", nullable = false, unique = true, length = 30)
    private String code;

    @Column(name = "intitule", nullable = false, length = 100)
    private String intitule;

    @Column(name = "niveau", nullable = false, length = 30)
    private String niveau;

    @Column(name = "salaire_min", nullable = false, precision = 14, scale = 2)
    private BigDecimal salaireMin;

    @Column(name = "salaire_max", nullable = false, precision = 14, scale = 2)
    private BigDecimal salaireMax;

    @Column(name = "devise", nullable = false, length = 3)
    private String devise = "MGA";

    @Column(name = "actif", nullable = false)
    private Boolean actif = true;

    @Builder
    @SuppressWarnings("java:S2637")
    private GrilleSalariale(String code, String intitule, String niveau, BigDecimal salaireMin, BigDecimal salaireMax, String devise, Boolean actif) {
        this.code = code;
        this.intitule = intitule;
        this.niveau = niveau;
        this.salaireMin = salaireMin;
        this.salaireMax = salaireMax;
        this.devise = devise != null ? devise : "MGA";
        this.actif = actif != null ? actif : true;
    }
}
