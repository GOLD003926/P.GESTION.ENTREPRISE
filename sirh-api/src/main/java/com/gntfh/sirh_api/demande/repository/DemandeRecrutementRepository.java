package com.gntfh.sirh_api.demande.repository;

import com.gntfh.sirh_api.demande.entity.DemandeRecrutement;
import com.gntfh.sirh_api.demande.enums.RoleValidateur;
import com.gntfh.sirh_api.demande.enums.StatutDemande;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface DemandeRecrutementRepository extends JpaRepository<DemandeRecrutement, Long>, JpaSpecificationExecutor<DemandeRecrutement> {

    @Query("SELECT d FROM DemandeRecrutement d WHERE d.reference = :reference")
    Optional<DemandeRecrutement> findByReference(@Param("reference") String reference);

    @Query("SELECT SUM(d.effectifDemande - d.effectifPourvu) FROM DemandeRecrutement d " +
           "WHERE d.poste.id = :posteId " +
           "AND d.statut IN ('EN_VALIDATION_RH', 'EN_VALIDATION_FINANCE', 'EN_VALIDATION_DG', 'VALIDEE', 'EN_RECRUTEMENT') " +
           "AND d.id != :exclureDemandeId")
    Integer sommeEffectifRestantPoste(@Param("posteId") Long posteId, @Param("exclureDemandeId") Long exclureDemandeId);

    @Query("SELECT d FROM DemandeRecrutement d WHERE d.statut = :statutCourant AND d.id IN " +
           "(SELECT v.demande.id FROM Validation v WHERE v.decision = 'EN_ATTENTE' AND v.roleValidateur = :role)")
    List<DemandeRecrutement> findByEtapeCourante(@Param("statutCourant") StatutDemande statutCourant, @Param("role") RoleValidateur role);
}
