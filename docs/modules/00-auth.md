# Module `auth` : référence (terminé)

Ce module est **terminé et figé**. Il sert de modèle : chaque nouveau module reproduit sa structure, ses conventions et son niveau de détail. Aucun fichier n'est modifié sans décision d'équipe, hormis les compléments explicitement autorisés dans les spécifications des modules suivants.

## 1. Fichiers de référence à lire avant de générer un module

| Besoin | Fichier à imiter |
|---|---|
| Migration SQL | `src/main/resources/db/migration/V2__auth.sql` |
| Entité | `auth/entity/Utilisateur.java` |
| Énumération | `auth/enums/Role.java` |
| Repository avec recherche | `auth/repository/UtilisateurRepository.java` |
| DTO de création / modification / sortie | `UtilisateurRequest`, `UtilisateurUpdateRequest`, `UtilisateurResponse` |
| Règle de validation partagée | `auth/dto/PasswordRules.java` |
| Mapper | `auth/mapper/UtilisateurMapper.java` |
| Service avec règles, recherche paginée, tri en liste blanche | `auth/service/UtilisateurService.java` |
| Contrôleur CRUD | `auth/controller/UtilisateurController.java` |
| Données de départ au démarrage | `auth/config/AdminInitializer.java` |

## 2. Entité `Utilisateur` (table `utilisateur`)

Hérite de `AuditableEntity`.

| Attribut | Colonne | Type | Null | Règle |
|---|---|---|---|---|
| `email` | `email` | `VARCHAR(150)` | Non | Unique sans tenir compte de la casse, enregistré en minuscules, immuable |
| `motDePasse` | `mot_de_passe` | `VARCHAR(100)` | Non | Hash BCrypt, jamais renvoyé |
| `nom` | `nom` | `VARCHAR(100)` | Non | |
| `prenom` | `prenom` | `VARCHAR(100)` | Non | |
| `role` | `role` | `VARCHAR(30)` | Non | Valeurs de `Role` |
| `actif` | `actif` | `BOOLEAN` | Non | Défaut `true` |
| `employeId` | `employe_id` | `BIGINT` | Oui | Lien facultatif vers l'employé. Clé étrangère ajoutée par la migration `V6__employe.sql` |

## 3. Endpoints

| Méthode | Route | Rôles | Rôle de l'endpoint |
|---|---|---|---|
| POST | `/api/auth/login` | Public | Connexion, retourne `TokenResponse` |
| GET | `/api/auth/me` | Authentifié | Profil de l'utilisateur connecté |
| PUT | `/api/auth/me/mot-de-passe` | Authentifié | Changer son mot de passe (204) |
| GET | `/api/utilisateurs` | ADMIN | Liste paginée (`q`, `role`, `actif`, `sort`, `page`, `size`) |
| GET | `/api/utilisateurs/{id}` | ADMIN | Détail |
| POST | `/api/utilisateurs` | ADMIN | Création (201) |
| PUT | `/api/utilisateurs/{id}` | ADMIN | Modification (nom, prénom, rôle, employé lié) |
| POST | `/api/utilisateurs/{id}/activer` | ADMIN | Activation |
| POST | `/api/utilisateurs/{id}/desactiver` | ADMIN | Désactivation |
| PUT | `/api/utilisateurs/{id}/mot-de-passe` | ADMIN | Réinitialisation (204) |

## 4. Règles de gestion appliquées

| Code | Règle |
|---|---|
| AUT-01 | Email unique sans tenir compte de la casse (409 sinon). |
| AUT-02 | Email immuable après création. |
| AUT-03 | Un administrateur ne peut ni désactiver son propre compte ni modifier son propre rôle (422). |
| AUT-04 | Pas de suppression : désactivation. |
| AUT-05 | Mot de passe : 8 à 72 caractères, au moins une majuscule, une minuscule et un chiffre. |
| AUT-06 | Mauvais mot de passe actuel : 422 (jamais 401). |
| AUT-07 | Tri autorisé uniquement sur `id`, `email`, `nom`, `prenom`, `role`, `actif`, `createdAt`. |
| AUT-08 | Un compte désactivé ne peut pas se connecter (403, `ACCOUNT_DISABLED`), l'état n'est révélé qu'après vérification du mot de passe. |

## 5. Contrat du jeton

| Claim | Contenu |
|---|---|
| `sub` | Email de l'utilisateur |
| `uid` | Identifiant de l'utilisateur |
| `role` | Valeur de `Role` |
| `iss` | `sirh-api` |
| `iat`, `exp` | Émission et expiration (120 minutes) |

## 6. Compléments autorisés par les modules suivants

| Module | Complément autorisé dans `auth` |
|---|---|
| `employe` | `UtilisateurService.creer/modifier` : vérifier l'existence de l'employé lié (`employeId`) et lancer `ResourceNotFoundException` sinon. |

## 7. Front : écran de connexion (terminé)

Fichiers : `views/auth/LoginView.vue`, `views/dashboard/DashboardView.vue`, `api/auth.js`, `api/http.js`, `stores/auth.js`, `router/guards.js`, `router/routes/auth.routes.js`, `router/routes/dashboard.routes.js`, composants `AppInput`, `AppButton`, `AppAlert`. Aucun écran de gestion des utilisateurs : la gestion des comptes se fait par Swagger.
