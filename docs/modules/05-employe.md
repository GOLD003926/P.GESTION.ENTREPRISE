# Module `employe` : employés, matricule, fiche PDF, affectations

Principe de conception P2 : **un candidat n'est pas un employé**. L'employé est créé uniquement à l'acceptation d'une proposition d'embauche. Les postes occupés sont historisés par les affectations (principe P3). Migration : `V6__employe.sql`. Dépend de : `common`, `auth`, `organisation`, `demande`, `recrutement`.

## 1. Fichiers

Existants (vides) :

```
employe/entity/{Employe, Affectation}
employe/enums/{StatutEmploye, MotifAffectation}
employe/repository/{Employe, Affectation}Repository
employe/dto/{Employe, Affectation}{Request, Response}
employe/mapper/{Employe, Affectation}Mapper
employe/service/{EmployeService, AffectationService, MatriculeService, FicheSalariePdfService}
employe/controller/{EmployeController, AffectationController}
```

À créer : `dto/EmployeUpdateRequest`, `dto/EmployeResumeResponse`, `dto/AffectationClotureRequest`, `service/AffectationOccupationAdapter`, `service/EmployeLookupAdapter`.

Dépendance à ajouter dans `pom.xml` : OpenPDF (`com.github.librepdf:openpdf`). La **version est à vérifier sur Maven Central avant l'ajout** ; elle n'est pas gérée par Spring Boot et ne doit pas être devinée.

## 2. Énumérations

| Énumération | Valeurs | Méthodes |
|---|---|---|
| `StatutEmploye` | `PRE_EMBAUCHE`, `ACTIF`, `SUSPENDU`, `SORTI` | `transitionsAutorisees()`, `peutPasserA`, `estTerminal()` |
| `MotifAffectation` | `EMBAUCHE`, `MUTATION`, `PROMOTION`, `REORGANISATION` | — |

| De | Vers autorisés |
|---|---|
| `PRE_EMBAUCHE` | `ACTIF`, `SORTI` (désistement avant début) |
| `ACTIF` | `SUSPENDU`, `SORTI` |
| `SUSPENDU` | `ACTIF`, `SORTI` |
| `SORTI` | aucune |

## 3. Entités (`AuditableEntity`)

### 3.1 `Employe` (table `employe`)

| Attribut | Colonne | Type | Null | Règle |
|---|---|---|---|---|
| `matricule` | `matricule` | `VARCHAR(30)` | Non | Unique, `EMP-AAAA-NNNN`, jamais réutilisé, immuable |
| `nom` | `nom` | `VARCHAR(100)` | Non | |
| `prenom` | `prenom` | `VARCHAR(100)` | Non | |
| `emailPro` | `email_pro` | `VARCHAR(150)` | Oui | Unique insensible à la casse quand renseigné |
| `emailPerso` | `email_perso` | `VARCHAR(150)` | Oui | |
| `telephone` | `telephone` | `VARCHAR(30)` | Oui | |
| `dateNaissance` | `date_naissance` | `DATE` | Oui | |
| `adresse` | `adresse` | `VARCHAR(255)` | Oui | |
| `numeroSecuriteSociale` | `numero_securite_sociale` | `VARCHAR(30)` | Oui | |
| `dateEmbauche` | `date_embauche` | `DATE` | Oui | Début du premier contrat actif |
| `statut` | `statut` | `VARCHAR(20)` | Non | `StatutEmploye`, défaut `PRE_EMBAUCHE` |
| `candidatureOrigine` | `candidature_origine_id` | `BIGINT` | Oui | `@ManyToOne` LAZY vers `Candidature`, clé étrangère, **unique** (relation 0..1 – 0..1) |

### 3.2 `Affectation` (table `affectation`)

| Attribut | Colonne | Type | Null | Règle |
|---|---|---|---|---|
| `employe` | `employe_id` | `BIGINT` | Non | `@ManyToOne` LAZY, clé étrangère |
| `poste` | `poste_id` | `BIGINT` | Non | `@ManyToOne` LAZY vers `Poste`, clé étrangère |
| `contratId` | `contrat_id` | `BIGINT` | Oui | `Long`, **sans clé étrangère** (ajoutée par `V7`) |
| `manager` | `manager_id` | `BIGINT` | Oui | `@ManyToOne` LAZY vers `Employe`, clé étrangère |
| `dateDebut` | `date_debut` | `DATE` | Non | |
| `dateFin` | `date_fin` | `DATE` | Oui | Nulle = en cours ; si renseignée, ≥ `date_debut` |
| `tauxOccupation` | `taux_occupation` | `NUMERIC(5,2)` | Non | Défaut 100 ; `> 0` et `≤ 100` |
| `motif` | `motif` | `VARCHAR(20)` | Non | `MotifAffectation` |

Une affectation est **active** à la date D si `dateDebut ≤ D` et (`dateFin` nulle ou `dateFin ≥ D`).

## 4. DTO

| DTO | Champs et validations |
|---|---|
| `EmployeRequest` | `nom`, `prenom` (obligatoires, ≤ 100), `emailPro`, `emailPerso` (valides), `telephone` (≤ 30), `dateNaissance` (passé), `adresse` (≤ 255), `numeroSecuriteSociale` (≤ 30) |
| `EmployeUpdateRequest` | idem |
| `EmployeResponse` | `id`, `matricule`, `nom`, `prenom`, `emailPro`, `emailPerso`, `telephone`, `dateNaissance`, `adresse`, `numeroSecuriteSociale`, `dateEmbauche`, `statut`, `candidatureOrigineId`, `posteActuelId`, `posteActuelIntitule`, `departementActuelNom`, `managerActuelId`, `managerActuelNom`, `createdAt` |
| `EmployeResumeResponse` | `id`, `matricule`, `nom`, `prenom`, `statut`, `dateEmbauche`, `posteActuelIntitule` (vue limitée pour manager et paie) |
| `AffectationRequest` | `employeId`, `posteId` (obligatoires), `managerId`, `dateDebut` (obligatoire), `tauxOccupation` (> 0, ≤ 100, défaut 100), `motif` (obligatoire), `cloturerActives` (booléen, défaut `false`) |
| `AffectationClotureRequest` | `dateFin` (obligatoire) |
| `AffectationResponse` | `id`, `employeId`, `employeNom`, `matricule`, `posteId`, `posteIntitule`, `departementNom`, `contratId`, `managerId`, `managerNom`, `dateDebut`, `dateFin`, `tauxOccupation`, `motif`, `active` |

## 5. Services

### 5.1 `MatriculeService`

`String generer()` : délègue à `NumeroService.prochain(SequenceNumero.EMPLOYE)`.

### 5.2 `FicheSalariePdfService`

`byte[] generer(Employe employe, Optional<Affectation> affectationActuelle)` : produit un PDF (OpenPDF) contenant matricule, identité, coordonnées, statut, date d'embauche, poste, département et manager actuels, date d'édition. Aucun appel de base de données dans ce service.

### 5.3 `EmployeService`

| Méthode | Type | Règle |
|---|---|---|
| `creer(EmployeRequest, CurrentUser)` | Contrôleur | Création manuelle (cas exceptionnel, par exemple reprise de données) : matricule généré, statut `PRE_EMBAUCHE` |
| `modifier(Long id, EmployeUpdateRequest, CurrentUser)` | Contrôleur | Matricule immuable, journal d'audit |
| `obtenir(Long id, CurrentUser)` | Contrôleur | EMP-05 (visibilité). Retourne `EmployeResponse` (RH, salarié lui-même) ou `EmployeResumeResponse` (manager, paie) |
| `moi(CurrentUser)` | Contrôleur | Dossier de l'utilisateur connecté via `utilisateur.employeId` ; 404 si non lié |
| `rechercher(String q, StatutEmploye statut, Long departementId, Long posteId, Pageable, CurrentUser)` | Contrôleur | `q` sur matricule, nom, prénom. Tri : `id`, `matricule`, `nom`, `prenom`, `statut`, `dateEmbauche` |
| `suspendre(Long id, CurrentUser)`, `reprendre(Long id, CurrentUser)` | Contrôleur | `ACTIF` ↔ `SUSPENDU`, historique |
| `byte[] ficheSalarie(Long id, CurrentUser)` | Contrôleur | RH uniquement |
| `historique(Long id, CurrentUser)` | Contrôleur | Historique des statuts |
| `Employe creerDepuisCandidature(Long candidatureId, LocalDate dateEmbauche, String auteur)` | **API interne** (appelée par `embauche`, RG-31, RG-32) | Copie nom, prénom, email (en `emailPerso`), téléphone, naissance et adresse du `Candidat` ; statut `PRE_EMBAUCHE` ; matricule ; `candidatureOrigine` renseignée ; refus si un employé existe déjà pour cette candidature (EMP-02). Le `Candidat` et la `Candidature` restent intacts. |
| `void activer(Long employeId, LocalDate dateDebut, String auteur)` | **API interne** (appelée par `contrat`, RG-45) | `PRE_EMBAUCHE` → `ACTIF` ; renseigne `dateEmbauche` si vide |
| `Employe trouver(Long id)` | **API interne** | 404 si absent |

### 5.4 `AffectationService`

| Méthode | Type | Règle |
|---|---|---|
| `creer(AffectationRequest, CurrentUser)` | Contrôleur | RG-02, RG-41, EMP-03, EMP-04 |
| `cloturer(Long id, AffectationClotureRequest, CurrentUser)` | Contrôleur | `dateFin ≥ dateDebut` |
| `lister(Long employeId, Long posteId, Boolean active, Pageable)` | Contrôleur | Tri : `id`, `dateDebut`, `dateFin` |
| `Affectation creerInitiale(Employe, Poste, Long contratId, Long managerId, LocalDate dateDebut, String auteur)` | **API interne** (appelée par `contrat`, RG-41) | Motif `EMBAUCHE`, taux 100, mêmes contrôles |
| `int occupationActuelle(Long posteId)` | **API interne** | Nombre d'affectations actives aujourd'hui sur le poste |

### 5.5 Adaptateurs de ports

| Classe | Implémente | Comportement |
|---|---|---|
| `AffectationOccupationAdapter` | `organisation.service.PosteOccupationProvider` | Délègue à `AffectationService.occupationActuelle` |
| `EmployeLookupAdapter` | `demande.service.EmployeLookup` | `existe` : l'employé existe ; `departementActuel` : département du poste de l'affectation active la plus récente |

Annotés `@Component`.

## 6. Endpoints

| Méthode | Route | Rôles | Description |
|---|---|---|---|
| GET | `/api/employes` | RH, MANAGER, PAIE | Liste |
| GET | `/api/employes/{id}` | RH, MANAGER, PAIE | Détail |
| GET | `/api/employes/moi` | Authentifié | Mon dossier |
| POST | `/api/employes` | RH | Création manuelle (201) |
| PUT | `/api/employes/{id}` | RH | Modification |
| POST | `/api/employes/{id}/suspendre` | RH | Suspension |
| POST | `/api/employes/{id}/reprendre` | RH | Reprise |
| GET | `/api/employes/{id}/fiche-pdf` | RH | Téléchargement du PDF |
| GET | `/api/employes/{id}/historique` | RH | Historique des statuts |
| GET | `/api/affectations` | RH, MANAGER | Liste (`employeId`, `posteId`, `active`) |
| POST | `/api/affectations` | RH | Création (201) |
| POST | `/api/affectations/{id}/cloturer` | RH | Clôture |

## 7. Règles de gestion

| Code | Règle | Erreur |
|---|---|---|
| RG-02 | Le nombre d'affectations actives d'un poste ne dépasse jamais `effectifAutorise`. | 422 |
| RG-31 | À l'acceptation d'une proposition : employé `PRE_EMBAUCHE`, matricule unique non réutilisable, fiche PDF générée (par `embauche`). | — |
| RG-32 | Le candidat et la candidature restent intacts ; l'employé garde le lien `candidatureOrigine`. | — |
| RG-41 | Une nouvelle affectation ferme la précédente quand `cloturerActives = true` (`dateFin = dateDebut − 1 jour`) ; une affectation initiale est créée à l'activation du contrat. | — |
| RG-45 | L'employé passe `ACTIF` à la date de début du contrat actif. | — |
| EMP-01 | Matricule unique, jamais réutilisé, immuable. | 409 |
| EMP-02 | Une candidature donne naissance à **au plus un** employé. | 409 |
| EMP-03 | Pour un employé, la somme des taux d'occupation des affectations qui se chevauchent ne dépasse pas 100. Sinon refus, sauf `cloturerActives = true`. | 422 |
| EMP-04 | Le poste d'une affectation est actif ; le manager désigné est un employé `ACTIF`. | 422 |
| EMP-05 | Visibilité : RH tout ; un manager ne voit que les employés dont l'affectation active désigne son propre `employeId` comme manager (vue résumée) ; paie : vue résumée ; salarié : uniquement `moi`. Hors périmètre : 404. | 404 |
| EMP-06 | Création et modification des employés et des affectations journalisées dans `JournalAudit`. | — |
| EMP-07 | Aucune suppression physique ; la sortie d'un employé (`SORTI`) relève du processus de rupture, hors MVP. | — |

## 8. Migration `V6__employe.sql`

1. Tables `employe` et `affectation` (colonne `contrat_id` **sans** clé étrangère).
2. Clés étrangères différées vers `employe(id)` : `fk_utilisateur_employe` sur `utilisateur.employe_id`, `fk_departement_responsable` sur `departement.responsable_id`, `fk_demande_employe_remplace` sur `demande_recrutement.employe_remplace_id`.
3. Contraintes : unicité `matricule`, `LOWER(email_pro)` partielle (non nul), `candidature_origine_id` unique, `ck_affectation_taux (taux_occupation > 0 AND taux_occupation <= 100)`, `ck_affectation_dates (date_fin IS NULL OR date_fin >= date_debut)`, CHECK sur statut et motif. Index : `idx_affectation_employe`, `idx_affectation_poste`, `idx_affectation_manager`.

## 9. Compléments dans les modules précédents

| Module | Complément autorisé |
|---|---|
| `organisation` | `DepartementService` : vérifier que `responsableId` désigne un employé existant. |
| `auth` | `UtilisateurService.creer/modifier` : vérifier l'existence de l'employé lié. |
| `demande` / `organisation` | Aucun changement de code : les adaptateurs de ports suffisent. |

## 10. Tests (squelettes désactivés)

`EmployeServiceTest` : création depuis une candidature (copie des champs, matricule), refus d'un second employé pour la même candidature, visibilité du manager. `AffectationServiceTest` : refus au-delà de `effectifAutorise`, cumul de taux > 100 refusé, `cloturerActives` ferme l'affectation précédente. `FicheSalariePdfServiceTest` : le PDF produit n'est pas vide.

## 11. Front

| Élément | Contenu |
|---|---|
| `api/` | `employes.js`, `affectations.js` |
| `views/employe/` | `EmployeListView`, `EmployeFormView`, `EmployeDetailView` (dossier : identité, affectations, contrats, onboarding, documents) |
| `components/employe/` | `EmployeForm`, `EmployeTable`, `EmployeCard`, `AffectationForm`, `AffectationHistory` (frise chronologique) |
| Routes | `/employes` : `[RH, MANAGER, PAIE]` ; création et modification : `[RH]` ; `/mon-dossier` : tout rôle authentifié (utilise `/api/employes/moi`) |
| Comportement | Bouton « Télécharger la fiche PDF » (RH) avec `utils/download.js`. Vue résumée pour manager et paie, complète pour la RH. |
