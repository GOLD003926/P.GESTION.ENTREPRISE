# Module `demande` : demande de recrutement et chaîne de validation

Bloc 1 du sujet : expression du besoin. Workflow : Manager → RH → Finance → Direction générale → ouverture du recrutement. Migration : `V4__demande_recrutement.sql`. Dépend de : `common`, `auth`, `organisation`.

## 1. Fichiers

Existants (vides) :

```
demande/entity/{DemandeRecrutement, Validation}
demande/enums/{TypeDemande, MotifDemande, StatutDemande, DecisionValidation, RoleValidateur}
demande/repository/{DemandeRecrutement, Validation}Repository
demande/dto/{DemandeRecrutementRequest, DemandeRecrutementResponse, ValidationRequest, ValidationResponse}
demande/mapper/{DemandeRecrutement, Validation}Mapper
demande/service/{DemandeRecrutementService, ValidationService, DemandeWorkflowService, BudgetService}
demande/controller/{DemandeRecrutementController, ValidationController}
```

À créer : `demande/dto/AnnulationRequest`, `demande/service/EmployeLookup` (port).

## 2. Énumérations

| Énumération | Valeurs | Méthodes |
|---|---|---|
| `TypeDemande` | `CREATION`, `REMPLACEMENT` | — |
| `MotifDemande` | `CROISSANCE`, `NOUVEAU_PROJET`, `RESTRUCTURATION`, `RENFORT_TEMPORAIRE`, `DEMISSION`, `LICENCIEMENT`, `RETRAITE`, `MUTATION`, `FIN_CONTRAT`, `DECES`, `AUTRE` | `boolean estValidePour(TypeDemande)` : les quatre premiers sont valides pour `CREATION`, les six suivants pour `REMPLACEMENT`, `AUTRE` pour les deux |
| `StatutDemande` | `BROUILLON`, `EN_VALIDATION_RH`, `EN_VALIDATION_FINANCE`, `EN_VALIDATION_DG`, `RETOURNEE`, `REJETEE`, `VALIDEE`, `EN_RECRUTEMENT`, `POURVUE`, `ANNULEE` | `transitionsAutorisees()`, `peutPasserA(cible)`, `estTerminal()` (`REJETEE`, `POURVUE`, `ANNULEE`), `estEnValidation()` |
| `DecisionValidation` | `EN_ATTENTE`, `APPROUVEE`, `REJETEE`, `RETOURNEE` | — |
| `RoleValidateur` | `RH(1)`, `FINANCE(2)`, `DG(3)` | `ordre()`, `Role versRole()` (rôle d'authentification correspondant), `StatutDemande statutEnValidation()` |

### Transitions de `StatutDemande`

| De | Vers autorisés |
|---|---|
| `BROUILLON` | `EN_VALIDATION_RH`, `ANNULEE` |
| `EN_VALIDATION_RH` | `EN_VALIDATION_FINANCE`, `RETOURNEE`, `REJETEE` |
| `EN_VALIDATION_FINANCE` | `EN_VALIDATION_DG`, `RETOURNEE`, `REJETEE` |
| `EN_VALIDATION_DG` | `VALIDEE`, `RETOURNEE`, `REJETEE` |
| `RETOURNEE` | `EN_VALIDATION_RH`, `ANNULEE` |
| `VALIDEE` | `EN_RECRUTEMENT` |
| `EN_RECRUTEMENT` | `POURVUE`, `ANNULEE` |
| `REJETEE`, `POURVUE`, `ANNULEE` | aucune |

## 3. Entités (`AuditableEntity`)

### 3.1 `DemandeRecrutement` (table `demande_recrutement`)

| Attribut | Colonne | Type | Null | Règle |
|---|---|---|---|---|
| `reference` | `reference` | `VARCHAR(30)` | Non | Unique, générée `DR-AAAA-NNNN` par `NumeroService` |
| `poste` | `poste_id` | `BIGINT` | Non | `@ManyToOne` LAZY, clé étrangère |
| `departement` | `departement_id` | `BIGINT` | Non | `@ManyToOne` LAZY, clé étrangère. **Dérivé du poste** à la création, non fourni par le client |
| `demandeur` | `demandeur_id` | `BIGINT` | Non | `@ManyToOne` LAZY vers `Utilisateur`, clé étrangère |
| `typeDemande` | `type_demande` | `VARCHAR(20)` | Non | `TypeDemande` |
| `employeRemplaceId` | `employe_remplace_id` | `BIGINT` | Oui | `Long`, **sans clé étrangère** (ajoutée par `V6`) |
| `effectifDemande` | `effectif_demande` | `INT` | Non | 1 à 50 |
| `effectifPourvu` | `effectif_pourvu` | `INT` | Non | Défaut 0 ; incrémenté à chaque embauche (RG-24) |
| `motif` | `motif` | `VARCHAR(30)` | Non | `MotifDemande` |
| `justification` | `justification` | `TEXT` | Oui | Obligatoire si motif `AUTRE` |
| `budgetEstime` | `budget_estime` | `NUMERIC(14,2)` | Non | Calculé (RG-05), jamais saisi |
| `dateSouhaitee` | `date_souhaitee` | `DATE` | Non | |
| `statut` | `statut` | `VARCHAR(30)` | Non | `StatutDemande`, défaut `BROUILLON` |
| `cycleCourant` | `cycle_courant` | `INT` | Non | Défaut 0 ; incrémenté à chaque soumission |

Index : `reference` unique, `idx_demande_statut`, `idx_demande_poste`, `idx_demande_demandeur`.

### 3.2 `Validation` (table `validation`)

| Attribut | Colonne | Type | Null | Règle |
|---|---|---|---|---|
| `demande` | `demande_id` | `BIGINT` | Non | `@ManyToOne` LAZY, clé étrangère |
| `cycle` | `cycle` | `INT` | Non | Numéro de soumission (≥ 1) |
| `ordre` | `ordre` | `INT` | Non | 1 RH, 2 Finance, 3 DG |
| `roleValidateur` | `role_validateur` | `VARCHAR(20)` | Non | `RoleValidateur` |
| `validateur` | `validateur_id` | `BIGINT` | Oui | `@ManyToOne` LAZY vers `Utilisateur` ; renseigné à la décision |
| `decision` | `decision` | `VARCHAR(20)` | Non | `DecisionValidation`, défaut `EN_ATTENTE` |
| `commentaire` | `commentaire` | `TEXT` | Oui | |
| `dateDecision` | `date_decision` | `TIMESTAMPTZ` | Oui | |

Unicité `(demande_id, cycle, ordre)`.

## 4. DTO

| DTO | Champs et validations |
|---|---|
| `DemandeRecrutementRequest` (création **et** modification) | `posteId` (obligatoire), `typeDemande` (obligatoire), `employeRemplaceId` (facultatif), `effectifDemande` (obligatoire, 1 à 50), `motif` (obligatoire), `justification` (≤ 2000), `dateSouhaitee` (obligatoire, aujourd'hui ou futur) |
| `DemandeRecrutementResponse` | `id`, `reference`, `posteId`, `posteIntitule`, `departementId`, `departementNom`, `demandeurId`, `demandeurNom`, `typeDemande`, `employeRemplaceId`, `effectifDemande`, `effectifPourvu`, `motif`, `justification`, `budgetEstime`, `dateSouhaitee`, `statut`, `cycleCourant`, `etapeCourante` (`RoleValidateur` ou nul), `createdAt`, `updatedAt` |
| `ValidationRequest` | `decision` (obligatoire, `APPROUVEE`, `REJETEE` ou `RETOURNEE`), `commentaire` (≤ 2000) |
| `ValidationResponse` | `id`, `demandeId`, `cycle`, `ordre`, `roleValidateur`, `validateurId`, `validateurNom`, `decision`, `commentaire`, `dateDecision` |
| `AnnulationRequest` | `motif` (obligatoire, ≤ 500) |

## 5. Services

### 5.1 `BudgetService`

`BigDecimal calculer(Poste poste, int effectif)` : `((salaireMin + salaireMax) / 2) × effectif × dureeBudgetMois × (1 + coefCharges)`, arrondi à 2 décimales (`HALF_UP`). Paramètres lus dans `SirhProperties.demande()` (RG-05).

### 5.2 Port `EmployeLookup`

Interface : `boolean existe(Long employeId)` et `Optional<Long> departementActuel(Long employeId)`. Implémentée par `employe`. Injectée avec `ObjectProvider` ; valeurs par défaut tant que `employe` n'existe pas : `existe` → `true`, `departementActuel` → vide.

### 5.3 `DemandeWorkflowService` (machine à états)

| Méthode | Type | Règle |
|---|---|---|
| `void transitionner(DemandeRecrutement, StatutDemande cible, String auteur, String commentaire)` | Interne au module | Vérifie `peutPasserA` (sinon `InvalidStateTransitionException`), applique, enregistre `HistoriqueStatut` |
| `void creerValidationsDuCycle(DemandeRecrutement)` | Interne au module | Incrémente `cycleCourant`, crée les trois lignes `Validation` (ordre 1, 2, 3) à `EN_ATTENTE` |
| `void marquerEnRecrutement(Long demandeId, String auteur)` | **API interne** (appelée par `recrutement` à la publication d'une offre) | `VALIDEE` → `EN_RECRUTEMENT` ; sans effet si déjà `EN_RECRUTEMENT` |
| `void enregistrerEmbauche(Long demandeId, String auteur)` | **API interne** (appelée par `embauche`) | Incrémente `effectifPourvu` ; si `effectifPourvu == effectifDemande` : `EN_RECRUTEMENT` → `POURVUE` (RG-24) |
| `int effectifRestantAPourvoir(Long demandeId)` | **API interne** | `effectifDemande - effectifPourvu` |

### 5.4 `DemandeRecrutementService`

| Méthode | Règle |
|---|---|
| `DemandeRecrutementResponse creer(DemandeRecrutementRequest, CurrentUser)` | DEM-01 à DEM-06, RG-03 à RG-07, génération de la référence, calcul du budget, statut `BROUILLON`, historique, journal d'audit |
| `DemandeRecrutementResponse modifier(Long id, DemandeRecrutementRequest, CurrentUser)` | RG-11 : uniquement `BROUILLON` ou `RETOURNEE`, uniquement par le demandeur ; recalcul du budget |
| `DemandeRecrutementResponse obtenir(Long id, CurrentUser)` | DEM-07 (visibilité) |
| `PageResponse<DemandeRecrutementResponse> rechercher(String q, StatutDemande statut, Long posteId, Long departementId, Pageable, CurrentUser)` | `q` sur la référence. DEM-07. Tri autorisé : `id`, `reference`, `statut`, `dateSouhaitee`, `budgetEstime`, `createdAt` |
| `DemandeRecrutementResponse soumettre(Long id, CurrentUser)` | RG-06 (date), RG-07, RG-08 : `BROUILLON` ou `RETOURNEE` → `EN_VALIDATION_RH` ; création des validations du cycle ; notification des utilisateurs RH |
| `DemandeRecrutementResponse annuler(Long id, AnnulationRequest, CurrentUser)` | DEM-08 : depuis `BROUILLON`, `RETOURNEE` (par le demandeur ou la RH) ou `EN_RECRUTEMENT` (par la RH) |
| `List<HistoriqueStatutResponse> historique(Long id, CurrentUser)` | Même visibilité que `obtenir` |

### 5.5 `ValidationService`

| Méthode | Règle |
|---|---|
| `PageResponse<DemandeRecrutementResponse> enAttentePour(CurrentUser, Pageable)` | Demandes dont l'étape courante correspond au rôle de l'utilisateur (boîte de validation) |
| `DemandeRecrutementResponse decider(Long demandeId, ValidationRequest, CurrentUser)` | RG-08 à RG-13 |
| `List<ValidationResponse> historiqueValidations(Long demandeId, CurrentUser)` | Tous les cycles, par cycle puis ordre |

Algorithme de `decider` : charger la validation `EN_ATTENTE` du cycle courant ; vérifier que son rôle correspond au rôle de l'acteur ; vérifier RG-09 (l'acteur n'est pas le demandeur) et RG-10 (commentaire obligatoire pour `REJETEE` et `RETOURNEE`) ; selon la décision :

| Décision | Effet |
|---|---|
| `APPROUVEE` | Validation à `APPROUVEE`. RH → statut `EN_VALIDATION_FINANCE` ; Finance → `EN_VALIDATION_DG` ; DG → `VALIDEE` (RG-13). Notification du rôle suivant, ou de la RH et du demandeur si `VALIDEE`. |
| `REJETEE` | Statut `REJETEE` (définitif). Notification du demandeur. |
| `RETOURNEE` | Statut `RETOURNEE`. Notification du demandeur. Les validations du cycle non traitées restent `EN_ATTENTE` mais sont ignorées. |

Dans tous les cas : renseigner `validateur`, `dateDecision`, enregistrer `HistoriqueStatut`.

## 6. Endpoints

| Méthode | Route | Rôles | Corps | Description |
|---|---|---|---|---|
| POST | `/api/demandes` | MANAGER | `DemandeRecrutementRequest` | Création en brouillon (201) |
| PUT | `/api/demandes/{id}` | MANAGER | `DemandeRecrutementRequest` | Modification (brouillon ou retournée) |
| GET | `/api/demandes` | MANAGER, RH, FINANCE, DG | — | Liste (`q`, `statut`, `posteId`, `departementId`, pagination) |
| GET | `/api/demandes/{id}` | MANAGER, RH, FINANCE, DG | — | Détail |
| POST | `/api/demandes/{id}/soumettre` | MANAGER | — | Soumission à validation |
| POST | `/api/demandes/{id}/annuler` | MANAGER, RH | `AnnulationRequest` | Annulation |
| GET | `/api/demandes/{id}/historique` | MANAGER, RH, FINANCE, DG | — | Historique des statuts |
| GET | `/api/validations/en-attente` | RH, FINANCE, DG | — | Boîte de validation (pagination) |
| POST | `/api/validations/demandes/{demandeId}/decision` | RH, FINANCE, DG | `ValidationRequest` | Décision |
| GET | `/api/validations/demandes/{demandeId}` | MANAGER, RH, FINANCE, DG | — | Validations de tous les cycles |

## 7. Règles de gestion

| Code | Règle | Erreur |
|---|---|---|
| RG-03 | `CREATION` : `employeRemplaceId` interdit. `REMPLACEMENT` : `employeRemplaceId` obligatoire (existence vérifiée par `EmployeLookup`) et `effectifDemande = 1`. | 422 |
| RG-04 | `CREATION` : `effectifDemande ≤ placesLibres(poste) − effectif restant des demandes en cours sur ce poste` (statuts `EN_VALIDATION_*`, `VALIDEE`, `EN_RECRUTEMENT`, hors la demande modifiée). | 422 avec les chiffres dans le message |
| RG-05 | Budget calculé par `BudgetService`, non modifiable par le client. | — |
| RG-06 | À la soumission : `dateSouhaitee ≥ aujourd'hui + delaiMinEmbaucheJours`. | 422 |
| RG-07 | Un manager ne crée de demande que pour **son** département : `departementActuel(utilisateur.employeId)` doit égaler le département du poste. Règle sans effet si l'utilisateur n'est lié à aucun employé (vérification complète après le module `employe`). | 422 |
| RG-08 | Chaîne de validation séquentielle RH → Finance → DG, sans saut d'étape. | 422 |
| RG-09 | Un validateur ne valide jamais une demande qu'il a lui-même créée. | 422 |
| RG-10 | Commentaire obligatoire pour un rejet ou un retour. | 422 |
| RG-11 | Modification interdite sauf en `BROUILLON` et `RETOURNEE`. | 422 |
| RG-12 | Un retour crée un **nouveau cycle** à la re-soumission ; les cycles précédents sont conservés. | — |
| RG-13 | Approbation du DG : statut `VALIDEE`. | — |
| RG-24 | `POURVUE` quand `effectifPourvu = effectifDemande`. | — |
| DEM-01 | Le poste doit être actif. | 422 |
| DEM-02 | Le motif doit être cohérent avec le type (`MotifDemande.estValidePour`). | 422 |
| DEM-03 | Motif `AUTRE` : justification obligatoire. | 422 |
| DEM-04 | Le département est toujours celui du poste. | — |
| DEM-05 | Seul le demandeur modifie ou soumet sa demande. | 403 |
| DEM-06 | Création et modification journalisées dans `JournalAudit`. | — |
| DEM-07 | Visibilité : un manager ne voit que **ses** demandes ; un brouillon n'est visible que de son demandeur ; RH, Finance et DG voient toutes les demandes hors brouillon. Une demande hors périmètre renvoie 404. | 404 |
| DEM-08 | Annulation : motif obligatoire, statuts autorisés par la machine à états. | 422 |

Notifications : soumission → RH (`DEMANDE_A_VALIDER`) ; approbation → rôle suivant (`DEMANDE_A_VALIDER`) ; décision finale, rejet, retour → demandeur (`DEMANDE_DECISION`).

## 8. Migration `V4__demande_recrutement.sql`

Tables `demande_recrutement` et `validation`. Clés étrangères : `fk_demande_poste`, `fk_demande_departement`, `fk_demande_demandeur`, `fk_validation_demande`, `fk_validation_validateur`. Colonne `employe_remplace_id` **sans** clé étrangère. Contraintes CHECK sur `type_demande`, `motif`, `statut`, `role_validateur`, `decision`, `effectif_demande BETWEEN 1 AND 50`, `ordre BETWEEN 1 AND 3`. Unicité `reference` et `(demande_id, cycle, ordre)`.

## 9. Compléments par les modules suivants

| Module | Complément |
|---|---|
| `employe` | `V6` ajoute `fk_demande_employe_remplace` ; implémentation du port `EmployeLookup`. |
| `recrutement` | Appelle `marquerEnRecrutement` et `effectifRestantAPourvoir`. |
| `embauche` | Appelle `enregistrerEmbauche`. |

## 10. Tests (squelettes désactivés)

`DemandeWorkflowServiceTest`, `BudgetServiceTest` (existants) et `ValidationServiceTest` (à créer) : budget calculé, transition interdite refusée, chaîne RH → Finance → DG, rejet sans commentaire refusé, validateur = demandeur refusé, retour puis nouveau cycle, `CREATION` au-delà des places libres refusée, `REMPLACEMENT` sans employé refusé.

## 11. Front

| Élément | Contenu |
|---|---|
| `api/` | `demandes.js`, `validations.js` |
| `constants/statuts.js` | Libellés et couleurs de `StatutDemande`, `DecisionValidation` |
| `views/demande/` | `DemandeListView`, `DemandeFormView`, `DemandeDetailView`, `MesValidationsView` |
| `components/demande/` | `DemandeForm`, `DemandeTable`, `DemandeFilters`, `DemandeStatusStepper` (étapes RH → Finance → DG), `BudgetSummary`, `ValidationTimeline`, `ValidationActionModal` (décision + commentaire obligatoire pour rejet/retour) |
| Routes | `/demandes` et `/demandes/nouvelle` : `[MANAGER]` ; `/demandes/:id` : `[MANAGER, RH, FINANCE, DG]` ; `/validations` : `[RH, FINANCE, DG]` |
| Comportement | Le formulaire affiche le budget estimé renvoyé par l'API après enregistrement. Boutons « Soumettre », « Annuler » visibles selon le statut et le rôle. Le détail montre le stepper, l'historique et les validations par cycle. |
