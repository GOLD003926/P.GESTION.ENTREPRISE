# AGENTS.md : règles permanentes pour les agents IA

Projet **SIRH** : système d'information de ressources humaines couvrant le cycle de vie du salarié (besoin, recrutement, embauche, contrat, onboarding). Monolithe modulaire Spring Boot + front Vue.js. Ce fichier s'applique à **toute** tâche confiée à un agent.

## 1. Documents à lire avant toute tâche

| Ordre | Document                        | Contenu                                                                              |
| ----- | ------------------------------- | ------------------------------------------------------------------------------------ |
| 1     | `docs/GUIDE_PROJET.md`          | Conventions, arborescence, migrations, dépendances entre modules                     |
| 2     | `docs/modules/<NN>-<module>.md` | Spécification du module demandé (**source de vérité**)                               |
| 3     | `docs/modules/00-auth.md`       | Module de référence et liste des fichiers modèles                                    |
| 4     | `docs/prompts/<NN>-<module>.md` | Tâche précise à exécuter                                                             |
| 5     | `docs/CONCEPTION.md`            | Règles de gestion RG-xx, machines à états, processus (en cas de doute sur le métier) |

La spécification du module prime sur toute intuition. **Ne jamais inventer** d'attribut, d'endpoint, de règle ou de statut absent de la spécification.

## 2. Stack et commandes

| Élément        | Valeur                                                                      |
| -------------- | --------------------------------------------------------------------------- |
| Langage        | Java 21 (records, `switch` à motifs autorisés)                              |
| Framework      | Spring Boot 4.1.1, Spring Data JPA (Hibernate 7), Spring Security 7, Flyway |
| Base           | PostgreSQL 16                                                               |
| Package racine | `com.gntfh.sirh_api`                                                        |
| Dossier API    | `sirh-api`                                                                  |
| Compilation    | `./mvnw -q compile`                                                         |
| Tests          | `./mvnw -q test`                                                            |
| Démarrage      | `./mvnw spring-boot:run` (variables `DB_USER`, `DB_PASSWORD`)               |

## 3. Fichiers gelés (modification interdite sans demande explicite)

- Tout le package `auth` (sauf compléments listés dans une spécification de module).
- `common/config/*` (sauf `SchedulingConfig`, `SirhProperties` quand une spécification ajoute une clé), `common/exception/*`, `common/dto/*`, `common/entity/*`.
- `SirhApiApplication.java`.
- Migrations déjà remplies : `V2__auth.sql`. Les autres migrations ne sont complétées que par le module qui les possède.
- Tout le dossier `front-web` (hors demande explicite).
- `pom.xml` : seules les dépendances nommées dans une spécification peuvent être ajoutées, **avec une version vérifiée** (ne jamais deviner une version : signaler le besoin).

Toute modification d'un fichier gelé est justifiée dans le rapport final.

## 4. Structure imposée d'un module

`entity`, `enums`, `repository`, `dto`, `mapper`, `service`, `controller` (voir `docs/GUIDE_PROJET.md`, section 5.3). Dossiers facultatifs : `rule`, `scheduler`, `event`, `listener`, `config`. Les fichiers déjà créés dans l'arborescence (vides) sont **complétés**, pas recréés ; les nouveaux fichiers listés dans la spécification sont ajoutés dans le dossier indiqué.

## 5. Conventions de code (résumé impératif)

### Entités

- Hériter de `AuditableEntity` (ou `BaseEntity` pour les tables d'insertion seule).
- `@Getter @Setter @NoArgsConstructor(access = AccessLevel.PROTECTED) @Entity @Table(name = "...")`. **Jamais `@Data`.**
- `@Builder` sur un constructeur **privé** ; aucun `new` hors builder.
- `@Enumerated(EnumType.STRING)` pour les énumérations.
- `@ManyToOne(fetch = FetchType.LAZY)` vers un module amont ou le même module ; `Long xxxId` vers un module aval.
- Types : `Long` (identifiants), `BigDecimal` (montants), `LocalDate` (dates métier), `Instant` (horodatages).
- Aucune logique métier dans l'entité.

### Énumérations de statut

Exposer `transitionsAutorisees()`, `peutPasserA(cible)`, `estTerminal()` conformément à la spécification.

### Repositories

Interface Spring Data ; `JpaSpecificationExecutor` pour les recherches à filtres facultatifs. Aucune règle métier.

### DTO

- `record` uniquement. `XRequest`, `XUpdateRequest`, `XResponse`.
- Validation Jakarta avec **message en français** sur chaque contrainte.
- Jamais d'entité dans une réponse d'API ; jamais de mot de passe.

### Mappers

`@Component`, conversion pure (`toResponse`, `toEntity`, `updateEntity`). Pas de requête, pas de règle.

### Repositories : requêtes

- Spring Data ne dérive que `findBy`, `existsBy`, `countBy` et `deleteBy` à partir d'un nom de méthode. `findMax…`, `findMin…`, `findSum…`, `findAvg…` sont interdits : utiliser `@Query` (JPQL) avec `@Param`.
- Chaque méthode de repository non triviale (`@Query`, agrégat, filtre combiné) est couverte par un test `@SpringBootTest @Transactional` sur la vraie base, exécuté et vert. Un repository simulé par Mockito ne prouve rien sur la requête.

### Services

- `@Service @RequiredArgsConstructor` ; injection par constructeur.
- `@Transactional(readOnly = true)` pour les lectures, `@Transactional` pour les écritures ; pas de `save()` pour modifier une entité chargée.
- Ne jamais lire le `SecurityContext` : utiliser `CurrentUser` fourni par le contrôleur.
- Exceptions : `ResourceNotFoundException.of(...)`, `DuplicateResourceException.of(...)`, `BusinessRuleException`, `InvalidStateTransitionException`. **Aucun code HTTP** dans un service.
- Chaque changement de statut : `peutPasserA`, puis `HistoriqueStatutService.enregistrer(...)`.
- Javadoc de chaque méthode de règle : code de la règle (`RG-xx` ou `<MODULE>-xx`).
- Tri de liste : liste blanche de champs (voir `UtilisateurService.verifierTri`).
- Une classe de service annotée `@Transactional(readOnly = true)` impose `@Transactional` (sans `readOnly`) sur CHAQUE méthode qui écrit en base. Un test unitaire Mockito ne détecte jamais cette erreur : la preuve se fait par un appel HTTP réel qui écrit puis relit.
- Le rapport indique, pour chaque service, la liste des méthodes d'écriture et leur annotation `@Transactional`.
-

### Contrôleurs

- `@RestController @RequestMapping("/api/...")`, `@Tag`, `@Operation(summary = ...)`.
- `@PreAuthorize` sur chaque méthode (ou la classe) selon la spécification.
- `@Valid @RequestBody`, `201` pour les créations, `204` sans contenu.
- Une méthode = un appel de service. Pas de logique.
- Listes : `@ParameterObject @PageableDefault(size = 20, sort = "...") Pageable`, retour `PageResponse<...>`.
- Acteur : `@AuthenticationPrincipal Jwt jwt` converti par `CurrentUser.from(jwt)` (une fois la classe créée par le module `common`).
- Pas de `DELETE` sur les données métier.

### Migrations SQL

- Un fichier par module (`Vn__nom.sql`), conventions de nommage du guide (section 7.4), énumérations en `VARCHAR` + `CHECK`, colonnes d'audit sur chaque table, contraintes nommées.
- Une clé étrangère ne référence jamais une table créée **plus tard** : la contrainte différée est ajoutée par la migration du module aval (guide, section 7.3).
- Entité et migration sont livrées **ensemble** (`ddl-auto=validate`).

### Droits et JSON

- Droits : toujours `hasRole` / `hasAnyRole`. `hasAuthority` et `hasAnyAuthority` sont interdits (le jeton produit des autorités `ROLE_xxx`).
- JSON : uniquement Jackson 3 (`tools.jackson.*`). L'import `com.fasterxml.jackson.databind` est interdit (les annotations `com.fasterxml.jackson.annotation` restent valides). Les exceptions Jackson 3 ne sont pas contrôlées (`tools.jackson.core.JacksonException`). Aucun bean `JsonMapper` personnalisé sans accord de l'équipe : le bean de Spring Boot est injecté tel quel.
- Un test ne se modifie jamais pour « le faire passer » : si un test échoue, le code est corrigé, ou le blocage est signalé.

### Langue et style

- Domaine en français, vocabulaire technique en anglais.
- Messages d'erreur et de validation, Javadoc, commentaires en **français**.
- `@Slf4j` pour les logs ; jamais de mot de passe, jeton ou donnée personnelle dans un log ; pas de `System.out`.
- Pas de dépendance, bibliothèque ou annotation hors de celles déjà présentes sans demande.

## 6. Mode de génération par défaut

| Type de fichier                                                              | Niveau                                                                                                                                                                                                            |
| ---------------------------------------------------------------------------- | ----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------- |
| Migration SQL, énumération, entité, repository, DTO, mapper, événement, port | **Complet**, fidèle à la spécification                                                                                                                                                                            |
| Contrôleur                                                                   | **Complet** : routes, droits, validation, Swagger, délégation au service                                                                                                                                          |
| Service                                                                      | **Squelette** : classe annotée, dépendances injectées, méthodes avec signature exacte, Javadoc (règle, comportement attendu, exceptions), corps `throw new UnsupportedOperationException("TODO <code de règle>")` |
| Planificateur                                                                | Complet (annotation + délégation vers le service)                                                                                                                                                                 |
| Test                                                                         | Squelette `@Disabled("TODO")` nommé selon les scénarios de la spécification (`methode_situation_resultatAttendu`)                                                                                                 |
| Migration de données de départ (seed)                                        | Complète quand la spécification la fournit                                                                                                                                                                        |

Exception : si le prompt de la tâche demande explicitement l'implémentation complète d'un service, ce prompt prime.

## 7. Vérifications obligatoires avant de conclure

1. `./mvnw -q compile` passe sans erreur ni avertissement nouveau.
2. L'application démarre : Flyway applique la migration, Hibernate valide le schéma sans erreur (base recréée au préalable si une migration a été modifiée).
3. Swagger (`/swagger-ui.html`) liste les endpoints du module avec le bon cadenas et la bonne description.
4. Aucun fichier gelé modifié sans justification.
5. Aucun `TODO` sans code de règle ; aucun attribut hors spécification.
6. Un commit par module, message : `feat(<module>): <résumé>`.
7. Aucune vérification ne peut être omise « par manque de temps ». Si une vérification
   n'est pas exécutée, le commit est interdit et le rapport l'indique en première ligne. 8.`./mvnw test` doit afficher BUILD SUCCESS avant tout commit, sortie brute à l'appui.
   Un test écrit mais non exécuté est interdit. Un test en échec ne se modifie pas pour
   passer : on corrige le code, ou on le signale.
8. Valeur par défaut d'un booléen dans un constructeur @Builder : « x != null && x »
   pour un défaut faux (archive, lue, verrouillee, consentementDonnees), « x == null || x »
   pour un défaut vrai (actif, active). Chaque entité à booléen reçoit un test de valeur par défaut.

## 8. Quand la spécification est ambiguë ou contradictoire

Ne pas trancher seul. Écrire `// TODO(question): <question précise>` à l'endroit concerné, choisir l'option la plus restrictive et sûre, et lister toutes les questions dans le rapport final.

## 9. Rapport final obligatoire

Format :

```
## Module <nom> : rapport de génération
### Fichiers créés ou complétés (liste)
### Fichiers gelés modifiés (liste et justification, sinon « aucun »)
### Vérifications (compilation / démarrage / Swagger : résultat de chacune)
### Méthodes de service à implémenter (nom → règle)
### Questions ouvertes (liste des TODO(question))
### Écarts avec la spécification (sinon « aucun »)
```
