package com.gntfh.sirh_api.employe.repository;

import com.gntfh.sirh_api.employe.entity.Employe;
import com.gntfh.sirh_api.employe.enums.StatutEmploye;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Repository pour l'entité Employe.
 */
@Repository
public interface EmployeRepository extends JpaRepository<Employe, Long>, JpaSpecificationExecutor<Employe> {

    Optional<Employe> findByMatricule(String matricule);

    @Query("SELECT e FROM Employe e WHERE LOWER(e.emailPro) = LOWER(:email)")
    Optional<Employe> findByEmailProIgnoreCase(@Param("email") String email);

    Optional<Employe> findByCandidatureOrigineId(Long candidatureId);

    @Query("SELECT COUNT(a) > 0 FROM Affectation a WHERE a.poste.id = :posteId AND :date BETWEEN a.dateDebut AND COALESCE(a.dateFin, :date)")
    boolean existeAffectationActivePourPoste(@Param("posteId") Long posteId, @Param("date") java.time.LocalDate date);

    @Query("SELECT COUNT(a) > 0 FROM Affectation a WHERE a.manager.id = :managerId AND :date BETWEEN a.dateDebut AND COALESCE(a.dateFin, :date)")
    boolean estManagerEnPoste(@Param("managerId") Long managerId, @Param("date") java.time.LocalDate date);
}
