package com.gntfh.sirh_api.employe.dto;

import com.gntfh.sirh_api.employe.enums.StatutEmploye;

import java.time.Instant;
import java.time.LocalDate;

/**
 * DTO pour la réponse détaillée d'un employé.
 */
public record EmployeResponse(
        Long id,
        String matricule,
        String nom,
        String prenom,
        String emailPro,
        String emailPerso,
        String telephone,
        LocalDate dateNaissance,
        String adresse,
        String numeroSecuriteSociale,
        LocalDate dateEmbauche,
        StatutEmploye statut,
        Long candidatureOrigineId,
        Long posteActuelId,
        String posteActuelIntitule,
        String departementActuelNom,
        Long managerActuelId,
        String managerActuelNom,
        Instant createdAt
) {
}
