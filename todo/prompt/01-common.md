# Prompt Devin 01 : module `common` (audit, documents, notifications, sécurité, numérotation)

## Contexte

Lire dans l'ordre : `AGENTS.md`, `docs/GUIDE_PROJET.md`, `docs/modules/00-auth.md`, `docs/modules/01-common.md`. La spécification `01-common.md` est la source de vérité.

Dépôt : `sirh-api`, package `com.gntfh.sirh_api`. Le module `auth` et les fichiers `common/config`, `common/exception`, `common/dto`, `common/entity` sont **terminés et gelés**.

## Mission

Générer les fondations transverses décrites dans `docs/modules/01-common.md` : audit (historique de statuts, journal), documents (stockage, versionnement), notifications, `CurrentUser`, numérotation par séquences. Compléter `SchedulingConfig`.

## Étape 0 : renommage des migrations

Dans `src/main/resources/db/migration`, exécuter **dans cet ordre** (voir guide, section 7.2) :

```bash
mv V7__onboarding.sql V8__onboarding.sql
mv V6__contrat.sql V7__contrat.sql
mv V5__embauche_employe.sql V6__employe.sql
mv V4__recrutement.sql V5__recrutement.sql
mv V3__demande_recrutement.sql V4__demande_recrutement.sql
mv V8__transverse.sql V3__transverse.sql
echo "-- V9 : embauche" > V9__embauche.sql
```

Vérifier ensuite que les fichiers sont `V1__organisation`, `V2__auth` (rempli, ne pas modifier), `V3__transverse`, `V4__demande_recrutement`, `V5__recrutement`, `V6__employe`, `V7__contrat`, `V8__onboarding`, `V9__embauche`.

## Fichiers à produire (niveau de génération)

| Fichiers | Niveau |
|---|---|
| `V3__transverse.sql` | Complet : quatre tables, quatre séquences, index, contraintes, clé étrangère de `notification` |
| Énumérations : `TypeEntite`, `TypeDocument`, `ActionAudit`, `CanalNotification`, `TypeNotification`, `SequenceNumero` | Complet |
| Entités : `Document`, `HistoriqueStatut`, `JournalAudit`, `Notification` | Complet |
| Repositories (4) | Complet, avec les méthodes de recherche et de comptage nécessaires aux services (versionnement de document : dernier numéro de version ; notifications : comptage des non lues ; journal : `Specification` ou requête à filtres facultatifs) |
| DTO : `HistoriqueStatutResponse`, `JournalAuditResponse`, `DocumentResponse`, `NotificationResponse`, `NotificationCompteurResponse`, `ContenuDocument` | Complet |
| `CurrentUser` | **Complet** (record + `from(Jwt)`) |
| `NumeroService` | **Complet** (requête native `nextval` via `JdbcClient`, format `<préfixe>-<année>-<0000>`) |
| `FileStorageService` | **Complet** (écriture, lecture, protection contre la sortie du dossier de stockage, nettoyage du nom de fichier) |
| `DocumentService`, `HistoriqueStatutService`, `AuditService`, `NotificationService` | **Squelettes** conformes à la section 5 de la spécification (signatures exactes, Javadoc, `UnsupportedOperationException("TODO COM-xx")`) |
| Contrôleurs : `DocumentController`, `AuditController`, `NotificationController` | Complets (routes, rôles, Swagger, téléchargement avec `Content-Disposition`, dépôt `multipart`) |
| `SchedulingConfig` | `@Configuration @EnableScheduling` |
| Tests : `DocumentServiceTest`, `NumeroServiceTest`, `NotificationServiceTest` | Squelettes `@Disabled` |

Mappers : non nécessaires (conversions courtes dans les services ou méthodes statiques `from` des DTO) ; si un mapper est créé, le placer dans le dossier `mapper` du sous-module.

## Points d'attention

- `HistoriqueStatut` et `JournalAudit` : **insertion seule**, pas de setters, aucune route de modification ni de suppression.
- `Document.entiteId` et `entiteType` : lien logique, **sans clé étrangère**. Le champ de numéro de version s'appelle `numeroVersion` (colonne `numero_version`) car `version` est le verrou optimiste.
- `NotificationService.notifierParRole` utilise `UtilisateurRepository` du module `auth` : seule dépendance autorisée de `common` vers `auth`.
- `valeursAvant` / `valeursApres` du journal sont du texte JSON ; le bean JSON de Spring Boot 4 est basé sur Jackson 3 (packages `tools.jackson.*`) : vérifier les imports à la compilation.
- Aucune nouvelle clé de configuration n'est nécessaire ; ne pas modifier `SirhProperties` ni `application.properties`.

## Vérifications

1. Recréer la base (`dropdb --force` puis `createdb`), puis `./mvnw spring-boot:run` : Flyway applique `V1` à `V9` sans erreur, Hibernate valide le schéma.
2. Swagger : les endpoints `/api/documents`, `/api/audit/*`, `/api/notifications*` sont listés, avec cadenas.
3. `GET /api/notifications/compteur` avec un jeton valide répond (la méthode lèvera `UnsupportedOperationException` tant que le service n'est pas implémenté : acceptable, mais **ne doit pas empêcher le démarrage**).

## Rapport final

Format de `AGENTS.md`, section 9.
