package com.gntfh.sirh_api.common.notification.entity;

import com.gntfh.sirh_api.common.audit.enums.TypeEntite;
import com.gntfh.sirh_api.common.entity.AuditableEntity;
import com.gntfh.sirh_api.common.notification.enums.CanalNotification;
import com.gntfh.sirh_api.common.notification.enums.TypeNotification;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;

@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
@Table(name = "notification")
public class Notification extends AuditableEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "destinataire_id", nullable = false)
    private com.gntfh.sirh_api.auth.entity.Utilisateur destinataire;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 40)
    private TypeNotification type;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private CanalNotification canal = CanalNotification.INTERNE;

    @Column(nullable = false, length = 150)
    private String titre;

    @Column(nullable = false, length = 500)
    private String message;

    @Enumerated(EnumType.STRING)
    @Column(name = "entite_type", length = 50)
    private TypeEntite entiteType;

    @Column(name = "entite_id")
    private Long entiteId;

    @Column(nullable = false)
    private boolean lue = false;

    @Column(name = "date_lecture")
    private Instant dateLecture;

    @Builder
    private Notification(com.gntfh.sirh_api.auth.entity.Utilisateur destinataire,
                         TypeNotification type, CanalNotification canal,
                         String titre, String message, TypeEntite entiteType,
                         Long entiteId, Boolean lue, Instant dateLecture) {
        this.destinataire = destinataire;
        this.type = type;
        this.canal = canal == null ? CanalNotification.INTERNE : canal;
        this.titre = titre;
        this.message = message;
        this.entiteType = entiteType;
        this.entiteId = entiteId;
        this.lue = lue == null || !lue;
        this.dateLecture = dateLecture;
    }
}
