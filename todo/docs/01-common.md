# Module `common` : audit, documents, notifications, sécurité, numérotation

Fondations transverses utilisées par tous les modules métier. À réaliser **en premier**. Migration : `V3__transverse.sql`.

Conventions de lecture : tous les types d'entité sont référencés par l'énumération `TypeEntite`. Chaque entité hérite de `AuditableEntity` sauf mention contraire.

## 1. Fichiers

### 1.1 Existants vides à compléter

```
common/audit/{controller/AuditController, dto/{HistoriqueStatutResponse,JournalAuditResponse},
  entity/{HistoriqueStatut,JournalAudit}, repository/{HistoriqueStatutRepository,JournalAuditRepository},
  service/{AuditService,HistoriqueStatutService}}
common/document/{controller/DocumentController, dto/DocumentResponse, entity/Document,
  enums/TypeDocument, repository/DocumentRepository, service/{DocumentService,FileStorageService}}
common/notification/{controller/NotificationController, dto/NotificationResponse, entity/Notification,
  enums/{CanalNotification,TypeNotification}, repository/NotificationRepository, service/NotificationService}
common/config/SchedulingConfig
```

### 1.2 Nouveaux fichiers

| Fichier | Rôle |
|---|---|
| `common/audit/enums/TypeEntite.java` | Énumération des types d'objets traçables |
| `common/audit/enums/ActionAudit.java` | `CREATION`, `MODIFICATION`, `SUPPRESSION_LOGIQUE` |
| `common/security/CurrentUser.java` | Identité de l'utilisateur connecté |
| `common/numerotation/SequenceNumero.java` | Énumération des séquences |
| `common/numerotation/NumeroService.java` | Génération de références |
| `common/document/dto/ContenuDocument.java` | Record `(Resource contenu, String nomFichier, String typeMime)` pour le téléchargement |
| `common/notification/dto/NotificationCompteurResponse.java` | Record `(long nonLues)` |

## 2. Énumérations

| Énumération | Valeurs |
|---|---|
| `TypeEntite` | `UTILISATEUR`, `DEPARTEMENT`, `GRILLE_SALARIALE`, `POSTE`, `DEMANDE_RECRUTEMENT`, `VALIDATION`, `OFFRE_EMPLOI`, `CANDIDAT`, `CANDIDATURE`, `ENTRETIEN`, `EVALUATION_CANDIDAT`, `PROPOSITION_EMBAUCHE`, `EMPLOYE`, `AFFECTATION`, `CONTRAT`, `MODELE_ONBOARDING`, `ONBOARDING`, `ONBOARDING_TACHE` |
| `TypeDocument` | `CV`, `LETTRE_MOTIVATION`, `PROPOSITION_EMBAUCHE`, `CONTRAT`, `CONTRAT_SIGNE`, `FICHE_SALARIE`, `PREUVE_ONBOARDING`, `AUTRE` |
| `ActionAudit` | `CREATION`, `MODIFICATION`, `SUPPRESSION_LOGIQUE` |
| `CanalNotification` | `INTERNE`, `EMAIL`, `PUSH` (seul `INTERNE` est utilisé dans le MVP) |
| `TypeNotification` | `DEMANDE_A_VALIDER`, `DEMANDE_DECISION`, `ENTRETIEN_PLANIFIE`, `EVALUATION_DEMANDEE`, `CANDIDATURE_DECISION`, `PROPOSITION_ENVOYEE`, `PROPOSITION_REPONDUE`, `PROPOSITION_EXPIREE`, `ONBOARDING_TACHE_ASSIGNEE`, `CONTRAT_ECHEANCE`, `AUTRE` |
| `SequenceNumero` | `DEMANDE("seq_demande_recrutement", "DR")`, `OFFRE("seq_offre_emploi", "OF")`, `EMPLOYE("seq_employe_matricule", préfixe issu de SirhProperties)`, `CONTRAT("seq_contrat", "CT")` |

## 3. Entités

### 3.1 `Document` (table `document`, `AuditableEntity`)

| Attribut | Colonne | Type | Null | Règle |
|---|---|---|---|---|
| `type` | `type` | `VARCHAR(30)` | Non | `TypeDocument`, contrainte CHECK |
| `nomFichier` | `nom_fichier` | `VARCHAR(255)` | Non | Nom d'origine, nettoyé |
| `typeMime` | `type_mime` | `VARCHAR(100)` | Non | |
| `tailleOctets` | `taille_octets` | `BIGINT` | Non | > 0 |
| `cheminStockage` | `chemin_stockage` | `VARCHAR(500)` | Non | Chemin **relatif** au dossier `sirh.storage.location` |
| `hashSha256` | `hash_sha256` | `CHAR(64)` | Non | Empreinte du contenu |
| `numeroVersion` | `numero_version` | `INT` | Non | Défaut 1 (le champ `version` de `BaseEntity` est le verrou optimiste, d'où ce nom) |
| `entiteType` | `entite_type` | `VARCHAR(50)` | Non | `TypeEntite` |
| `entiteId` | `entite_id` | `BIGINT` | Non | Lien logique, **sans clé étrangère** |
| `archive` | `archive` | `BOOLEAN` | Non | Défaut `false` |

Index : `(entite_type, entite_id, type)`. Les anciennes versions sont conservées avec `archive = true` (RG-23, RG-50).

### 3.2 `HistoriqueStatut` (table `historique_statut`, `BaseEntity`, insertion seule)

| Attribut | Colonne | Type | Null |
|---|---|---|---|
| `entiteType` | `entite_type` | `VARCHAR(50)` | Non |
| `entiteId` | `entite_id` | `BIGINT` | Non |
| `ancienStatut` | `ancien_statut` | `VARCHAR(40)` | Oui (création) |
| `nouveauStatut` | `nouveau_statut` | `VARCHAR(40)` | Non |
| `auteur` | `auteur` | `VARCHAR(150)` | Non |
| `dateChangement` | `date_changement` | `TIMESTAMPTZ` | Non |
| `commentaire` | `commentaire` | `TEXT` | Oui |

Pas de setters, pas de modification, pas de suppression. Index `(entite_type, entite_id)`.

### 3.3 `JournalAudit` (table `journal_audit`, `BaseEntity`, insertion seule)

| Attribut | Colonne | Type | Null | Règle |
|---|---|---|---|---|
| `entiteType` | `entite_type` | `VARCHAR(50)` | Non | |
| `entiteId` | `entite_id` | `BIGINT` | Non | |
| `action` | `action` | `VARCHAR(30)` | Non | `ActionAudit` |
| `valeursAvant` | `valeurs_avant` | `TEXT` | Oui | JSON (instantané de la `Response` du module) |
| `valeursApres` | `valeurs_apres` | `TEXT` | Oui | JSON |
| `auteur` | `auteur` | `VARCHAR(150)` | Non | |
| `dateAction` | `date_action` | `TIMESTAMPTZ` | Non | |

Index `(entite_type, entite_id)`.

### 3.4 `Notification` (table `notification`, `AuditableEntity`)

| Attribut | Colonne | Type | Null | Règle |
|---|---|---|---|---|
| `destinataire` | `destinataire_id` | `BIGINT` | Non | `@ManyToOne` LAZY vers `Utilisateur`, clé étrangère |
| `type` | `type` | `VARCHAR(40)` | Non | `TypeNotification` |
| `canal` | `canal` | `VARCHAR(20)` | Non | Défaut `INTERNE` |
| `titre` | `titre` | `VARCHAR(150)` | Non | |
| `message` | `message` | `VARCHAR(500)` | Non | |
| `entiteType` | `entite_type` | `VARCHAR(50)` | Oui | |
| `entiteId` | `entite_id` | `BIGINT` | Oui | |
| `lue` | `lue` | `BOOLEAN` | Non | Défaut `false` |
| `dateLecture` | `date_lecture` | `TIMESTAMPTZ` | Oui | |

Index `(destinataire_id, lue)`.

### 3.5 Séquences (dans `V3__transverse.sql`)

`seq_demande_recrutement`, `seq_offre_emploi`, `seq_employe_matricule`, `seq_contrat` : `START 1 INCREMENT 1`.

## 4. DTO

| DTO | Champs |
|---|---|
| `HistoriqueStatutResponse` | `id`, `entiteType`, `entiteId`, `ancienStatut`, `nouveauStatut`, `auteur`, `dateChangement`, `commentaire` |
| `JournalAuditResponse` | `id`, `entiteType`, `entiteId`, `action`, `valeursAvant`, `valeursApres`, `auteur`, `dateAction` |
| `DocumentResponse` | `id`, `type`, `nomFichier`, `typeMime`, `tailleOctets`, `numeroVersion`, `entiteType`, `entiteId`, `archive`, `createdAt`, `createdBy` |
| `NotificationResponse` | `id`, `type`, `titre`, `message`, `entiteType`, `entiteId`, `lue`, `dateLecture`, `createdAt` |
| `NotificationCompteurResponse` | `nonLues` |

Aucun `Request` JSON : le dépôt de document est un `multipart/form-data`.

## 5. Services

### 5.1 `CurrentUser` (record)

`CurrentUser(Long id, String email, Role role)` ; méthode statique `from(Jwt jwt)` : `email` = claim `sub`, `id` = claim `uid` (nombre converti en `Long`), `role` = claim `role` converti en `Role`.

### 5.2 `NumeroService`

| Méthode | Comportement |
|---|---|
| `String prochain(SequenceNumero sequence)` | Lit `nextval` de la séquence (requête native via `JdbcClient`) et retourne `<préfixe>-<année>-<numéro sur 4 chiffres>`, par exemple `DR-2026-0001`. Le préfixe `EMPLOYE` vient de `SirhProperties.employe().matriculePrefixe()`. Un numéro consommé n'est jamais réutilisé. |

### 5.3 `FileStorageService`

| Méthode | Comportement |
|---|---|
| `String sauvegarder(byte[] contenu, TypeEntite type, Long entiteId, String nomFichier)` | Écrit le fichier sous `<storage>/<TYPE>/<entiteId>/<uuid>-<nomNettoyé>` et retourne le chemin relatif. Crée les dossiers. |
| `Resource charger(String cheminRelatif)` | Retourne la ressource. |

Sécurité : normaliser le chemin et refuser toute sortie du dossier de stockage (`..`) ; nettoyer le nom de fichier (caractères autorisés : lettres, chiffres, point, tiret, souligné).

### 5.4 `DocumentService`

| Méthode | Type | Règle |
|---|---|---|
| `Document enregistrer(MultipartFile fichier, TypeDocument type, TypeEntite entiteType, Long entiteId)` | API interne | Contrôle du fichier (vide interdit ; types MIME autorisés : PDF, DOC, DOCX, PNG, JPEG ; taille limitée par la configuration), calcul SHA-256, **versionnement** (même `entiteType`, `entiteId`, `type` : nouvelle version = max + 1, ancienne `archive = true`) |
| `Document enregistrer(byte[] contenu, String nomFichier, String typeMime, TypeDocument type, TypeEntite entiteType, Long entiteId)` | API interne | Même logique pour les PDF générés par l'application |
| `DocumentResponse televerser(MultipartFile, TypeDocument, TypeEntite, Long, CurrentUser)` | Contrôleur | Délègue à `enregistrer`, retourne le DTO |
| `DocumentResponse obtenir(Long id)` | Contrôleur | 404 si absent |
| `List<DocumentResponse> lister(TypeEntite, Long entiteId, boolean inclureArchives)` | Contrôleur | Triés par `type`, puis `numeroVersion` décroissant |
| `ContenuDocument telecharger(Long id)` | Contrôleur | Charge le fichier ; 404 si fichier physique absent |

### 5.5 `HistoriqueStatutService`

| Méthode | Règle |
|---|---|
| `void enregistrer(TypeEntite type, Long entiteId, String ancienStatut, String nouveauStatut, String auteur, String commentaire)` | Insertion d'une ligne avec `dateChangement = maintenant`. `ancienStatut` nul pour une création. Appelé par **tous** les services à **chaque** transition. |
| `List<HistoriqueStatutResponse> lister(TypeEntite, Long entiteId)` | Ordre chronologique croissant. |

### 5.6 `AuditService`

| Méthode | Règle |
|---|---|
| `void journaliser(ActionAudit action, TypeEntite type, Long entiteId, Object avant, Object apres, String auteur)` | Sérialise `avant` et `apres` en JSON avec le bean JSON fourni par Spring Boot 4 (Jackson 3, packages `tools.jackson.*`). Les objets passés sont des `Response` DTO (jamais des entités, pour éviter les proxys Hibernate). |
| `PageResponse<JournalAuditResponse> rechercher(TypeEntite, Long entiteId, String auteur, Pageable)` | Filtres facultatifs, tri par défaut `dateAction` décroissant. |

### 5.7 `NotificationService`

| Méthode | Type | Règle |
|---|---|---|
| `void notifier(Long destinataireId, TypeNotification type, String titre, String message, TypeEntite entiteType, Long entiteId)` | API interne | Crée une notification `INTERNE` non lue. |
| `void notifierParRole(Role role, TypeNotification type, String titre, String message, TypeEntite entiteType, Long entiteId)` | API interne | Notifie chaque utilisateur **actif** du rôle (utilise `UtilisateurRepository`). |
| `PageResponse<NotificationResponse> mesNotifications(CurrentUser, Boolean lue, Pageable)` | Contrôleur | Uniquement celles de l'utilisateur connecté. |
| `NotificationCompteurResponse compter(CurrentUser)` | Contrôleur | Nombre de non lues. |
| `NotificationResponse marquerLue(Long id, CurrentUser)` | Contrôleur | 404 si la notification n'appartient pas à l'utilisateur (ne pas révéler son existence). |
| `void toutMarquerLues(CurrentUser)` | Contrôleur | |

## 6. Endpoints

| Méthode | Route | Rôles | Description |
|---|---|---|---|
| POST | `/api/documents` | RH, IT_SERVICES, PAIE | Dépôt (`multipart` : `fichier`, `type`, `entiteType`, `entiteId`), 201 |
| GET | `/api/documents` | RH, MANAGER, DG, IT_SERVICES, PAIE | Liste (`entiteType`, `entiteId`, `inclureArchives=false`) |
| GET | `/api/documents/{id}` | idem | Métadonnées |
| GET | `/api/documents/{id}/contenu` | idem | Téléchargement avec `Content-Disposition` |
| GET | `/api/audit/historique-statuts` | RH, DG | Historique (`entiteType`, `entiteId` obligatoires) |
| GET | `/api/audit/journal` | RH, DG | Journal paginé (`entiteType`, `entiteId`, `auteur`) |
| GET | `/api/notifications` | Authentifié | Mes notifications (`lue`, pagination) |
| GET | `/api/notifications/compteur` | Authentifié | Nombre de non lues |
| PUT | `/api/notifications/{id}/lue` | Authentifié | Marquer lue |
| PUT | `/api/notifications/lues` | Authentifié | Tout marquer lu (204) |

## 7. Règles

| Code | Règle |
|---|---|
| COM-01 | `HistoriqueStatut` et `JournalAudit` sont en insertion seule : aucune route de modification ou de suppression. |
| COM-02 | Un document n'est jamais supprimé : nouvelle version et archivage (RG-23). |
| COM-03 | Le chemin physique ne sort jamais du dossier de stockage. |
| COM-04 | Une notification n'est visible que par son destinataire. |
| COM-05 | `SchedulingConfig` : `@Configuration @EnableScheduling`. |
| COM-06 | Aucun mot de passe, jeton ni contenu de fichier dans les journaux. |

## 8. Migration `V3__transverse.sql`

Contenu : tables `document`, `historique_statut`, `journal_audit`, `notification` ; quatre séquences ; index cités ci-dessus ; contraintes CHECK sur les colonnes énumérées ; clé étrangère `fk_notification_destinataire` vers `utilisateur(id)`. Conventions : section 7 du guide.

## 9. Tests (squelettes désactivés)

`DocumentServiceTest` (versionnement, refus d'un fichier vide, chemin hors dossier), `NumeroServiceTest` (format de référence), `NotificationServiceTest` (notification par rôle ignore les comptes inactifs, un utilisateur ne lit pas la notification d'un autre).

## 10. Front

Aucun écran dans ce module. Éléments à prévoir plus tard : `api/notifications.js`, `api/documents.js`, `api/audit.js`, `components/layout/NotificationBell.vue`, `components/documents/{DocumentList,DocumentUploader,DocumentPreview}.vue`, `components/audit/{HistoriqueStatutTimeline,JournalAuditTable}.vue`, `views/notifications/NotificationListView.vue`, `views/audit/JournalAuditView.vue`.
