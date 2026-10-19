# Module `onboarding` : modèles de checklist, onboarding, tâches

Bloc 4 du sujet : intégration du nouveau salarié par checklist et workflow. Migration : `V8__onboarding.sql`. Dépend de : `common`, `auth`, `employe`, `contrat` (via événements).

## 1. Fichiers

Existants (vides) :

```
onboarding/entity/{ModeleOnboarding, ModeleTache, Onboarding, OnboardingTache}
onboarding/enums/{StatutOnboarding, StatutTache}
onboarding/repository/{ModeleOnboarding, ModeleTache, Onboarding, OnboardingTache}Repository
onboarding/dto/{ModeleOnboarding, Onboarding, OnboardingTache}{Request, Response}
onboarding/mapper/{ModeleOnboarding, Onboarding, OnboardingTache}Mapper
onboarding/service/{ModeleOnboardingService, OnboardingService, OnboardingTacheService}
onboarding/controller/{ModeleOnboardingController, OnboardingController, OnboardingTacheController}
```

À créer : `dto/ModeleTacheRequest`, `dto/ModeleTacheResponse`, `listener/ContratEventListener`.

## 2. Énumérations

| Énumération | Valeurs | Transitions |
|---|---|---|
| `StatutOnboarding` | `A_DEMARRER`, `EN_COURS`, `TERMINE`, `ANNULE` | `A_DEMARRER` → `EN_COURS`, `ANNULE` ; `EN_COURS` → `TERMINE`, `ANNULE` ; `TERMINE`, `ANNULE` terminaux |
| `StatutTache` | `A_FAIRE`, `EN_COURS`, `FAIT`, `NON_APPLICABLE` | `A_FAIRE` → `EN_COURS`, `FAIT`, `NON_APPLICABLE` ; `EN_COURS` → `FAIT`, `NON_APPLICABLE` ; `FAIT`, `NON_APPLICABLE` terminaux. `NON_APPLICABLE` interdit pour une tâche obligatoire. |

Les rôles responsables utilisent l'énumération `auth.enums.Role` (valeurs utilisées : `RH`, `IT_SERVICES`, `PAIE`, `MANAGER`).

## 3. Entités (`AuditableEntity`)

### 3.1 `ModeleOnboarding` (table `modele_onboarding`)

| Attribut | Colonne | Type | Null | Règle |
|---|---|---|---|---|
| `nom` | `nom` | `VARCHAR(100)` | Non | Unique insensible à la casse |
| `typeContrat` | `type_contrat` | `VARCHAR(10)` | Oui | `ESSAI`, `CDD`, `CDI` ou nul (modèle par défaut). Colonne texte avec CHECK ; la valeur est comparée au `TypeContrat` sans dépendance de code vers le module `contrat` |
| `actif` | `actif` | `BOOLEAN` | Non | Défaut `true` |

### 3.2 `ModeleTache` (table `modele_tache`)

| Attribut | Colonne | Type | Null | Règle |
|---|---|---|---|---|
| `modele` | `modele_id` | `BIGINT` | Non | `@ManyToOne` LAZY, clé étrangère |
| `ordre` | `ordre` | `INT` | Non | ≥ 1 ; unique par modèle |
| `code` | `code` | `VARCHAR(40)` | Non | Majuscules ; unique par modèle |
| `libelle` | `libelle` | `VARCHAR(200)` | Non | |
| `obligatoire` | `obligatoire` | `BOOLEAN` | Non | |
| `roleResponsable` | `role_responsable` | `VARCHAR(30)` | Non | `Role` |
| `echeanceJours` | `echeance_jours` | `INT` | Non | Relatif à la date de début du contrat (négatif = avant) |

### 3.3 `Onboarding` (table `onboarding`)

| Attribut | Colonne | Type | Null | Règle |
|---|---|---|---|---|
| `employe` | `employe_id` | `BIGINT` | Non | `@ManyToOne` LAZY vers `Employe`, clé étrangère |
| `contratId` | `contrat_id` | `BIGINT` | Non | `Long` **avec** clé étrangère SQL vers `contrat`, unique (un onboarding par contrat). Attribut `Long` : le module `contrat` est en amont et l'entité n'est pas nécessaire |
| `modele` | `modele_id` | `BIGINT` | Non | `@ManyToOne` LAZY |
| `responsableRh` | `responsable_rh_id` | `BIGINT` | Oui | `@ManyToOne` LAZY vers `Utilisateur` |
| `dateDebut` | `date_debut` | `DATE` | Non | Date de début du contrat |
| `dateCible` | `date_cible` | `DATE` | Non | Dernière échéance de tâche |
| `statut` | `statut` | `VARCHAR(20)` | Non | `StatutOnboarding`, défaut `A_DEMARRER` |
| `dateCloture` | `date_cloture` | `TIMESTAMPTZ` | Oui | |

### 3.4 `OnboardingTache` (table `onboarding_tache`)

| Attribut | Colonne | Type | Null | Règle |
|---|---|---|---|---|
| `onboarding` | `onboarding_id` | `BIGINT` | Non | `@ManyToOne` LAZY |
| `modeleTache` | `modele_tache_id` | `BIGINT` | Oui | `@ManyToOne` LAZY |
| `code` | `code` | `VARCHAR(40)` | Non | Copie du modèle |
| `libelle` | `libelle` | `VARCHAR(200)` | Non | Copie du modèle |
| `obligatoire` | `obligatoire` | `BOOLEAN` | Non | Copie du modèle |
| `roleResponsable` | `role_responsable` | `VARCHAR(30)` | Non | `Role` |
| `assigneA` | `assigne_a_id` | `BIGINT` | Oui | `@ManyToOne` LAZY vers `Utilisateur` |
| `echeance` | `echeance` | `DATE` | Non | `dateDebut + echeanceJours` |
| `statut` | `statut` | `VARCHAR(20)` | Non | `StatutTache`, défaut `A_FAIRE` |
| `dateRealisation` | `date_realisation` | `TIMESTAMPTZ` | Oui | |
| `preuveDocument` | `preuve_document_id` | `BIGINT` | Oui | `@ManyToOne` LAZY vers `Document` |
| `commentaire` | `commentaire` | `TEXT` | Oui | |

Index : `idx_onboarding_tache_onboarding`, `idx_onboarding_tache_role_statut`.

## 4. DTO

| DTO | Champs et validations |
|---|---|
| `ModeleTacheRequest` | `ordre` (≥ 1), `code` (obligatoire, ≤ 40, motif `[A-Z0-9_]+`), `libelle` (obligatoire, ≤ 200), `obligatoire` (obligatoire), `roleResponsable` (obligatoire, parmi `RH`, `IT_SERVICES`, `PAIE`, `MANAGER`), `echeanceJours` (entre −60 et 365) |
| `ModeleTacheResponse` | mêmes champs + `id` |
| `ModeleOnboardingRequest` | `nom` (obligatoire, ≤ 100), `typeContrat` (facultatif : `ESSAI`, `CDD`, `CDI`), `taches` (au moins 1, `@Valid`) |
| `ModeleOnboardingResponse` | `id`, `nom`, `typeContrat`, `actif`, `taches`, `createdAt` |
| `OnboardingRequest` | `responsableRhId` (facultatif) |
| `OnboardingResponse` | `id`, `employeId`, `employeNom`, `matricule`, `contratId`, `modeleId`, `responsableRhId`, `dateDebut`, `dateCible`, `statut`, `dateCloture`, `tachesObligatoiresFaites`, `tachesObligatoiresTotal`, `taches` |
| `OnboardingTacheRequest` | `commentaire` (≤ 2000), `preuveDocumentId`, `assigneAId` |
| `OnboardingTacheResponse` | `id`, `onboardingId`, `employeId`, `employeNom`, `matricule`, `code`, `libelle`, `obligatoire`, `roleResponsable`, `assigneAId`, `echeance`, `statut`, `dateRealisation`, `preuveDocumentId`, `commentaire`, `enRetard` (échéance dépassée et tâche non terminée) |

## 5. Services

### 5.1 `ModeleOnboardingService`

| Méthode | Règle |
|---|---|
| `creer(ModeleOnboardingRequest, CurrentUser)` | ONB-01, ONB-02 |
| `modifier(Long id, ModeleOnboardingRequest, CurrentUser)` | Remplace les tâches du modèle ; n'affecte pas les onboardings déjà créés (ONB-03) |
| `obtenir(Long id)`, `rechercher(String q, Boolean actif, Pageable)` | Tri : `id`, `nom`, `actif` |
| `activer(Long id)`, `desactiver(Long id)` | |
| `ModeleOnboarding choisirPour(String typeContrat)` | **API interne** : modèle actif de ce type ; à défaut, modèle actif sans type ; à défaut `BusinessRuleException` |

### 5.2 `OnboardingService`

| Méthode | Type | Règle |
|---|---|---|
| `Onboarding creerPourContrat(Long contratId, Long employeId, String typeContrat, LocalDate dateDebut, String auteur)` | Interne au module (appelée par le listener) | RG-42 : choisit le modèle, crée l'onboarding `A_DEMARRER`, génère les tâches (copie, échéance = `dateDebut + echeanceJours`), calcule `dateCible`, notifie les utilisateurs du rôle de chaque tâche (`ONBOARDING_TACHE_ASSIGNEE`), historique |
| `void marquerContratSigne(Long contratId, String auteur)` | Interne au module | Tâche `CONTRAT_SIGNE` → `FAIT` |
| `void annulerPourContrat(Long contratId, String auteur)` | Interne au module | Onboarding → `ANNULE` |
| `obtenir(Long id, CurrentUser)`, `rechercher(Long employeId, StatutOnboarding statut, Pageable, CurrentUser)` | Contrôleur | ONB-08. Tri : `id`, `dateDebut`, `dateCible`, `statut` |
| `monOnboarding(CurrentUser)` | Contrôleur | Onboarding le plus récent de l'employé lié à l'utilisateur ; lecture seule |
| `modifier(Long id, OnboardingRequest, CurrentUser)` | Contrôleur | Désignation du responsable RH |
| `void verifierCloture(Onboarding, String auteur)` | Interne au module | ONB-06 |
| `historique(Long id, CurrentUser)` | Contrôleur | |

### 5.3 `OnboardingTacheService`

| Méthode | Règle |
|---|---|
| `PageResponse<OnboardingTacheResponse> mesTaches(StatutTache statut, Pageable, CurrentUser)` | Tâches ouvertes dont le rôle responsable est celui de l'utilisateur ; la RH voit toutes les tâches. Tri : `echeance`, `statut`, `code` |
| `demarrer(Long id, OnboardingTacheRequest, CurrentUser)` | `A_FAIRE` → `EN_COURS` ; l'onboarding passe `EN_COURS` s'il était `A_DEMARRER` |
| `terminer(Long id, OnboardingTacheRequest, CurrentUser)` | ONB-04, ONB-05 : → `FAIT`, `dateRealisation`, preuve facultative ; appelle `verifierCloture` |
| `marquerNonApplicable(Long id, OnboardingTacheRequest, CurrentUser)` | ONB-07 : RH, tâche facultative uniquement |
| `assigner(Long id, OnboardingTacheRequest, CurrentUser)` | RH ; l'utilisateur désigné est actif et a le rôle responsable |

### 5.4 `ContratEventListener`

Méthodes `@EventListener` sur `ContratCreeEvent` (→ `creerPourContrat`), `ContratSigneEvent` (→ `marquerContratSigne`), `ContratAnnuleEvent` (→ `annulerPourContrat`). Auteur : `system`.

## 6. Endpoints

| Méthode | Route | Rôles | Description |
|---|---|---|---|
| GET | `/api/modeles-onboarding` | RH, ADMIN | Liste |
| GET | `/api/modeles-onboarding/{id}` | RH, ADMIN | Détail |
| POST | `/api/modeles-onboarding` | RH, ADMIN | Création (201) |
| PUT | `/api/modeles-onboarding/{id}` | RH, ADMIN | Modification |
| POST | `/api/modeles-onboarding/{id}/activer` | RH, ADMIN | Activation |
| POST | `/api/modeles-onboarding/{id}/desactiver` | RH, ADMIN | Désactivation |
| GET | `/api/onboardings` | RH, MANAGER | Liste (`employeId`, `statut`) |
| GET | `/api/onboardings/{id}` | RH, MANAGER | Détail avec tâches |
| GET | `/api/onboardings/mon-onboarding` | SALARIE | Mon onboarding (lecture seule) |
| PUT | `/api/onboardings/{id}` | RH | Désigner le responsable RH |
| GET | `/api/onboardings/{id}/historique` | RH | Historique des statuts |
| GET | `/api/onboarding-taches/mes-taches` | RH, MANAGER, IT_SERVICES, PAIE | Tâches à traiter |
| POST | `/api/onboarding-taches/{id}/demarrer` | RH, MANAGER, IT_SERVICES, PAIE | Démarrage |
| POST | `/api/onboarding-taches/{id}/terminer` | RH, MANAGER, IT_SERVICES, PAIE | Terminer |
| POST | `/api/onboarding-taches/{id}/non-applicable` | RH | Non applicable (facultative) |
| POST | `/api/onboarding-taches/{id}/assigner` | RH | Assigner à un utilisateur |

## 7. Règles de gestion

| Code | Règle | Erreur |
|---|---|---|
| RG-42 | L'onboarding est créé automatiquement avec le contrat, avec les tâches du modèle correspondant. | — |
| RG-43 | Obligatoires : contrat signé, compte, badge, inscription en paie. Facultatives : compte bancaire, matériel, formations. | — |
| RG-44 / ONB-06 | L'onboarding passe `TERMINE` quand **toutes** les tâches obligatoires sont `FAIT` ; `dateCloture` renseignée ; historique. | — |
| ONB-01 | Nom de modèle unique sans tenir compte de la casse. | 409 |
| ONB-02 | Un modèle contient au moins une tâche ; `ordre` et `code` uniques dans le modèle. | 422 |
| ONB-03 | Modifier un modèle ne change pas les onboardings existants (les tâches sont copiées). | — |
| ONB-04 | Une tâche n'est traitée que par le rôle responsable (la RH peut tout traiter). | 403 |
| ONB-05 | Une tâche `FAIT` n'est plus modifiable. | 422 |
| ONB-07 | `NON_APPLICABLE` uniquement pour une tâche facultative. | 422 |
| ONB-08 | Visibilité : RH tout ; manager : onboardings de ses employés ; salarié : le sien en lecture seule. Hors périmètre : 404. | 404 |
| ONB-09 | La tâche `CONTRAT_SIGNE` est passée à `FAIT` automatiquement à l'enregistrement de la signature. | — |
| ONB-10 | L'annulation d'un contrat annule son onboarding. | — |

## 8. Migration `V8__onboarding.sql`

Tables `modele_onboarding`, `modele_tache`, `onboarding`, `onboarding_tache`. Clés étrangères nommées, y compris `fk_onboarding_contrat` vers `contrat`. Unicités : `LOWER(nom)`, `(modele_id, ordre)`, `(modele_id, code)`, `onboarding.contrat_id`. CHECK sur statuts, `type_contrat`, `role_responsable`.

**Données de départ** : un modèle actif « Intégration standard » (`type_contrat` nul) et ses sept tâches :

| Ordre | Code | Libellé | Obligatoire | Responsable | Échéance (jours) |
|---|---|---|---|---|---|
| 1 | `CONTRAT_SIGNE` | Contrat signé et archivé | Oui | `RH` | −3 |
| 2 | `PAIE` | Inscription en paie | Oui | `PAIE` | −3 |
| 3 | `COMPTE_SI` | Création des comptes (email, applications) | Oui | `IT_SERVICES` | −2 |
| 4 | `BADGE` | Remise du badge | Oui | `IT_SERVICES` | 0 |
| 5 | `MATERIEL` | Matériel nécessaire | Non | `IT_SERVICES` | −1 |
| 6 | `COMPTE_BANCAIRE` | Ouverture du compte bancaire | Non | `RH` | 15 |
| 7 | `FORMATION` | Formations d'intégration | Non | `MANAGER` | 30 |

## 9. Tests (squelettes désactivés)

`OnboardingServiceTest` (existant) : génération des sept tâches avec les bonnes échéances, choix du modèle par type puis repli sur le modèle par défaut, clôture quand les obligatoires sont faites (les facultatives ouvertes n'empêchent pas la clôture), annulation avec le contrat. `OnboardingTacheServiceTest` : mauvais rôle refusé, `NON_APPLICABLE` refusé sur une tâche obligatoire, tâche `FAIT` non modifiable.

## 10. Front

| Élément | Contenu |
|---|---|
| `api/` | `onboardings.js`, `modelesOnboarding.js`, `onboardingTaches.js` |
| `views/onboarding/` | `OnboardingListView`, `OnboardingDetailView`, `MesTachesView`, `ModeleOnboardingListView`, `ModeleOnboardingFormView` |
| `components/onboarding/` | `OnboardingChecklist`, `OnboardingTacheItem`, `OnboardingProgress`, `OnboardingTable`, `ModeleOnboardingForm`, `ModeleTacheEditor` |
| Routes | `/onboardings` : `[RH, MANAGER]` ; `/mes-taches` : `[RH, MANAGER, IT_SERVICES, PAIE]` ; `/mon-onboarding` : `[SALARIE]` ; `/modeles-onboarding` : `[RH, ADMIN]` |
| Comportement | `MesTachesView` est l'écran principal d'IT, de la paie et des services généraux. Les tâches en retard sont signalées. Le salarié voit sa checklist en lecture seule. |
