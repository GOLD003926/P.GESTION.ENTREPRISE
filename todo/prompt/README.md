# Prompts de génération pour Devin

Un prompt par module, à exécuter **dans l'ordre**. Chaque prompt renvoie à `AGENTS.md` (règles permanentes), à `docs/GUIDE_PROJET.md` (conventions) et à la spécification du module dans `docs/modules/`.

## Ordre d'exécution

| # | Prompt | Prérequis (modules terminés **et implémentés**) |
|---|---|---|
| 1 | `01-common.md` | `auth` |
| 2 | `02-organisation.md` | `common` |
| 3 | `03-demande.md` | `organisation` |
| 4 | `04-recrutement.md` | `demande` |
| 5 | `05-employe.md` | `recrutement` |
| 6 | `06-contrat.md` | `employe` |
| 7 | `07-onboarding.md` | `contrat` |
| 8 | `08-embauche.md` | `onboarding` (et tous les précédents) |

La génération produit des **squelettes de services** : un module suivant dépend de l'**interface** (signatures) du précédent, mais l'équipe doit avoir implémenté les services d'un module avant de tester le suivant de bout en bout.

## Mode d'emploi

1. Créer une branche : `git checkout -b feature/<module>-generation`.
2. Si une migration doit être recréée : recréer la base locale (`dropdb --force` puis `createdb`, voir guide, section 3.1).
3. Donner à Devin **le contenu du prompt** du module (copier-coller) en précisant la branche.
4. Attendre le rapport final (format de `AGENTS.md`, section 9) et vérifier chaque point par soi-même : compilation, démarrage, Swagger.
5. Relire la demande de fusion : une personne autre que l'auteur du lancement.
6. Traiter les « questions ouvertes » du rapport avant d'implémenter les services.

## Principes

- Un module à la fois : jamais deux prompts dans la même session.
- Le rapport final de Devin n'est pas une preuve : exécuter les vérifications.
- Toute divergence entre le code généré et la spécification se corrige dans le **code** ; si la spécification est fautive, la corriger dans `docs/modules/` **avant** de relancer.
- Après implémentation des services, mettre à jour le tableau d'avancement du guide (section 2).
