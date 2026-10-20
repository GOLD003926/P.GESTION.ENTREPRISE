package com.gntfh.sirh_api.organisation.entity;

import com.gntfh.sirh_api.common.entity.AuditableEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Entité représentant un département de l'entreprise.
 * Peut avoir une structure hiérarchique avec des sous-départements.
 */
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
@Table(name = "departement")
public class Departement extends AuditableEntity {

    @Column(name = "code", nullable = false, unique = true, length = 30)
    private String code;

    @Column(name = "nom", nullable = false, length = 150)
    private String nom;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "departement_parent_id")
    private Departement departementParent;

    @Column(name = "responsable_id")
    private Long responsableId;

    @Column(name = "actif", nullable = false)
    private Boolean actif = true;

    @Builder
    @SuppressWarnings("java:S2637")
    private Departement(String code, String nom, Departement departementParent, Long responsableId, Boolean actif) {
        this.code = code;
        this.nom = nom;
        this.departementParent = departementParent;
        this.responsableId = responsableId;
        this.actif = actif != null ? actif : true;
    }
}
