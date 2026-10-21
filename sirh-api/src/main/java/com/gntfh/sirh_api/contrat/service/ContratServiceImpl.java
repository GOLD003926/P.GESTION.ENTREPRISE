package com.gntfh.sirh_api.contrat.service;

import com.gntfh.sirh_api.common.audit.dto.HistoriqueStatutResponse;
import com.gntfh.sirh_api.common.dto.PageResponse;
import com.gntfh.sirh_api.common.security.CurrentUser;
import com.gntfh.sirh_api.contrat.dto.ContratInitialCommande;
import com.gntfh.sirh_api.contrat.dto.ContratRequest;
import com.gntfh.sirh_api.contrat.dto.ContratResponse;
import com.gntfh.sirh_api.contrat.dto.ContratUpdateRequest;
import com.gntfh.sirh_api.contrat.dto.FinContratRequest;
import com.gntfh.sirh_api.contrat.dto.RenouvellementRequest;
import com.gntfh.sirh_api.contrat.dto.TransformationRequest;
import com.gntfh.sirh_api.contrat.entity.Contrat;
import com.gntfh.sirh_api.contrat.enums.StatutContrat;
import com.gntfh.sirh_api.contrat.enums.TypeContrat;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
import java.util.List;

/**
 * Squelette de service du module contrat.
 * Les règles détaillées restent à implémenter par l'équipe métier.
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ContratServiceImpl implements ContratService {

    private UnsupportedOperationException todo(String codeRegle) {
        return new UnsupportedOperationException("TODO " + codeRegle);
    }

    /**
     * RG-33 à RG-38, CON-01 à CON-03, CON-05, CON-14.
     */
    @Override
    @Transactional
    public ContratResponse creer(ContratRequest request, CurrentUser currentUser) {
        throw todo("CON-01");
    }

    /**
     * CON-09, CON-14.
     */
    @Override
    @Transactional
    public ContratResponse modifier(Long id, ContratUpdateRequest request, CurrentUser currentUser) {
        throw todo("CON-09");
    }

    /**
     * CON-12 : contrôle la visibilité du contrat selon le rôle et le périmètre.
     */
    @Override
    public ContratResponse obtenir(Long id, CurrentUser currentUser) {
        throw todo("CON-12");
    }

    /**
     * CON-12 : contrôle la visibilité, les filtres et la liste blanche de tri
     * (id, numero, dateDebut, dateFinPrevue, statut).
     */
    @Override
    public PageResponse<ContratResponse> rechercher(
            Long employeId,
            TypeContrat type,
            StatutContrat statut,
            LocalDate finAvant,
            Pageable pageable,
            CurrentUser currentUser
    ) {
        throw todo("CON-12");
    }

    /**
     * CON-12.
     */
    @Override
    public PageResponse<ContratResponse> mesContrats(CurrentUser currentUser) {
        throw todo("CON-12");
    }

    /**
     * RG-39, CON-10 : enregistre le document signé puis tente l'activation.
     */
    @Override
    @Transactional
    public void envoyerPourSignature(Long id, CurrentUser currentUser) {
        throw todo("CON-10");
    }

    /**
     * CON-10.
     */
    @Override
    @Transactional
    public void enregistrerSignature(Long id, MultipartFile fichier, LocalDate dateSignature, CurrentUser currentUser) {
        throw todo("CON-10");
    }

    /**
     * RG-37, CON-04 à CON-06, CON-08.
     */
    @Override
    @Transactional
    public ContratResponse renouveler(Long id, RenouvellementRequest request, CurrentUser currentUser) {
        throw todo("CON-04");
    }

    /**
     * RG-37, CON-07, CON-08.
     */
    @Override
    @Transactional
    public ContratResponse transformer(Long id, TransformationRequest request, CurrentUser currentUser) {
        throw todo("CON-07");
    }

    /**
     * RG-39, CON-11, CON-14.
     */
    @Override
    @Transactional
    public void terminer(Long id, FinContratRequest request, CurrentUser currentUser) {
        throw todo("CON-11");
    }

    /**
     * CON-08 : annulation réservée aux contrats BROUILLON ou A_SIGNER.
     */
    @Override
    @Transactional
    public void annuler(Long id, CurrentUser currentUser) {
        throw todo("CON-08");
    }

    /**
     * CON-12 : retourne l'historique seulement si l'utilisateur peut voir le contrat.
     */
    @Override
    public List<HistoriqueStatutResponse> historique(Long id, CurrentUser currentUser) {
        throw todo("CON-12");
    }

    /**
     * RG-33.
     */
    @Override
    @Transactional
    public Contrat creerContratInitial(ContratInitialCommande commande, String auteur) {
        throw todo("RG-33");
    }

    /**
     * RG-39, RG-41, CON-05, CON-10, CON-11.
     *
     * Étapes : charger le contrat ; vérifier A_SIGNER, document signé et date de début atteinte ;
     * terminer le précédent actif avec le motif RENOUVELLEMENT ou TRANSFORMATION et une date de fin
     * égale à la veille du nouveau contrat ; activer le nouveau contrat ; activer l'employé ; créer
     * l'affectation initiale si aucune affectation active n'existe ; enregistrer les historiques
     * des transitions. Toute erreur annule l'ensemble de la transaction.
     */
    @Override
    @Transactional
    public void activerSiPret(Long contratId, String auteur) {
        throw todo("CON-10");
    }

    /**
     * CON-10.
     */
    @Override
    @Transactional
    public int activerContratsDus(LocalDate aujourdhui, String auteur) {
        throw todo("CON-10");
    }

    /**
     * RG-40, CON-13 : alerte la RH une seule fois pour chaque fin proche d'ESSAI ou de CDD.
     */
    @Override
    @Transactional
    public int alerterFinsProches(LocalDate aujourdhui) {
        throw todo("CON-13");
    }
}
