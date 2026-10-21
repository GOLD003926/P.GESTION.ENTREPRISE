package com.gntfh.sirh_api.employe.service;

import com.gntfh.sirh_api.common.dto.PageResponse;
import com.gntfh.sirh_api.common.security.CurrentUser;
import com.gntfh.sirh_api.employe.dto.AffectationClotureRequest;
import com.gntfh.sirh_api.employe.dto.AffectationRequest;
import com.gntfh.sirh_api.employe.dto.AffectationResponse;
import com.gntfh.sirh_api.employe.entity.Affectation;
import com.gntfh.sirh_api.employe.entity.Employe;
import com.gntfh.sirh_api.organisation.entity.Poste;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;

/**
 * Service pour la gestion des affectations.
 */
public interface AffectationService {

    /**
     * Créer une affectation.
     * RG-02, RG-41, EMP-03, EMP-04.
     * EMP-06 : journal d'audit.
     */
    AffectationResponse creer(AffectationRequest request, CurrentUser currentUser);

    /**
     * Clôturer une affectation.
     */
    void cloturer(Long id, AffectationClotureRequest request, CurrentUser currentUser);

    /**
     * Lister les affectations.
     */
    PageResponse<AffectationResponse> lister(Long employeId, Long posteId, Boolean active, Pageable pageable);

    /**
     * Créer une affectation initiale.
     * API interne : appelée par contrat.
     * RG-41 : motif EMBAUCHE, taux 100.
     */
    Affectation creerInitiale(Employe employe, Poste poste, Long contratId, Long managerId, LocalDate dateDebut, String auteur);

    /**
     * Occupation actuelle d'un poste.
     * API interne.
     */
    int occupationActuelle(Long posteId);
}
