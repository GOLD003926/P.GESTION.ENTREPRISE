package com.gntfh.sirh_api.organisation.repository;

import com.gntfh.sirh_api.organisation.entity.GrilleSalariale;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

/**
 * Repository pour l'entité GrilleSalariale.
 */
public interface GrilleSalarialeRepository extends JpaRepository<GrilleSalariale, Long>, JpaSpecificationExecutor<GrilleSalariale> {

    /**
     * Vérifie l'existence d'une grille par son code (insensible à la casse).
     */
    boolean existsByCodeIgnoreCase(String code);

    /**
     * Recherche une grille par son code (insensible à la casse).
     */
    Optional<GrilleSalariale> findByCodeIgnoreCase(String code);

    /**
     * Compte le nombre de postes actifs utilisant cette grille.
     */
    @Query("SELECT COUNT(p) FROM Poste p WHERE p.grilleSalariale.id = :grilleSalarialeId AND p.actif = true")
    long countPostesActifsByGrilleSalarialeId(@Param("grilleSalarialeId") Long grilleSalarialeId);
}
