package com.gntfh.sirh_api.demande.repository;

import com.gntfh.sirh_api.demande.entity.Validation;
import com.gntfh.sirh_api.demande.enums.DecisionValidation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ValidationRepository extends JpaRepository<Validation, Long> {

    @Query("SELECT v FROM Validation v WHERE v.demande.id = :demandeId AND v.cycle = :cycle AND v.ordre = :ordre")
    Optional<Validation> findByDemandeIdAndCycleAndOrdre(@Param("demandeId") Long demandeId, @Param("cycle") Integer cycle, @Param("ordre") Integer ordre);

    @Query("SELECT v FROM Validation v WHERE v.demande.id = :demandeId AND v.cycle = :cycle AND v.decision = 'EN_ATTENTE' AND v.roleValidateur = :role")
    Optional<Validation> findEnAttentePourCycleEtRole(@Param("demandeId") Long demandeId, @Param("cycle") Integer cycle, @Param("role") com.gntfh.sirh_api.demande.enums.RoleValidateur role);

    @Query("SELECT v FROM Validation v WHERE v.demande.id = :demandeId ORDER BY v.cycle DESC, v.ordre ASC")
    List<Validation> findByDemandeIdOrderByCycleDescOrdreAsc(@Param("demandeId") Long demandeId);
}
