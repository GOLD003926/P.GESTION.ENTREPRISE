package com.gntfh.sirh_api.employe.dto;

import com.gntfh.sirh_api.employe.enums.StatutEmploye;

import java.time.LocalDate;

/**
 * DTO pour la réponse résumée d'un employé (vue pour manager et paie).
 */
public record EmployeResumeResponse(
        Long id,
        String matricule,
        String nom,
        String prenom,
        StatutEmploye statut,
        LocalDate dateEmbauche,
        String posteActuelIntitule
) {
}
