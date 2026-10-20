# Module `organisation` : départements, grilles salariales, postes

Référentiels de l'entreprise. Principe de conception P1 : **un poste existe indépendamment de l'employé** (il peut avoir plusieurs occupants). Migration : `V1__organisation.sql`. Dépend de : `common`, `auth`.

## 1. Fichiers

Tous existent déjà (vides) sauf mention :

```
organisation/entity/{Departement, GrilleSalariale, Poste}
organisation/repository/{Departement, GrilleSalariale, Poste}Repository
organisation/dto/{Departement, GrilleSalariale, Poste}{Request, Response}
organisation/mapper/{Departement, GrilleSalariale, Poste}Mapper
organisation/service/{Departement, GrilleSalariale, Poste}Service
organisation/controller/{Departement, GrilleSalariale, Poste}Controller
```

Fichiers à créer : `dto/{Departement,GrilleSalariale,Poste}UpdateRequest`, `service/PosteOccupationProvider` (port, voir 5.4).

## 2. Entités (toutes `AuditableEntity`)

### 2.1 `GrilleSalariale` (table `grille_salariale`)

| Attribut | Colonne | Type | Null | Règle |
|---|---|---|---|---|
| `code` | `code` | `VARCHAR(30)` | Non | Unique, majuscules, immuable |
| `intitule` | `intitule` | `VARCHAR(100)` | Non | |
| `niveau` | `niveau` | `VARCHAR(30)` | Non | Libellé (ex. « Cadre », « Technicien ») |
| `salaireMin` | `salaire_min` | `NUMERIC(14,2)` | Non | > 0 |
| `salaireMax` | `salaire_max` | `NUMERIC(14,2)` | Non | ≥ `salaire_min` (contrainte CHECK) |
| `devise` | `devise` | `VARCHAR(3)` | Non | Défaut `MGA` |
| `actif` | `actif` | `BOOLEAN` | Non | Défaut `true` |

### 2.2 `Departement` (table `departement`)

| Attribut | Colonne | Type | Null | Règle |
|---|---|---|---|---|
| `code` | `code` | `VARCHAR(30)` | Non | Unique, majuscules, immuable |
| `nom` | `nom` | `VARCHAR(150)` | Non | |
| `departementParent` | `departement_parent_id` | `BIGINT` | Oui | `@ManyToOne` LAZY vers `Departement`, clé étrangère |
| `responsableId` | `responsable_id` | `BIGINT` | Oui | Identifiant d'**employé**, **sans clé étrangère** ici (ajoutée par `V6__employe.sql`), attribut `Long` |
| `actif` | `actif` | `BOOLEAN` | Non | Défaut `true` |

### 2.3 `Poste` (table `poste`)

| Attribut | Colonne | Type | Null | Règle |
|---|---|---|---|---|
| `code` | `code` | `VARCHAR(30)` | Non | Unique, majuscules, immuable |
| `intitule` | `intitule` | `VARCHAR(150)` | Non | |
| `description` | `description` | `TEXT` | Oui | |
| `competencesRequises` | `competences_requises` | `TEXT` | Oui | |
| `departement` | `departement_id` | `BIGINT` | Non | `@ManyToOne` LAZY, clé étrangère |
| `grilleSalariale` | `grille_salariale_id` | `BIGINT` | Non | `@ManyToOne` LAZY, clé étrangère |
| `effectifAutorise` | `effectif_autorise` | `INT` | Non | ≥ 1 (CHECK) : nombre maximal d'occupants simultanés |
| `actif` | `actif` | `BOOLEAN` | Non | Défaut `true` |

Index : `idx_poste_departement`, `idx_poste_grille`, `uk_*_code` insensibles à la casse (`LOWER(code)`).

## 3. DTO

| DTO | Champs et validations |
|---|---|
| `GrilleSalarialeRequest` | `code` (obligatoire, ≤ 30, motif `[A-Z0-9_-]+`), `intitule` (obligatoire, ≤ 100), `niveau` (obligatoire, ≤ 30), `salaireMin` (obligatoire, > 0), `salaireMax` (obligatoire, > 0), `devise` (facultatif, 3 lettres, défaut `MGA`) |
| `GrilleSalarialeUpdateRequest` | idem **sans** `code` |
| `GrilleSalarialeResponse` | `id`, `code`, `intitule`, `niveau`, `salaireMin`, `salaireMax`, `devise`, `actif`, `createdAt`, `updatedAt` |
| `DepartementRequest` | `code`, `nom` (obligatoire, ≤ 150), `departementParentId` (facultatif, positif), `responsableId` (facultatif, positif) |
| `DepartementUpdateRequest` | idem sans `code` |
| `DepartementResponse` | `id`, `code`, `nom`, `departementParentId`, `departementParentNom`, `responsableId`, `actif`, `createdAt`, `updatedAt` |
| `PosteRequest` | `code`, `intitule` (obligatoire, ≤ 150), `description` (≤ 4000), `competencesRequises` (≤ 4000), `departementId` (obligatoire), `grilleSalarialeId` (obligatoire), `effectifAutorise` (obligatoire, 1 à 1000) |
| `PosteUpdateRequest` | idem sans `code` |
| `PosteResponse` | `id`, `code`, `intitule`, `description`, `competencesRequises`, `departementId`, `departementNom`, `grilleSalarialeId`, `grilleSalarialeIntitule`, `salaireMin`, `salaireMax`, `effectifAutorise`, `occupationActuelle`, `placesLibres`, `actif`, `createdAt`, `updatedAt` |

Messages de validation en français sur chaque contrainte.

## 4. Mappers

`toResponse`, `toEntity`, `updateEntity` par entité. Le mapper de `Poste` reçoit `occupationActuelle` en paramètre (calculée par le service) pour renseigner `placesLibres = effectifAutorise - occupationActuelle`. La résolution du parent, du département et de la grille est faite par le service (les mappers ne chargent rien).

## 5. Services

### 5.1 `GrilleSalarialeService`

| Méthode | Règle |
|---|---|
| `PageResponse<GrilleSalarialeResponse> rechercher(String q, Boolean actif, Pageable)` | `q` sur code et intitulé. Tri autorisé : `id`, `code`, `intitule`, `niveau`, `salaireMin`, `salaireMax`, `actif`. |
| `GrilleSalarialeResponse obtenir(Long id)` | 404 si absent |
| `GrilleSalarialeResponse creer(GrilleSalarialeRequest)` | ORG-01, ORG-02, enregistrement du journal d'audit |
| `GrilleSalarialeResponse modifier(Long id, GrilleSalarialeUpdateRequest)` | ORG-02, ORG-05 |
| `GrilleSalarialeResponse activer(Long id)` / `desactiver(Long id)` | ORG-04 |

### 5.2 `DepartementService`

| Méthode | Règle |
|---|---|
| `PageResponse<DepartementResponse> rechercher(String q, Boolean actif, Long parentId, Pageable)` | Tri autorisé : `id`, `code`, `nom`, `actif` |
| `obtenir`, `creer`, `modifier`, `activer`, `desactiver` | ORG-01, ORG-03, ORG-04, ORG-07 |

### 5.3 `PosteService`

| Méthode | Règle |
|---|---|
| `PageResponse<PosteResponse> rechercher(String q, Long departementId, Long grilleSalarialeId, Boolean actif, Pageable)` | Tri autorisé : `id`, `code`, `intitule`, `effectifAutorise`, `actif` |
| `obtenir`, `creer`, `modifier`, `activer`, `desactiver` | RG-01, RG-02, ORG-01, ORG-05, ORG-06, ORG-04 |
| `int placesLibres(Long posteId)` | **API interne** : `effectifAutorise - occupationActuelle` (utilisée par `demande`, RG-04) |
| `Poste trouverActif(Long posteId)` | **API interne** : 404 si absent, `BusinessRuleException` si inactif |

### 5.4 Port `PosteOccupationProvider`

Interface `int occupationActuelle(Long posteId)`. Le module `employe` en fournit l'implémentation. `PosteService` l'injecte avec `ObjectProvider<PosteOccupationProvider>` et utilise `getIfAvailable(() -> posteId -> 0)` : tant que le module `employe` n'existe pas, l'occupation vaut 0.

## 6. Endpoints

Lecture : `RH, MANAGER, FINANCE, DG, ADMIN`. Écriture départements et grilles : `RH, ADMIN`. Écriture postes : `RH`.

| Méthode | Route | Corps | Description |
|---|---|---|---|
| GET | `/api/departements` | — | Liste (`q`, `actif`, `parentId`, pagination) |
| GET | `/api/departements/{id}` | — | Détail |
| POST | `/api/departements` | `DepartementRequest` | Création (201) |
| PUT | `/api/departements/{id}` | `DepartementUpdateRequest` | Modification |
| POST | `/api/departements/{id}/activer` | — | Activation |
| POST | `/api/departements/{id}/desactiver` | — | Désactivation |
| GET | `/api/grilles-salariales` | — | Liste (`q`, `actif`) |
| GET | `/api/grilles-salariales/{id}` | — | Détail |
| POST | `/api/grilles-salariales` | `GrilleSalarialeRequest` | Création (201) |
| PUT | `/api/grilles-salariales/{id}` | `GrilleSalarialeUpdateRequest` | Modification |
| POST | `/api/grilles-salariales/{id}/activer` | — | Activation |
| POST | `/api/grilles-salariales/{id}/desactiver` | — | Désactivation |
| GET | `/api/postes` | — | Liste (`q`, `departementId`, `grilleSalarialeId`, `actif`) |
| GET | `/api/postes/{id}` | — | Détail avec occupation |
| POST | `/api/postes` | `PosteRequest` | Création (201) |
| PUT | `/api/postes/{id}` | `PosteUpdateRequest` | Modification |
| POST | `/api/postes/{id}/activer` | — | Activation |
| POST | `/api/postes/{id}/desactiver` | — | Désactivation |

## 7. Règles de gestion

| Code | Règle | Erreur |
|---|---|---|
| RG-01 | Un poste est créé et maintenu par la RH, indépendamment de tout employé. | 403 pour un autre rôle |
| RG-02 | Le nombre d'affectations actives d'un poste ne dépasse jamais `effectifAutorise` (contrôlé par `employe`). | — |
| ORG-01 | Le code est unique sans tenir compte de la casse, normalisé en majuscules. | 409 `DUPLICATE_RESOURCE` |
| ORG-02 | `salaireMax ≥ salaireMin`. | 422 |
| ORG-03 | Un département ne peut pas être son propre parent, ni créer un cycle dans la hiérarchie (parcourir la chaîne des parents). Le parent doit être actif. | 422 |
| ORG-04 | Désactivation interdite : pour un département s'il possède des postes actifs ou des sous-départements actifs ; pour une grille si des postes actifs l'utilisent ; pour un poste si `occupationActuelle > 0`. | 422 avec message précisant la cause |
| ORG-05 | `effectifAutorise` ne peut pas être inférieur à l'occupation actuelle du poste ; les bornes d'une grille ne peuvent pas exclure le salaire d'un contrat actif (non vérifié dans le MVP). | 422 |
| ORG-06 | Un poste référence un département **actif** et une grille **active**. | 422 |
| ORG-07 | Le code est immuable (absent des `UpdateRequest`). | — |
| ORG-08 | Création et modification : enregistrement dans `JournalAudit` (avant/après). | — |

## 8. Migration `V1__organisation.sql`

Créer dans l'ordre : `grille_salariale`, `departement` (colonne `responsable_id` **sans** clé étrangère), `poste`. Contraintes : unicité `LOWER(code)` sur les trois tables, `ck_grille_salariale_bornes (salaire_max >= salaire_min)`, `ck_poste_effectif (effectif_autorise >= 1)`, clés étrangères `fk_departement_parent`, `fk_poste_departement`, `fk_poste_grille`, index cités.

Données de départ : aucune.

## 9. Compléments par les modules suivants

| Module | Complément |
|---|---|
| `employe` | `V6` ajoute `fk_departement_responsable` ; `DepartementService` vérifie que `responsableId` désigne un employé existant ; implémentation du port `PosteOccupationProvider`. |

## 10. Tests (squelettes désactivés)

`PosteServiceTest` : code dupliqué refusé, effectif sous l'occupation refusé, désactivation d'un poste occupé refusée, poste sur grille inactive refusé. `DepartementServiceTest` : détection de cycle. `GrilleSalarialeServiceTest` : bornes incohérentes refusées.

## 11. Front

| Élément | Contenu |
|---|---|
| `api/` | `departements.js`, `grillesSalariales.js`, `postes.js` |
| `views/organisation/` | `DepartementListView`, `DepartementFormView`, `PosteListView`, `PosteFormView`, `GrilleSalarialeListView`, `GrilleSalarialeFormView` |
| `components/organisation/` | `DepartementForm`, `PosteForm`, `GrilleSalarialeForm`, `PosteOccupationBadge` (affiche `occupationActuelle / effectifAutorise`) |
| `router/routes/organisation.routes.js` | Lecture : tous les rôles listés ; écrans de création et de modification : `meta.roles = [RH, ADMIN]` (postes : `[RH]`) |
| Menu | Entrée « Organisation » avec trois sous-entrées |
| Comportement | Liste avec recherche, filtre actif/inactif, pagination ; bouton « Désactiver » avec confirmation ; l'erreur 422 de l'ORG-04 est affichée telle quelle dans une alerte |
