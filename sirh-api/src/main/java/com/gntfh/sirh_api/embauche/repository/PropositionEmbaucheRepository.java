package com.gntfh.sirh_api.embauche.repository;

import com.gntfh.sirh_api.embauche.entity.PropositionEmbauche;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface PropositionEmbaucheRepository extends JpaRepository<PropositionEmbauche, Long>,
        JpaSpecificationExecutor<PropositionEmbauche> {

    @Query("""
            SELECT p
            FROM PropositionEmbauche p
            WHERE p.candidature.id = :candidatureId
              AND p.statut IN (
                  com.gntfh.sirh_api.embauche.enums.StatutProposition.BROUILLON,
                  com.gntfh.sirh_api.embauche.enums.StatutProposition.ENVOYEE,
                  com.gntfh.sirh_api.embauche.enums.StatutProposition.DELAI_ACCORDE
              )
            """)
    Optional<PropositionEmbauche> findActiveByCandidatureId(@Param("candidatureId") Long candidatureId);

    @Query("""
            SELECT p
            FROM PropositionEmbauche p
            WHERE p.statut IN (com.gntfh.sirh_api.embauche.enums.StatutProposition.ENVOYEE,
                               com.gntfh.sirh_api.embauche.enums.StatutProposition.DELAI_ACCORDE)
              AND p.dateLimiteReponse < :aujourdhui
            """)
    List<PropositionEmbauche> findEchues(@Param("aujourdhui") LocalDate aujourdhui);
}
