# Prompt Devin 08 : module `embauche`

## Contexte

Lire dans l'ordre : `AGENTS.md`, `docs/GUIDE_PROJET.md`, `docs/modules/00-auth.md`, `docs/modules/08-embauche.md`. La spécification `08-embauche.md` est la source de vérité. Lire aussi les sections « API interne » des spécifications `03-demande.md`, `04-recrutement.md`, `05-employe.md`, `06-contrat.md` : ce module les appelle toutes. Pour le métier : `docs/CONCEPTION.md`, sections 4.3, 8.3, 9.3.

Prérequis : **tous** les modules précédents terminés **et implémentés** : `CandidatureService` (transitions), `OffreEmploiService.cloturerSiEffectifAtteint`, `DemandeWorkflowService.enregistrerEmbauche`, `EmployeService.creerDepuisCandidature`, `FicheSalariePdfService`, `ContratService.creerContratInitial`.

## Mission

Générer le module `embauche` : proposition d'embauche, dérogation de salaire, réponse du candidat (accepter, refuser, demander un délai), expiration automatique, et l'orchestration atomique de l'acceptation.

## Fichiers à produire (niveau de génération)

| Fichiers | Niveau |
|---|---|
| `V9__embauche.sql` | Complet : table `proposition_embauche`, clés étrangères nommées, index unique partiel (une proposition active par candidature), CHECK |
| Énumérations : `StatutProposition` (avec `estActive`, transitions), `ReponseCandidat` | Complet |
| Entité `PropositionEmbauche` | Complet |
| `PropositionEmbaucheRepository` | Complet (`JpaSpecificationExecutor` ; méthodes : proposition active d'une candidature, propositions `ENVOYEE` ou `DELAI_ACCORDE` dont la date limite est dépassée) |
| DTO : `PropositionEmbaucheRequest`, `PropositionEmbaucheUpdateRequest`, `DerogationRequest`, `ReponsePropositionRequest`, `PropositionEmbaucheResponse`, `ReponsePropositionResultat` | Complet |
| `PropositionEmbaucheMapper` | Complet (`salaireMinGrille` et `salaireMaxGrille` issus de la grille du poste ; `derogationValidee` déduit de `derogationValideePar`) |
| `PropositionPdfService` | **Complet** (PDF OpenPDF décrit en 5.1 ; la dépendance est déjà au `pom.xml` depuis le module `employe`) |
| `PropositionEmbaucheService`, `EmbaucheService` | **Squelettes** conformes à 5.2 et 5.3, avec l'**algorithme d'acceptation** en Javadoc, étape par étape (8 étapes) et la mention « transaction unique : toute erreur annule l'ensemble » |
| `PropositionExpirationScheduler` | **Complet** (`@Scheduled` quotidien 00:10, délégation à `EmbaucheService.expirerPropositionsEchues`) |
| `PropositionEmbaucheController` | Complet : routes et rôles de la section 6 |
| Tests : `EmbaucheServiceTest` (existant), `PropositionEmbaucheServiceTest` | Squelettes `@Disabled` couvrant les scénarios de la section 9 |

## Points d'attention

- `EmbaucheService.accepter` est la **seule** méthode du projet qui orchestre plusieurs modules : elle n'appelle que des méthodes d'**API interne** documentées dans les spécifications (jamais un repository d'un autre module).
- Le PDF de la proposition est enregistré via `DocumentService.enregistrer(byte[], ...)` ; aucun endpoint de téléchargement dédié (`/api/documents/{id}/contenu` suffit).
- Le type de contrat créé est `typeContratPropose` (défaut `ESSAI`, RG-33).
- Pas de portail candidat : la réponse est saisie par la RH (EMB-06).
- Ne pas ajouter de clé de configuration : `sirh.proposition.*` existe déjà.

## Vérifications

1. Recréer la base puis démarrer : migrations `V1` à `V9` appliquées, schéma validé, index partiel présent (`\d proposition_embauche`).
2. Swagger : les 9 endpoints de la section 6.
3. Le code du module compile **sans** import d'un `repository` d'un autre module (vérifier par recherche dans `embauche/`).
4. Chaque règle `RG-26` à `RG-33`, `EMB-01` à `EMB-08` apparaît dans la Javadoc d'une méthode de service.

## Rapport final

Format de `AGENTS.md`, section 9.
