package com.gntfh.sirh_api.contrat.repository;

import com.gntfh.sirh_api.contrat.entity.Contrat;
import com.gntfh.sirh_api.contrat.enums.StatutContrat;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * Repository des contrats.
 */
public interface ContratRepository extends JpaRepository<Contrat, Long>, JpaSpecificationExecutor<Contrat> {

    /**
     * Recherche le contrat actif d'un employé.
     */
    Optional<Contrat> findByEmployeIdAndStatut(Long employeId, StatutContrat statut);

    /**
     * Contrats à activer automatiquement.
     */
    @Query("""
            SELECT c
            FROM Contrat c
            WHERE c.statut = com.gntfh.sirh_api.contrat.enums.StatutContrat.A_SIGNER
              AND c.documentSigne IS NOT NULL
              AND c.dateDebut <= :dateReference
            """)
    List<Contrat> findContratsASignerActivables(@Param("dateReference") LocalDate dateReference);

    /**
     * Contrats actifs proches de l'échéance et non encore alertés.
     */
    @Query("""
            SELECT c
            FROM Contrat c
            WHERE c.statut = com.gntfh.sirh_api.contrat.enums.StatutContrat.ACTIF
              AND c.type IN (
                  com.gntfh.sirh_api.contrat.enums.TypeContrat.ESSAI,
                  com.gntfh.sirh_api.contrat.enums.TypeContrat.CDD
              )
              AND c.dateFinPrevue IS NOT NULL
              AND c.dateFinPrevue <= :dateLimite
              AND c.alerteFinEnvoyee = false
            """)
    List<Contrat> findContratsActifsAvecFinAvantEtAlerteNonEnvoyee(@Param("dateLimite") LocalDate dateLimite);

    /**
     * Recherche le successeur éventuel d'un contrat.
     */
    Optional<Contrat> findByContratPrecedentId(Long contratPrecedentId);
}
