package com.gntfh.sirh_api.onboarding.entity;

import com.gntfh.sirh_api.auth.enums.Role;
import com.gntfh.sirh_api.common.entity.AuditableEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
@Table(name = "modele_tache", uniqueConstraints = {
        @UniqueConstraint(name = "uk_modele_tache_ordre", columnNames = {"modele_id", "ordre"}),
        @UniqueConstraint(name = "uk_modele_tache_code", columnNames = {"modele_id", "code"})
})
public class ModeleTache extends AuditableEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "modele_id", nullable = false)
    private ModeleOnboarding modele;

    @Column(name = "ordre", nullable = false)
    private Integer ordre;

    @Column(name = "code", nullable = false, length = 40)
    private String code;

    @Column(name = "libelle", nullable = false, length = 200)
    private String libelle;

    @Column(name = "obligatoire", nullable = false)
    private Boolean obligatoire;

    @Enumerated(EnumType.STRING)
    @Column(name = "role_responsable", nullable = false, length = 30)
    private Role roleResponsable;

    @Column(name = "echeance_jours", nullable = false)
    private Integer echeanceJours;

    @Builder
    private ModeleTache(ModeleOnboarding modele, Integer ordre, String code, String libelle,
                        Boolean obligatoire, Role roleResponsable, Integer echeanceJours) {
        this.modele = modele;
        this.ordre = ordre;
        this.code = code;
        this.libelle = libelle;
        this.obligatoire = obligatoire;
        this.roleResponsable = roleResponsable;
        this.echeanceJours = echeanceJours;
    }
}
