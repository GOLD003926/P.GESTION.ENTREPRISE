# Module `embauche` : proposition d'embauche et orchestration de l'embauche

Bloc 3 du sujet : le candidat retenu reçoit une proposition d'embauche. Il peut accepter, refuser ou demander du temps. À l'acceptation : création du dossier salarié, du matricule, de la fiche PDF et du contrat d'essai. Dernier module à réaliser : il orchestre `recrutement`, `employe`, `contrat`, `demande`. Migration : `V9__embauche.sql`. Dépend de : tous les modules précédents.

## 1. Fichiers

Existants (vides) :

```
embauche/entity/PropositionEmbauche
embauche/enums/{StatutProposition, ReponseCandidat}
embauche/repository/PropositionEmbaucheRepository
embauche/dto/{PropositionEmbaucheRequest, PropositionEmbaucheResponse, ReponsePropositionRequest}
embauche/mapper/PropositionEmbaucheMapper
embauche/service/{PropositionEmbaucheService, EmbaucheService}
embauche/controller/PropositionEmbaucheController
```

À créer : `dto/PropositionEmbaucheUpdateRequest`, `dto/DerogationRequest`, `dto/ReponsePropositionResultat`, `service/PropositionPdfService`, `scheduler/PropositionExpirationScheduler`.

## 2. Énumérations

| Énumération | Valeurs | Méthodes |
|---|---|---|
| `StatutProposition` | `BROUILLON`, `ENVOYEE`, `DELAI_ACCORDE`, `ACCEPTEE`, `REFUSEE`, `EXPIREE`, `RETIREE` | `transitionsAutorisees()`, `peutPasserA`, `estTerminal()`, `estActive()` (`BROUILLON`, `ENVOYEE`, `DELAI_ACCORDE`) |
| `ReponseCandidat` | `ACCEPTER`, `REFUSER`, `DEMANDER_DELAI` | — |

| De | Vers autorisés |
|---|---|
| `BROUILLON` | `ENVOYEE`, `RETIREE` |
| `ENVOYEE` | `ACCEPTEE`, `REFUSEE`, `DELAI_ACCORDE`, `EXPIREE`, `RETIREE` |
| `DELAI_ACCORDE` | `ACCEPTEE`, `REFUSEE`, `EXPIREE`, `RETIREE` |
| `ACCEPTEE`, `REFUSEE`, `EXPIREE`, `RETIREE` | aucune |

## 3. Entité `PropositionEmbauche` (table `proposition_embauche`, `AuditableEntity`)

| Attribut | Colonne | Type | Null | Règle |
|---|---|---|---|---|
| `candidature` | `candidature_id` | `BIGINT` | Non | `@ManyToOne` LAZY, clé étrangère |
| `poste` | `poste_id` | `BIGINT` | Non | `@ManyToOne` LAZY vers `Poste`, clé étrangère |
| `typeContratPropose` | `type_contrat_propose` | `VARCHAR(10)` | Non | `TypeContrat`, défaut `ESSAI` |
| `salairePropose` | `salaire_propose` | `NUMERIC(14,2)` | Non | > 0 |
| `devise` | `devise` | `VARCHAR(3)` | Non | Défaut `MGA` |
| `horairesTravail` | `horaires_travail` | `VARCHAR(255)` | Non | Mention obligatoire |
| `lieuTravail` | `lieu_travail` | `VARCHAR(255)` | Non | Mention obligatoire |
| `dateDebutPrevue` | `date_debut_prevue` | `DATE` | Non | |
| `dureeMois` | `duree_mois` | `INT` | Oui | Durée de l'essai ou du CDD ; nulle pour un CDI |
| `dateLimiteReponse` | `date_limite_reponse` | `DATE` | Non | Défaut : jour d'envoi + `proposition.delaiReponseJours` |
| `nbProlongationsDelai` | `nb_prolongations_delai` | `INT` | Non | Défaut 0, maximum 1 |
| `statut` | `statut` | `VARCHAR(20)` | Non | `StatutProposition`, défaut `BROUILLON` |
| `dateEnvoi` | `date_envoi` | `TIMESTAMPTZ` | Oui | |
| `dateReponse` | `date_reponse` | `TIMESTAMPTZ` | Oui | |
| `motifRefus` | `motif_refus` | `VARCHAR(500)` | Oui | |
| `derogationRequise` | `derogation_requise` | `BOOLEAN` | Non | Défaut `false` ; vrai si le salaire sort de la fourchette de la grille |
| `derogationValideePar` | `derogation_validee_par_id` | `BIGINT` | Oui | `@ManyToOne` LAZY vers `Utilisateur` (DG) |
| `dateDerogation` | `date_derogation` | `TIMESTAMPTZ` | Oui | |
| `document` | `document_id` | `BIGINT` | Oui | `@ManyToOne` LAZY vers `Document` (PDF de la proposition) |
| `employe` | `employe_id` | `BIGINT` | Oui | `@ManyToOne` LAZY vers `Employe`, renseigné à l'acceptation |

Index : `idx_proposition_candidature`, `idx_proposition_statut`, `idx_proposition_limite`. Index unique **partiel** `(candidature_id) WHERE statut IN ('BROUILLON','ENVOYEE','DELAI_ACCORDE')` : une seule proposition active par candidature.

## 4. DTO

| DTO | Champs et validations |
|---|---|
| `PropositionEmbaucheRequest` | `candidatureId` (obligatoire), `typeContratPropose` (défaut `ESSAI`), `salairePropose` (> 0), `devise` (3 lettres), `horairesTravail` (obligatoire, ≤ 255), `lieuTravail` (obligatoire, ≤ 255), `dateDebutPrevue` (obligatoire, aujourd'hui ou futur), `dureeMois` (1 à 60 ; obligatoire sauf `CDI`, interdit pour `CDI`), `dateLimiteReponse` (facultative, futur) |
| `PropositionEmbaucheUpdateRequest` | idem sans `candidatureId` |
| `DerogationRequest` | `commentaire` (obligatoire, ≤ 500) |
| `ReponsePropositionRequest` | `reponse` (obligatoire), `motifRefus` (≤ 500, obligatoire si `REFUSER`), `commentaire` (≤ 500) |
| `PropositionEmbaucheResponse` | `id`, `candidatureId`, `candidatNom`, `posteId`, `posteIntitule`, `typeContratPropose`, `salairePropose`, `devise`, `salaireMinGrille`, `salaireMaxGrille`, `horairesTravail`, `lieuTravail`, `dateDebutPrevue`, `dureeMois`, `dateLimiteReponse`, `nbProlongationsDelai`, `statut`, `dateEnvoi`, `dateReponse`, `motifRefus`, `derogationRequise`, `derogationValidee` (booléen), `documentId`, `employeId`, `createdAt` |
| `ReponsePropositionResultat` | `proposition` (`PropositionEmbaucheResponse`), `employeId`, `matricule`, `contratId` (renseignés seulement en cas d'acceptation) |

## 5. Services

### 5.1 `PropositionPdfService`

`byte[] generer(PropositionEmbauche proposition)` : PDF (OpenPDF, dépendance ajoutée par le module `employe`) contenant identité du candidat, poste, type de contrat, salaire et devise, horaires, lieu, date de début, durée, date limite de réponse. Aucun accès à la base.

### 5.2 `PropositionEmbaucheService`

| Méthode | Règle |
|---|---|
| `creer(PropositionEmbaucheRequest, CurrentUser)` | RG-26, RG-27, EMB-01 à EMB-04 ; statut `BROUILLON` ; journal d'audit |
| `modifier(Long id, PropositionEmbaucheUpdateRequest, CurrentUser)` | Uniquement `BROUILLON` ; recalcul de `derogationRequise` |
| `obtenir(Long id, CurrentUser)`, `rechercher(Long candidatureId, StatutProposition statut, Pageable, CurrentUser)` | Tri : `id`, `statut`, `dateEnvoi`, `dateLimiteReponse`, `createdAt` |
| `validerDerogation(Long id, DerogationRequest, CurrentUser)` | DG ; EMB-03 ; renseigne `derogationValideePar` et `dateDerogation` |
| `envoyer(Long id, CurrentUser)` | EMB-05 ; génère le PDF via `PropositionPdfService`, l'enregistre (`PROPOSITION_EMBAUCHE`, `PROPOSITION_EMBAUCHE`, id) ; `dateEnvoi` ; `dateLimiteReponse` par défaut ; `BROUILLON` → `ENVOYEE` ; `CandidatureService.marquerPropositionEnvoyee` ; historique ; notification RH |
| `retirer(Long id, CurrentUser)` | → `RETIREE` ; `CandidatureService.remettreRetenue` ; historique |
| `historique(Long id, CurrentUser)` | |

### 5.3 `EmbaucheService` (orchestration)

| Méthode | Règle |
|---|---|
| `ReponsePropositionResultat repondre(Long propositionId, ReponsePropositionRequest, CurrentUser)` | Aiguille vers `accepter`, `refuser` ou `demanderDelai` selon `reponse` ; la proposition doit être `ENVOYEE` ou `DELAI_ACCORDE` |
| `ReponsePropositionResultat accepter(Long propositionId, String auteur)` | Voir l'algorithme ci-dessous |
| `PropositionEmbaucheResponse refuser(Long propositionId, String motif, String auteur)` | RG-30 : → `REFUSEE`, `dateReponse`, `motifRefus` ; `CandidatureService.marquerNonRetenueApresProposition` ; notification RH (`PROPOSITION_REPONDUE`) invitant à proposer au candidat suivant de la liste d'attente |
| `PropositionEmbaucheResponse demanderDelai(Long propositionId, String auteur)` | RG-28 : une seule prolongation ; `dateLimiteReponse += proposition.dureeProlongationJours` ; → `DELAI_ACCORDE` ; `nbProlongationsDelai = 1` |
| `int expirerPropositionsEchues(LocalDate aujourdhui)` | RG-29 : propositions `ENVOYEE` ou `DELAI_ACCORDE` dont `dateLimiteReponse < aujourdhui` → `EXPIREE` ; candidature → `NON_RETENUE` ; notification RH (`PROPOSITION_EXPIREE`) ; retourne le nombre traité |

**Algorithme de `accepter`** (une seule transaction ; toute erreur annule l'ensemble) :

1. Proposition → `ACCEPTEE`, `dateReponse`.
2. `CandidatureService.marquerEmbauchee(candidatureId)`.
3. `EmployeService.creerDepuisCandidature(candidatureId, dateDebutPrevue, auteur)` : employé `PRE_EMBAUCHE`, matricule unique (RG-31).
4. `FicheSalariePdfService.generer(employe, Optional.empty())` puis `DocumentService.enregistrer(..., FICHE_SALARIE, EMPLOYE, employeId)`.
5. `ContratService.creerContratInitial(ContratInitialCommande)` avec `type = typeContratPropose` (défaut `ESSAI`, RG-33), poste, manager (nul si inconnu), dates, durée, salaire, devise, horaires, lieu : le contrat est `BROUILLON` et déclenche l'onboarding par événement.
6. `proposition.employe = employe`.
7. `DemandeWorkflowService.enregistrerEmbauche(demandeId, auteur)` (incrémente l'effectif pourvu ; `POURVUE` si atteint) puis `OffreEmploiService.cloturerSiEffectifAtteint(demandeId, auteur)` (RG-24).
8. Historique, journal d'audit, notification RH (`PROPOSITION_REPONDUE`) avec matricule dans le message.

### 5.4 Planificateur

`PropositionExpirationScheduler` : quotidien, 00:10, délègue à `EmbaucheService.expirerPropositionsEchues`.

## 6. Endpoints

| Méthode | Route | Rôles | Corps | Description |
|---|---|---|---|---|
| GET | `/api/propositions` | RH, MANAGER, DG | — | Liste (`candidatureId`, `statut`) |
| GET | `/api/propositions/{id}` | RH, MANAGER, DG | — | Détail |
| POST | `/api/propositions` | RH | `PropositionEmbaucheRequest` | Création (201) |
| PUT | `/api/propositions/{id}` | RH | `PropositionEmbaucheUpdateRequest` | Modification (brouillon) |
| POST | `/api/propositions/{id}/derogation` | DG | `DerogationRequest` | Valider une dérogation de salaire |
| POST | `/api/propositions/{id}/envoyer` | RH | — | Envoi au candidat |
| POST | `/api/propositions/{id}/reponse` | RH | `ReponsePropositionRequest` | Enregistrer la réponse du candidat, retourne `ReponsePropositionResultat` |
| POST | `/api/propositions/{id}/retirer` | RH | — | Retrait par la RH |
| GET | `/api/propositions/{id}/historique` | RH, MANAGER, DG | — | Historique des statuts |

Le PDF de la proposition se télécharge par `GET /api/documents/{documentId}/contenu`.

## 7. Règles de gestion

| Code | Règle | Erreur |
|---|---|---|
| RG-26 | Une proposition n'est créée que pour une candidature `RETENUE`. | 422 |
| RG-27 | Mentions obligatoires : poste, type de contrat, salaire, horaires, lieu, date de début, durée (sauf CDI), date limite de réponse. Salaire dans la fourchette de la grille du poste ; au-delà, dérogation du DG nécessaire (EMB-03). | 422 |
| RG-28 | Le candidat a trois choix : accepter, refuser, demander un délai. Le délai n'est accordé qu'**une seule fois**. | 422 |
| RG-29 | Sans réponse à la date limite : `EXPIREE` automatiquement. | — |
| RG-30 | En cas de refus ou d'expiration, la RH peut proposer au candidat suivant de la liste d'attente tant que l'effectif n'est pas atteint. | — |
| RG-31 | À l'acceptation : employé `PRE_EMBAUCHE`, matricule, fiche PDF. | — |
| RG-32 | Candidat et candidature intacts, lien conservé. | — |
| RG-33 | Contrat d'essai par défaut. | — |
| EMB-01 | Une seule proposition active par candidature. | 409 |
| EMB-02 | Le poste de la proposition est celui de la demande à l'origine de l'offre de la candidature. | 422 |
| EMB-03 | Salaire hors `[salaireMin, salaireMax]` de la grille : `derogationRequise = true` ; l'envoi est interdit tant que le DG n'a pas validé. | 422 |
| EMB-04 | `dureeMois` obligatoire pour `ESSAI` et `CDD` (respect des plafonds de `ContratRuleValidator`), interdit pour `CDI`. | 422 |
| EMB-05 | L'envoi exige une proposition `BROUILLON` complète (et dérogation validée si requise). | 422 |
| EMB-06 | Seule la RH enregistre la réponse du candidat (pas de portail candidat dans le MVP). | 403 |
| EMB-07 | L'acceptation est atomique : si une étape échoue, rien n'est créé. | — |
| EMB-08 | Refus : motif obligatoire. | 422 |

Notifications : proposition envoyée → RH (`PROPOSITION_ENVOYEE`) ; réponse enregistrée → RH (`PROPOSITION_REPONDUE`) ; dérogation requise → utilisateurs DG (`AUTRE`) ; expiration → RH (`PROPOSITION_EXPIREE`).

## 8. Migration `V9__embauche.sql`

Table `proposition_embauche`, clés étrangères nommées (`fk_proposition_candidature`, `fk_proposition_poste`, `fk_proposition_derogation`, `fk_proposition_document`, `fk_proposition_employe`), index unique partiel cité plus haut, CHECK sur `statut`, `type_contrat_propose`, `salaire_propose > 0`, `nb_prolongations_delai BETWEEN 0 AND 1`.

## 9. Tests (squelettes désactivés)

`EmbaucheServiceTest` (existant) : acceptation crée employé, fiche, contrat d'essai et incrémente l'effectif ; l'effectif atteint passe la demande à `POURVUE` et clôture l'offre ; second délai refusé ; expiration des propositions échues ; refus sans motif refusé ; acceptation atomique (une étape en erreur n'écrit rien). `PropositionEmbaucheServiceTest` : salaire hors grille bloque l'envoi, dérogation débloque, seconde proposition active refusée.

## 10. Front

| Élément | Contenu |
|---|---|
| `api/` | `propositions.js` |
| `views/embauche/` | `PropositionListView`, `PropositionFormView`, `PropositionDetailView` |
| `components/embauche/` | `PropositionForm`, `PropositionTable`, `PropositionReponsePanel` (trois boutons : accepter, refuser avec motif, demander un délai), `DelaiReponseCountdown` (jours restants) |
| Routes | `/propositions` : `[RH, MANAGER, DG]` ; création et actions : `[RH]` ; dérogation : `[DG]` |
| Comportement | Depuis la fiche d'une candidature `RETENUE`, bouton « Préparer la proposition ». Après acceptation, affichage du matricule et liens vers le dossier employé, le contrat et l'onboarding. |
