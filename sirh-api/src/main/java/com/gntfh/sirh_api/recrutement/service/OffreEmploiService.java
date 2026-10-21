package com.gntfh.sirh_api.recrutement.service;

import com.gntfh.sirh_api.common.dto.PageResponse;
import com.gntfh.sirh_api.common.security.CurrentUser;
import com.gntfh.sirh_api.recrutement.dto.OffreEmploiRequest;
import com.gntfh.sirh_api.recrutement.dto.OffreEmploiResponse;
import com.gntfh.sirh_api.recrutement.dto.OffreEmploiUpdateRequest;
import com.gntfh.sirh_api.recrutement.enums.StatutOffre;
import org.springframework.data.domain.Pageable;

/**
 * Service pour la gestion des offres d'emploi.
 */
public interface OffreEmploiService {

    OffreEmploiResponse creer(OffreEmploiRequest request, CurrentUser currentUser);

    OffreEmploiResponse modifier(Long id, OffreEmploiUpdateRequest request, CurrentUser currentUser);

    OffreEmploiResponse obtenir(Long id);

    PageResponse<OffreEmploiResponse> rechercher(String q, StatutOffre statut, Long demandeId, Pageable pageable);

    OffreEmploiResponse publier(Long id, CurrentUser currentUser);

    OffreEmploiResponse cloturer(Long id, CurrentUser currentUser);

    OffreEmploiResponse annuler(Long id, CurrentUser currentUser);

    void cloturerSiEffectifAtteint(Long demandeId, String auteur);

    int cloturerOffresEchues(java.time.LocalDate aujourdhui);
}
