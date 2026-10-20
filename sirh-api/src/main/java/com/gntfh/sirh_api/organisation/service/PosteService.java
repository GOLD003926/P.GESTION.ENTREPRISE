package com.gntfh.sirh_api.organisation.service;

import com.gntfh.sirh_api.common.dto.PageResponse;
import com.gntfh.sirh_api.organisation.dto.PosteRequest;
import com.gntfh.sirh_api.organisation.dto.PosteResponse;
import com.gntfh.sirh_api.organisation.dto.PosteUpdateRequest;
import com.gntfh.sirh_api.organisation.entity.Poste;
import com.gntfh.sirh_api.organisation.mapper.PosteMapper;
import com.gntfh.sirh_api.organisation.port.PosteOccupationProvider;
import com.gntfh.sirh_api.organisation.repository.PosteRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service pour la gestion des postes.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class PosteService {

    private final PosteRepository repository;
    private final PosteMapper mapper;
    private final ObjectProvider<PosteOccupationProvider> occupationProvider;

    @Transactional(readOnly = true)
    public PageResponse<PosteResponse> rechercher(String q, Long departementId, Long grilleSalarialeId, Boolean actif, Pageable pageable) {
        throw new UnsupportedOperationException("TODO ORG-08");
    }

    @Transactional(readOnly = true)
    public PosteResponse obtenir(Long id) {
        throw new UnsupportedOperationException("TODO ORG-08");
    }

    @Transactional
    public PosteResponse creer(PosteRequest request) {
        throw new UnsupportedOperationException("TODO RG-01 RG-02 ORG-01 ORG-05 ORG-06 ORG-08");
    }

    @Transactional
    public PosteResponse modifier(Long id, PosteUpdateRequest request) {
        throw new UnsupportedOperationException("TODO RG-01 RG-02 ORG-05 ORG-06 ORG-08");
    }

    @Transactional
    public PosteResponse activer(Long id) {
        throw new UnsupportedOperationException("TODO ORG-04 ORG-08");
    }

    @Transactional
    public PosteResponse desactiver(Long id) {
        throw new UnsupportedOperationException("TODO ORG-04 ORG-08");
    }

    /**
     * API interne : retourne le nombre de places libres d'un poste.
     * Règle RG-04 : utilisé par le module demande.
     *
     * @param posteId identifiant du poste
     * @return nombre de places libres
     */
    @Transactional(readOnly = true)
    public int placesLibres(Long posteId) {
        throw new UnsupportedOperationException("TODO RG-04");
    }

    /**
     * API interne : trouve un poste actif par son identifiant.
     * Lève BusinessRuleException si le poste est inactif.
     *
     * @param posteId identifiant du poste
     * @return poste trouvé
     */
    @Transactional(readOnly = true)
    public Poste trouverActif(Long posteId) {
        throw new UnsupportedOperationException("TODO ORG-06");
    }
}
