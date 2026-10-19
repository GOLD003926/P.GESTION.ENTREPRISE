# Module `recrutement` : offres, candidats, candidatures, entretiens, évaluations

Bloc 2 du sujet : transformer une demande validée en offre, recevoir des candidatures, sélectionner en trois niveaux, conserver évaluations et décision finale avec traçabilité complète. Migration : `V5__recrutement.sql`. Dépend de : `common`, `auth`, `organisation`, `demande`.

## 1. Fichiers

Existants (vides) :

```
recrutement/entity/{OffreEmploi, Candidat, Candidature, Entretien, EntretienIntervenant, EvaluationCandidat}
recrutement/enums/{StatutOffre, StatutCandidature, DecisionFinale, TypeEntretien, StatutEntretien, Avis}
recrutement/repository/{OffreEmploi, Candidat, Candidature, Entretien, EntretienIntervenant, EvaluationCandidat}Repository
recrutement/dto/{OffreEmploi, Candidat, Candidature, Entretien, EvaluationCandidat}{Request, Response}
recrutement/dto/{DecisionFinaleRequest, PreselectionRequest}
recrutement/mapper/{OffreEmploi, Candidat, Candidature, Entretien, EvaluationCandidat}Mapper
recrutement/service/{OffreEmploi, Candidat, Candidature, Entretien, EvaluationCandidat}Service, SelectionWorkflowService
recrutement/controller/{OffreEmploi, Candidat, Candidature, Entretien, EvaluationCandidat}Controller
```

À créer : `dto/OffreEmploiUpdateRequest`, `dto/EntretienUpdateRequest`, `dto/EvaluationCandidatUpdateRequest`, `dto/IntervenantRequest`, `dto/IntervenantResponse`, `dto/CritereNote`, `dto/AnnulationEntretienRequest`.

## 2. Énumérations

| Énumération | Valeurs | Méthodes |
|---|---|---|
| `StatutOffre` | `BROUILLON`, `PUBLIEE`, `CLOTUREE`, `ANNULEE` | Transitions : `BROUILLON` → `PUBLIEE`, `ANNULEE` ; `PUBLIEE` → `CLOTUREE`, `ANNULEE` ; `CLOTUREE`, `ANNULEE` terminaux |
| `StatutCandidature` | `RECUE`, `PRESELECTIONNEE`, `REFUSEE_DOSSIER`, `EN_ENTRETIEN`, `NON_RETENUE`, `LISTE_ATTENTE`, `RETENUE`, `PROPOSITION_ENVOYEE`, `EMBAUCHEE`, `DESISTEE` | Voir transitions ci-dessous |
| `DecisionFinale` | `RETENU`, `NON_RETENU`, `LISTE_ATTENTE` | — |
| `TypeEntretien` | `ENTRETIEN_RH` (niveau 1), `TEST_TECHNIQUE` (niveau 2), `ENTRETIEN_FINAL` (niveau 3) | `static TypeEntretien pourNiveau(int niveau)` |
| `StatutEntretien` | `PLANIFIE`, `REPORTE`, `REALISE`, `ANNULE`, `CANDIDAT_ABSENT` | `PLANIFIE` ↔ `REPORTE` ; `PLANIFIE` et `REPORTE` → `REALISE`, `ANNULE`, `CANDIDAT_ABSENT` ; trois derniers terminaux |
| `Avis` | `FAVORABLE`, `RESERVE`, `DEFAVORABLE` | — |

### Transitions de `StatutCandidature`

| De | Vers autorisés |
|---|---|
| `RECUE` | `PRESELECTIONNEE`, `REFUSEE_DOSSIER`, `DESISTEE` |
| `PRESELECTIONNEE` | `EN_ENTRETIEN`, `DESISTEE` |
| `EN_ENTRETIEN` | `EN_ENTRETIEN` (niveau suivant), `NON_RETENUE`, `RETENUE`, `LISTE_ATTENTE`, `DESISTEE` |
| `LISTE_ATTENTE` | `RETENUE`, `NON_RETENUE`, `DESISTEE` |
| `RETENUE` | `PROPOSITION_ENVOYEE`, `NON_RETENUE` |
| `PROPOSITION_ENVOYEE` | `EMBAUCHEE`, `NON_RETENUE`, `RETENUE` (proposition retirée, nouvelle proposition possible) |
| `REFUSEE_DOSSIER`, `NON_RETENUE`, `EMBAUCHEE`, `DESISTEE` | aucune |

## 3. Entités (`AuditableEntity`)

### 3.1 `OffreEmploi` (table `offre_emploi`)

| Attribut | Colonne | Type | Null | Règle |
|---|---|---|---|---|
| `reference` | `reference` | `VARCHAR(30)` | Non | Unique, `OF-AAAA-NNNN` via `NumeroService` |
| `demande` | `demande_id` | `BIGINT` | Non | `@ManyToOne` LAZY vers `DemandeRecrutement`, clé étrangère |
| `titre` | `titre` | `VARCHAR(150)` | Non | |
| `description` | `description` | `TEXT` | Non | |
| `profilRecherche` | `profil_recherche` | `TEXT` | Oui | |
| `nombrePostes` | `nombre_postes` | `INT` | Non | ≥ 1 |
| `nombreNiveauxSelection` | `nombre_niveaux_selection` | `INT` | Non | 1 à 3, défaut 3 |
| `canalPublication` | `canal_publication` | `VARCHAR(100)` | Oui | |
| `datePublication` | `date_publication` | `DATE` | Oui | |
| `dateCloture` | `date_cloture` | `DATE` | Oui | Date limite de dépôt |
| `statut` | `statut` | `VARCHAR(20)` | Non | `StatutOffre`, défaut `BROUILLON` |

### 3.2 `Candidat` (table `candidat`)

| Attribut | Colonne | Type | Null | Règle |
|---|---|---|---|---|
| `nom` | `nom` | `VARCHAR(100)` | Non | |
| `prenom` | `prenom` | `VARCHAR(100)` | Non | |
| `email` | `email` | `VARCHAR(150)` | Non | Unique insensible à la casse, enregistré en minuscules |
| `telephone` | `telephone` | `VARCHAR(30)` | Oui | |
| `dateNaissance` | `date_naissance` | `DATE` | Oui | Dans le passé |
| `adresse` | `adresse` | `VARCHAR(255)` | Oui | |
| `nationalite` | `nationalite` | `VARCHAR(60)` | Oui | |
| `consentementDonnees` | `consentement_donnees` | `BOOLEAN` | Non | Défaut `false` |

### 3.3 `Candidature` (table `candidature`)

| Attribut | Colonne | Type | Null | Règle |
|---|---|---|---|---|
| `candidat` | `candidat_id` | `BIGINT` | Non | `@ManyToOne` LAZY, clé étrangère |
| `offre` | `offre_id` | `BIGINT` | Non | `@ManyToOne` LAZY, clé étrangère |
| `cvDocument` | `cv_document_id` | `BIGINT` | Oui | `@ManyToOne` LAZY vers `Document`, clé étrangère |
| `lettreMotivation` | `lettre_motivation` | `TEXT` | Oui | |
| `dateDepot` | `date_depot` | `TIMESTAMPTZ` | Non | Instant de l'enregistrement |
| `statut` | `statut` | `VARCHAR(30)` | Non | `StatutCandidature`, défaut `RECUE` |
| `scorePreselection` | `score_preselection` | `INT` | Oui | 0 à 100 |
| `motifRefus` | `motif_refus` | `VARCHAR(500)` | Oui | |
| `decisionFinale` | `decision_finale` | `VARCHAR(20)` | Oui | `DecisionFinale` |
| `rangListeAttente` | `rang_liste_attente` | `INT` | Oui | ≥ 1 |
| `decideur` | `decideur_id` | `BIGINT` | Oui | `@ManyToOne` LAZY vers `Utilisateur` |
| `dateDecision` | `date_decision` | `TIMESTAMPTZ` | Oui | |
| `motifDecision` | `motif_decision` | `TEXT` | Oui | |

Unicité `(candidat_id, offre_id)`. Index `idx_candidature_offre`, `idx_candidature_statut`.

### 3.4 `Entretien` (table `entretien`)

| Attribut | Colonne | Type | Null | Règle |
|---|---|---|---|---|
| `candidature` | `candidature_id` | `BIGINT` | Non | `@ManyToOne` LAZY, clé étrangère |
| `niveau` | `niveau` | `INT` | Non | 1 à 3 |
| `type` | `type` | `VARCHAR(30)` | Non | `TypeEntretien`, dérivé du niveau |
| `dateHeure` | `date_heure` | `TIMESTAMPTZ` | Non | |
| `dureeMinutes` | `duree_minutes` | `INT` | Non | 15 à 480, défaut 60 |
| `lieuOuLien` | `lieu_ou_lien` | `VARCHAR(255)` | Oui | |
| `statut` | `statut` | `VARCHAR(20)` | Non | `StatutEntretien`, défaut `PLANIFIE` |
| `motifAnnulation` | `motif_annulation` | `VARCHAR(500)` | Oui | |

Index unique **partiel** `(candidature_id, niveau) WHERE statut <> 'ANNULE'`.

### 3.5 `EntretienIntervenant` (table `entretien_intervenant`)

| Attribut | Colonne | Type | Null |
|---|---|---|---|
| `entretien` | `entretien_id` | `BIGINT` | Non (`@ManyToOne` LAZY) |
| `utilisateur` | `utilisateur_id` | `BIGINT` | Non (`@ManyToOne` LAZY vers `Utilisateur`) |
| `roleIntervenant` | `role_intervenant` | `VARCHAR(50)` | Oui |

Unicité `(entretien_id, utilisateur_id)`.

### 3.6 `EvaluationCandidat` (table `evaluation_candidat`)

| Attribut | Colonne | Type | Null | Règle |
|---|---|---|---|---|
| `entretien` | `entretien_id` | `BIGINT` | Non | `@ManyToOne` LAZY |
| `evaluateur` | `evaluateur_id` | `BIGINT` | Non | `@ManyToOne` LAZY vers `Utilisateur` |
| `noteGlobale` | `note_globale` | `NUMERIC(4,2)` | Non | 0 à 20 (CHECK) |
| `grilleCriteres` | `grille_criteres` | `TEXT` | Oui | JSON : liste `{critere, note}` |
| `avis` | `avis` | `VARCHAR(20)` | Non | `Avis` |
| `commentaire` | `commentaire` | `TEXT` | Oui | |
| `dateEvaluation` | `date_evaluation` | `TIMESTAMPTZ` | Non | |
| `verrouillee` | `verrouillee` | `BOOLEAN` | Non | Défaut `false` |
| `active` | `active` | `BOOLEAN` | Non | Défaut `true` ; `false` quand remplacée par une correction |
| `evaluationPrecedente` | `evaluation_precedente_id` | `BIGINT` | Oui | `@ManyToOne` LAZY vers `EvaluationCandidat` |

Index unique **partiel** `(entretien_id, evaluateur_id) WHERE active`.

## 4. DTO

| DTO | Champs et validations |
|---|---|
| `OffreEmploiRequest` | `demandeId` (obligatoire), `titre` (obligatoire, ≤ 150), `description` (obligatoire), `profilRecherche`, `nombrePostes` (≥ 1), `nombreNiveauxSelection` (1 à 3, défaut 3), `canalPublication` (≤ 100), `dateCloture` (futur) |
| `OffreEmploiUpdateRequest` | idem sans `demandeId` |
| `OffreEmploiResponse` | `id`, `reference`, `demandeId`, `demandeReference`, `posteIntitule`, `titre`, `description`, `profilRecherche`, `nombrePostes`, `nombreNiveauxSelection`, `canalPublication`, `datePublication`, `dateCloture`, `statut`, `nombreCandidatures`, `createdAt` |
| `CandidatRequest` | `nom`, `prenom` (obligatoires, ≤ 100), `email` (obligatoire, valide), `telephone` (≤ 30), `dateNaissance` (passé), `adresse`, `nationalite`, `consentementDonnees` (booléen) |
| `CandidatResponse` | tous les champs + `id`, `createdAt` |
| `CandidatureRequest` | `offreId` (obligatoire), `candidatId` (facultatif), `candidat` (`CandidatRequest`, facultatif, `@Valid`), `lettreMotivation` ; **exactement un** de `candidatId` ou `candidat` |
| `CandidatureResponse` | `id`, `candidatId`, `candidatNom`, `candidatEmail`, `offreId`, `offreReference`, `offreTitre`, `cvDocumentId`, `lettreMotivation`, `dateDepot`, `statut`, `scorePreselection`, `motifRefus`, `decisionFinale`, `rangListeAttente`, `decideurId`, `dateDecision`, `motifDecision`, `prochainNiveau` (entier ou nul) |
| `PreselectionRequest` | `accepter` (obligatoire), `score` (0 à 100), `motif` (≤ 500, obligatoire si `accepter = false`) |
| `DecisionFinaleRequest` | `decision` (obligatoire), `motif` (obligatoire si `NON_RETENU`) |
| `IntervenantRequest` | `utilisateurId` (obligatoire), `roleIntervenant` (≤ 50) |
| `IntervenantResponse` | `utilisateurId`, `utilisateurNom`, `roleIntervenant` |
| `EntretienRequest` | `candidatureId`, `niveau` (1 à 3), `dateHeure` (futur), `dureeMinutes` (15 à 480), `lieuOuLien` (≤ 255), `intervenants` (au moins 1, `@Valid`) |
| `EntretienUpdateRequest` | `dateHeure` (futur), `dureeMinutes`, `lieuOuLien`, `intervenants` |
| `AnnulationEntretienRequest` | `motif` (obligatoire, ≤ 500) |
| `EntretienResponse` | `id`, `candidatureId`, `candidatNom`, `niveau`, `type`, `dateHeure`, `dureeMinutes`, `lieuOuLien`, `statut`, `motifAnnulation`, `intervenants`, `avisNiveau` (`Avis` ou nul) |
| `CritereNote` | `critere` (obligatoire, ≤ 100), `note` (0 à 20) |
| `EvaluationCandidatRequest` | `entretienId` (obligatoire), `noteGlobale` (0 à 20), `criteres` (liste de `CritereNote`), `avis` (obligatoire), `commentaire` (≤ 4000) |
| `EvaluationCandidatUpdateRequest` | idem sans `entretienId` |
| `EvaluationCandidatResponse` | `id`, `entretienId`, `evaluateurId`, `evaluateurNom`, `noteGlobale`, `criteres`, `avis`, `commentaire`, `dateEvaluation`, `verrouillee`, `active`, `evaluationPrecedenteId` |

Le mapper d'évaluation convertit la liste `criteres` en JSON et inversement avec le bean JSON de Spring Boot (Jackson 3).

## 5. Services

### 5.1 `OffreEmploiService`

| Méthode | Règle |
|---|---|
| `creer(OffreEmploiRequest, CurrentUser)` | RG-14, REC-01, référence `OF-...`, statut `BROUILLON` |
| `modifier(Long id, OffreEmploiUpdateRequest, CurrentUser)` | Uniquement `BROUILLON` (REC-02) |
| `obtenir(Long id)`, `rechercher(String q, StatutOffre statut, Long demandeId, Pageable)` | Tri autorisé : `id`, `reference`, `titre`, `statut`, `datePublication`, `dateCloture` |
| `publier(Long id, CurrentUser)` | RG-15 : `BROUILLON` → `PUBLIEE` ; `datePublication = aujourd'hui` ; `dateCloture` obligatoire et postérieure à aujourd'hui ; appelle `DemandeWorkflowService.marquerEnRecrutement` |
| `cloturer(Long id, CurrentUser)`, `annuler(Long id, CurrentUser)` | Transitions de la machine à états |
| `void cloturerSiEffectifAtteint(Long demandeId, String auteur)` | **API interne** (appelée par `embauche`, RG-24) : si `effectifRestantAPourvoir(demandeId) = 0`, clôture les offres `PUBLIEE` de la demande |
| `int cloturerOffresEchues(LocalDate aujourdhui)` | Clôture les offres `PUBLIEE` dont `dateCloture` est dépassée (appelée par un planificateur quotidien, facultatif dans le MVP) |

### 5.2 `CandidatService`

| Méthode | Règle |
|---|---|
| `creer(CandidatRequest)` | RG-16 : email unique (409 sinon) |
| `modifier(Long id, CandidatRequest)`, `obtenir(Long id)` | |
| `rechercher(String q, Pageable)` | `q` sur nom, prénom, email. Tri : `id`, `nom`, `prenom`, `email`, `createdAt` |
| `Candidat trouverOuCreer(CandidatRequest)` | **API interne** : retourne le candidat existant (même email) sans le modifier, sinon le crée |

### 5.3 `CandidatureService`

| Méthode | Règle |
|---|---|
| `creer(CandidatureRequest, CurrentUser)` | RG-16 (une candidature par candidat et par offre), RG-17 (offre `PUBLIEE` et date de clôture non dépassée), exactement un de `candidatId`/`candidat`, statut `RECUE` |
| `televerserCv(Long id, MultipartFile, CurrentUser)` | REC-03 : `DocumentService.enregistrer(..., CV, CANDIDATURE, id)` puis rattachement de `cvDocument` |
| `obtenir(Long id, CurrentUser)`, `rechercher(Long offreId, StatutCandidature statut, Long candidatId, Pageable, CurrentUser)` | REC-09 (visibilité). Tri : `id`, `dateDepot`, `statut`, `scorePreselection` |
| `desister(Long id, CurrentUser)` | Transition vers `DESISTEE` |
| `promouvoirListeAttente(Long id, CurrentUser)` | REC-08 : `LISTE_ATTENTE` → `RETENUE` si l'effectif restant de la demande est > 0 |
| `historique(Long id, CurrentUser)` | |
| API interne : `marquerPropositionEnvoyee(Long id, String auteur)`, `marquerEmbauchee(Long id, String auteur)`, `marquerNonRetenueApresProposition(Long id, String auteur, String motif)`, `remettreRetenue(Long id, String auteur)` | Transitions appelées par `embauche` |

### 5.4 `SelectionWorkflowService`

| Méthode | Règle |
|---|---|
| `CandidatureResponse preselectionner(Long candidatureId, PreselectionRequest, CurrentUser)` | RG-18 : `RECUE` → `PRESELECTIONNEE` (accepté) ou `REFUSEE_DOSSIER` (motif obligatoire) ; notification facultative |
| `EntretienResponse planifierEntretien(EntretienRequest, CurrentUser)` | RG-19, REC-04, REC-05 |
| `EntretienResponse reprogrammer(Long entretienId, EntretienUpdateRequest, CurrentUser)` | Statut `REPORTE` puis `PLANIFIE` |
| `EntretienResponse cloturerEntretien(Long entretienId, CurrentUser)` | RG-20 : au moins une évaluation active et verrouillée ; `REALISE` ; calcule l'avis du niveau (REC-06) ; `DEFAVORABLE` → candidature `NON_RETENUE` (notification) ; `FAVORABLE` ouvre le niveau suivant ; `RESERVE` laisse la décision à la RH |
| `EntretienResponse annulerEntretien(Long id, AnnulationEntretienRequest, CurrentUser)`, `marquerAbsent(Long id, CurrentUser)` | |
| `CandidatureResponse decisionFinale(Long candidatureId, DecisionFinaleRequest, CurrentUser)` | RG-22, REC-07 |
| `Avis avisDuNiveau(Long candidatureId, int niveau)` | Calcul de l'avis agrégé |

### 5.5 `EntretienService` et `EvaluationCandidatService`

| Méthode | Règle |
|---|---|
| `EntretienService.obtenir(Long id, CurrentUser)`, `rechercher(Long candidatureId, StatutEntretien statut, LocalDate du, LocalDate au, Pageable, CurrentUser)` | Lecture. Tri : `id`, `dateHeure`, `niveau`, `statut` |
| `EvaluationCandidatService.creer(EvaluationCandidatRequest, CurrentUser)` | RG-20 : l'acteur est intervenant de l'entretien (REC-10), entretien `PLANIFIE` ou `REALISE`, pas d'évaluation active existante du même évaluateur |
| `modifier(Long id, EvaluationCandidatUpdateRequest, CurrentUser)` | Auteur uniquement, évaluation non verrouillée (RG-21) |
| `verrouiller(Long id, CurrentUser)` | Auteur ou RH ; irréversible |
| `corriger(Long id, EvaluationCandidatUpdateRequest, CurrentUser)` | RG-21 : RH ; crée une **nouvelle** évaluation liée à la précédente (`evaluationPrecedente`), passe l'ancienne à `active = false` ; historique |
| `lister(Long entretienId, CurrentUser)` | Évaluations actives par défaut |

## 6. Endpoints

| Méthode | Route | Rôles | Description |
|---|---|---|---|
| GET | `/api/offres` | RH, MANAGER, DG | Liste (`q`, `statut`, `demandeId`) |
| GET | `/api/offres/{id}` | RH, MANAGER, DG | Détail |
| POST | `/api/offres` | RH | Création (201) |
| PUT | `/api/offres/{id}` | RH | Modification (brouillon) |
| POST | `/api/offres/{id}/publier` | RH | Publication |
| POST | `/api/offres/{id}/cloturer` | RH | Clôture |
| POST | `/api/offres/{id}/annuler` | RH | Annulation |
| GET | `/api/candidats` | RH | Liste (`q`) |
| GET | `/api/candidats/{id}` | RH | Détail |
| POST | `/api/candidats` | RH | Création (201) |
| PUT | `/api/candidats/{id}` | RH | Modification |
| GET | `/api/candidatures` | RH, MANAGER | Liste (`offreId`, `statut`, `candidatId`) |
| GET | `/api/candidatures/{id}` | RH, MANAGER | Détail |
| POST | `/api/candidatures` | RH | Enregistrement (201) |
| POST | `/api/candidatures/{id}/cv` | RH | Dépôt du CV (`multipart`, champ `fichier`) |
| POST | `/api/candidatures/{id}/preselection` | RH | Présélection sur dossier |
| POST | `/api/candidatures/{id}/decision-finale` | RH, MANAGER | Décision finale |
| POST | `/api/candidatures/{id}/promouvoir-liste-attente` | RH | Liste d'attente → retenue |
| POST | `/api/candidatures/{id}/desister` | RH | Désistement |
| GET | `/api/candidatures/{id}/historique` | RH, MANAGER | Historique des statuts |
| GET | `/api/entretiens` | RH, MANAGER | Liste (`candidatureId`, `statut`, `du`, `au`) |
| GET | `/api/entretiens/{id}` | RH, MANAGER | Détail |
| POST | `/api/entretiens` | RH | Planification (201) |
| PUT | `/api/entretiens/{id}` | RH | Reprogrammation |
| POST | `/api/entretiens/{id}/cloturer` | RH | Clôture |
| POST | `/api/entretiens/{id}/annuler` | RH | Annulation |
| POST | `/api/entretiens/{id}/absent` | RH | Candidat absent |
| GET | `/api/evaluations` | RH, MANAGER | Liste (`entretienId` obligatoire) |
| POST | `/api/evaluations` | RH, MANAGER | Création par un intervenant (201) |
| PUT | `/api/evaluations/{id}` | RH, MANAGER | Modification par l'auteur |
| POST | `/api/evaluations/{id}/verrouiller` | RH, MANAGER | Verrouillage |
| POST | `/api/evaluations/{id}/corriger` | RH | Correction (nouvelle version) |

## 7. Règles de gestion

| Code | Règle | Erreur |
|---|---|---|
| RG-14 | Offre créée uniquement à partir d'une demande `VALIDEE` ou `EN_RECRUTEMENT` ; `nombrePostes ≤ effectif restant de la demande − postes des autres offres actives (BROUILLON, PUBLIEE)`. | 422 |
| RG-15 | Publication, clôture, annulation réservées à la RH. | 403 |
| RG-16 | Candidat unique par email ; une seule candidature par candidat et par offre. | 409 |
| RG-17 | Candidature acceptée seulement si l'offre est `PUBLIEE` et avant sa date de clôture. | 422 |
| RG-18 | Refus au dossier : motif obligatoire. | 422 |
| RG-19 | Trois niveaux **séquentiels** : le niveau n+1 n'est planifiable que si le niveau n est `REALISE` avec avis agrégé `FAVORABLE`. Le niveau 1 exige la candidature `PRESELECTIONNEE`. Le niveau maximal est `offre.nombreNiveauxSelection`. | 422 |
| RG-20 | Un entretien a au moins un intervenant ; il ne peut être clôturé sans au moins une évaluation active verrouillée. | 422 |
| RG-21 | Une évaluation verrouillée n'est plus modifiée ; une correction crée une nouvelle version tracée. | 422 |
| RG-22 | Décision finale seulement après **tous** les niveaux requis réalisés et favorables ; datée, signée (décideur) et motivée. | 422 |
| RG-23 | Aucune suppression physique des données de recrutement (CV, évaluations, décisions). | — |
| RG-24 | Quand l'effectif est atteint, l'offre est clôturée et la demande passe `POURVUE` (déclenché par `embauche`). Les candidats bien classés restent en `LISTE_ATTENTE`. | — |
| RG-25 | Conservation limitée des candidats non retenus (`sirh.candidat.conservation-mois`) : traitement hors MVP. | — |
| REC-01 | La demande de l'offre doit être `VALIDEE` ou `EN_RECRUTEMENT`. | 422 |
| REC-02 | Une offre n'est modifiable qu'en `BROUILLON`. | 422 |
| REC-03 | CV : types PDF, DOC, DOCX ; un nouveau dépôt crée une version (via `DocumentService`). | 422 |
| REC-04 | Le type de l'entretien est dérivé du niveau (`TypeEntretien.pourNiveau`). | — |
| REC-05 | Un seul entretien non annulé par niveau et par candidature. Intervenants : utilisateurs **actifs** de rôle autre que `SALARIE`. | 422 / 409 |
| REC-06 | Avis agrégé d'un niveau : `DEFAVORABLE` si au moins une évaluation active `DEFAVORABLE` ; `FAVORABLE` si toutes les évaluations actives sont `FAVORABLE` ; `RESERVE` sinon. | — |
| REC-07 | Décision finale `RETENU` : statut `RETENUE` ; `NON_RETENU` : `NON_RETENUE` (motif obligatoire) ; `LISTE_ATTENTE` : `LISTE_ATTENTE` avec `rangListeAttente = max + 1` pour l'offre. Pour `RETENU`, l'effectif restant doit être > 0, sinon passage en liste d'attente. Enregistrée par la RH ou le manager de la demande. | 422 |
| REC-08 | Promotion depuis la liste d'attente : effectif restant > 0 exigé. | 422 |
| REC-09 | Visibilité : RH voit tout ; un manager ne voit que les candidatures des offres issues de **ses** demandes, ou celles où il est intervenant. Hors périmètre : 404. | 404 |
| REC-10 | Seul un intervenant désigné de l'entretien saisit une évaluation. | 403 |

Notifications : entretien planifié → intervenants (`ENTRETIEN_PLANIFIE`) ; évaluation attendue après la date → intervenants (`EVALUATION_DEMANDEE`, facultatif) ; décision finale → RH et demandeur (`CANDIDATURE_DECISION`).
Historique : chaque transition de `OffreEmploi`, `Candidature`, `Entretien` ; journal d'audit : création et modification des offres et des évaluations.

## 8. Migration `V5__recrutement.sql`

Tables : `offre_emploi`, `candidat`, `candidature`, `entretien`, `entretien_intervenant`, `evaluation_candidat`. Clés étrangères nommées `fk_<table>_<cible>`. Unicités : `reference` (offre), `LOWER(email)` (candidat), `(candidat_id, offre_id)`, index partiels cités, `(entretien_id, utilisateur_id)`. CHECK : statuts, types, niveaux (`BETWEEN 1 AND 3`), notes (`BETWEEN 0 AND 20`), scores (`BETWEEN 0 AND 100`).

## 9. Tests (squelettes désactivés)

`SelectionWorkflowServiceTest` (existant) : niveau 2 refusé si niveau 1 non favorable, niveau 1 refusé sans présélection, avis agrégé (trois cas), décision finale refusée si niveaux incomplets, mise en liste d'attente si effectif atteint. `CandidatureServiceTest` : doublon de candidature, offre non publiée refusée. `EvaluationCandidatServiceTest` : non-intervenant refusé, modification après verrouillage refusée, correction crée une version.

## 10. Front

| Élément | Contenu |
|---|---|
| `api/` | `offres.js`, `candidats.js`, `candidatures.js`, `entretiens.js`, `evaluations.js` |
| `views/recrutement/` | `OffreListView`, `OffreFormView`, `OffreDetailView`, `CandidatListView`, `CandidatDetailView`, `CandidatureListView`, `CandidatureDetailView`, `EntretienListView`, `EntretienDetailView` |
| `components/recrutement/` | `OffreForm`, `OffreTable`, `OffreCard`, `CandidatForm`, `CandidatureTable`, `CandidatureFilters`, `CandidaturePipeline` (colonnes par statut), `PreselectionModal`, `EntretienForm`, `EntretienTable`, `EvaluationForm`, `EvaluationList`, `DecisionFinaleModal`, `CvViewer` |
| Routes | Offres, candidats, entretiens : `[RH]` (lecture offres aussi `MANAGER`, `DG`) ; candidatures et évaluations : `[RH, MANAGER]` |
| Comportement | Le détail d'une candidature affiche statut, CV, entretiens par niveau, évaluations, historique ; les actions proposées dépendent du statut et du rôle (présélectionner, planifier le niveau suivant, décision finale). |
