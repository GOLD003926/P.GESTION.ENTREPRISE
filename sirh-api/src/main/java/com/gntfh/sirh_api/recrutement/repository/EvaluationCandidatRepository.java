package com.gntfh.sirh_api.recrutement.repository;

import com.gntfh.sirh_api.recrutement.entity.EvaluationCandidat;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

/**
 * Accès aux données pour EvaluationCandidat.
 * JpaSpecificationExecutor : recherche avec filtres facultatifs.
 */
public interface EvaluationCandidatRepository
        extends JpaRepository<EvaluationCandidat, Long>, JpaSpecificationExecutor<EvaluationCandidat> {

    /**
     * Recherche une évaluation active pour un entretien et un évaluateur donnés.
     * Correspond à l'index unique partiel uk_evaluation_active.
     */
    @Query("SELECT e FROM EvaluationCandidat e WHERE e.entretien.id = :entretienId " +
           "AND e.evaluateur.id = :evaluateurId AND e.active = true")
    Optional<EvaluationCandidat> findActiveByEntretienIdAndEvaluateurId(@Param("entretienId") Long entretienId,
                                                                         @Param("evaluateurId") Long evaluateurId);

    /**
     * Liste des évaluations actives pour un entretien donné.
     */
    @Query("SELECT e FROM EvaluationCandidat e WHERE e.entretien.id = :entretienId AND e.active = true")
    List<EvaluationCandidat> findActiveByEntretienId(@Param("entretienId") Long entretienId);

    /**
     * Liste de toutes les évaluations (actives et inactives) pour un entretien donné.
     */
    List<EvaluationCandidat> findByEntretienId(Long entretienId);

    /**
     * Compte les évaluations actives et verrouillées pour un entretien donné.
     * Utilisé pour RG-20 (au moins une évaluation active verrouillée pour clôturer).
     */
    @Query("SELECT COUNT(e) FROM EvaluationCandidat e WHERE e.entretien.id = :entretienId " +
           "AND e.active = true AND e.verrouillee = true")
    long countActiveAndVerrouilleeByEntretienId(@Param("entretienId") Long entretienId);
}
