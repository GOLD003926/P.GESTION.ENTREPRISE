package com.gntfh.sirh_api.demande.service;

import java.util.Optional;

/**
 * Port pour la recherche d'employés.
 * Implémenté par le module employe.
 * Tant que le module employe n'existe pas, les valeurs par défaut sont : existe → true, departementActuel → vide.
 */
public interface EmployeLookup {
    boolean existe(Long employeId);
    Optional<Long> departementActuel(Long employeId);
}
