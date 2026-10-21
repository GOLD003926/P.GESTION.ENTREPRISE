package com.gntfh.sirh_api.embauche.service;

import com.gntfh.sirh_api.common.audit.dto.HistoriqueStatutResponse;
import com.gntfh.sirh_api.common.audit.service.AuditService;
import com.gntfh.sirh_api.common.audit.service.HistoriqueStatutService;
import com.gntfh.sirh_api.common.document.service.DocumentService;
import com.gntfh.sirh_api.common.dto.PageResponse;
import com.gntfh.sirh_api.common.notification.service.NotificationService;
import com.gntfh.sirh_api.common.security.CurrentUser;
import com.gntfh.sirh_api.embauche.dto.DerogationRequest;
import com.gntfh.sirh_api.embauche.dto.PropositionEmbaucheRequest;
import com.gntfh.sirh_api.embauche.dto.PropositionEmbaucheResponse;
import com.gntfh.sirh_api.embauche.dto.PropositionEmbaucheUpdateRequest;
import com.gntfh.sirh_api.embauche.entity.PropositionEmbauche;
import com.gntfh.sirh_api.embauche.enums.StatutProposition;
import com.gntfh.sirh_api.embauche.mapper.PropositionEmbaucheMapper;
import com.gntfh.sirh_api.embauche.repository.PropositionEmbaucheRepository;
import com.gntfh.sirh_api.common.config.SirhProperties;
import com.gntfh.sirh_api.recrutement.service.CandidatureService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PropositionEmbaucheServiceImpl implements PropositionEmbaucheService {

    private final PropositionEmbaucheRepository propositionRepository;
    private final PropositionEmbaucheMapper mapper;
    private final PropositionPdfService propositionPdfService;
    private final DocumentService documentService;
    private final CandidatureService candidatureService;
    private final HistoriqueStatutService historiqueStatutService;
    private final AuditService auditService;
    private final NotificationService notificationService;
    private final SirhProperties sirhProperties;

    private UnsupportedOperationException todo(String codeRegle) {
        return new UnsupportedOperationException("TODO " + codeRegle);
    }

    /**
     * RG-26, RG-27, EMB-01, EMB-02, EMB-03 et EMB-04 : crée un brouillon uniquement pour une candidature
     * RETENUE, reprend le poste de sa demande, contrôle les mentions obligatoires et calcule la dérogation
     * à partir de la grille salariale.
     *
     * @throws BusinessRuleException si la candidature, le poste, le salaire ou la durée ne respecte pas les règles
     * @throws DuplicateResourceException si une proposition active existe déjà pour cette candidature
     */
    @Override
    @Transactional
    public PropositionEmbaucheResponse creer(PropositionEmbaucheRequest request, CurrentUser currentUser) {
        throw todo("EMB-01");
    }

    /**
     * EMB-04 : modifie uniquement une proposition BROUILLON et recalcule la dérogation salariale requise.
     *
     * @throws ResourceNotFoundException si la proposition n'existe pas
     * @throws BusinessRuleException si la proposition n'est plus modifiable ou si les données sont incohérentes
     */
    @Override
    @Transactional
    public PropositionEmbaucheResponse modifier(
            Long id,
            PropositionEmbaucheUpdateRequest request,
            CurrentUser currentUser
    ) {
        throw todo("EMB-04");
    }

    /**
     * Retourne une proposition visible par l'acteur.
     *
     * @throws ResourceNotFoundException si la proposition est absente ou hors périmètre
     */
    @Override
    public PropositionEmbaucheResponse obtenir(Long id, CurrentUser currentUser) {
        throw todo("EMB-01");
    }

    /**
     * Recherche les propositions visibles, avec tri limité à id, statut, dateEnvoi, dateLimiteReponse et createdAt.
     *
     * @throws BusinessRuleException si le tri demandé n'est pas autorisé
     */
    @Override
    public PageResponse<PropositionEmbaucheResponse> rechercher(
            Long candidatureId,
            StatutProposition statut,
            Pageable pageable,
            CurrentUser currentUser
    ) {
        throw todo("EMB-01");
    }

    /**
     * EMB-03 : enregistre le valideur DG et l'horodatage de validation de la dérogation salariale.
     *
     * @throws ResourceNotFoundException si la proposition n'existe pas
     * @throws BusinessRuleException si aucune dérogation n'est requise ou si l'acteur n'est pas autorisé
     */
    @Override
    @Transactional
    public PropositionEmbaucheResponse validerDerogation(
            Long id,
            DerogationRequest request,
            CurrentUser currentUser
    ) {
        throw todo("EMB-03");
    }

    /**
     * EMB-03, EMB-05 : exige un brouillon complet et la validation DG si nécessaire, génère et archive le PDF,
     * fixe la date d'envoi et, si aucune date limite n'a été fournie, la calcule avec le délai configuré.
     * Passe ensuite la candidature à PROPOSITION_ENVOYEE, met à jour l'historique et notifie la RH.
     *
     * @throws ResourceNotFoundException si la proposition n'existe pas
     * @throws BusinessRuleException si le brouillon est incomplet ou si la dérogation requise n'est pas validée
     * @throws InvalidStateTransitionException si la proposition ne peut pas être envoyée
     */
    @Override
    @Transactional
    public PropositionEmbaucheResponse envoyer(Long id, CurrentUser currentUser) {
        throw todo("EMB-05");
    }

    /**
     * Retire une proposition active, remet la candidature au statut RETENUE et enregistre l'historique.
     *
     * @throws ResourceNotFoundException si la proposition n'existe pas
     * @throws InvalidStateTransitionException si le retrait n'est pas autorisé depuis le statut courant
     */
    @Override
    @Transactional
    public PropositionEmbaucheResponse retirer(Long id, CurrentUser currentUser) {
        throw todo("EMB-01");
    }

    /**
     * Retourne l'historique des statuts d'une proposition visible par l'acteur.
     *
     * @throws ResourceNotFoundException si la proposition est absente ou hors périmètre
     */
    @Override
    public List<HistoriqueStatutResponse> historique(Long id, CurrentUser currentUser) {
        throw todo("EMB-01");
    }
}
