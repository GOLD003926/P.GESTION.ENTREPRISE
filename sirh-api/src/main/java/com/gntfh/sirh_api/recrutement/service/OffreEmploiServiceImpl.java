package com.gntfh.sirh_api.recrutement.service;

import com.gntfh.sirh_api.common.dto.PageResponse;
import com.gntfh.sirh_api.common.security.CurrentUser;
import com.gntfh.sirh_api.recrutement.dto.OffreEmploiRequest;
import com.gntfh.sirh_api.recrutement.dto.OffreEmploiResponse;
import com.gntfh.sirh_api.recrutement.dto.OffreEmploiUpdateRequest;
import com.gntfh.sirh_api.recrutement.enums.StatutOffre;
import com.gntfh.sirh_api.recrutement.entity.OffreEmploi;
import com.gntfh.sirh_api.recrutement.repository.OffreEmploiRepository;
import com.gntfh.sirh_api.recrutement.mapper.OffreEmploiMapper;
import com.gntfh.sirh_api.demande.repository.DemandeRecrutementRepository;
import com.gntfh.sirh_api.demande.service.DemandeWorkflowService;
import com.gntfh.sirh_api.common.exception.*;
import com.gntfh.sirh_api.common.numerotation.NumeroService;
import com.gntfh.sirh_api.common.numerotation.SequenceNumero;
import com.gntfh.sirh_api.common.audit.enums.TypeEntite;
import com.gntfh.sirh_api.common.audit.service.HistoriqueStatutService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Implémentation du service OffreEmploiService.
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class OffreEmploiServiceImpl implements OffreEmploiService {
    private final OffreEmploiRepository repository;
    private final OffreEmploiMapper mapper;
    private final DemandeRecrutementRepository demandeRepository;
    private final NumeroService numeroService;
    private final DemandeWorkflowService demandeWorkflowService;
    private final HistoriqueStatutService historique;

    @Override
    @Transactional
    public OffreEmploiResponse creer(OffreEmploiRequest request, CurrentUser currentUser) {
        var demande = demandeRepository.findById(request.demandeId()).orElseThrow(() -> ResourceNotFoundException.of("DemandeRecrutement", request.demandeId()));
        int actif = repository.sumPostesOffresActivesByDemandeId(demande.getId());
        if (actif + request.nombrePostes() > demande.getEffectifDemande() - demande.getEffectifPourvu())
            throw new BusinessRuleException("Le nombre de postes dépasse l'effectif restant.");
        return mapper.toResponse(repository.save(mapper.toEntity(request, demande, numeroService.prochain(SequenceNumero.OFFRE))));
    }

    @Override
    @Transactional
    public OffreEmploiResponse modifier(Long id, OffreEmploiUpdateRequest request, CurrentUser currentUser) {
        OffreEmploi o = repository.findById(id).orElseThrow(() -> ResourceNotFoundException.of("OffreEmploi", id));
        if (o.getStatut() != StatutOffre.BROUILLON) throw new BusinessRuleException("Seule une offre brouillon est modifiable.");
        mapper.updateEntity(request, o); return mapper.toResponse(o);
    }

    @Override
    public OffreEmploiResponse obtenir(Long id) {
        return mapper.toResponse(repository.findById(id).orElseThrow(() -> ResourceNotFoundException.of("OffreEmploi", id)));
    }

    @Override
    public PageResponse<OffreEmploiResponse> rechercher(String q, StatutOffre statut, Long demandeId, Pageable pageable) {
        Specification<OffreEmploi> s = (r,qry,cb) -> {
            var p = cb.conjunction();
            if (q != null && !q.isBlank()) p = cb.and(p, cb.or(cb.like(cb.lower(r.get("titre")), "%"+q.toLowerCase()+"%"), cb.like(cb.lower(r.get("reference")), "%"+q.toLowerCase()+"%")));
            if (statut != null) p = cb.and(p, cb.equal(r.get("statut"), statut));
            if (demandeId != null) p = cb.and(p, cb.equal(r.get("demande").get("id"), demandeId));
            return p;
        };
        return PageResponse.from(repository.findAll(s, pageable), mapper::toResponse);
    }

    @Override
    @Transactional
    public OffreEmploiResponse publier(Long id, CurrentUser currentUser) {
        OffreEmploi o = repository.findById(id).orElseThrow(() -> ResourceNotFoundException.of("OffreEmploi", id));
        if (o.getDateCloture() == null || !o.getDateCloture().isAfter(java.time.LocalDate.now())) throw new BusinessRuleException("La date de clôture doit être future.");
        transition(o, StatutOffre.PUBLIEE, currentUser.email()); o.setDatePublication(java.time.LocalDate.now()); demandeWorkflowService.marquerEnRecrutement(o.getDemande().getId(), currentUser.email()); return mapper.toResponse(o);
    }

    @Override
    @Transactional
    public OffreEmploiResponse cloturer(Long id, CurrentUser currentUser) {
        OffreEmploi o = repository.findById(id).orElseThrow(() -> ResourceNotFoundException.of("OffreEmploi", id)); transition(o, StatutOffre.CLOTUREE, currentUser.email()); return mapper.toResponse(o);
    }

    @Override
    @Transactional
    public OffreEmploiResponse annuler(Long id, CurrentUser currentUser) {
        OffreEmploi o = repository.findById(id).orElseThrow(() -> ResourceNotFoundException.of("OffreEmploi", id)); transition(o, StatutOffre.ANNULEE, currentUser.email()); return mapper.toResponse(o);
    }

    @Override
    @Transactional
    public void cloturerSiEffectifAtteint(Long demandeId, String auteur) {
        // API interne
        if (demandeWorkflowService.effectifRestantAPourvoir(demandeId) != 0) return;
        repository.findAll((r,q,cb) -> cb.and(cb.equal(r.get("demande").get("id"), demandeId), cb.equal(r.get("statut"), StatutOffre.PUBLIEE))).forEach(o -> transition(o, StatutOffre.CLOTUREE, auteur));
    }

    @Override
    @Transactional
    public int cloturerOffresEchues(java.time.LocalDate aujourdhui) {
        var offres = repository.findAll((r,q,cb) -> cb.and(cb.equal(r.get("statut"), StatutOffre.PUBLIEE), cb.lessThan(r.get("dateCloture"), aujourdhui)));
        offres.forEach(o -> transition(o, StatutOffre.CLOTUREE, "PLANIFICATEUR")); return offres.size();
    }
    private void transition(OffreEmploi o, StatutOffre cible, String auteur) {
        StatutOffre ancien = o.getStatut(); if (!ancien.peutPasserA(cible)) throw new InvalidStateTransitionException("OffreEmploi", ancien, cible);
        o.setStatut(cible); historique.enregistrer(TypeEntite.OFFRE_EMPLOI, o.getId(), ancien.name(), cible.name(), auteur, null);
    }
}
