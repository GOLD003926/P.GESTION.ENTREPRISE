# Prompt Devin 07 : module `onboarding`

## Contexte

Lire dans l'ordre : `AGENTS.md`, `docs/GUIDE_PROJET.md`, `docs/modules/00-auth.md`, `docs/modules/07-onboarding.md`. La spécification `07-onboarding.md` est la source de vérité. Lire aussi la section 5 (événements) de `docs/modules/06-contrat.md`. Pour le métier : `docs/CONCEPTION.md`, sections 4.4 et 9.4.

Prérequis : modules `auth`, `common`, `employe`, `contrat` terminés **et implémentés** (les trois événements `ContratCreeEvent`, `ContratSigneEvent`, `ContratAnnuleEvent` existent et sont publiés).

## Mission

Générer le module `onboarding` : modèles de checklist, onboarding, tâches, écoute des événements du contrat, modèle par défaut.

## Fichiers à produire (niveau de génération)

| Fichiers | Niveau |
|---|---|
| `V8__onboarding.sql` | Complet : quatre tables, clés étrangères nommées (dont `fk_onboarding_contrat`), unicités, CHECK, **données de départ** : modèle « Intégration standard » et ses sept tâches (tableau de la section 8) |
| Énumérations : `StatutOnboarding`, `StatutTache` (avec transitions) | Complet |
| Entités (4) | Complet (`Onboarding.contratId` en `Long`, voir section 3.3) |
| Repositories (4) | Complet (onboarding par contrat et par employé, tâches par onboarding, tâches ouvertes par rôle, modèle actif par type de contrat, modèle actif sans type) |
| DTO : `ModeleTacheRequest`, `ModeleTacheResponse`, `ModeleOnboardingRequest`, `ModeleOnboardingResponse`, `OnboardingRequest`, `OnboardingResponse`, `OnboardingTacheRequest`, `OnboardingTacheResponse` | Complet |
| Mappers (3) | Complet (`OnboardingResponse` calcule `tachesObligatoiresFaites` et `tachesObligatoiresTotal` ; `OnboardingTacheResponse.enRetard` calculé) |
| `ContratEventListener` | **Complet** (trois méthodes `@EventListener` qui délèguent à `OnboardingService`, auteur `system`) |
| `ModeleOnboardingService`, `OnboardingService`, `OnboardingTacheService` | **Squelettes** conformes à la section 5 (signatures exactes, Javadoc avec codes de règle) |
| Contrôleurs : `ModeleOnboardingController`, `OnboardingController`, `OnboardingTacheController` | Complets : routes et rôles de la section 6 ; `GET /api/onboardings/mon-onboarding` déclaré **avant** `/{id}` |
| Tests : `OnboardingServiceTest` (existant), `OnboardingTacheServiceTest` | Squelettes `@Disabled` couvrant les scénarios de la section 9 |

## Points d'attention

- L'onboarding est créé **uniquement** par l'événement `ContratCreeEvent` ; aucun endpoint de création directe.
- Les tâches d'un onboarding sont des **copies** des tâches du modèle (ONB-03) : modifier un modèle n'affecte pas les onboardings existants.
- Aucune dépendance de code du module `contrat` vers `onboarding`. `onboarding` importe uniquement les classes d'événements de `contrat.event`.
- La clôture de l'onboarding (ONB-06) ne dépend que des tâches **obligatoires**.
- Les données de départ doivent être dans la migration (pas dans un `ApplicationRunner`).
- Ne pas ajouter de clé de configuration.

## Vérifications

1. Recréer la base puis démarrer : migrations appliquées, schéma validé ; `SELECT count(*) FROM modele_tache` retourne 7 ; un seul modèle actif sans type de contrat.
2. Swagger : les 16 endpoints de la section 6.
3. Le listener est bien enregistré (bean `ContratEventListener` présent au démarrage).
4. Chaque règle `RG-42` à `RG-44`, `ONB-01` à `ONB-10` apparaît dans la Javadoc d'une méthode de service.

## Rapport final

Format de `AGENTS.md`, section 9.
