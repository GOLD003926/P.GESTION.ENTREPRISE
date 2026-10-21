package com.gntfh.sirh_api.employe.repository;

import com.gntfh.sirh_api.employe.entity.Affectation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

/**
 * Repository pour l'entité Affectation.
 */
@Repository
public interface AffectationRepository extends JpaRepository<Affectation, Long>, JpaSpecificationExecutor<Affectation> {

    @Query("SELECT a FROM Affectation a WHERE a.employe.id = :employeId AND :date BETWEEN a.dateDebut AND COALESCE(a.dateFin, :date)")
    List<Affectation> findActivesByEmployeId(@Param("employeId") Long employeId, @Param("date") LocalDate date);

    @Query("SELECT a FROM Affectation a WHERE a.poste.id = :posteId AND :date BETWEEN a.dateDebut AND COALESCE(a.dateFin, :date)")
    List<Affectation> findActivesByPosteId(@Param("posteId") Long posteId, @Param("date") LocalDate date);

    @Query("SELECT a FROM Affectation a WHERE a.manager.id = :managerId AND :date BETWEEN a.dateDebut AND COALESCE(a.dateFin, :date)")
    List<Affectation> findActivesByManagerId(@Param("managerId") Long managerId, @Param("date") LocalDate date);

    @Query("""
            SELECT a FROM Affectation a
            WHERE a.employe.id = :employeId
              AND a.dateDebut <= :dateFin
              AND (a.dateFin IS NULL OR a.dateFin >= :dateDebut)
            """)
    List<Affectation> findOverlappingByEmployeId(
            @Param("employeId") Long employeId,
            @Param("dateDebut") LocalDate dateDebut,
            @Param("dateFin") LocalDate dateFin
    );

    @Query("SELECT COUNT(a) FROM Affectation a WHERE a.poste.id = :posteId AND :date BETWEEN a.dateDebut AND COALESCE(a.dateFin, :date)")
    int countActivesByPosteId(@Param("posteId") Long posteId, @Param("date") LocalDate date);
}
