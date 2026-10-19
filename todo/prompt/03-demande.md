# Prompt Devin 03 : module `demande`

## Contexte

Lire dans l'ordre : `AGENTS.md`, `docs/GUIDE_PROJET.md`, `docs/modules/00-auth.md`, `docs/modules/03-demande.md`. La spécification `03-demande.md` est la source de vérité. Pour la compréhension métier : `docs/CONCEPTION.md`, sections 4.1, 8.1 et 9.1.

Prérequis : modules `auth`, `common`, `organisation` terminés **et implémentés**.

## Mission

Générer le module `demande` : demande de recrutement et chaîne de validation RH → Finance → DG.

## Fichiers à produire (niveau de génération)

| Fichiers | Niveau |
|---|---|
| `V4__demande_recrutement.sql` | Complet (tables `demande_recrutement`, `validation` ; `employe_remplace_id` **sans** clé étrangère) |
| Énumérations : `TypeDemande`, `MotifDemande`, `StatutDemande`, `DecisionValidation`, `RoleValidateur` | **Complet**, y compris `estValidePour`, `transitionsAutorisees`, `peutPasserA`, `estTerminal`, `estEnValidation`, `ordre`, `versRole`, `statutEnValidation` (table des transitions de la section 2) |
| Entités : `DemandeRecrutement`, `Validation` | Complet |
| Repositories (2) | Complet (`JpaSpecificationExecutor` pour la demande ; méthodes : validation `EN_ATTENTE` d'un cycle, validations par demande triées, somme de l'effectif restant des demandes en cours d'un poste hors une demande donnée, demandes à l'étape courante d'un rôle) |
| DTO : `DemandeRecrutementRequest`, `DemandeRecrutementResponse`, `ValidationRequest`, `ValidationResponse`, `AnnulationRequest` | Complet |
| Mappers : `DemandeRecrutementMapper`, `ValidationMapper` | Complet (`etapeCourante` calculée à partir du statut) |
| Port `EmployeLookup` | Complet (interface) |
| `BudgetService` | **Complet** : la formule de la section 5.1 est entièrement spécifiée |
| `DemandeWorkflowService`, `DemandeRecrutementService`, `ValidationService` | **Squelettes** conformes aux sections 5.3 à 5.5, `UnsupportedOperationException("TODO DEM-xx / RG-xx")`. Les services injectent `EmployeLookup` avec `ObjectProvider` (valeurs par défaut décrites en 5.2) |
| Contrôleurs : `DemandeRecrutementController`, `ValidationController` | Complets (routes, rôles, Swagger, pagination ; l'acteur est `CurrentUser.from(jwt)`) |
| Tests : `DemandeWorkflowServiceTest`, `BudgetServiceTest` (existants), `ValidationServiceTest` (nouveau) | Squelettes `@Disabled`. `BudgetServiceTest` : un test complet de la formule avec des valeurs fixes est bienvenu |

## Points d'attention

- `departement` de la demande est **dérivé du poste** : il n'apparaît pas dans `DemandeRecrutementRequest`.
- `budgetEstime` n'apparaît jamais dans une requête.
- Les méthodes d'**API interne** (`marquerEnRecrutement`, `enregistrerEmbauche`, `effectifRestantAPourvoir`) doivent exister avec leur signature exacte et le commentaire `// API interne` : les modules suivants les appellent.
- `MesValidations` : l'endpoint s'appelle `/api/validations/en-attente`.
- Ne pas importer de classe du module `employe` (inexistant).
- Ne pas ajouter de clé de configuration : `SirhProperties.demande()` existe déjà.

## Vérifications

1. Recréer la base puis démarrer : migrations appliquées, schéma validé.
2. Swagger : les 10 endpoints de la section 6 sont présents avec la bonne sécurité.
3. Chaque règle `RG-03` à `RG-13`, `RG-24`, `DEM-01` à `DEM-08` apparaît dans la Javadoc d'une méthode de service.
4. Les énumérations de statut reproduisent exactement le tableau des transitions de la spécification.

## Rapport final

Format de `AGENTS.md`, section 9.
