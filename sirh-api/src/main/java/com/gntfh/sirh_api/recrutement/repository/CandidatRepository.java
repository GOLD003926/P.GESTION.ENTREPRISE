package com.gntfh.sirh_api.recrutement.repository;

import com.gntfh.sirh_api.recrutement.entity.Candidat;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.Optional;

/**
 * Accès aux données pour Candidat.
 * JpaSpecificationExecutor : recherche avec filtres facultatifs.
 */
public interface CandidatRepository
        extends JpaRepository<Candidat, Long>, JpaSpecificationExecutor<Candidat> {

    Optional<Candidat> findByEmailIgnoreCase(String email);

    boolean existsByEmailIgnoreCase(String email);
}
