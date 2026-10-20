package com.gntfh.sirh_api.common.audit.entity;

import com.gntfh.sirh_api.common.entity.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
@Table(name = "journal_audit")
public class JournalAudit extends BaseEntity {

    @Column(name = "entite_type", nullable = false, length = 50)
    private String entiteType;

    @Column(name = "entite_id", nullable = false)
    private Long entiteId;

    @Column(nullable = false, length = 30)
    private String action;

    @Column(name = "valeurs_avant", columnDefinition = "TEXT")
    private String valeursAvant;

    @Column(name = "valeurs_apres", columnDefinition = "TEXT")
    private String valeursApres;

    @Column(nullable = false, length = 150)
    private String auteur;

    @Column(name = "date_action", nullable = false)
    private Instant dateAction;

    @Builder
    private JournalAudit(String entiteType, Long entiteId, String action,
                         String valeursAvant, String valeursApres, String auteur,
                         Instant dateAction) {
        this.entiteType = entiteType;
        this.entiteId = entiteId;
        this.action = action;
        this.valeursAvant = valeursAvant;
        this.valeursApres = valeursApres;
        this.auteur = auteur;
        this.dateAction = dateAction;
    }
}
