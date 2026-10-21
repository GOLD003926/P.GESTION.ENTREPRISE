package com.gntfh.sirh_api.common.document.entity;

import com.gntfh.sirh_api.common.audit.enums.TypeEntite;
import com.gntfh.sirh_api.common.document.enums.TypeDocument;
import com.gntfh.sirh_api.common.entity.AuditableEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
@Table(name = "document")
public class Document extends AuditableEntity {

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private TypeDocument type;

    @Column(name = "nom_fichier", nullable = false, length = 255)
    private String nomFichier;

    @Column(name = "type_mime", nullable = false, length = 100)
    private String typeMime;

    @Column(name = "taille_octets", nullable = false)
    private Long tailleOctets;

    @Column(name = "chemin_stockage", nullable = false, length = 500)
    private String cheminStockage;

    @Column(name = "hash_sha256", nullable = false, length = 64)
    private String hashSha256;

    @Column(name = "numero_version", nullable = false)
    private Integer numeroVersion = 1;

    @Enumerated(EnumType.STRING)
    @Column(name = "entite_type", nullable = false, length = 50)
    private TypeEntite entiteType;

    @Column(name = "entite_id", nullable = false)
    private Long entiteId;

    @Column(nullable = false)
    private boolean archive = false;

    @Builder
    private Document(TypeDocument type, String nomFichier, String typeMime, Long tailleOctets,
                     String cheminStockage, String hashSha256, Integer numeroVersion,
                     TypeEntite entiteType, Long entiteId, Boolean archive) {
        this.type = type;
        this.nomFichier = nomFichier;
        this.typeMime = typeMime;
        this.tailleOctets = tailleOctets;
        this.cheminStockage = cheminStockage;
        this.hashSha256 = hashSha256;
        this.numeroVersion = numeroVersion == null ? 1 : numeroVersion;
        this.entiteType = entiteType;
        this.entiteId = entiteId;
        this.archive = archive != null && archive;
    }

    @PrePersist
    void nettoyerNomFichier() {
        if (nomFichier != null) {
            nomFichier = nomFichier.replaceAll("[^a-zA-Z0-9._-]", "_");
        }
    }
}
