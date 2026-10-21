package com.gntfh.sirh_api.recrutement.repository;

import com.gntfh.sirh_api.recrutement.entity.Entretien;
import com.gntfh.sirh_api.recrutement.enums.StatutEntretien;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

/**
 * Accès aux données pour Entretien.
 * JpaSpecificationExecutor : recherche avec filtres facultatifs.
 */
public interface EntretienRepository
        extends JpaRepository<Entretien, Long>, JpaSpecificationExecutor<Entretien> {

    /**
     * Recherche un entretien actif (non annulé) pour une candidature et un niveau donnés.
     * Correspond à l'index unique partiel uk_entretien_candidature_niveau.
     */
    @Query("SELECT e FROM Entretien e WHERE e.candidature.id = :candidatureId " +
           "AND e.niveau = :niveau AND e.statut <> 'ANNULE'")
    Optional<Entretien> findActifByCandidatureIdAndNiveau(@Param("candidatureId") Long candidatureId,
                                                           @Param("niveau") Integer niveau);

    /**
     * Compte les entretiens actifs (non annulés) pour une candidature et un niveau donnés.
     */
    @Query("SELECT COUNT(e) FROM Entretien e WHERE e.candidature.id = :candidatureId " +
           "AND e.niveau = :niveau AND e.statut <> 'ANNULE'")
    long countActifByCandidatureIdAndNiveau(@Param("candidatureId") Long candidatureId,
                                           @Param("niveau") Integer niveau);
}
