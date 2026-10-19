# Prompt Devin 05 : module `employe`

## Contexte

Lire dans l'ordre : `AGENTS.md`, `docs/GUIDE_PROJET.md`, `docs/modules/00-auth.md`, `docs/modules/05-employe.md`. La spécification `05-employe.md` est la source de vérité. Lire aussi les spécifications `02-organisation.md` (port `PosteOccupationProvider`) et `03-demande.md` (port `EmployeLookup`) : ce module en fournit les implémentations.

Prérequis : modules `auth`, `common`, `organisation`, `demande`, `recrutement` terminés **et implémentés**.

## Mission

Générer le module `employe` : employés, matricule, fiche PDF, affectations, adaptateurs de ports ; ajouter les clés étrangères différées vers `employe`.

## Dépendance Maven

Ajouter `com.github.librepdf:openpdf` au `pom.xml`. **Ne pas deviner la version** : consulter Maven Central, utiliser la dernière version stable, noter la version retenue dans le rapport. Si la version ne peut pas être vérifiée, ne pas modifier le `pom.xml`, laisser `FicheSalariePdfService` avec le corps `UnsupportedOperationException("TODO dépendance OpenPDF")` et signaler le blocage.

## Fichiers à produire (niveau de génération)

| Fichiers | Niveau |
|---|---|
| `V6__employe.sql` | Complet : tables `employe`, `affectation` ; clés étrangères différées `fk_utilisateur_employe`, `fk_departement_responsable`, `fk_demande_employe_remplace` ; contraintes et index de la section 8 |
| Énumérations : `StatutEmploye` (avec transitions), `MotifAffectation` | Complet |
| Entités : `Employe`, `Affectation` | Complet (`Affectation.contratId` en `Long` sans association) |
| Repositories (2) | Complet (affectations actives d'un poste et d'un employé à une date, employés encadrés par un manager, recherche à filtres facultatifs) |
| DTO : `EmployeRequest`, `EmployeUpdateRequest`, `EmployeResponse`, `EmployeResumeResponse`, `AffectationRequest`, `AffectationClotureRequest`, `AffectationResponse` | Complet |
| Mappers (2) | Complet |
| `MatriculeService` | **Complet** (délègue à `NumeroService`) |
| `FicheSalariePdfService` | **Complet** (génération du PDF décrite en 5.2), sous réserve de la dépendance OpenPDF |
| `EmployeService`, `AffectationService` | **Squelettes** conformes à 5.3 et 5.4 (signatures exactes, y compris toutes les méthodes d'**API interne**) |
| `AffectationOccupationAdapter`, `EmployeLookupAdapter` | **Complets** (`@Component`, délégation courte) |
| Contrôleurs : `EmployeController`, `AffectationController` | Complets : routes et rôles de la section 6, téléchargement du PDF avec `Content-Disposition`, `GET /api/employes/moi` déclaré **avant** `/{id}` pour éviter l'ambiguïté de route |
| Tests : `EmployeServiceTest`, `AffectationServiceTest`, `FicheSalariePdfServiceTest` | Squelettes `@Disabled` ; `FicheSalariePdfServiceTest` peut être complet |

## Compléments autorisés dans des modules gelés ou terminés

| Fichier | Complément |
|---|---|
| `auth/service/UtilisateurService` | Dans `creer` et `modifier`, si `employeId` est renseigné, vérifier l'existence de l'employé (`ResourceNotFoundException` sinon). Modification minimale, signalée dans le rapport. |
| `organisation/service/DepartementService` | Dans `creer` et `modifier`, si `responsableId` est renseigné, vérifier l'existence de l'employé. |

Aucune autre modification de module existant.

## Points d'attention

- Les deux adaptateurs sont les **seules** classes du module qui implémentent des interfaces d'un module amont.
- La visibilité (EMP-05) est une règle de service : un manager reçoit `EmployeResumeResponse`, la RH `EmployeResponse`.
- Un candidat n'est pas modifié lors de la création d'un employé (RG-32).
- Aucune dépendance vers `contrat` ou `onboarding` (modules suivants).
- Ne pas ajouter de clé de configuration.

## Vérifications

1. Recréer la base puis démarrer : migrations appliquées, schéma validé, les trois clés étrangères différées existent (`\d utilisateur`, `\d departement`, `\d demande_recrutement`).
2. Swagger : les 12 endpoints de la section 6.
3. Le port `PosteOccupationProvider` est bien fourni : `PosteService` ne retombe plus sur la valeur par défaut (vérifier que le bean existe au démarrage).
4. Chaque règle `RG-02`, `RG-31`, `RG-32`, `RG-41`, `RG-45`, `EMP-01` à `EMP-07` apparaît dans la Javadoc d'une méthode de service.

## Rapport final

Format de `AGENTS.md`, section 9, avec la version d'OpenPDF retenue.
