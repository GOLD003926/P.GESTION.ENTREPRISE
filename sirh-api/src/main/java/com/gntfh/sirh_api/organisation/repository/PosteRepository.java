package com.gntfh.sirh_api.organisation.repository;

import com.gntfh.sirh_api.organisation.entity.Poste;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.Optional;

/**
 * Repository pour l'entité Poste.
 */
public interface PosteRepository extends JpaRepository<Poste, Long>, JpaSpecificationExecutor<Poste> {

    /**
     * Vérifie l'existence d'un poste par son code (insensible à la casse).
     */
    boolean existsByCodeIgnoreCase(String code);

    /**
     * Recherche un poste par son code (insensible à la casse).
     */
    Optional<Poste> findByCodeIgnoreCase(String code);
}
