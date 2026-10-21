package com.gntfh.sirh_api.recrutement.repository;

import com.gntfh.sirh_api.recrutement.entity.OffreEmploi;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

/**
 * Accès aux données pour OffreEmploi.
 * JpaSpecificationExecutor : recherche avec filtres facultatifs.
 */
public interface OffreEmploiRepository
        extends JpaRepository<OffreEmploi, Long>, JpaSpecificationExecutor<OffreEmploi> {

    Optional<OffreEmploi> findByReference(String reference);

    boolean existsByReference(String reference);

    /**
     * Compte le nombre de candidatures pour une offre donnée.
     */
    @Query("SELECT COUNT(c) FROM com.gntfh.sirh_api.recrutement.entity.Candidature c WHERE c.offre.id = :offreId")
    long countCandidaturesByOffreId(@Param("offreId") Long offreId);

    /**
     * Somme des postes des offres actives (BROUILLON ou PUBLIEE) d'une demande.
     * Utilisé pour vérifier RG-14 (nombrePostes ≤ effectif restant).
     */
    @Query("SELECT COALESCE(SUM(o.nombrePostes), 0) FROM OffreEmploi o " +
           "WHERE o.demande.id = :demandeId AND o.statut IN ('BROUILLON', 'PUBLIEE')")
    Integer sumPostesOffresActivesByDemandeId(@Param("demandeId") Long demandeId);
}
