# Prompt Devin 04 : module `recrutement`

## Contexte

Lire dans l'ordre : `AGENTS.md`, `docs/GUIDE_PROJET.md`, `docs/modules/00-auth.md`, `docs/modules/04-recrutement.md`. La spécification `04-recrutement.md` est la source de vérité. Pour le métier : `docs/CONCEPTION.md`, sections 4.2, 8.2, 9.2.

Prérequis : modules `auth`, `common`, `organisation`, `demande` terminés **et implémentés** (en particulier `DemandeWorkflowService.marquerEnRecrutement` et `effectifRestantAPourvoir`, `DocumentService.enregistrer`).

## Mission

Générer le module `recrutement` : offres, candidats, candidatures, entretiens (3 niveaux), évaluations, décision finale.

## Fichiers à produire (niveau de génération)

| Fichiers | Niveau |
|---|---|
| `V5__recrutement.sql` | Complet : six tables, index uniques **partiels** (entretien actif par niveau, évaluation active par évaluateur), CHECK, clés étrangères nommées |
| Énumérations (6) | **Complet**, avec `transitionsAutorisees` / `peutPasserA` / `estTerminal` pour `StatutOffre`, `StatutCandidature`, `StatutEntretien` et `TypeEntretien.pourNiveau` |
| Entités (6) | Complet |
| Repositories (6) | Complet, avec les méthodes nécessaires (candidature par candidat et offre, entretiens par candidature et niveau, évaluations actives par entretien, nombre de candidatures par offre, rang maximal de liste d'attente par offre, somme des postes des offres actives d'une demande) |
| DTO : tous ceux de la section 4, y compris `IntervenantRequest`, `IntervenantResponse`, `CritereNote`, `AnnulationEntretienRequest`, les `UpdateRequest` | Complet |
| Mappers (5) | Complet (le mapper d'évaluation convertit `criteres` ↔ JSON avec le bean JSON de Spring Boot, Jackson 3) |
| `OffreEmploiService`, `CandidatService`, `CandidatureService`, `EntretienService`, `EvaluationCandidatService`, `SelectionWorkflowService` | **Squelettes** conformes à la section 5 (signatures exactes, y compris toutes les méthodes d'**API interne**, Javadoc avec codes de règle) |
| Contrôleurs (5) | Complets : routes et rôles de la section 6, dépôt de CV en `multipart`, pagination, Swagger |
| Tests : `SelectionWorkflowServiceTest` (existant), `CandidatureServiceTest`, `EvaluationCandidatServiceTest` | Squelettes `@Disabled` nommés d'après les scénarios de la section 9 |

## Points d'attention

- Toutes les énumérations portent leurs transitions exactement comme dans les tableaux de la spécification.
- L'**avis agrégé d'un niveau** (REC-06) est une règle de service : la documenter en Javadoc de `avisDuNiveau`.
- La décision finale est ouverte à `RH` et `MANAGER` ; la visibilité d'un manager est limitée (REC-09) : prévoir les méthodes de service qui reçoivent `CurrentUser`.
- `CandidatureRequest` contient **exactement un** de `candidatId` ou `candidat` : documenter la vérification dans le service.
- L'évaluation corrigée crée une **nouvelle ligne** ; l'ancienne passe `active = false`.
- Aucune référence au module `employe` ni à `embauche` : ces modules appelleront les méthodes d'API interne de `CandidatureService` et `OffreEmploiService`.
- Ne pas ajouter de clé de configuration.

## Vérifications

1. Recréer la base puis démarrer : migrations `V1` à `V5` (et suivantes vides) appliquées, schéma validé.
2. Swagger : les 33 endpoints de la section 6 sont présents avec la bonne sécurité.
3. Les index partiels de la migration sont bien créés (consulter `\d entretien` et `\d evaluation_candidat` dans `psql`).
4. Chaque règle `RG-14` à `RG-25`, `REC-01` à `REC-10` apparaît dans la Javadoc d'une méthode de service.

## Rapport final

Format de `AGENTS.md`, section 9.
