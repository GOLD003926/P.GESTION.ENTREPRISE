package com.gntfh.sirh_api.recrutement.repository;

import com.gntfh.sirh_api.recrutement.entity.EntretienIntervenant;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;
import java.util.Optional;

/**
 * Accès aux données pour EntretienIntervenant.
 * JpaSpecificationExecutor : recherche avec filtres facultatifs.
 */
public interface EntretienIntervenantRepository
        extends JpaRepository<EntretienIntervenant, Long>, JpaSpecificationExecutor<EntretienIntervenant> {

    Optional<EntretienIntervenant> findByEntretienIdAndUtilisateurId(Long entretienId, Long utilisateurId);

    boolean existsByEntretienIdAndUtilisateurId(Long entretienId, Long utilisateurId);

    List<EntretienIntervenant> findByEntretienId(Long entretienId);
}
