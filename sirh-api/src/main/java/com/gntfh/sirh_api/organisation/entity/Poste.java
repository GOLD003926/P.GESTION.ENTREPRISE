package com.gntfh.sirh_api.organisation.entity;

import com.gntfh.sirh_api.common.entity.AuditableEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Entité représentant un poste de travail.
 * Un poste existe indépendamment de tout employé et peut avoir plusieurs occupants.
 */
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
@Table(name = "poste")
public class Poste extends AuditableEntity {

    @Column(name = "code", nullable = false, unique = true, length = 30)
    private String code;

    @Column(name = "intitule", nullable = false, length = 150)
    private String intitule;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Column(name = "competences_requises", columnDefinition = "TEXT")
    private String competencesRequises;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "departement_id", nullable = false)
    private Departement departement;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "grille_salariale_id", nullable = false)
    private GrilleSalariale grilleSalariale;

    @Column(name = "effectif_autorise", nullable = false)
    private Integer effectifAutorise;

    @Column(name = "actif", nullable = false)
    private Boolean actif = true;

    @Builder
    @SuppressWarnings("java:S2637")
    private Poste(String code, String intitule, String description, String competencesRequises,
                  Departement departement, GrilleSalariale grilleSalariale, Integer effectifAutorise, Boolean actif) {
        this.code = code;
        this.intitule = intitule;
        this.description = description;
        this.competencesRequises = competencesRequises;
        this.departement = departement;
        this.grilleSalariale = grilleSalariale;
        this.effectifAutorise = effectifAutorise;
        this.actif = actif != null ? actif : true;
    }
}
