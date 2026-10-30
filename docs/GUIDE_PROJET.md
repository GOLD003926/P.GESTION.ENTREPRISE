# Guide du projet SIRH

Document de référence de l'équipe : rôle de chaque dossier, fichier et configuration, conventions de code, procédures de travail. Rédigé de façon neutre. À lire en entier avant toute contribution, puis à consulter comme un manuel.

Documents associés :

| Document | Contenu |
|---|---|
| `docs/CONCEPTION.md` | Processus métier (BPMN), modèle de données, règles de gestion RG-01 à RG-50, machines à états (copie de `Conception_SIRH.md`). |
| `docs/modules/*.md` | Spécification détaillée de chaque module : entités, attributs, relations, endpoints, règles, droits. |
| `docs/prompts/*.md` | Prompts de génération de code pour Devin, un par module. |
| `AGENTS.md` | Règles permanentes pour les agents IA (Devin). |

---

## Sommaire

1. Stack et prérequis
2. État d'avancement
3. Démarrage local
4. Architecture générale
5. API : arborescence et rôle de chaque élément
6. Fichier `application.properties`
7. Migrations Flyway
8. Conventions de code de l'API
9. Dépendances entre modules
10. Front-web : arborescence, rôle de chaque élément, conventions
11. Procédure : ajouter un module (API puis front)
12. Organisation d'équipe et Git
13. Décisions d'architecture et hypothèses à confirmer
14. Utilisation de Devin

---

## 1. Stack et prérequis

| Élément | Version / choix |
|---|---|
| Java | 21 (LTS) |
| Spring Boot | 4.1.1 (Spring Framework 7, Hibernate 7, Spring Security 7) |
| Base de données | PostgreSQL 16 |
| Migrations | Flyway (`spring-boot-starter-flyway` + `flyway-database-postgresql`) |
| Sécurité | Spring Security, JWT HS256 (OAuth2 Resource Server), BCrypt |
| Documentation API | springdoc-openapi 3.1.1 (Swagger UI) |
| Build | Maven (wrapper `./mvnw`) |
| Front web | Vue 3, Vite, Pinia, Vue Router, Axios, JavaScript (pas de TypeScript) |
| Qualité front | ESLint + Prettier (pas de point-virgule, guillemets simples) |
| Node | 22.18 ou plus récent (`nvm use 22`) |

Prérequis machine : JDK 21, PostgreSQL 16 avec la base `sirh_db`, Node 22, `jq` et `curl` pour les tests manuels.

---

## 2. État d'avancement

| Module | API | Front | Spécification |
|---|---|---|---|
| `auth` (connexion, utilisateurs) | Terminé | Écran de connexion + page `h1` | `docs/modules/00-auth.md` |
| `common/config`, `common/exception`, `common/dto`, `common/entity` | Terminé | — | Section 5 |
| `common` (audit, document, notification, sécurité, numérotation) | À faire | — | `docs/modules/01-common.md` |
| `organisation` | À faire | À faire | `docs/modules/02-organisation.md` |
| `demande` | À faire | À faire | `docs/modules/03-demande.md` |
| `recrutement` | À faire | À faire | `docs/modules/04-recrutement.md` |
| `employe` | Terminé | À faire | `docs/modules/05-employe.md` |
| `contrat` | À faire | À faire | `docs/modules/06-contrat.md` |
| `onboarding` | À faire | À faire | `docs/modules/07-onboarding.md` |
| `embauche` | À faire | À faire | `docs/modules/08-embauche.md` |

Le module `auth` est la **référence de style** : toute nouvelle classe reproduit la structure et les conventions de ses équivalents dans `auth`.

---

## 3. Démarrage local

### 3.1 Base de données

```bash
createdb -U <utilisateur> sirh_db
# Recréer la base de zéro (aucune session psql ouverte) :
dropdb -U <utilisateur> --force sirh_db && createdb -U <utilisateur> sirh_db
```

Le propriétaire de la base doit être l'utilisateur utilisé par l'API (`ALTER DATABASE sirh_db OWNER TO <utilisateur>;`), sinon Flyway ne peut pas créer les tables.

### 3.2 Variables d'environnement (jamais committées)

| Variable | Rôle | Valeur par défaut |
|---|---|---|
| `DB_USER` | Utilisateur PostgreSQL | `postgres` |
| `DB_PASSWORD` | Mot de passe PostgreSQL | `postgres` |
| `JWT_SECRET` | Secret de signature des jetons (32 caractères minimum) | secret de développement |

### 3.3 Lancement

```bash
# API (dossier sirh-api)
export DB_USER=... DB_PASSWORD=...
./mvnw spring-boot:run

# Front (dossier front-web)
npm install
npm run dev
```

| Adresse | Contenu |
|---|---|
| http://localhost:8080/swagger-ui.html | Documentation interactive de l'API |
| http://localhost:5173 | Front web |

Compte administrateur créé au premier démarrage : `admin@sirh.local` / `Admin@12345` (valeurs de `application.properties`, mot de passe de développement).

### 3.4 Commandes utiles

| Commande | Effet |
|---|---|
| `./mvnw compile` | Compile sans démarrer |
| `./mvnw test` | Lance les tests |
| `npm run format` | Formate le front (obligatoire avant commit) |
| `npm run lint` | Contrôle ESLint du front |
| `npm run build` | Compilation de production du front |

---

## 4. Architecture générale

```
Navigateur ──► front-web (Vue) ──HTTP/JSON + JWT──► API Spring Boot ──► PostgreSQL
                                                          └──► dossier de fichiers (uploads/)
```

Trajet d'une requête dans l'API :

1. Le filtre de sécurité lit l'en-tête `Authorization: Bearer <jeton>`, vérifie signature, expiration et émetteur, puis construit l'identité (email, rôle).
2. Le **contrôleur** reçoit la requête, déclenche la validation (`@Valid`), vérifie le rôle (`@PreAuthorize`) et appelle **un** service.
3. Le **service** applique les règles de gestion dans une transaction, utilise les **repositories** et les **mappers**.
4. Toute exception est transformée par `GlobalExceptionHandler` en réponse au format unique `ApiError`.

Le projet est un **monolithe modulaire** : un seul déploiement, un package par module métier, des dépendances entre modules strictement contrôlées (section 9).

---

## 5. API : arborescence et rôle de chaque élément

### 5.1 Racine du dossier `sirh-api`

| Élément | Rôle |
|---|---|
| `pom.xml` | Dépendances et plugins Maven. Toute nouvelle dépendance est discutée en équipe avant ajout. |
| `mvnw`, `mvnw.cmd`, `.mvn/` | Wrapper Maven : garantit la même version de Maven pour tous. À versionner. |
| `HELP.md` | Fichier généré par Spring Initializr, ignoré par Git. |
| `uploads/` | Dossier des fichiers déposés (CV, contrats, PDF). Créé à l'exécution, ignoré par Git. |
| `target/` | Sortie de compilation, ignorée par Git. |

### 5.2 `src/main/resources`

| Élément | Rôle |
|---|---|
| `application.properties` | Configuration de l'application (section 6). |
| `db/migration/V*.sql` | Migrations Flyway : seule source de vérité du schéma de base (section 7). |

### 5.3 `src/main/java/com/gntfh/sirh_api` : structure d'un module

Chaque module métier possède exactement les mêmes sous-dossiers :

| Dossier | Contenu | Règle essentielle |
|---|---|---|
| `entity` | Classes JPA (une par table) | Aucune logique métier. Hérite de `AuditableEntity`. |
| `enums` | Énumérations du module (statuts, types) | Les énumérations de statut portent leurs transitions autorisées. |
| `repository` | Interfaces Spring Data | Accès aux données uniquement, aucune règle de gestion. |
| `dto` | `record` d'échange avec le front | `Request` = entrée validée, `Response` = sortie sans donnée sensible. |
| `mapper` | Conversion entité ↔ DTO | Conversion pure, aucune règle de gestion. |
| `service` | **Toute** la logique métier et les transactions | Les règles RG-xx vivent ici et nulle part ailleurs. |
| `controller` | Couche HTTP | Routes, validation, droits, Swagger, un appel au service par méthode. |

Dossiers facultatifs selon les besoins du module : `rule` (validateurs de règles complexes), `scheduler` (tâches planifiées), `event` (événements publiés), `config` (configuration propre au module).

### 5.4 Le package `common` (transverse)

| Élément | Rôle |
|---|---|
| `config/SecurityConfig` | Règles d'accès HTTP (public / authentifié), sans session, BCrypt, relais des erreurs de sécurité vers `GlobalExceptionHandler`. `@EnableMethodSecurity` active `@PreAuthorize`. |
| `config/CorsConfig` | Autorise l'origine du front (`sirh.cors.allowed-origins`). |
| `config/OpenApiConfig` | Description de l'API et schéma d'authentification Bearer pour Swagger. |
| `config/JpaAuditingConfig` | Renseigne `created_by` / `updated_by` avec l'email de l'utilisateur connecté, ou `system` hors requête (démarrage, tâches planifiées). |
| `config/SchedulingConfig` | Active `@Scheduled`. |
| `config/SirhProperties` | Paramètres typés `sirh.*`, validés au démarrage. Seul point d'accès aux paramètres ; `@Value` est interdit. |
| `entity/BaseEntity` | Parent de toutes les entités : `id` (identité auto), `version` (verrouillage optimiste), `equals`/`hashCode` compatibles Hibernate. |
| `entity/AuditableEntity` | `BaseEntity` + `created_at`, `updated_at`, `created_by`, `updated_by`. |
| `dto/ApiError` | Format unique de toutes les erreurs. |
| `dto/PageResponse` | Format unique de toutes les listes paginées. |
| `exception/*` | Quatre exceptions métier + `GlobalExceptionHandler` (seul endroit qui connaît les codes HTTP). |
| `audit/` | `HistoriqueStatut` (transitions de statut) et `JournalAudit` (avant/après) : traçabilité exigée par le sujet. |
| `document/` | Stockage et versionnement des fichiers (CV, contrats, PDF). |
| `notification/` | Notifications internes destinées aux utilisateurs. |
| `security/` | `CurrentUser` : identité de l'utilisateur connecté transmise aux services. |
| `numerotation/` | Génération des références (`DR-2026-0001`, matricules, etc.) via séquences PostgreSQL. |

### 5.5 Le package `auth` (module de référence)

| Fichier | Rôle |
|---|---|
| `entity/Utilisateur` | Compte de connexion (email, hash BCrypt, nom, prénom, rôle, actif, `employeId`). |
| `enums/Role` | `ADMIN`, `RH`, `MANAGER`, `FINANCE`, `DG`, `IT_SERVICES`, `PAIE`, `SALARIE`. |
| `repository/UtilisateurRepository` | Recherche par email (insensible à la casse), recherche à filtres facultatifs (`Specification`). |
| `config/JwtConfig` | Format du jeton : encodeur, décodeur (signature, expiration, émetteur), conversion du claim `role` en droit Spring `ROLE_xxx`. |
| `config/AdminInitializer` | Crée le premier administrateur au démarrage si aucun compte `ADMIN` n'existe. |
| `service/JwtService` | Fabrique le jeton (claims : `sub` = email, `uid`, `role`, `iss`, `iat`, `exp`). |
| `service/AuthService` | Connexion, profil, changement de son mot de passe. |
| `service/UtilisateurService` | CRUD des comptes, activation/désactivation, réinitialisation de mot de passe. |
| `controller/AuthController` | `/api/auth/login`, `/api/auth/me`, `/api/auth/me/mot-de-passe`. |
| `controller/UtilisateurController` | `/api/utilisateurs/**`, réservé à `ADMIN`. |
| `dto/*` | `LoginRequest`, `TokenResponse`, `UtilisateurRequest`, `UtilisateurUpdateRequest`, `UtilisateurResponse`, `ChangePasswordRequest`, `ResetPasswordRequest`, `PasswordRules`. |
| `mapper/UtilisateurMapper` | Conversions. |

### 5.6 Modules métier

| Module | Responsabilité | Spécification |
|---|---|---|
| `organisation` | Départements, grilles salariales, postes (un poste existe indépendamment de l'employé) | `02-organisation.md` |
| `demande` | Demande de recrutement et chaîne de validation RH → Finance → DG | `03-demande.md` |
| `recrutement` | Offres, candidats, candidatures, entretiens, évaluations, décision finale | `04-recrutement.md` |
| `employe` | Employés, matricule, fiche PDF, affectations (historique des postes) | `05-employe.md` |
| `contrat` | Contrats successifs (essai, CDD, CDI), règles de durée, alertes | `06-contrat.md` |
| `onboarding` | Modèles de checklist, onboarding, tâches | `07-onboarding.md` |
| `embauche` | Proposition d'embauche, réponse du candidat, création de l'employé et du contrat | `08-embauche.md` |

### 5.7 Tests (`src/test/java`)

Le dossier reproduit l'arborescence de `main`. Tests unitaires de services avec JUnit 5 et Mockito : les règles de gestion sont testées en priorité. Convention de nommage : `methode_situation_resultatAttendu` (par exemple `soumettre_demandeBrouillon_passeEnValidationRh`).

---

## 6. Fichier `application.properties`

| Clé | Rôle |
|---|---|
| `spring.application.name` | Nom de l'application. |
| `server.port` | Port HTTP de l'API (8080). |
| `spring.datasource.url/username/password` | Connexion PostgreSQL ; identifiants lus dans `DB_USER` et `DB_PASSWORD`. |
| `spring.jpa.hibernate.ddl-auto=validate` | Hibernate **vérifie** que les entités correspondent au schéma et ne le modifie jamais. Une entité sans migration empêche le démarrage. |
| `spring.jpa.open-in-view=false` | Interdit le chargement paresseux hors transaction (évite les requêtes cachées dans les contrôleurs). |
| `spring.jpa.properties.hibernate.jdbc.time_zone=UTC` | Dates stockées en UTC. |
| `spring.jpa.properties.hibernate.format_sql`, `show-sql` | Affichage des requêtes SQL pour le débogage. |
| `spring.flyway.enabled`, `locations` | Active Flyway sur `classpath:db/migration`. |
| `spring.servlet.multipart.max-file-size/max-request-size` | Taille maximale des fichiers déposés (10 Mo). |
| `spring.web.locale`, `locale-resolver` | Messages de validation en français. |
| `spring.data.web.pageable.default-page-size/max-page-size` | Pagination : 20 par défaut, 100 au maximum. |
| `springdoc.*` | Chemins de Swagger, conservation du jeton après rechargement. |
| `sirh.storage.location` | Dossier de stockage des documents (`./uploads`). |
| `sirh.jwt.secret`, `expiration-minutes` | Secret de signature (variable `JWT_SECRET`) et durée de vie du jeton (120 minutes). |
| `sirh.cors.allowed-origins` | Origines autorisées pour le front. |
| `sirh.admin.*` | Compte administrateur initial. |
| `sirh.demande.*` | Délai minimal d'embauche, durée budgétée, coefficient de charges (règles RG-05 et RG-06). |
| `sirh.proposition.*` | Délai de réponse et durée de prolongation d'une proposition d'embauche. |
| `sirh.contrat.*` | Durées maximales de l'essai et du CDD, délai d'alerte de fin de contrat. |
| `sirh.candidat.conservation-mois` | Durée de conservation des candidats non retenus. |
| `sirh.employe.matricule-prefixe` | Préfixe du matricule. |

Toute nouvelle clé `sirh.*` est déclarée **à deux endroits** : `application.properties` et `SirhProperties` (avec validation).

---

## 7. Migrations Flyway

### 7.1 Principes

- Le schéma de la base est modifié **uniquement** par des fichiers `src/main/resources/db/migration/Vn__nom.sql`.
- Une migration déjà fusionnée dans la branche principale n'est **jamais modifiée** : une nouvelle migration est ajoutée. Pendant le développement initial (avant la première démo), la base locale est recréée si Flyway signale un « checksum mismatch ».
- Une entité sans table correspondante empêche le démarrage (`ddl-auto=validate`) : entité et migration sont toujours livrées ensemble.

### 7.2 Numérotation (ordre figé, un fichier par module)

| Fichier | Module | Contenu |
|---|---|---|
| `V1__organisation.sql` | organisation | `grille_salariale`, `departement`, `poste` |
| `V2__auth.sql` | auth | `utilisateur` (terminé) |
| `V3__transverse.sql` | common | `document`, `historique_statut`, `journal_audit`, `notification`, quatre séquences de numérotation |
| `V4__demande_recrutement.sql` | demande | `demande_recrutement`, `validation` |
| `V5__recrutement.sql` | recrutement | `offre_emploi`, `candidat`, `candidature`, `entretien`, `entretien_intervenant`, `evaluation_candidat` |
| `V6__employe.sql` | employe | `employe`, `affectation` + clés étrangères différées vers `employe` |
| `V7__contrat.sql` | contrat | `contrat` + clé étrangère différée `affectation.contrat_id` |
| `V8__onboarding.sql` | onboarding | `modele_onboarding`, `modele_tache`, `onboarding`, `onboarding_tache` + modèle par défaut |
| `V9__embauche.sql` | embauche | `proposition_embauche` |

Renommage des fichiers vides actuels (à exécuter une seule fois, dans `src/main/resources/db/migration`, **dans cet ordre**) :

```bash
mv V7__onboarding.sql V8__onboarding.sql
mv V6__contrat.sql V7__contrat.sql
mv V5__embauche_employe.sql V6__employe.sql
mv V4__recrutement.sql V5__recrutement.sql
mv V3__demande_recrutement.sql V4__demande_recrutement.sql
mv V8__transverse.sql V3__transverse.sql
echo "-- V9 : embauche" > V9__embauche.sql
```

Après renommage, la base locale est recréée (section 3.1).

### 7.3 Clés étrangères différées

Une clé étrangère ne peut référencer qu'une table **déjà créée**. Quand un module amont référence un module aval, la colonne est créée **sans contrainte** et la contrainte est ajoutée par la migration du module aval :

| Colonne | Créée dans | Contrainte ajoutée dans |
|---|---|---|
| `utilisateur.employe_id` | V2 | V6 (`fk_utilisateur_employe`) |
| `departement.responsable_id` | V1 | V6 (`fk_departement_responsable`) |
| `demande_recrutement.employe_remplace_id` | V4 | V6 (`fk_demande_employe_remplace`) |
| `affectation.contrat_id` | V6 | V7 (`fk_affectation_contrat`) |

### 7.4 Conventions de nommage SQL

| Élément | Règle | Exemple |
|---|---|---|
| Table | `snake_case`, singulier | `demande_recrutement` |
| Colonne | `snake_case` | `date_souhaitee` |
| Clé primaire | `id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY` | |
| Verrou optimiste | `version BIGINT NOT NULL DEFAULT 0` | |
| Audit | `created_at`, `updated_at` (`TIMESTAMPTZ NOT NULL`), `created_by`, `updated_by` (`VARCHAR(150) NOT NULL`) | |
| Clé étrangère | `<entité>_id`, contrainte `fk_<table>_<cible>` | `fk_poste_departement` |
| Unicité | `uk_<table>_<colonne>` | `uk_poste_code` |
| Contrôle | `ck_<table>_<colonne>` | `ck_contrat_type` |
| Index | `idx_<table>_<colonne>` | `idx_candidature_offre` |
| Énumération | `VARCHAR` + contrainte `CHECK ... IN (...)` | |
| Montant | `NUMERIC(14,2)` | `salaire_base` |
| Date / horodatage | `DATE` / `TIMESTAMPTZ` | |
| Booléen | `BOOLEAN NOT NULL DEFAULT ...` | |

Tables d'historique et de journal (`historique_statut`, `journal_audit`) : sans colonnes `updated_*` (insertion seule), mais avec `id` et `version`.

---

## 8. Conventions de code de l'API

### 8.1 Entités

- Hériter de `AuditableEntity`, sauf entités d'insertion seule (`BaseEntity`).
- Annotations : `@Getter @Setter @NoArgsConstructor(access = PROTECTED) @Entity @Table(name = "...")`. `@Data` est interdit sur une entité.
- Création par `@Builder` placé sur un constructeur privé ; l'instanciation avec `new` est interdite hors builder.
- Énumérations : `@Enumerated(EnumType.STRING)`.
- Relations dans un même module ou vers un module **amont** : `@ManyToOne(fetch = FetchType.LAZY)`. Vers un module **aval** ou en cas de cycle : simple identifiant `Long xxxId`.
- Montants : `BigDecimal`. Dates : `LocalDate`. Horodatages : `Instant`. Identifiants : `Long`.
- Aucune logique métier dans l'entité, hormis normalisations triviales (`@PrePersist`).

### 8.2 Énumérations de statut

Une énumération de statut expose : `transitionsAutorisees()` (ensemble des statuts atteignables), `peutPasserA(cible)` et `estTerminal()`. Le service appelle `peutPasserA` avant tout changement et lève `InvalidStateTransitionException` en cas d'échec.

### 8.3 Repositories

- Interface étendant `JpaRepository<E, Long>` (et `JpaSpecificationExecutor<E>` si recherche à filtres facultatifs).
- Requêtes déduites du nom de méthode ; `@Query` seulement pour les cas qui l'exigent.
- Aucune règle de gestion.

### 8.4 DTO

- Toujours des `record`. Jamais d'entité exposée par l'API.
- `XRequest` : création ; `XUpdateRequest` : modification (champs immuables absents) ; `XResponse` : sortie.
- Validation Jakarta avec **message en français** sur chaque contrainte.
- Aucun mot de passe, même haché, dans une `Response`.
- Les `Response` incluent les libellés utiles à l'affichage (par exemple `posteIntitule` à côté de `posteId`) pour éviter des appels supplémentaires du front.
- Les règles communes (mot de passe, par exemple) sont définies une fois dans une constante.

### 8.5 Mappers

- `@Component` avec `toResponse`, `toEntity`, `updateEntity`. Conversion pure.
- Le hachage, le calcul, la génération de référence sont des décisions du service, jamais du mapper.

### 8.6 Services

- `@Service @RequiredArgsConstructor` ; injection par constructeur uniquement.
- Lectures : `@Transactional(readOnly = true)`. Écritures : `@Transactional`. Une entité chargée dans la transaction est modifiée directement (pas de `save()` pour une mise à jour).
- Le service **ne lit jamais** le contexte de sécurité. L'identité de l'acteur est transmise par le contrôleur sous forme de `CurrentUser`.
- Exceptions : voir section 8.9. Aucun code HTTP dans un service.
- Chaque changement de statut : contrôle par l'énumération, enregistrement dans `HistoriqueStatut`, notification des personnes concernées.
- Deux familles de méthodes publiques : **API de contrôleur** (renvoient des DTO) et **API interne** (appelées par d'autres modules, renvoient entités ou identifiants, signalées par le commentaire `// API interne` et listées dans la spécification du module).
- Chaque méthode implémentant une règle porte en Javadoc le code de la règle (`RG-xx` ou `<MODULE>-xx`).

### 8.7 Contrôleurs

- `@RestController @RequestMapping("/api/<ressource-au-pluriel>")`, `@Tag` Swagger.
- `@PreAuthorize` sur chaque méthode (ou sur la classe si la règle est identique partout). `SecurityConfig` ne porte que la règle large (public / authentifié).
- `@Valid @RequestBody` sur chaque corps. Codes de réponse : `201` création (`@ResponseStatus(CREATED)`), `204` sans contenu, `200` sinon.
- Une méthode = une ligne d'appel au service.
- Listes : `@ParameterObject @PageableDefault(size = 20, sort = "...") Pageable` et retour `PageResponse<XResponse>`.
- Actions d'état : `POST /{id}/<action>` (`/soumettre`, `/activer`, `/desactiver`) et non un champ modifiable du `PUT`.
- Pas de `DELETE` sur les données métier : désactivation, annulation ou archivage (RG-47).
- Téléchargement : `ResponseEntity<Resource>` avec `Content-Disposition`.

### 8.8 Sécurité : expressions de droits

| Besoin | Expression |
|---|---|
| Un rôle | `hasRole('RH')` |
| Plusieurs rôles | `hasAnyRole('RH','MANAGER')` |
| Règle fine selon l'objet (périmètre du manager, propriétaire) | Dans le service, avec `CurrentUser` |

Le jeton contient `sub` (email), `uid` (identifiant utilisateur) et `role`. `CurrentUser.from(jwt)` extrait ces valeurs.

### 8.9 Erreurs

| Situation | Exception | HTTP | `code` |
|---|---|---|---|
| Ressource introuvable | `ResourceNotFoundException.of("Poste", id)` | 404 | `RESOURCE_NOT_FOUND` |
| Valeur unique déjà utilisée | `DuplicateResourceException.of("Un poste", "le code", code)` | 409 | `DUPLICATE_RESOURCE` |
| Règle de gestion violée | `BusinessRuleException("message clair")` | 422 | `BUSINESS_RULE_VIOLATION` |
| Transition de statut interdite | `InvalidStateTransitionException` | 422 | `INVALID_STATE_TRANSITION` |
| Corps invalide | (automatique) | 400 | `VALIDATION_ERROR` + `fieldErrors` |
| Jeton absent ou invalide | (automatique) | 401 | `UNAUTHORIZED` |
| Identifiants incorrects | (Spring Security) | 401 | `BAD_CREDENTIALS` |
| Compte désactivé | (Spring Security) | 403 | `ACCOUNT_DISABLED` |
| Rôle insuffisant | (automatique) | 403 | `FORBIDDEN` |
| Modification concurrente | (automatique) | 409 | `CONCURRENT_MODIFICATION` |

Le message d'une `BusinessRuleException` est lu par l'utilisateur final : phrase complète, en français, sans jargon technique. **Un mauvais mot de passe actuel renvoie 422 et jamais 401** : le front déconnecte l'utilisateur à chaque 401.

### 8.10 Recherche, tri et pagination

- Recherche à filtres facultatifs : `Specification` + `JpaSpecificationExecutor`.
- Tri autorisé uniquement sur une **liste blanche** de champs, vérifiée dans le service (jamais de tri sur un champ sensible).
- Page par défaut 20, maximum 100.
- Réponse de liste : toujours `PageResponse`.

### 8.11 Historique, audit, notifications, documents, numérotation

| Besoin | Outil | Quand |
|---|---|---|
| Tracer un changement de statut | `HistoriqueStatutService.enregistrer(...)` | À **chaque** transition de statut |
| Tracer une modification sensible (avant/après) | `AuditService.journaliser(...)` | Création et modification de contrat, affectation, employé, proposition, demande, poste |
| Prévenir un utilisateur | `NotificationService.notifier(...)` / `notifierParRole(...)` | Événements listés dans chaque spécification |
| Stocker un fichier | `DocumentService.enregistrer(...)` | CV, contrat signé, PDF générés, preuves |
| Générer une référence | `NumeroService.prochain(...)` | Demande, offre, matricule, contrat |

### 8.12 Journalisation

`@Slf4j`. Niveau `info` pour les événements métier majeurs, `warn` pour les anomalies récupérables. Aucun mot de passe, jeton ni donnée personnelle dans les logs.

### 8.13 Tâches planifiées

Classe dans `scheduler/`, méthode `@Scheduled(cron = "...")` qui délègue **immédiatement** à une méthode de service (testable sans horloge). L'auteur enregistré est `system` automatiquement.

### 8.14 Langue

- Domaine métier en français (`DemandeRecrutement`, `dateSouhaitee`).
- Vocabulaire technique en anglais (`Service`, `Controller`, `Request`).
- Messages d'erreur et de validation, Javadoc et commentaires en français.

---

## 9. Dépendances entre modules

### 9.1 Ordre et sens autorisé

```
common ◄── auth
   ▲
organisation ◄── demande ◄── recrutement ◄── employe ◄── contrat ◄── onboarding
                                                              ▲            ▲
                                                              └── embauche ┘
```

Un module peut dépendre de `common`, de `auth` et de tout module **situé avant lui** dans l'ordre : `organisation` → `demande` → `recrutement` → `employe` → `contrat` → `onboarding` → `embauche`. Dépendre d'un module situé après est interdit. Exception unique : `common/notification` utilise `auth` (`UtilisateurRepository`) pour notifier par rôle.

### 9.2 Appeler un module aval : ports et événements

Quand un module amont a besoin d'une information d'un module aval, ou doit réagir à un changement aval, deux mécanismes remplacent la dépendance directe.

**Ports** (interface définie dans le module amont, implémentée dans le module aval, injectée avec `ObjectProvider` pour fonctionner avant l'existence de l'implémentation) :

| Port | Défini dans | Implémenté par | Rôle |
|---|---|---|---|
| `PosteOccupationProvider` | `organisation/service` | `employe` (`AffectationOccupationAdapter`) | Nombre d'occupants actifs d'un poste |
| `EmployeLookup` | `demande/service` | `employe` (`EmployeLookupAdapter`) | Existence d'un employé, département actuel d'un employé |

**Événements** (publiés par un module, écoutés par un module aval, dans la même transaction) :

| Événement | Publié par | Écouté par | Réaction |
|---|---|---|---|
| `ContratCreeEvent` | `contrat` | `onboarding` | Création de l'onboarding et de ses tâches |
| `ContratSigneEvent` | `contrat` | `onboarding` | Tâche `CONTRAT_SIGNE` passée à `FAIT` |
| `ContratAnnuleEvent` | `contrat` | `onboarding` | Onboarding passé à `ANNULE` |

### 9.3 Orchestration

`embauche` est le dernier module : il orchestre `recrutement`, `employe`, `contrat` et `demande` lors de l'acceptation d'une proposition. L'orchestration vit dans `EmbaucheService`, et nulle part ailleurs.

---

## 10. Front-web : arborescence, rôle de chaque élément, conventions

### 10.1 État actuel

Un seul écran fonctionnel (connexion) et une page `h1` après connexion. La structure complète est déjà créée ; les fichiers vides sont des emplacements prévus pour les modules suivants.

### 10.2 Racine du dossier `front-web`

| Élément | Rôle |
|---|---|
| `package.json`, `package-lock.json` | Dépendances et scripts. Le fichier de verrouillage est versionné. |
| `vite.config.js` | Configuration de Vite, dont l'alias `@` = `src/`. |
| `jsconfig.json` | Aide à l'IDE pour l'alias `@`. |
| `eslint.config.js` | Règles de qualité du code. |
| `.prettierrc.json` | Formatage (pas de point-virgule, guillemets simples). |
| `index.html` | Page d'entrée de l'application monopage. |
| `.env` | Variables locales, ignoré par Git. |
| `.env.example` | Modèle des variables, versionné. |
| `public/` | Fichiers servis tels quels. |

Variable d'environnement : `VITE_API_URL` (URL de l'API). Toute variable lue par le navigateur commence par `VITE_`.

### 10.3 `src/`

| Dossier / fichier | Rôle | État |
|---|---|---|
| `main.js` | Point d'entrée : installe Pinia **avant** le routeur, branche la déconnexion automatique sur 401. | Fait |
| `App.vue` | Composant racine : uniquement `<RouterView />`. | Fait |
| `config/env.js` | Seul fichier qui lit `import.meta.env`. | Fait |
| `api/http.js` | Client Axios **unique** : joint le jeton, normalise toute erreur, déclenche la déconnexion sur 401 (sauf sur `/login`). | Fait |
| `api/auth.js` | Appels du contrôleur `AuthController`. | Fait |
| `api/*.js` (autres) | Un fichier par contrôleur de l'API. Aucun composant n'appelle Axios directement. | Vides |
| `stores/auth.js` | Session : jeton, utilisateur, rôle, expiration, connexion, déconnexion, rechargement du profil. Persistance dans `localStorage` (clé `sirh.auth`). | Fait |
| `stores/ui.js`, `notification.js`, `referentiel.js` | État global de l'interface, messages à l'écran, listes de référence mises en cache. | Vides |
| `router/index.js` | Assemble les fichiers de routes, installe les gardes, redirige les adresses inconnues. | Fait |
| `router/guards.js` | Garde de navigation : session valide, pages réservées aux visiteurs, vérification du profil auprès de l'API, rôles autorisés. | Fait |
| `router/routes/*.routes.js` | Un fichier de routes par module. | `auth`, `dashboard` faits |
| `constants/routeNames.js` | Noms de routes (la navigation se fait par nom, jamais par chemin écrit en dur). | Fait |
| `constants/roles.js` | Miroir de l'énumération `Role` de l'API et libellés. | Fait |
| `constants/statuts.js`, `enums.js` | Miroirs des énumérations de l'API et leurs libellés français. | Vides |
| `constants/permissions.js` | Matrice des droits par rôle. | Vide |
| `constants/navigation.js` | Éléments du menu avec rôles autorisés. | Vide |
| `utils/storage.js` | Accès sécurisé au `localStorage`. | Fait |
| `utils/errors.js` | Transforme toute erreur Axios en `{ status, code, message, fieldErrors, isNetworkError }`. | Fait |
| `utils/date.js`, `format.js`, `download.js`, `validators.js`, `workflow.js` | Dates, montants, téléchargement de fichiers, validations, aides sur les statuts. | Vides |
| `composables/*.js` | Logique réutilisable (`useAuth`, `usePermissions`, `useNotify`, `useConfirm`, `usePagination`, `useAsyncAction`, `useDebounce`, `useFormErrors`). | Vides |
| `directives/can.js` | Directive `v-can` : masque un élément selon le rôle. | Vide |
| `layouts/` | `AuthLayout` (écrans de connexion), `MainLayout` (menu, en-tête). | Vides |
| `assets/styles/variables.css` | **Référence de design** : couleurs, typographie, espacements, rayons. Aucune valeur de design en dur ailleurs. | Fait |
| `assets/styles/reset.css`, `main.css`, `utilities.css` | Remise à zéro, import des styles, classes utilitaires. | Faits sauf `utilities.css` |
| `assets/images`, `assets/icons` | Images (`login-image.webp`) et icônes. | Partiel |
| `components/common/` | Composants de base `App*`. Faits : `AppInput`, `AppButton`, `AppAlert`. Autres prévus : modale, tableau, pagination, badge, etc. | Partiel |
| `components/<module>/` | Composants propres à un module. | Vides |
| `components/layout/` | Menu latéral, en-tête, fil d'Ariane, cloche de notifications. | Vides |
| `views/<module>/` | Une page par route. Suffixe `View`. | `LoginView`, `DashboardView` faits |

### 10.4 Conventions du front

| Sujet | Règle |
|---|---|
| Appels réseau | Via `src/api/*.js` uniquement. Chaque fonction renvoie directement les données (`response.data`). |
| Erreurs | Les composants lisent `error.message` et `error.fieldErrors`, jamais la structure brute d'Axios. |
| Statuts 401 / 403 | 401 : session fermée automatiquement. 403 : message « droits insuffisants », pas de déconnexion. |
| Routes | Un fichier `*.routes.js` par module, enregistré dans `router/index.js`. `meta` : `public`, `guestOnly`, `roles`, `title`. |
| Noms de routes | Constantes dans `routeNames.js`. |
| Rôles | Constantes de `roles.js`, jamais de chaîne écrite en dur. |
| Droits dans l'interface | Directive `v-can` et composable `usePermissions`. Le contrôle réel reste côté API. |
| Composants | `PascalCase`, préfixe `App` pour les composants communs, `<script setup>`, `defineModel` pour `v-model`. |
| Pages | Suffixe `View`, dans `views/<module>/`. |
| Style | Variables CSS de `variables.css` uniquement, styles `scoped`. |
| Accessibilité | Un `label` par champ, `aria-*` pour les erreurs, focus visible, navigation clavier. |
| Responsive | Mobile d'abord, point de rupture 900 px pour les mises en page à deux colonnes. |
| Formulaires | Validation sur place avant l'appel, erreurs affichées sous les champs, bouton en état de chargement, double soumission bloquée. |
| Liste de données | Pagination et tri pilotés par l'API (`PageResponse`), jamais de tri local sur de gros volumes. |
| Format de code | `npm run format` avant chaque commit ; `npm run lint` sans erreur. |

### 10.5 Flux de connexion

1. `LoginView` valide les champs, appelle `auth.login()`.
2. `stores/auth` appelle `POST /api/auth/login`, stocke jeton, expiration et profil.
3. `http.js` joint ensuite `Authorization: Bearer <jeton>` à chaque requête.
4. À chaque navigation, `guards.js` contrôle l'expiration locale ; au premier passage après chargement de la page, il confirme la session par `GET /api/auth/me`.
5. Sur 401 d'une route protégée : session fermée, retour à `/login?redirect=<page>` avec le message « session expirée ».

---

## 11. Procédure : ajouter un module

### 11.1 API (dans cet ordre)

1. Lire `docs/modules/NN-<module>.md` et vérifier que les modules amont sont terminés.
2. **Migration** : compléter `Vn__<module>.sql` (tables, contraintes, index, données de départ).
3. **Énumérations** (avec transitions pour les statuts).
4. **Entités**.
5. **Repositories**.
6. **DTO** (`Request`, `UpdateRequest`, `Response`).
7. **Mappers**.
8. **Services** (règles de gestion, historique, notifications).
9. **Contrôleurs** (routes, droits, Swagger).
10. **Tests** des services (règles de gestion).
11. **Vérification** : `./mvnw compile`, démarrage sans erreur (Flyway applique la migration, Hibernate valide), parcours de bout en bout dans Swagger, vérification des codes HTTP d'erreur.
12. Mettre à jour le tableau de la section 2 et ouvrir une demande de fusion.

### 11.2 Front

1. Compléter `src/api/<ressource>.js` (un fichier par contrôleur).
2. Ajouter les miroirs d'énumérations dans `constants/statuts.js` / `enums.js`.
3. Créer composants (`components/<module>/`) et pages (`views/<module>/`).
4. Compléter `router/routes/<module>.routes.js` avec `meta.roles`, puis l'importer dans `router/index.js`.
5. Ajouter l'entrée de menu dans `constants/navigation.js`.
6. `npm run format`, `npm run lint`, test manuel avec chaque rôle concerné.

### 11.3 Définition de « terminé » pour un module

- Migration, entités, DTO, mappers, services, contrôleurs complets et documentés dans Swagger.
- Chaque règle de gestion de la spécification est implémentée **et** testée.
- Chaque transition de statut écrit dans l'historique.
- Droits vérifiés avec au moins un rôle autorisé et un rôle refusé.
- Aucune erreur `500` sur les cas d'erreur prévus (doublon, transition interdite, ressource absente).
- Écrans du front fonctionnels pour tous les rôles concernés.
- Relecture par au moins une autre personne.

---

## 12. Organisation d'équipe et Git

### 12.1 Ordre de réalisation

| Étape | Module | Bloque |
|---|---|---|
| 1 | `common` (audit, document, notification, sécurité, numérotation) | Tous les autres |
| 2 | `organisation` | `demande` |
| 3 | `demande` | `recrutement` |
| 4 | `recrutement` | `employe` |
| 5 | `employe` | `contrat` |
| 6 | `contrat` | `onboarding`, `embauche` |
| 7 | `onboarding` | `embauche` |
| 8 | `embauche` | — |

Un module est assigné à une personne responsable (propriétaire). Les fronts peuvent progresser en parallèle des services dès que le contrat d'API (la spécification du module) est figé, car les routes, champs et codes d'erreur y sont définis.

### 12.2 Branches et commits

| Sujet | Règle |
|---|---|
| Branche principale | `main`, protégée : aucune modification directe. |
| Branches de travail | `feature/<module>-<sujet>` (par exemple `feature/demande-validation`). |
| Commits | Courts, à l'infinitif, préfixés : `feat:`, `fix:`, `refactor:`, `docs:`, `test:`. |
| Fusion | Demande de fusion relue par au moins une personne. |
| Migrations | Chaque module possède son fichier `Vn`. Deux personnes ne modifient jamais le même fichier en parallèle. En cas de conflit, le propriétaire du module tranche. |
| Fichiers partagés (`pom.xml`, `application.properties`, `SirhProperties`, `router/index.js`) | Modifications minimales, annoncées à l'équipe, fusionnées rapidement. |
| Fichiers de référence | Le module `auth` et `common/config`, `common/exception`, `common/dto`, `common/entity` ne sont modifiés qu'après décision d'équipe. |

---

## 13. Décisions d'architecture et hypothèses à confirmer

### 13.1 Décisions prises

| Sujet | Décision |
|---|---|
| Identifiant de connexion | Email, unique sans tenir compte de la casse. |
| Rôles | Un seul rôle par utilisateur. « Intervenant » est une situation, pas un rôle. |
| Jeton | Un seul jeton d'accès de 120 minutes, pas de jeton de rafraîchissement. |
| Stockage du jeton (front) | `localStorage`. |
| Suppression | Jamais physique : désactivation, annulation, archivage. |
| Références | `DR-AAAA-NNNN` (demande), `OF-AAAA-NNNN` (offre), `CT-AAAA-NNNN` (contrat), `EMP-AAAA-NNNN` (matricule) via séquences PostgreSQL. |
| Budget estimé d'une demande | Moyenne de la fourchette de la grille × effectif × durée budgétée × (1 + coefficient de charges). |
| Devise par défaut | `MGA` sur les montants (à confirmer). |
| Pas de portail candidat | La réponse à une proposition est saisie par la RH. |

### 13.2 Hypothèses du sujet (à confirmer avec l'enseignant)

| # | Point | Hypothèse retenue |
|---|---|---|
| H1 | « CE par défaut » | Contrat d'essai. |
| H2 | Essai | ≤ 6 mois, renouvelable une fois, cumul ≤ 12 mois. |
| H3 | CDD | ≤ 24 mois cumulés, renouvelable une fois (interprétation stricte, centralisée dans `ContratRuleValidator`). |
| H4 | Validation Finance « selon le budget » | Chaîne fixe RH → Finance → DG. |
| H5 | Trois niveaux de sélection | 1 : entretien RH, 2 : test technique, 3 : entretien final. |
| H6 | « Attendre » | Demande de délai, accordée une seule fois. |
| H7 | Matériel et formation | Facultatifs. |
| H8 | Mentions du contrat | Salaire, horaires, lieu, date de début, durée. |
| H9 | Retour d'un ancien salarié | Hors MVP. |
| H10 | Remplacement | L'employé remplacé reste en base. |
| H11 | Décision finale « RH + Manager » | Enregistrée par la RH **ou** le manager concerné (pas de double validation). |
| H12 | Liens poste/manager d'un contrat | Le contrat porte `posteId` et `managerId` pour créer l'affectation initiale (écart avec le schéma de conception). |

### 13.3 Limites connues

- Un jeton reste valide jusqu'à son expiration même si le compte est désactivé entre-temps ; seule la route `/api/auth/me` relit l'état du compte.
- Pas d'envoi d'email ni de notification push dans le MVP (notifications internes uniquement).
- Pas de signature électronique : le contrat signé est déposé sous forme de fichier.

---

## 14. Utilisation de Devin

1. Lire `AGENTS.md` : règles permanentes que Devin applique à chaque tâche.
2. Générer **un module à la fois**, dans l'ordre de la section 12.1, avec le prompt `docs/prompts/NN-<module>.md`.
3. Devin génère : migration, énumérations, entités, repositories, DTO, mappers, contrôleurs (complets) ; services (**squelettes** : signatures, Javadoc des règles, `UnsupportedOperationException`) ; tests (squelettes désactivés).
4. L'équipe implémente ensuite les services, en suivant les règles de gestion de la spécification.
5. Vérifications après chaque génération : compilation, démarrage de l'application, présence des endpoints dans Swagger.
6. Une génération = une branche = une demande de fusion relue.
