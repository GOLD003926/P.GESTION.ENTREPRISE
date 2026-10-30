package com.gntfh.sirh_api.onboarding.service;

import com.gntfh.sirh_api.common.dto.PageResponse;
import com.gntfh.sirh_api.common.audit.enums.TypeEntite;
import com.gntfh.sirh_api.common.audit.service.HistoriqueStatutService;
import com.gntfh.sirh_api.common.document.entity.Document;
import com.gntfh.sirh_api.common.document.repository.DocumentRepository;
import com.gntfh.sirh_api.common.exception.BusinessRuleException;
import com.gntfh.sirh_api.common.exception.InvalidStateTransitionException;
import com.gntfh.sirh_api.common.exception.ResourceNotFoundException;
import com.gntfh.sirh_api.common.security.CurrentUser;
import com.gntfh.sirh_api.auth.entity.Utilisateur;
import com.gntfh.sirh_api.auth.enums.Role;
import com.gntfh.sirh_api.auth.repository.UtilisateurRepository;
import com.gntfh.sirh_api.onboarding.entity.OnboardingTache;
import com.gntfh.sirh_api.onboarding.dto.OnboardingTacheRequest;
import com.gntfh.sirh_api.onboarding.dto.OnboardingTacheResponse;
import com.gntfh.sirh_api.onboarding.enums.StatutTache;
import com.gntfh.sirh_api.onboarding.mapper.OnboardingTacheMapper;
import com.gntfh.sirh_api.onboarding.repository.OnboardingTacheRepository;
import com.gntfh.sirh_api.onboarding.repository.OnboardingRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class OnboardingTacheServiceImpl implements OnboardingTacheService {

    private final OnboardingTacheRepository repository;
    private final OnboardingRepository onboardingRepository;
    private final UtilisateurRepository utilisateurRepository;
    private final DocumentRepository documentRepository;
    private final OnboardingTacheMapper mapper;
    private final OnboardingService onboardingService;
    private final HistoriqueStatutService historique;

    /** ONB-04 : RH voit tout ; les autres acteurs voient les tâches correspondant à leur rôle. */
    @Override
    public PageResponse<OnboardingTacheResponse> mesTaches(StatutTache statut, Pageable pageable,
                                                            CurrentUser currentUser) {
        var statuts = statut == null ? java.util.List.of(StatutTache.A_FAIRE, StatutTache.EN_COURS) : java.util.List.of(statut);
        org.springframework.data.domain.Page<OnboardingTache> page = currentUser.role() == Role.RH
                ? repository.findByRoleResponsableInAndStatutIn(java.util.Arrays.asList(Role.values()), statuts, pageable)
                : repository.findByRoleResponsableAndStatutIn(currentUser.role(), statuts, pageable);
        return PageResponse.from(page, mapper::toResponse);
    }

    /** ONB-04 : contrôle le rôle responsable avant A_FAIRE → EN_COURS. */
    @Override
    @Transactional
    public OnboardingTacheResponse demarrer(Long id, OnboardingTacheRequest request, CurrentUser currentUser) {
        OnboardingTache tache = trouver(id);
        verifierAcces(tache, currentUser);
        transitionner(tache, StatutTache.EN_COURS, currentUser.email());
        if (tache.getOnboarding().getStatut() == com.gntfh.sirh_api.onboarding.enums.StatutOnboarding.A_DEMARRER) {
            historique.enregistrer(TypeEntite.ONBOARDING, tache.getOnboarding().getId(),
                    tache.getOnboarding().getStatut().name(),
                    com.gntfh.sirh_api.onboarding.enums.StatutOnboarding.EN_COURS.name(),
                    currentUser.email(), null);
            tache.getOnboarding().setStatut(com.gntfh.sirh_api.onboarding.enums.StatutOnboarding.EN_COURS);
        }
        appliquer(tache, request);
        return mapper.toResponse(tache);
    }

    /** ONB-04, ONB-05 : réserve le traitement au responsable et refuse une tâche déjà FAIT. */
    @Override
    @Transactional
    public OnboardingTacheResponse terminer(Long id, OnboardingTacheRequest request, CurrentUser currentUser) {
        OnboardingTache tache = trouver(id);
        verifierAcces(tache, currentUser);
        transitionner(tache, StatutTache.FAIT, currentUser.email());
        appliquer(tache, request);
        tache.setDateRealisation(java.time.Instant.now());
        onboardingService.verifierCloture(tache.getOnboarding(), currentUser.email());
        return mapper.toResponse(tache);
    }

    /** ONB-07 : la RH seule peut marquer NON_APPLICABLE une tâche facultative. */
    @Override
    @Transactional
    public OnboardingTacheResponse marquerNonApplicable(Long id, OnboardingTacheRequest request,
                                                         CurrentUser currentUser) {
        if (currentUser.role() != Role.RH) throw new BusinessRuleException("Action réservée à la RH.");
        OnboardingTache tache = trouver(id);
        if (Boolean.TRUE.equals(tache.getObligatoire())) throw new BusinessRuleException("Une tâche obligatoire ne peut pas être non applicable.");
        transitionner(tache, StatutTache.NON_APPLICABLE, currentUser.email());
        appliquer(tache, request);
        onboardingService.verifierCloture(tache.getOnboarding(), currentUser.email());
        return mapper.toResponse(tache);
    }

    /** ONB-04 : vérifie que l'utilisateur assigné est actif et possède le rôle responsable. */
    @Override
    @Transactional
    public OnboardingTacheResponse assigner(Long id, OnboardingTacheRequest request, CurrentUser currentUser) {
        if (currentUser.role() != Role.RH) throw new BusinessRuleException("Action réservée à la RH.");
        OnboardingTache tache = trouver(id);
        if (request.assigneAId() == null) {
            tache.setAssigneA(null);
        } else {
            Utilisateur u = utilisateurRepository.findById(request.assigneAId())
                    .orElseThrow(() -> ResourceNotFoundException.of("Utilisateur", request.assigneAId()));
            if (!u.isActif() || u.getRole() != tache.getRoleResponsable()) {
                throw new BusinessRuleException("L'utilisateur doit être actif et avoir le rôle responsable.");
            }
            tache.setAssigneA(u);
        }
        return mapper.toResponse(tache);
    }

    private OnboardingTache trouver(Long id) {
        return repository.findById(id).orElseThrow(() -> ResourceNotFoundException.of("OnboardingTache", id));
    }

    private void verifierAcces(OnboardingTache tache, CurrentUser user) {
        if (user.role() != Role.RH && user.role() != tache.getRoleResponsable()) {
            throw new BusinessRuleException("Cette tâche ne peut pas être traitée par votre rôle.");
        }
    }

    private void transitionner(OnboardingTache tache, StatutTache cible, String auteur) {
        if (!tache.getStatut().peutPasserA(cible)) {
            throw new InvalidStateTransitionException("OnboardingTache", tache.getStatut(), cible);
        }
        StatutTache ancien = tache.getStatut();
        tache.setStatut(cible);
        historique.enregistrer(TypeEntite.ONBOARDING_TACHE, tache.getId(), ancien.name(), cible.name(), auteur, null);
    }

    private void appliquer(OnboardingTache tache, OnboardingTacheRequest request) {
        Document document = request.preuveDocumentId() == null ? null : documentRepository.findById(request.preuveDocumentId())
                .orElseThrow(() -> ResourceNotFoundException.of("Document", request.preuveDocumentId()));
        mapper.updateEntity(tache, request.commentaire(), document);
    }
}
