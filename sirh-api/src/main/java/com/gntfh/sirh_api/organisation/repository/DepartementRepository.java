package com.gntfh.sirh_api.organisation.repository;

import com.gntfh.sirh_api.organisation.entity.Departement;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

/**
 * Repository pour l'entité Departement.
 */
public interface DepartementRepository extends JpaRepository<Departement, Long>, JpaSpecificationExecutor<Departement> {

    /**
     * Vérifie l'existence d'un département par son code (insensible à la casse).
     */
    boolean existsByCodeIgnoreCase(String code);

    /**
     * Recherche un département par son code (insensible à la casse).
     */
    Optional<Departement> findByCodeIgnoreCase(String code);

    /**
     * Vérifie l'existence de sous-départements actifs pour un parent donné.
     */
    @Query("SELECT CASE WHEN COUNT(d) > 0 THEN true ELSE false END FROM Departement d WHERE d.departementParent.id = :parentId AND d.actif = true")
    boolean existsSousDepartementsActifsByParentId(@Param("parentId") Long parentId);

    /**
     * Compte le nombre de postes actifs dans un département.
     */
    @Query("SELECT COUNT(p) FROM Poste p WHERE p.departement.id = :departementId AND p.actif = true")
    long countPostesActifsByDepartementId(@Param("departementId") Long departementId);
}
