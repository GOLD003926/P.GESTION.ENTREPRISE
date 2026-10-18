package com.gntfh.sirh_api.auth.repository;

import com.gntfh.sirh_api.auth.entity.Utilisateur;
import com.gntfh.sirh_api.auth.enums.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.Optional;

/**
 * Accès aux données uniquement : pas de règle métier ici.
 * JpaSpecificationExecutor : recherche avec filtres facultatifs (voir UtilisateurService.rechercher).
 */
public interface UtilisateurRepository
        extends JpaRepository<Utilisateur, Long>, JpaSpecificationExecutor<Utilisateur> {

    Optional<Utilisateur> findByEmailIgnoreCase(String email);

    boolean existsByEmailIgnoreCase(String email);

    boolean existsByRole(Role role);
}
