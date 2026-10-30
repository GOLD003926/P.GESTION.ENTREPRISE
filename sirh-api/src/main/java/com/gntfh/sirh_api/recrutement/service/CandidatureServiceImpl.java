package com.gntfh.sirh_api.recrutement.service;

import com.gntfh.sirh_api.common.audit.dto.HistoriqueStatutResponse;
import com.gntfh.sirh_api.common.dto.PageResponse;
import com.gntfh.sirh_api.common.security.CurrentUser;
import com.gntfh.sirh_api.recrutement.dto.CandidatureRequest;
import com.gntfh.sirh_api.recrutement.dto.CandidatureResponse;
import com.gntfh.sirh_api.recrutement.enums.StatutCandidature;
import com.gntfh.sirh_api.recrutement.entity.Candidature;
import com.gntfh.sirh_api.recrutement.entity.Candidat;
import com.gntfh.sirh_api.recrutement.entity.OffreEmploi;
import com.gntfh.sirh_api.recrutement.mapper.CandidatureMapper;
import com.gntfh.sirh_api.recrutement.repository.CandidatureRepository;
import com.gntfh.sirh_api.recrutement.repository.OffreEmploiRepository;
import com.gntfh.sirh_api.recrutement.repository.CandidatRepository;
import com.gntfh.sirh_api.common.document.service.DocumentService;
import com.gntfh.sirh_api.common.document.enums.TypeDocument;
import com.gntfh.sirh_api.common.audit.enums.TypeEntite;
import com.gntfh.sirh_api.common.audit.service.HistoriqueStatutService;
import com.gntfh.sirh_api.common.exception.*;
import com.gntfh.sirh_api.recrutement.service.CandidatService;
import org.springframework.data.jpa.domain.Specification;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

/**
 * Implémentation du service CandidatureService.
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CandidatureServiceImpl implements CandidatureService {
    private final CandidatureRepository repository;
    private final OffreEmploiRepository offreRepository;
    private final CandidatService candidatService;
    private final CandidatRepository candidatRepository;
    private final CandidatureMapper mapper;
    private final DocumentService documentService;
    private final HistoriqueStatutService historiqueService;

    @Override
    @Transactional
    public CandidatureResponse creer(CandidatureRequest request, CurrentUser currentUser) {
        OffreEmploi offre = offreRepository.findById(request.offreId()).orElseThrow(() -> ResourceNotFoundException.of("OffreEmploi", request.offreId()));
        if (offre.getStatut() != com.gntfh.sirh_api.recrutement.enums.StatutOffre.PUBLIEE || (offre.getDateCloture() != null && offre.getDateCloture().isBefore(java.time.LocalDate.now()))) throw new BusinessRuleException("Cette offre n'accepte plus de candidatures.");
        Candidat candidat = request.candidatId() != null ? candidatRepository.findById(request.candidatId()).orElseThrow(() -> ResourceNotFoundException.of("Candidat", request.candidatId())) : candidatService.trouverOuCreer(request.candidat());
        if (repository.existsByCandidatIdAndOffreId(candidat.getId(), offre.getId())) throw DuplicateResourceException.of("Candidature", "candidat et offre", candidat.getId()+"/"+offre.getId());
        return mapper.toResponse(repository.save(mapper.toEntity(request, candidat, offre)));
    }

    @Override
    @Transactional
    public CandidatureResponse televerserCv(Long id, MultipartFile fichier, CurrentUser currentUser) {
        Candidature c = get(id); try { c.setCvDocument(documentService.enregistrer(fichier, TypeDocument.CV, TypeEntite.CANDIDATURE, id)); } catch (java.io.IOException e) { throw new BusinessRuleException("Impossible d'enregistrer le CV."); } return mapper.toResponse(c);
    }

    @Override
    public CandidatureResponse obtenir(Long id, CurrentUser currentUser) {
        return mapper.toResponse(get(id));
    }

    @Override
    public PageResponse<CandidatureResponse> rechercher(Long offreId, StatutCandidature statut, Long candidatId,
                                                        Pageable pageable, CurrentUser currentUser) {
        Specification<Candidature> s=(r,q,cb)->{var p=cb.conjunction(); if(offreId!=null)p=cb.and(p,cb.equal(r.get("offre").get("id"),offreId)); if(statut!=null)p=cb.and(p,cb.equal(r.get("statut"),statut)); if(candidatId!=null)p=cb.and(p,cb.equal(r.get("candidat").get("id"),candidatId)); return p;};
        return PageResponse.from(repository.findAll(s,pageable), mapper::toResponse);
    }

    @Override
    @Transactional
    public CandidatureResponse desister(Long id, CurrentUser currentUser) {
        Candidature c=get(id); transition(c, StatutCandidature.DESISTEE, currentUser.email(), null); return mapper.toResponse(c);
    }

    @Override
    @Transactional
    public CandidatureResponse promouvoirListeAttente(Long id, CurrentUser currentUser) {
        Candidature c=get(id); if(c.getStatut()!=StatutCandidature.LISTE_ATTENTE || c.getOffre().getDemande()==null) throw new BusinessRuleException("Candidature non éligible."); if(c.getOffre().getDemande().getEffectifDemande()-c.getOffre().getDemande().getEffectifPourvu()<=0) throw new BusinessRuleException("Aucun poste disponible."); transition(c,StatutCandidature.RETENUE,currentUser.email(),null); return mapper.toResponse(c);
    }

    @Override
    public PageResponse<HistoriqueStatutResponse> historique(Long id, CurrentUser currentUser) {
        var content = historiqueService.lister(TypeEntite.CANDIDATURE,id);
        return new PageResponse<>(content, 0, content.size(), content.size(), content.isEmpty()?0:1, true, true);
    }

    // ==================== API interne ====================

    @Override
    @Transactional
    public void marquerPropositionEnvoyee(Long id, String auteur) {
        // API interne
        transition(get(id), StatutCandidature.PROPOSITION_ENVOYEE, auteur, null);
    }

    @Override
    @Transactional
    public void marquerEmbauchee(Long id, String auteur) {
        // API interne
        transition(get(id), StatutCandidature.EMBAUCHEE, auteur, null);
    }

    @Override
    @Transactional
    public void marquerNonRetenueApresProposition(Long id, String auteur, String motif) {
        // API interne
        Candidature c=get(id); c.setMotifRefus(motif); transition(c,StatutCandidature.NON_RETENUE,auteur,motif);
    }

    @Override
    @Transactional
    public void remettreRetenue(Long id, String auteur) {
        // API interne
        transition(get(id), StatutCandidature.RETENUE, auteur, null);
    }
    @Override
    public Candidature trouver(Long id){return repository.findById(id).orElseThrow(()->ResourceNotFoundException.of("Candidature",id));}

    private Candidature get(Long id){return trouver(id);}
    private void transition(Candidature c, StatutCandidature target, String author, String comment){var old=c.getStatut(); if(!old.peutPasserA(target)) throw new InvalidStateTransitionException("Candidature",old,target); c.setStatut(target); historiqueService.enregistrer(TypeEntite.CANDIDATURE,c.getId(),old.name(),target.name(),author,comment);}
}
