# Prompt Devin 06 : module `contrat`

## Contexte

Lire dans l'ordre : `AGENTS.md`, `docs/GUIDE_PROJET.md`, `docs/modules/00-auth.md`, `docs/modules/06-contrat.md`. La spécification `06-contrat.md` est la source de vérité. Pour le métier : `docs/CONCEPTION.md`, sections 4.4, 4.5, 8.4.

Prérequis : modules `auth`, `common`, `organisation`, `employe` terminés **et implémentés** (en particulier `EmployeService.activer`, `AffectationService.creerInitiale`).

## Mission

Générer le module `contrat` : contrats successifs (essai, CDD, CDI), règles de durée et de succession, activation, alertes, événements à destination de `onboarding`.

## Fichiers à produire (niveau de génération)

| Fichiers | Niveau |
|---|---|
| `V7__contrat.sql` | Complet : table `contrat`, index unique partiel (un seul contrat `ACTIF` par employé), contraintes CHECK (dont `ck_contrat_fin_type`), puis `ALTER TABLE affectation ADD CONSTRAINT fk_affectation_contrat ...` |
| Énumérations : `TypeContrat` (avec `aDateFin`), `StatutContrat` (avec transitions), `MotifFinContrat` | Complet |
| Entité `Contrat` | Complet |
| `ContratRepository` | Complet (`JpaSpecificationExecutor` ; méthodes : contrat actif d'un employé, contrats `A_SIGNER` à activer à une date, contrats `ACTIF` de type `ESSAI`/`CDD` dont la fin prévue est avant une date et sans alerte envoyée, chaîne des contrats précédents) |
| DTO : `ContratRequest`, `ContratUpdateRequest`, `RenouvellementRequest`, `TransformationRequest`, `FinContratRequest`, `ContratResponse`, `ContratInitialCommande` | Complet |
| Événements : `ContratCreeEvent`, `ContratSigneEvent`, `ContratAnnuleEvent` | Complet (records) |
| `ContratMapper` | Complet |
| `ContratRuleValidator` | **Squelette** conforme à 6.1 avec Javadoc détaillée de **chaque règle** (CON-01 à CON-07, valeurs lues dans `SirhProperties.contrat()`). Cette classe porte les règles les plus sensibles : l'implémentation revient à l'équipe |
| `ContratService` | **Squelette** conforme à 6.2, y compris l'algorithme d'`activerSiPret` décrit en Javadoc étape par étape, et toutes les méthodes d'**API interne** |
| `ContratActivationScheduler`, `ContratAlerteScheduler` | **Complets** (`@Scheduled` quotidien, délégation à `ContratService`) |
| `ContratController` | Complet : routes et rôles de la section 7, signature en `multipart` (`fichier` + `dateSignature`), pagination |
| Tests : `ContratRuleValidatorTest` (existant), `ContratServiceTest` | Squelettes `@Disabled` couvrant les scénarios de la section 10 |

## Points d'attention

- Aucune dépendance vers le module `onboarding` (aval) : la communication passe **uniquement** par les trois événements publiés avec `ApplicationEventPublisher`.
- `renouveler` et `transformer` créent toujours un **nouveau** contrat `BROUILLON` ; ils ne modifient jamais le contrat existant.
- `Contrat.poste` et `Contrat.manager` existent pour créer l'affectation initiale (écart H12, documenté).
- Le plafond du CDD (H3) est concentré dans `ContratRuleValidator` ; ne dupliquer aucune valeur de durée dans les services.
- Planificateurs : classes minces ; aucune règle dedans.
- Ne pas ajouter de clé de configuration : `sirh.contrat.*` existe déjà.

## Vérifications

1. Recréer la base puis démarrer : migrations appliquées, schéma validé, `fk_affectation_contrat` existe (`\d affectation`), index partiel présent (`\d contrat`).
2. Swagger : les 12 endpoints de la section 7.
3. Les planificateurs sont enregistrés (journal de démarrage ou test de contexte) sans exécuter de logique non implémentée au démarrage.
4. Chaque règle `RG-33` à `RG-41`, `CON-01` à `CON-14` apparaît dans la Javadoc d'une méthode de `ContratService` ou `ContratRuleValidator`.

## Rapport final

Format de `AGENTS.md`, section 9.
