package com.gntfh.sirh_api.employe.service;

import com.gntfh.sirh_api.common.dto.PageResponse;
import com.gntfh.sirh_api.common.audit.dto.HistoriqueStatutResponse;
import com.gntfh.sirh_api.common.security.CurrentUser;
import com.gntfh.sirh_api.employe.dto.EmployeRequest;
import com.gntfh.sirh_api.employe.dto.EmployeResponse;
import com.gntfh.sirh_api.employe.dto.EmployeResumeResponse;
import com.gntfh.sirh_api.employe.dto.EmployeUpdateRequest;
import com.gntfh.sirh_api.employe.entity.Employe;
import com.gntfh.sirh_api.employe.enums.StatutEmploye;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;

/**
 * Service pour la gestion des employés.
 */
public interface EmployeService {

    /**
     * Créer un employé (cas exceptionnel, par exemple reprise de données).
     * Matricule généré, statut PRE_EMBAUCHE.
     * EMP-06 : journal d'audit.
     */
    EmployeResponse creer(EmployeRequest request, CurrentUser currentUser);

    /**
     * Modifier un employé.
     * Matricule immuable.
     * EMP-01 : matricule unique et immuable.
     * EMP-06 : journal d'audit.
     */
    EmployeResponse modifier(Long id, EmployeUpdateRequest request, CurrentUser currentUser);

    /**
     * Obtenir un employé.
     * EMP-05 : visibilité RH tout, manager/vue résumée, paie/vue résumée, salarié/moi.
     */
    Object obtenir(Long id, CurrentUser currentUser);

    /**
     * Dossier de l'utilisateur connecté.
     */
    EmployeResponse moi(CurrentUser currentUser);

    /**
     * Rechercher des employés.
     * EMP-05 : visibilité manager/vue résumée.
     */
    PageResponse<?> rechercher(String q, StatutEmploye statut, Long departementId, Long posteId, Pageable pageable, CurrentUser currentUser);

    /**
     * Suspendre un employé.
     * ACTIF → SUSPENDU.
     */
    void suspendre(Long id, CurrentUser currentUser);

    /**
     * Reprendre un employé.
     * SUSPENDU → ACTIF.
     */
    void reprendre(Long id, CurrentUser currentUser);

    /**
     * Générer la fiche salariale PDF.
     * RH uniquement.
     */
    byte[] ficheSalarie(Long id, CurrentUser currentUser);

    /**
     * Historique des statuts d'un employé.
     */
    java.util.List<HistoriqueStatutResponse> historique(Long id, CurrentUser currentUser);

    /**
     * Créer un employé depuis une candidature.
     * API interne : appelée par embauche.
     * RG-31, RG-32, EMP-02 : une candidature donne au plus un employé.
     */
    Employe creerDepuisCandidature(Long candidatureId, LocalDate dateEmbauche, String auteur);

    /**
     * Activer un employé.
     * API interne : appelée par contrat.
     * RG-45 : PRE_EMBAUCHE → ACTIF.
     */
    void activer(Long employeId, LocalDate dateDebut, String auteur);

    /**
     * Trouver un employé.
     * API interne.
     */
    Employe trouver(Long id);
}
