package com.gntfh.sirh_api.recrutement.repository;

import com.gntfh.sirh_api.recrutement.entity.Candidature;
import com.gntfh.sirh_api.recrutement.enums.StatutCandidature;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

/**
 * Accès aux données pour Candidature.
 * JpaSpecificationExecutor : recherche avec filtres facultatifs.
 */
public interface CandidatureRepository
        extends JpaRepository<Candidature, Long>, JpaSpecificationExecutor<Candidature> {

    Optional<Candidature> findByCandidatIdAndOffreId(Long candidatId, Long offreId);

    boolean existsByCandidatIdAndOffreId(Long candidatId, Long offreId);

    /**
     * Rang maximal de liste d'attente pour une offre donnée.
     * Utilisé pour REC-08 (promotion depuis liste d'attente).
     */
    @Query("SELECT COALESCE(MAX(c.rangListeAttente), 0) FROM Candidature c " +
           "WHERE c.offre.id = :offreId AND c.statut = 'LISTE_ATTENTE'")
    Integer maxRangListeAttenteByOffreId(@Param("offreId") Long offreId);
}
