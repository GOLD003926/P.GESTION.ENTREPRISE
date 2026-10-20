# Module `contrat` : contrats successifs, règles de durée, alertes

Bloc 4 du sujet. Principe P3 : **ne jamais écraser l'historique**. Un contrat est une ligne ; chaque renouvellement ou transformation crée un nouveau contrat chaîné au précédent. Le type de contrat courant d'un employé est **dérivé** de son contrat actif et n'est jamais stocké sur l'employé. Migration : `V7__contrat.sql`. Dépend de : `common`, `auth`, `organisation`, `employe`.

## 1. Fichiers

Existants (vides) :

```
contrat/entity/Contrat
contrat/enums/{TypeContrat, StatutContrat, MotifFinContrat}
contrat/repository/ContratRepository
contrat/dto/{ContratRequest, ContratResponse, RenouvellementRequest, TransformationRequest}
contrat/mapper/ContratMapper
contrat/service/ContratService
contrat/rule/ContratRuleValidator
contrat/scheduler/ContratAlerteScheduler
contrat/controller/ContratController
```

À créer : `dto/ContratUpdateRequest`, `dto/FinContratRequest`, `dto/ContratInitialCommande` (record d'API interne), `event/{ContratCreeEvent, ContratSigneEvent, ContratAnnuleEvent}`, `scheduler/ContratActivationScheduler`.

## 2. Énumérations

| Énumération | Valeurs | Méthodes |
|---|---|---|
| `TypeContrat` | `ESSAI`, `CDD`, `CDI` | `boolean aDateFin()` (vrai pour `ESSAI`, `CDD`) |
| `StatutContrat` | `BROUILLON`, `A_SIGNER`, `ACTIF`, `TERMINE`, `ANNULE` | Transitions ci-dessous |
| `MotifFinContrat` | `ECHEANCE`, `RENOUVELLEMENT`, `TRANSFORMATION`, `ESSAI_NON_CONCLUANT`, `DEMISSION`, `LICENCIEMENT`, `RETRAITE`, `RUPTURE_CONVENTIONNELLE`, `AUTRE` | — |

| De | Vers autorisés |
|---|---|
| `BROUILLON` | `A_SIGNER`, `ANNULE` |
| `A_SIGNER` | `ACTIF`, `BROUILLON` (retour pour correction), `ANNULE` |
| `ACTIF` | `TERMINE` |
| `TERMINE`, `ANNULE` | aucune |

## 3. Entité `Contrat` (table `contrat`, `AuditableEntity`)

| Attribut | Colonne | Type | Null | Règle |
|---|---|---|---|---|
| `numero` | `numero` | `VARCHAR(30)` | Non | Unique, `CT-AAAA-NNNN` via `NumeroService` |
| `employe` | `employe_id` | `BIGINT` | Non | `@ManyToOne` LAZY, clé étrangère |
| `type` | `type` | `VARCHAR(10)` | Non | `TypeContrat` |
| `numeroRenouvellement` | `numero_renouvellement` | `INT` | Non | 0 = initial, 1 = renouvelé |
| `contratPrecedent` | `contrat_precedent_id` | `BIGINT` | Oui | `@ManyToOne` LAZY vers `Contrat`, clé étrangère, unique (un contrat a au plus un successeur) |
| `poste` | `poste_id` | `BIGINT` | Non | `@ManyToOne` LAZY vers `Poste` (écart H12 : sert à créer l'affectation initiale) |
| `manager` | `manager_id` | `BIGINT` | Oui | `@ManyToOne` LAZY vers `Employe` |
| `dateDebut` | `date_debut` | `DATE` | Non | |
| `dateFinPrevue` | `date_fin_prevue` | `DATE` | Oui | Obligatoire pour `ESSAI` et `CDD`, nulle pour `CDI` |
| `dateFinReelle` | `date_fin_reelle` | `DATE` | Oui | |
| `salaireBase` | `salaire_base` | `NUMERIC(14,2)` | Non | > 0 |
| `devise` | `devise` | `VARCHAR(3)` | Non | Défaut `MGA` |
| `horairesTravail` | `horaires_travail` | `VARCHAR(255)` | Non | |
| `lieuTravail` | `lieu_travail` | `VARCHAR(255)` | Non | |
| `statut` | `statut` | `VARCHAR(20)` | Non | `StatutContrat`, défaut `BROUILLON` |
| `motifFin` | `motif_fin` | `VARCHAR(30)` | Oui | `MotifFinContrat` |
| `documentSigne` | `document_signe_id` | `BIGINT` | Oui | `@ManyToOne` LAZY vers `Document` |
| `dateSignature` | `date_signature` | `DATE` | Oui | |
| `alerteFinEnvoyee` | `alerte_fin_envoyee` | `BOOLEAN` | Non | Défaut `false` |

Index : `idx_contrat_employe`, `idx_contrat_statut`, `idx_contrat_fin` (`date_fin_prevue`). Index unique partiel `(employe_id) WHERE statut = 'ACTIF'` (un seul contrat actif par employé).

## 4. DTO

| DTO | Champs et validations |
|---|---|
| `ContratRequest` | `employeId` (obligatoire), `type` (obligatoire, défaut `ESSAI`), `posteId` (obligatoire), `managerId`, `dateDebut` (obligatoire), `dureeMois` (1 à 60 ; obligatoire pour `ESSAI` et `CDD`, interdit pour `CDI`), `salaireBase` (> 0), `devise` (3 lettres), `horairesTravail` (obligatoire, ≤ 255), `lieuTravail` (obligatoire, ≤ 255) |
| `ContratUpdateRequest` | idem sans `employeId` |
| `RenouvellementRequest` | `dureeMois` (obligatoire), `dateDebut` (défaut : lendemain de la fin prévue du contrat précédent), `salaireBase`, `horairesTravail`, `lieuTravail` (hérités du précédent si absents) |
| `TransformationRequest` | `typeCible` (`CDD` ou `CDI`), `dureeMois` (obligatoire pour `CDD`), `dateDebut`, `salaireBase`, `horairesTravail`, `lieuTravail` |
| `FinContratRequest` | `motifFin` (obligatoire), `dateFin` (obligatoire), `commentaire` (≤ 500) |
| `ContratResponse` | `id`, `numero`, `employeId`, `employeNom`, `matricule`, `type`, `numeroRenouvellement`, `contratPrecedentId`, `dateDebut`, `dateFinPrevue`, `dateFinReelle`, `salaireBase`, `devise`, `horairesTravail`, `lieuTravail`, `posteId`, `posteIntitule`, `managerId`, `statut`, `motifFin`, `documentSigneId`, `dateSignature`, `joursRestants` (entier ou nul), `createdAt` |
| `ContratInitialCommande` (record interne) | `employeId`, `posteId`, `managerId`, `type`, `dateDebut`, `dureeMois`, `salaireBase`, `devise`, `horairesTravail`, `lieuTravail` |

La signature se fait par `multipart/form-data` : `fichier` + `dateSignature`.

## 5. Événements

| Événement (record) | Champs | Publié |
|---|---|---|
| `ContratCreeEvent` | `contratId`, `employeId`, `type`, `dateDebut` | À la création d'un contrat |
| `ContratSigneEvent` | `contratId` | À l'enregistrement du document signé |
| `ContratAnnuleEvent` | `contratId` | À l'annulation |

Publication avec `ApplicationEventPublisher`, écoute synchrone (même transaction) par `onboarding`.

## 6. Services

### 6.1 `ContratRuleValidator` (règles de durée et de succession)

| Méthode | Règle |
|---|---|
| `void verifierCreation(TypeContrat type, LocalDate debut, Integer dureeMois)` | CON-01 à CON-03 |
| `void verifierRenouvellement(Contrat precedent, int dureeMois, LocalDate debut)` | CON-04 à CON-06 |
| `void verifierTransformation(Contrat precedent, TypeContrat cible, Integer dureeMois, LocalDate debut)` | CON-07 |
| `int dureeCumuleeMois(Contrat contrat)` | Durée totale de la chaîne de contrats **de même type** menant à ce contrat (en remontant `contratPrecedent`) |
| `LocalDate calculerFinPrevue(LocalDate debut, int dureeMois)` | `debut + dureeMois mois − 1 jour` |

Toutes les durées maximales viennent de `SirhProperties.contrat()`. L'interprétation du plafond du CDD (hypothèse H3) est **concentrée dans cette classe** pour pouvoir changer sans toucher aux services.

### 6.2 `ContratService`

| Méthode | Type | Règle |
|---|---|---|
| `creer(ContratRequest, CurrentUser)` | Contrôleur | Employé existant ; CON-01, CON-02, CON-03 ; calcul de `dateFinPrevue` ; référence `CT-...` ; statut `BROUILLON` ; publie `ContratCreeEvent` ; historique ; journal d'audit |
| `modifier(Long id, ContratUpdateRequest, CurrentUser)` | Contrôleur | Uniquement `BROUILLON` (CON-09) |
| `obtenir(Long id, CurrentUser)`, `rechercher(Long employeId, TypeContrat type, StatutContrat statut, LocalDate finAvant, Pageable, CurrentUser)` | Contrôleur | CON-12 (visibilité). Tri : `id`, `numero`, `dateDebut`, `dateFinPrevue`, `statut` |
| `mesContrats(CurrentUser)` | Contrôleur | Contrats du salarié connecté (via `employeId`) |
| `envoyerPourSignature(Long id, CurrentUser)` | Contrôleur | `BROUILLON` → `A_SIGNER` |
| `enregistrerSignature(Long id, MultipartFile fichier, LocalDate dateSignature, CurrentUser)` | Contrôleur | `A_SIGNER` ; `DocumentService.enregistrer(..., CONTRAT_SIGNE, CONTRAT, id)` ; renseigne `documentSigne`, `dateSignature` ; publie `ContratSigneEvent` ; appelle `activerSiPret` |
| `renouveler(Long id, RenouvellementRequest, CurrentUser)` | Contrôleur | CON-04 à CON-06 ; crée un **nouveau** contrat `BROUILLON` du même type, `numeroRenouvellement = précédent + 1`, chaîné ; publie `ContratCreeEvent` |
| `transformer(Long id, TransformationRequest, CurrentUser)` | Contrôleur | CON-07 ; nouveau contrat `BROUILLON` du type cible chaîné ; publie `ContratCreeEvent` |
| `terminer(Long id, FinContratRequest, CurrentUser)` | Contrôleur | `ACTIF` → `TERMINE`, `motifFin`, `dateFinReelle` (cas : essai non concluant, démission, etc.) |
| `annuler(Long id, CurrentUser)` | Contrôleur | `BROUILLON`, `A_SIGNER` → `ANNULE` ; publie `ContratAnnuleEvent` |
| `historique(Long id, CurrentUser)` | Contrôleur | Historique des statuts |
| `Contrat creerContratInitial(ContratInitialCommande, String auteur)` | **API interne** (appelée par `embauche`, RG-33) | Même logique que `creer`, sans `CurrentUser` |
| `void activerSiPret(Long contratId, String auteur)` | **API interne** | CON-10, CON-11 |
| `int activerContratsDus(LocalDate aujourdhui, String auteur)` | **API interne** | Active tous les contrats `A_SIGNER` signés dont `dateDebut ≤ aujourdhui` ; retourne le nombre activé |
| `int alerterFinsProches(LocalDate aujourdhui)` | **API interne** | CON-13 |

Algorithme de `activerSiPret` : si le contrat est `A_SIGNER`, possède un `documentSigne` et `dateDebut ≤ aujourd'hui` : (1) s'il existe un contrat précédent `ACTIF`, le passer `TERMINE` avec `dateFinReelle = dateDebut − 1 jour` et `motifFin = RENOUVELLEMENT` (même type) ou `TRANSFORMATION` (type différent) ; (2) passer le contrat à `ACTIF` ; (3) `EmployeService.activer(employeId, dateDebut, auteur)` ; (4) si l'employé n'a aucune affectation active, `AffectationService.creerInitiale(employe, poste, contratId, managerId, dateDebut, auteur)` (RG-41) ; (5) historique.

### 6.3 Planificateurs

| Classe | Planification | Délègue à |
|---|---|---|
| `ContratActivationScheduler` | Quotidienne, 00:05 | `ContratService.activerContratsDus` |
| `ContratAlerteScheduler` | Quotidienne, 07:00 | `ContratService.alerterFinsProches` |

## 7. Endpoints

| Méthode | Route | Rôles | Description |
|---|---|---|---|
| GET | `/api/contrats` | RH, MANAGER, PAIE | Liste (`employeId`, `type`, `statut`, `finAvant`) |
| GET | `/api/contrats/{id}` | RH, MANAGER, PAIE | Détail |
| GET | `/api/contrats/mes-contrats` | Authentifié | Mes contrats |
| POST | `/api/contrats` | RH | Création (201) |
| PUT | `/api/contrats/{id}` | RH | Modification (brouillon) |
| POST | `/api/contrats/{id}/envoyer-signature` | RH | Envoi pour signature |
| POST | `/api/contrats/{id}/signature` | RH | Dépôt du contrat signé (`multipart`) |
| POST | `/api/contrats/{id}/renouveler` | RH | Renouvellement |
| POST | `/api/contrats/{id}/transformer` | RH | Transformation de type |
| POST | `/api/contrats/{id}/terminer` | RH | Fin de contrat |
| POST | `/api/contrats/{id}/annuler` | RH | Annulation |
| GET | `/api/contrats/{id}/historique` | RH | Historique des statuts |

## 8. Règles de gestion

| Code | Règle | Erreur |
|---|---|---|
| RG-33 | Le contrat créé par défaut après acceptation d'une proposition est un contrat d'essai. | — |
| RG-34 / CON-01 | Essai : durée initiale ≤ `essai.dureeMaxMois` (6). | 422 |
| CON-04 | Essai : renouvelable **une seule fois** (`numeroRenouvellement` ≤ 1) ; durée cumulée de la chaîne d'essais ≤ `essai.cumulMaxMois` (12). | 422 |
| RG-35 / CON-02 | CDD : durée ≤ `cdd.dureeMaxMois` (24) ; renouvelable **une seule fois** ; durée cumulée de la chaîne de CDD ≤ `cdd.dureeMaxMois` (hypothèse H3). | 422 |
| RG-36 / CON-03 | CDI : aucune date de fin. `ESSAI` et `CDD` : date de fin obligatoire. | 422 |
| RG-37 | Plusieurs contrats successifs, chacun une ligne chaînée ; rien n'est écrasé. | — |
| RG-38 / CON-05 | Un seul contrat `ACTIF` par employé ; le type courant est dérivé du contrat actif. | 409 |
| RG-39 / CON-10 | Un contrat devient `ACTIF` seulement si le document signé est déposé et la date de début atteinte. | 422 |
| RG-40 / CON-13 | Alerte à la RH quand la fin prévue d'un contrat `ACTIF` `ESSAI` ou `CDD` est dans `alerteFinJours` jours ou moins ; une seule alerte par contrat (`alerteFinEnvoyee`). Notification `CONTRAT_ECHEANCE` aux utilisateurs RH. | — |
| RG-41 | Affectation initiale créée à l'activation. | — |
| CON-06 | Renouvellement : le contrat précédent est `ACTIF` ; le nouveau est du même type que le précédent ; `dateDebut` entre le lendemain du début du précédent et le lendemain de sa fin prévue. | 422 |
| CON-07 | Transformation autorisée : `ESSAI` → `CDD` ou `CDI` ; `CDD` → `CDI`. `CDI` ne se transforme pas. | 422 |
| CON-08 | `renouveler` et `transformer` créent toujours un nouveau contrat `BROUILLON`, jamais une modification du contrat existant. | — |
| CON-09 | Un contrat n'est modifiable qu'en `BROUILLON`. | 422 |
| CON-11 | À l'activation d'un contrat successeur, le précédent `ACTIF` est terminé automatiquement (motif `RENOUVELLEMENT` ou `TRANSFORMATION`). | — |
| CON-12 | Visibilité : RH tout ; manager : contrats des employés qu'il encadre ; paie : lecture ; salarié : ses contrats seulement. Hors périmètre : 404. | 404 |
| CON-14 | Création, modification et fin de contrat journalisées dans `JournalAudit`. | — |

## 9. Migration `V7__contrat.sql`

Table `contrat`, clés étrangères nommées (`fk_contrat_employe`, `fk_contrat_precedent`, `fk_contrat_poste`, `fk_contrat_manager`, `fk_contrat_document`), unicités (`numero`, `contrat_precedent_id`), index unique partiel `WHERE statut = 'ACTIF'` sur `employe_id`, contraintes CHECK (`type`, `statut`, `motif_fin`, `numero_renouvellement BETWEEN 0 AND 1`, `ck_contrat_fin_type` : date de fin nulle si `CDI`, non nulle sinon, `salaire_base > 0`). Puis : `ALTER TABLE affectation ADD CONSTRAINT fk_affectation_contrat FOREIGN KEY (contrat_id) REFERENCES contrat(id)`.

## 10. Tests (squelettes désactivés)

`ContratRuleValidatorTest` (existant) : essai de 7 mois refusé, second renouvellement refusé, cumul d'essai > 12 mois refusé, CDD de 25 mois refusé, CDI avec date de fin refusé, transformation de CDI refusée. `ContratServiceTest` : activation sans document signé refusée, activation du successeur termine le précédent, un seul contrat actif, alerte envoyée une seule fois.

## 11. Front

| Élément | Contenu |
|---|---|
| `api/` | `contrats.js` |
| `views/contrat/` | `ContratListView`, `ContratFormView`, `ContratDetailView` |
| `components/contrat/` | `ContratForm`, `ContratTable`, `ContratTimeline` (chaîne essai → renouvellement → CDI), `RenouvellementModal`, `TransformationModal`, `ContratAlertBanner` (contrats arrivant à échéance) |
| Routes | `/contrats` : `[RH, MANAGER, PAIE]` ; création et actions : `[RH]` |
| Comportement | Le détail d'un employé affiche la frise de ses contrats. Les boutons « Renouveler », « Transformer », « Terminer » apparaissent selon le type, le statut et la possibilité de renouvellement. Un message distinct est affiché quand le renouvellement n'est plus possible (422). |
