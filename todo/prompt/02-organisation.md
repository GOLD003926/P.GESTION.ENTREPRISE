# Prompt Devin 02 : module `organisation`

## Contexte

Lire dans l'ordre : `AGENTS.md`, `docs/GUIDE_PROJET.md`, `docs/modules/00-auth.md`, `docs/modules/02-organisation.md`. La spécification `02-organisation.md` est la source de vérité.

Prérequis : modules `auth` et `common` terminés **et implémentés** (en particulier `HistoriqueStatutService`, `AuditService`, `CurrentUser`).

## Mission

Générer le module `organisation` : départements, grilles salariales, postes.

## Fichiers à produire (niveau de génération)

| Fichiers | Niveau |
|---|---|
| `V1__organisation.sql` | Complet (tables `grille_salariale`, `departement`, `poste` ; colonne `departement.responsable_id` **sans** clé étrangère) |
| Entités : `GrilleSalariale`, `Departement`, `Poste` | Complet |
| Repositories (3) | Complet (`JpaSpecificationExecutor` ; méthodes : existence et recherche par code insensible à la casse, existence de sous-départements actifs, nombre de postes actifs par département et par grille) |
| DTO : `*Request`, `*UpdateRequest`, `*Response` pour les trois entités | Complet, messages de validation en français |
| Mappers (3) | Complet (`PosteMapper.toResponse` reçoit `occupationActuelle` en paramètre) |
| `PosteOccupationProvider` (interface du port) | Complet |
| `GrilleSalarialeService`, `DepartementService`, `PosteService` | **Squelettes** conformes à la section 5 (signatures exactes, Javadoc avec codes de règle, `UnsupportedOperationException("TODO ORG-xx")`). `PosteService` injecte le port avec `ObjectProvider<PosteOccupationProvider>` |
| Contrôleurs (3) | Complets : routes, `@PreAuthorize` selon le tableau de la section 6, `@Tag`, `@Operation`, pagination |
| Tests : `PosteServiceTest` (existant), `DepartementServiceTest`, `GrilleSalarialeServiceTest` | Squelettes `@Disabled` |

## Points d'attention

- Le `code` est **immuable** : il figure dans les `Request` de création uniquement.
- Écriture départements et grilles : `RH`, `ADMIN` ; écriture postes : `RH` ; lecture : `RH`, `MANAGER`, `FINANCE`, `DG`, `ADMIN`.
- Aucun accès au module `employe` (non encore généré) : l'occupation passe exclusivement par le port.
- Tri de liste : liste blanche par service (section 5 de la spécification).
- Ne pas ajouter de clé de configuration.

## Vérifications

1. Recréer la base puis démarrer : Flyway applique `V1` et suivantes, Hibernate valide le schéma.
2. Swagger : les 18 endpoints de la section 6 sont présents avec la bonne sécurité.
3. Vérifier par lecture de code que chaque règle `RG-01`, `RG-02`, `ORG-01` à `ORG-08` apparaît dans la Javadoc d'une méthode de service.

## Rapport final

Format de `AGENTS.md`, section 9.
