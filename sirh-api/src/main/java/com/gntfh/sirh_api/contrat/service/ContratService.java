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
import org.springframework.data.domain.Pageable;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
import java.util.List;

/**
 * Service de gestion des contrats.
 */
public interface ContratService {

    /**
     * Crée un contrat en statut BROUILLON après validation de sa durée et de ses dates.
     *
     * RG-33 à RG-38, CON-01 à CON-03, CON-05, CON-14.
     */
    ContratResponse creer(ContratRequest request, CurrentUser currentUser);

    /**
     * Modifie un contrat existant.
     *
     * CON-09, CON-14.
     */
    ContratResponse modifier(Long id, ContratUpdateRequest request, CurrentUser currentUser);

    /**
     * Retourne le détail d'un contrat.
     *
     * CON-12 : applique la visibilité selon le rôle et le périmètre de l'utilisateur.
     */
    ContratResponse obtenir(Long id, CurrentUser currentUser);

    /**
     * Recherche paginée de contrats.
     *
     * CON-12 : limite la recherche aux contrats visibles et n'accepte que les tris autorisés
     * (id, numero, dateDebut, dateFinPrevue, statut).
     */
    PageResponse<ContratResponse> rechercher(
            Long employeId,
            TypeContrat type,
            StatutContrat statut,
            LocalDate finAvant,
            Pageable pageable,
            CurrentUser currentUser
    );

    /**
     * Retourne les contrats du salarié connecté.
     *
     * CON-12.
     */
    PageResponse<ContratResponse> mesContrats(CurrentUser currentUser);

    /**
     * Passe un contrat de BROUILLON à A_SIGNER.
     */
    void envoyerPourSignature(Long id, CurrentUser currentUser);

    /**
     * Enregistre le contrat signé.
     *
     * RG-39, CON-10 : enregistre la preuve de signature et tente l'activation du contrat.
     */
    void enregistrerSignature(Long id, MultipartFile fichier, LocalDate dateSignature, CurrentUser currentUser);

    /**
     * Crée un nouveau contrat brouillon par renouvellement.
     *
     * RG-37, CON-04 à CON-06, CON-08.
     */
    ContratResponse renouveler(Long id, RenouvellementRequest request, CurrentUser currentUser);

    /**
     * Crée un nouveau contrat brouillon par transformation.
     *
     * RG-37, CON-07, CON-08.
     */
    ContratResponse transformer(Long id, TransformationRequest request, CurrentUser currentUser);

    /**
     * Termine un contrat actif.
     *
     * RG-39, CON-11, CON-14.
     */
    void terminer(Long id, FinContratRequest request, CurrentUser currentUser);

    /**
     * Annule un contrat en brouillon ou en attente de signature et publie l'événement associé.
     *
     * CON-08.
     */
    void annuler(Long id, CurrentUser currentUser);

    /**
     * Retourne l'historique des transitions d'un contrat visible par l'utilisateur.
     *
     * CON-12.
     */
    List<HistoriqueStatutResponse> historique(Long id, CurrentUser currentUser);

    /**
     * API interne : création du contrat initial depuis l'embauche.
     *
     * RG-33.
     */
    Contrat creerContratInitial(ContratInitialCommande commande, String auteur);

    /**
     * API interne : active un contrat uniquement après dépôt du document signé et atteinte
     * de sa date de début.
     *
     * RG-39, RG-41, CON-05, CON-10, CON-11.
     *
     * Algorithme attendu :
     * 1. Charger le contrat et ne rien activer s'il n'est pas A_SIGNER, n'a pas de document signé
     *    ou si dateDebut est postérieure à la date de référence.
     * 2. Rechercher un contrat précédent ACTIF pour le même employé.
     * 3. S'il existe, vérifier la transition ACTIF → TERMINE, définir sa date de fin réelle à
     *    dateDebut moins un jour et son motif à RENOUVELLEMENT (type identique) ou TRANSFORMATION
     *    (type différent), puis enregistrer son historique de statut.
     * 4. Vérifier la transition A_SIGNER → ACTIF et activer le contrat courant.
     * 5. Activer l'employé avec EmployeService.activer(employeId, dateDebut, auteur).
     * 6. Si aucune affectation active n'existe pour l'employé, créer l'affectation initiale
     *    avec AffectationService.creerInitiale(employe, poste, contratId, managerId, dateDebut, auteur).
     * 7. Enregistrer l'historique de statut du contrat courant.
     * 8. Laisser la transaction englober toutes les étapes afin que toute erreur annule l'ensemble.
     */
    void activerSiPret(Long contratId, String auteur);

    /**
     * API interne : active en lot les contrats signés dont la date de début est échue.
     */
    int activerContratsDus(LocalDate aujourdhui, String auteur);

    /**
     * API interne : envoie une seule alerte RH pour chaque contrat actif ESSAI ou CDD qui arrive
     * à échéance dans le délai configuré, puis marque l'alerte comme envoyée.
     *
     * RG-40, CON-13.
     */
    int alerterFinsProches(LocalDate aujourdhui);
}
