package com.gntfh.sirh_api.recrutement.service;

import com.gntfh.sirh_api.common.dto.PageResponse;
import com.gntfh.sirh_api.common.security.CurrentUser;
import com.gntfh.sirh_api.recrutement.dto.EvaluationCandidatRequest;
import com.gntfh.sirh_api.recrutement.dto.EvaluationCandidatResponse;
import com.gntfh.sirh_api.recrutement.dto.EvaluationCandidatUpdateRequest;
import com.gntfh.sirh_api.recrutement.entity.*;
import com.gntfh.sirh_api.recrutement.mapper.EvaluationCandidatMapper;
import com.gntfh.sirh_api.recrutement.repository.*;
import com.gntfh.sirh_api.auth.repository.UtilisateurRepository;
import com.gntfh.sirh_api.common.exception.*;
import com.gntfh.sirh_api.common.audit.enums.TypeEntite;
import com.gntfh.sirh_api.common.audit.service.HistoriqueStatutService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Implémentation du service EvaluationCandidatService.
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class EvaluationCandidatServiceImpl implements EvaluationCandidatService {
    private final EvaluationCandidatRepository repository;
    private final EntretienRepository entretienRepository;
    private final EntretienIntervenantRepository intervenantRepository;
    private final UtilisateurRepository utilisateurRepository;
    private final EvaluationCandidatMapper mapper;
    private final HistoriqueStatutService historique;

    @Override
    @Transactional
    public EvaluationCandidatResponse creer(EvaluationCandidatRequest request, CurrentUser currentUser) {
        Entretien e=entretienRepository.findById(request.entretienId()).orElseThrow(()->ResourceNotFoundException.of("Entretien",request.entretienId())); var u=utilisateurRepository.findById(currentUser.id()).orElseThrow(()->ResourceNotFoundException.of("Utilisateur",currentUser.id())); if(!intervenantRepository.existsByEntretienIdAndUtilisateurId(e.getId(),u.getId()))throw new BusinessRuleException("L'évaluateur n'est pas intervenant."); if(e.getStatut()!=com.gntfh.sirh_api.recrutement.enums.StatutEntretien.PLANIFIE&&e.getStatut()!=com.gntfh.sirh_api.recrutement.enums.StatutEntretien.REALISE)throw new BusinessRuleException("Entretien non évaluable."); if(repository.findActiveByEntretienIdAndEvaluateurId(e.getId(),u.getId()).isPresent())throw new DuplicateResourceException("Une évaluation active existe déjà."); return mapper.toResponse(repository.save(mapper.toEntity(request,e,u)));
    }

    @Override
    @Transactional
    public EvaluationCandidatResponse modifier(Long id, EvaluationCandidatUpdateRequest request, CurrentUser currentUser) {
        EvaluationCandidat e=get(id); checkAuthor(e,currentUser); if(e.isVerrouillee())throw new BusinessRuleException("Evaluation verrouillée."); mapper.updateEntity(request,e); return mapper.toResponse(e);
    }

    @Override
    @Transactional
    public EvaluationCandidatResponse verrouiller(Long id, CurrentUser currentUser) {
        EvaluationCandidat e=get(id); if(!e.getEvaluateur().getId().equals(currentUser.id())&&currentUser.role()!=com.gntfh.sirh_api.auth.enums.Role.RH)throw new BusinessRuleException("Action non autorisée."); e.setVerrouillee(true); return mapper.toResponse(e);
    }

    @Override
    @Transactional
    public EvaluationCandidatResponse corriger(Long id, EvaluationCandidatUpdateRequest request, CurrentUser currentUser) {
        if(currentUser.role()!=com.gntfh.sirh_api.auth.enums.Role.RH)throw new BusinessRuleException("Correction réservée à la RH."); EvaluationCandidat old=get(id); old.setActive(false); EvaluationCandidat n=mapper.toEntity(new com.gntfh.sirh_api.recrutement.dto.EvaluationCandidatRequest(old.getEntretien().getId(),request.noteGlobale(),request.criteres(),request.avis(),request.commentaire()),old.getEntretien(),old.getEvaluateur()); n.setEvaluationPrecedente(old); return mapper.toResponse(repository.save(n));
    }

    @Override
    public PageResponse<EvaluationCandidatResponse> lister(Long entretienId, Pageable pageable, CurrentUser currentUser) {
        var list=repository.findActiveByEntretienId(entretienId); return new PageResponse<>(list.stream().map(mapper::toResponse).toList(),0,list.size(),list.size(),list.isEmpty()?0:1,true,true);
    }
    private EvaluationCandidat get(Long id){return repository.findById(id).orElseThrow(()->ResourceNotFoundException.of("EvaluationCandidat",id));}
    private void checkAuthor(EvaluationCandidat e,CurrentUser u){if(!e.getEvaluateur().getId().equals(u.id()))throw new BusinessRuleException("Seul l'auteur peut modifier.");}
}
