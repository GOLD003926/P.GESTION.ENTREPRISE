# Conception du SIRH – Cycle de vie du salarié

**Version 1.0 – Document de modélisation (avant développement)**

---

## Sommaire

1. Périmètre, objectifs et principes de conception
2. Acteurs et rôles
3. Vue d'ensemble du cycle de vie
4. Processus métier (BPMN) – Blocs 1 à 4
5. Modèle conceptuel de données (MCD) – diagrammes et cardinalités
6. Modèle UML – diagramme de classes du domaine
7. Énumérations (valeurs des statuts et types)
8. Machines à états (cycles de vie des objets)
9. Workflows détaillés
10. Règles de gestion (RG)
11. Traçabilité et audit
12. Droits par rôle
13. Cas d'utilisation du MVP
14. Périmètre MVP / hors périmètre
15. Hypothèses et points à valider avec l'enseignant

---

## 1. Périmètre, objectifs et principes de conception

### 1.1 Objectif

Concevoir un **SIRH** (Système d'Information de Ressources Humaines) qui couvre le cycle de vie du salarié :

`Besoin → Recrutement → Embauche → Contrat & Onboarding → Vie de salarié → Évolution → Rupture`

Le MVP couvre en détail les **blocs 1 à 4**. Les phases *Vie de salarié, Évolution, Rupture* sont modélisées de façon légère (section 3 et 14) pour que l'architecture soit prête à les accueillir.

### 1.2 Principes de conception (points clés du sujet)

| # | Principe | Conséquence sur le modèle |
|---|----------|---------------------------|
| P1 | **Un POSTE existe indépendamment de l'EMPLOYÉ** | Entité `Poste` autonome. Le lien employé ↔ poste passe par `Affectation` (avec dates). Un poste peut avoir plusieurs occupants (`effectif_autorise`). |
| P2 | **CANDIDAT ≠ EMPLOYÉ** | Deux entités distinctes. L'employé est créé **uniquement à l'acceptation** de la proposition d'embauche, avec un lien vers la candidature d'origine. |
| P3 | **Ne jamais écraser l'historique** | Pas de champ `employe.type_contrat`. Un `Contrat` = une ligne, chaque renouvellement ou changement de type crée une nouvelle ligne chaînée. Idem pour `Affectation`. |
| P4 | **Tout est traçable** (CV, dates, candidat, évaluations, décisions) | `HistoriqueStatut` + `JournalAudit` + `Document` versionné + suppression logique uniquement. |
| P5 | **Processus avant écrans** | Chaque fonctionnalité découle d'un processus, d'un workflow ou d'une règle de gestion documentés ici. |
| P6 | **Workflow = machine à états + table de validations** | Chaque objet métier a des statuts et des transitions autorisées (section 8). |

---

## 2. Acteurs et rôles

| Acteur | Rôle dans le système | Intervient dans |
|--------|----------------------|-----------------|
| **Manager** | Exprime le besoin, participe aux entretiens, décide avec la RH, encadre le nouvel arrivant | Blocs 1, 2, 4 |
| **RH** | Pilote tout le processus : vérifie, publie l'offre, présélectionne, organise, propose, crée dossier et contrat | Blocs 1 à 4 |
| **Finance** | Valide la disponibilité budgétaire | Bloc 1 |
| **Direction Générale (DG)** | Validation finale de la demande | Bloc 1 |
| **Intervenant / Évaluateur** | Mène un entretien ou un test et saisit l'évaluation | Bloc 2 |
| **Candidat** (externe) | Postule, répond à la proposition d'embauche | Blocs 2, 3 |
| **IT / Services généraux** | Crée les comptes, prépare badge et matériel | Bloc 4 |
| **Paie** | Inscrit le salarié en paie | Bloc 4 |
| **Salarié** | Consulte son onboarding, signe son contrat | Bloc 4 |
| **Administrateur** | Paramètres, rôles, référentiels (départements, grilles, modèles de checklist) | Transverse |

---

## 3. Vue d'ensemble du cycle de vie

```mermaid
flowchart LR
  B1["Bloc 1<br/>Expression du besoin<br/>(DemandeRecrutement + Validations)"]
  B2["Bloc 2<br/>Recrutement<br/>(Offre, Candidatures, Entretiens)"]
  B3["Bloc 3<br/>Proposition d'embauche<br/>(création Employé + matricule)"]
  B4["Bloc 4<br/>Contrat & Onboarding"]
  B5["Vie de salarié<br/>(hors MVP)"]
  B6["Évolution<br/>(hors MVP)"]
  B7["Rupture<br/>(hors MVP)"]

  B1 -->|"demande VALIDEE"| B2
  B2 -->|"candidat RETENU"| B3
  B3 -->|"proposition ACCEPTEE"| B4
  B4 --> B5
  B5 --> B6
  B6 --> B5
  B5 --> B7
  B6 --> B7
  B7 -.->|"départ = nouveau besoin<br/>(demande de remplacement)"| B1
```

---

## 4. Processus métier (BPMN)

Convention : chaque processus est décrit par un diagramme (couloirs = acteurs) puis par un tableau d'étapes avec le **type d'élément BPMN** à utiliser dans un outil de modélisation (*Start Event, User Task, Service Task, Exclusive Gateway XOR, Parallel Gateway +, Timer Event, End Event*).

### 4.1 Processus 1 – Expression du besoin et validation

```mermaid
flowchart TD
  subgraph MGR["Couloir Manager"]
    M0(("Besoin en personnel identifié"))
    M1["Saisir la demande<br/>poste, département, effectif,<br/>motif, date souhaitée"]
    M2["Soumettre la demande"]
    M3["Corriger la demande"]
  end
  subgraph RH["Couloir RH"]
    R1["Vérifier : poste, grille salariale,<br/>budget estimé, cohérence"]
    R2{"Conforme ?"}
  end
  subgraph FIN["Couloir Finance"]
    F1["Vérifier la disponibilité budgétaire"]
    F2{"Budget disponible ?"}
  end
  subgraph DG["Couloir Direction Générale"]
    D1["Examiner l'opportunité"]
    D2{"Approuvée ?"}
  end
  E1(("Demande VALIDEE<br/>→ ouverture du recrutement"))
  E2(("Demande REJETEE"))

  M0 --> M1 --> M2 --> R1 --> R2
  R2 -->|"Oui"| F1
  R2 -->|"À corriger"| M3
  R2 -->|"Rejet"| E2
  F1 --> F2
  F2 -->|"Oui"| D1
  F2 -->|"À ajuster"| M3
  F2 -->|"Non"| E2
  M3 --> M2
  D1 --> D2
  D2 -->|"Oui"| E1
  D2 -->|"À corriger"| M3
  D2 -->|"Non"| E2
```

| Étape | Acteur | Élément BPMN | Entrée → Sortie |
|-------|--------|--------------|-----------------|
| Besoin identifié | Manager | Start Event | – |
| Saisir la demande | Manager | User Task | Demande en BROUILLON |
| Soumettre | Manager | User Task | Demande EN_VALIDATION_RH |
| Vérifier conformité | RH | User Task + Exclusive Gateway | Approuvée / Retournée / Rejetée |
| Vérifier budget | Finance | User Task + Exclusive Gateway | Approuvée / Retournée / Rejetée |
| Examiner opportunité | DG | User Task + Exclusive Gateway | Approuvée / Retournée / Rejetée |
| Notification du résultat | Système | Service Task | Notification au manager et à la RH |
| Fin | – | End Event (VALIDEE ou REJETEE) | – |

### 4.2 Processus 2 – Recrutement

```mermaid
flowchart TD
  subgraph RH["Couloir RH"]
    A0(("Demande VALIDEE"))
    A1["Créer l'offre d'emploi<br/>à partir de la demande"]
    A2["Publier l'offre"]
    A4["Enregistrer la candidature<br/>(créer ou retrouver le Candidat)"]
    A5["Présélection sur dossier"]
    A6{"Présélectionné ?"}
    A7["Planifier entretien ou test<br/>de niveau n"]
    A10["Décision finale RH + Manager"]
    A11{"Retenu ?"}
  end
  subgraph CAN["Couloir Candidat"]
    C1["Postuler : CV + lettre"]
  end
  subgraph EVA["Couloir Évaluateurs / Manager"]
    V1["Mener l'entretien ou le test<br/>et saisir l'évaluation"]
    V2{"Avis favorable ?"}
    V3{"Dernier niveau (3) atteint ?"}
  end
  X1(("Candidature non retenue<br/>+ notification"))
  X2(("Candidat RETENU<br/>→ Bloc 3 Proposition d'embauche"))

  A0 --> A1 --> A2 --> C1 --> A4 --> A5 --> A6
  A6 -->|"Non"| X1
  A6 -->|"Oui"| A7 --> V1 --> V2
  V2 -->|"Non"| X1
  V2 -->|"Oui"| V3
  V3 -->|"Non : niveau n+1"| A7
  V3 -->|"Oui"| A10 --> A11
  A11 -->|"Non"| X1
  A11 -->|"Oui"| X2
```

| Étape | Acteur | Élément BPMN | Remarque |
|-------|--------|--------------|----------|
| Créer l'offre | RH | User Task | Copie poste, effectif, profil depuis la demande validée |
| Publier | RH | User Task | Canal : site, réseaux, cabinet… |
| Postuler | Candidat | User Task (message start) | CV déposé = `Document` daté et versionné |
| Enregistrer candidature | Système / RH | Service Task | Dédoublonnage du candidat (email, téléphone) |
| Présélection | RH | User Task + XOR | Motif obligatoire en cas de refus |
| Niveau 1 / 2 / 3 | RH + Évaluateurs | User Task en boucle (*multi-instance*) | 1 = entretien RH, 2 = test technique, 3 = entretien final (paramétrable) |
| Évaluation | Évaluateur | User Task | Note, avis, commentaire |
| Décision finale | RH + Manager | User Task + XOR | Conservée avec date, auteur, motif |
| Notification | Système | Service Task | Au candidat, à chaque issue |

### 4.3 Processus 3 – Proposition d'embauche

```mermaid
flowchart TD
  subgraph RH["Couloir RH"]
    P0(("Candidat RETENU"))
    P1["Préparer la proposition :<br/>poste, type de contrat, salaire,<br/>horaires, lieu, date de début,<br/>date limite de réponse"]
    P2["Envoyer la proposition"]
    P6["Enregistrer le refus + motif"]
    P7["Proposer au suivant<br/>(liste d'attente) si effectif non atteint"]
    P8["Enregistrer la demande de délai"]
  end
  subgraph CAN["Couloir Candidat"]
    P3{"Réponse du candidat"}
  end
  subgraph SYS["Couloir Système"]
    S1["Créer le dossier Employé<br/>+ matricule unique"]
    S2["Exporter la fiche salarié<br/>en PDF"]
    S3["Créer le contrat d'essai (par défaut)"]
    T1(("Date limite dépassée"))
    T2["Marquer la proposition EXPIREE"]
  end
  END1(("→ Bloc 4 Contrat & Onboarding"))
  END2(("Fin sans embauche"))

  P0 --> P1 --> P2 --> P3
  P3 -->|"Accepte"| S1 --> S2 --> S3 --> END1
  P3 -->|"Refuse"| P6 --> P7 --> END2
  P3 -->|"Demande un délai"| P8 --> P3
  P2 -.-> T1
  T1 --> T2 --> P7
```

| Étape | Acteur | Élément BPMN | Remarque |
|-------|--------|--------------|----------|
| Préparer la proposition | RH | User Task | Mentions obligatoires : voir RG-22 |
| Envoyer | RH | User Task / Send Task | Document PDF joint, `Document` archivé |
| Réponse | Candidat | Event-based Gateway | Trois issues : accepter, refuser, attendre (délai) |
| Date limite | Système | Timer Boundary Event | Expiration automatique |
| Création Employé + matricule | Système | Service Task | Matricule unique et non réutilisable |
| Export PDF | Système | Service Task | Fiche salarié avec matricule |
| Création contrat d'essai | Système | Service Task | Contrat par défaut = **Essai** (RG-27) |

### 4.4 Processus 4 – Contrat et onboarding

```mermaid
flowchart TD
  subgraph RH["Couloir RH"]
    K0(("Employé créé"))
    K1["Générer le contrat<br/>(essai par défaut)"]
    K2["Envoyer pour signature"]
    K3{"Contrat signé ?"}
    K4["Activer le contrat<br/>+ créer l'Affectation initiale"]
    K5["Générer la checklist d'onboarding<br/>depuis le modèle"]
  end
  subgraph IT["IT / Services généraux"]
    T1["Créer les comptes"]
    T2["Préparer le badge"]
    T3["Préparer le matériel (facultatif)"]
  end
  subgraph PAIE["Paie"]
    T4["Inscrire le salarié en paie"]
  end
  subgraph MGR["Manager / RH"]
    T5["Planifier les formations (facultatif)"]
    T6["Accompagner l'ouverture<br/>du compte bancaire (facultatif)"]
  end
  J1{"Toutes les tâches<br/>obligatoires faites ?"}
  K6(("Onboarding TERMINE"))

  K0 --> K1 --> K2 --> K3
  K3 -->|"Non, relance"| K2
  K3 -->|"Oui"| K4 --> K5
  K5 --> T1
  K5 --> T2
  K5 --> T3
  K5 --> T4
  K5 --> T5
  K5 --> T6
  T1 --> J1
  T2 --> J1
  T3 --> J1
  T4 --> J1
  T5 --> J1
  T6 --> J1
  J1 -->|"Oui"| K6
  J1 -->|"Non, relance"| J1
```

| Étape | Acteur | Élément BPMN | Remarque |
|-------|--------|--------------|----------|
| Générer le contrat | RH | User Task | Type par défaut : Essai |
| Signature | RH + Salarié | User Task | Document signé téléversé (preuve) |
| Activation + Affectation | Système / RH | Service Task | Poste, département, manager, date de début |
| Génération de la checklist | Système | Service Task | À partir du `ModeleOnboarding` |
| Tâches d'intégration | IT, Paie, RH, Manager | Parallel Gateway (+) en divergence puis convergence | Tâches obligatoires et facultatives |
| Clôture | Système | Exclusive Gateway + End Event | Quand toutes les tâches obligatoires sont faites |

### 4.5 Sous-processus – Évolution du contrat

```mermaid
flowchart LR
  E1["Contrat d'ESSAI<br/>≤ 6 mois"]
  E2["Essai renouvelé<br/>(1 seule fois, cumul ≤ 12 mois)"]
  C1["CDD<br/>≤ 2 ans"]
  C2["CDD renouvelé<br/>(1 seule fois)"]
  C3["CDI"]
  F1(("Fin de relation<br/>(rupture)"))

  E1 -->|"Renouvellement"| E2
  E1 -->|"Concluant"| C3
  E1 -->|"Concluant"| C1
  E2 -->|"Concluant"| C3
  E2 -->|"Concluant"| C1
  E1 -->|"Non concluant"| F1
  E2 -->|"Non concluant"| F1
  C1 -->|"Renouvellement"| C2
  C1 -->|"Transformation"| C3
  C2 -->|"Transformation"| C3
  C1 -->|"Échéance"| F1
  C2 -->|"Échéance"| F1
  C3 -->|"Démission, licenciement, retraite…"| F1
```

Chaque flèche = **un nouveau `Contrat` lié au précédent** (champ `contrat_precedent_id`). Rien n'est écrasé.

---

## 5. Modèle conceptuel de données (MCD)

Le modèle est découpé en 3 diagrammes pour rester lisible, puis complété par le tableau des cardinalités (notation Merise).

### 5.1 Diagramme A – Organisation et expression du besoin

```mermaid
erDiagram
  DEPARTEMENT ||--o{ POSTE : "regroupe"
  DEPARTEMENT |o--o{ DEPARTEMENT : "parent de"
  GRILLE_SALARIALE ||--o{ POSTE : "classe"
  POSTE ||--o{ DEMANDE_RECRUTEMENT : "est concerne par"
  DEPARTEMENT ||--o{ DEMANDE_RECRUTEMENT : "emet"
  UTILISATEUR ||--o{ DEMANDE_RECRUTEMENT : "cree"
  DEMANDE_RECRUTEMENT ||--|{ VALIDATION : "est soumise a"
  UTILISATEUR |o--o{ VALIDATION : "decide"
  EMPLOYE |o--o{ DEMANDE_RECRUTEMENT : "est remplace par"

  DEPARTEMENT {
    bigint id PK
    string code UK
    string nom
    bigint departement_parent_id FK
    bigint responsable_id FK "Employe"
    boolean actif
  }
  GRILLE_SALARIALE {
    bigint id PK
    string code UK
    string intitule
    string niveau
    decimal salaire_min
    decimal salaire_max
    string devise
  }
  POSTE {
    bigint id PK
    string code UK
    string intitule
    text description
    text competences_requises
    bigint departement_id FK
    bigint grille_salariale_id FK
    int effectif_autorise "nb max d'occupants"
    boolean actif
  }
  UTILISATEUR {
    bigint id PK
    string login UK
    string email UK
    string mot_de_passe_hash
    string role "MANAGER, RH, FINANCE, DG, IT, PAIE, ADMIN..."
    bigint employe_id FK "optionnel"
    boolean actif
  }
  DEMANDE_RECRUTEMENT {
    bigint id PK
    string reference UK
    bigint poste_id FK
    bigint departement_id FK
    bigint demandeur_id FK "Utilisateur"
    string type_demande "CREATION ou REMPLACEMENT"
    bigint employe_remplace_id FK "si REMPLACEMENT"
    int effectif_demande
    string motif
    text justification
    decimal budget_estime "calcule depuis la grille"
    date date_souhaitee
    string statut
    int cycle_courant
    datetime date_creation
    datetime date_derniere_modification
  }
  VALIDATION {
    bigint id PK
    bigint demande_id FK
    int cycle "n° de soumission"
    int ordre "1 RH, 2 Finance, 3 DG"
    string role_validateur
    bigint validateur_id FK
    string decision "EN_ATTENTE, APPROUVEE, REJETEE, RETOURNEE"
    text commentaire
    datetime date_decision
  }
```

### 5.2 Diagramme B – Recrutement

```mermaid
erDiagram
  DEMANDE_RECRUTEMENT ||--o{ OFFRE_EMPLOI : "donne lieu a"
  OFFRE_EMPLOI ||--o{ CANDIDATURE : "recoit"
  CANDIDAT ||--o{ CANDIDATURE : "depose"
  CANDIDATURE ||--o{ ENTRETIEN : "comporte"
  ENTRETIEN ||--|{ ENTRETIEN_INTERVENANT : "est mene par"
  UTILISATEUR ||--o{ ENTRETIEN_INTERVENANT : "intervient"
  ENTRETIEN ||--o{ EVALUATION_CANDIDAT : "produit"
  UTILISATEUR ||--o{ EVALUATION_CANDIDAT : "redige"
  CANDIDATURE }o--o| DOCUMENT : "CV joint"
  CANDIDAT ||--o{ DOCUMENT : "possede"

  OFFRE_EMPLOI {
    bigint id PK
    string reference UK
    bigint demande_id FK
    string titre
    text description
    text profil_recherche
    int nombre_postes
    int nombre_niveaux_selection "defaut 3"
    string canal_publication
    date date_publication
    date date_cloture
    string statut
    bigint cree_par_id FK
  }
  CANDIDAT {
    bigint id PK
    string nom
    string prenom
    string email UK
    string telephone
    date date_naissance
    string adresse
    string nationalite
    datetime date_creation
    boolean consentement_donnees
  }
  CANDIDATURE {
    bigint id PK
    bigint candidat_id FK
    bigint offre_id FK
    bigint cv_document_id FK
    text lettre_motivation
    datetime date_depot
    string statut
    int score_preselection
    string motif_refus
    string decision_finale "RETENU, NON_RETENU, LISTE_ATTENTE"
    int rang_liste_attente
    bigint decideur_id FK
    datetime date_decision
    text motif_decision
  }
  ENTRETIEN {
    bigint id PK
    bigint candidature_id FK
    int niveau "1, 2 ou 3"
    string type "ENTRETIEN_RH, TEST_TECHNIQUE, ENTRETIEN_FINAL"
    datetime date_heure
    int duree_minutes
    string lieu_ou_lien
    string statut
    datetime date_creation
  }
  ENTRETIEN_INTERVENANT {
    bigint id PK
    bigint entretien_id FK
    bigint utilisateur_id FK
    string role_intervenant
  }
  EVALUATION_CANDIDAT {
    bigint id PK
    bigint entretien_id FK
    bigint evaluateur_id FK
    decimal note_globale
    text grille_criteres "JSON : critere + note"
    string avis "FAVORABLE, RESERVE, DEFAVORABLE"
    text commentaire
    datetime date_evaluation
    boolean verrouillee
  }
  DOCUMENT {
    bigint id PK
    string type "CV, LETTRE, CONTRAT, PROPOSITION, FICHE_SALARIE..."
    string nom_fichier
    string chemin_stockage
    string hash_sha256
    int version
    string entite_type "lien logique"
    bigint entite_id "lien logique"
    bigint depose_par_id FK
    datetime date_depot
    boolean archive
  }
```

### 5.3 Diagramme C – Embauche, contrat et onboarding

```mermaid
erDiagram
  CANDIDATURE ||--o{ PROPOSITION_EMBAUCHE : "aboutit a"
  CANDIDATURE |o--o| EMPLOYE : "donne naissance a"
  EMPLOYE ||--o{ CONTRAT : "est lie par"
  CONTRAT |o--o| CONTRAT : "succede a"
  EMPLOYE ||--o{ AFFECTATION : "est affecte"
  POSTE ||--o{ AFFECTATION : "est occupe via"
  CONTRAT |o--o{ AFFECTATION : "justifie"
  EMPLOYE |o--o{ AFFECTATION : "encadre (manager)"
  EMPLOYE ||--o{ ONBOARDING : "suit"
  CONTRAT ||--o| ONBOARDING : "declenche"
  MODELE_ONBOARDING ||--o{ ONBOARDING : "instancie"
  MODELE_ONBOARDING ||--|{ MODELE_TACHE : "contient"
  ONBOARDING ||--|{ ONBOARDING_TACHE : "comprend"
  MODELE_TACHE ||--o{ ONBOARDING_TACHE : "genere"
  ONBOARDING_TACHE }o--o| DOCUMENT : "preuve"
  CONTRAT }o--o| DOCUMENT : "contrat signe"

  PROPOSITION_EMBAUCHE {
    bigint id PK
    bigint candidature_id FK
    bigint poste_id FK
    string type_contrat_propose "ESSAI par defaut"
    decimal salaire_propose
    string devise
    string horaires_travail "ex. 40h/sem, lun-ven 8h-17h"
    string lieu_travail
    date date_debut_prevue
    int duree_essai_mois
    date date_limite_reponse
    int nb_prolongations_delai
    string statut
    datetime date_envoi
    datetime date_reponse
    string motif_refus
    bigint document_id FK
  }
  EMPLOYE {
    bigint id PK
    string matricule UK "ex. EMP-2026-0001"
    string nom
    string prenom
    string email_pro
    string email_perso
    string telephone
    date date_naissance
    string adresse
    string numero_cnaps "ou securite sociale"
    date date_embauche
    string statut "PRE_EMBAUCHE, ACTIF, SUSPENDU, SORTI"
    bigint candidature_origine_id FK "nullable"
    datetime date_creation
  }
  CONTRAT {
    bigint id PK
    string numero UK
    bigint employe_id FK
    string type "ESSAI, CDD, CDI"
    int numero_renouvellement "0 = initial, 1 = renouvele"
    bigint contrat_precedent_id FK
    date date_debut
    date date_fin_prevue "NULL pour CDI"
    date date_fin_reelle
    decimal salaire_base
    string devise
    string horaires_travail
    string lieu_travail
    string statut
    string motif_fin
    bigint document_signe_id FK
    datetime date_signature
  }
  AFFECTATION {
    bigint id PK
    bigint employe_id FK
    bigint poste_id FK
    bigint contrat_id FK
    bigint manager_id FK "Employe"
    date date_debut
    date date_fin "NULL = en cours"
    decimal taux_occupation "100 = temps plein"
    string motif "EMBAUCHE, MUTATION, PROMOTION..."
  }
  MODELE_ONBOARDING {
    bigint id PK
    string nom
    string type_contrat "optionnel"
    boolean actif
  }
  MODELE_TACHE {
    bigint id PK
    bigint modele_id FK
    int ordre
    string code "CONTRAT_SIGNE, COMPTE_SI, BADGE, PAIE..."
    string libelle
    boolean obligatoire
    string role_responsable "RH, IT, SG, PAIE, MANAGER"
    int echeance_jours "relatif a la date de debut, ex. -7, 0, +30"
  }
  ONBOARDING {
    bigint id PK
    bigint employe_id FK
    bigint contrat_id FK
    bigint modele_id FK
    bigint responsable_rh_id FK
    date date_debut
    date date_cible
    string statut
    datetime date_cloture
  }
  ONBOARDING_TACHE {
    bigint id PK
    bigint onboarding_id FK
    bigint modele_tache_id FK
    string libelle
    boolean obligatoire
    string role_responsable
    bigint assigne_a_id FK "Utilisateur"
    date echeance
    string statut "A_FAIRE, EN_COURS, FAIT, NON_APPLICABLE"
    datetime date_realisation
    bigint preuve_document_id FK
    text commentaire
  }
```

### 5.4 Entités transverses

| Entité | Rôle | Attributs principaux |
|--------|------|----------------------|
| **HistoriqueStatut** | Trace chaque changement de statut de n'importe quel objet métier | id, entite_type, entite_id, ancien_statut, nouveau_statut, auteur_id, date_changement, commentaire |
| **JournalAudit** | Trace technique de toute création / modification / suppression logique | id, entite_type, entite_id, action, valeurs_avant (JSON), valeurs_apres (JSON), auteur_id, date_action, adresse_ip |
| **Notification** | Notifications internes, email et push mobile | id, destinataire_id, type, message, entite_type, entite_id, canal, lue, date_creation |
| **Parametre** | Valeurs configurables (seuils, délais, durées) | cle, valeur, description |

Paramètres prévus : `delai_min_embauche_jours`, `coef_charges_patronales`, `duree_budget_mois`, `delai_reponse_proposition_jours`, `duree_prolongation_delai_jours`, `essai_duree_max_mois`, `essai_cumul_max_mois`, `cdd_duree_max_mois`, `alerte_fin_essai_jours`, `conservation_candidats_mois`.

### 5.5 Cardinalités (notation Merise)

Lecture : « chaque entité A participe à l'association avec la cardinalité indiquée à gauche ; chaque entité B avec celle indiquée à droite ».

| Entité A | Card. A | Association | Card. B | Entité B |
|----------|---------|-------------|---------|----------|
| POSTE | 1,1 | appartient à | 0,n | DEPARTEMENT |
| POSTE | 1,1 | est classé dans | 0,n | GRILLE_SALARIALE |
| DEMANDE_RECRUTEMENT | 1,1 | concerne | 0,n | POSTE |
| DEMANDE_RECRUTEMENT | 1,1 | est émise par | 0,n | DEPARTEMENT |
| DEMANDE_RECRUTEMENT | 1,1 | est créée par | 0,n | UTILISATEUR |
| DEMANDE_RECRUTEMENT | 0,1 | remplace | 0,n | EMPLOYE |
| VALIDATION | 1,1 | porte sur | 3,n | DEMANDE_RECRUTEMENT |
| OFFRE_EMPLOI | 1,1 | est issue de | 0,n | DEMANDE_RECRUTEMENT |
| CANDIDATURE | 1,1 | répond à | 0,n | OFFRE_EMPLOI |
| CANDIDATURE | 1,1 | est déposée par | 0,n | CANDIDAT |
| ENTRETIEN | 1,1 | concerne | 0,3 | CANDIDATURE |
| ENTRETIEN | 1,n | est mené par | 0,n | UTILISATEUR (via ENTRETIEN_INTERVENANT) |
| EVALUATION_CANDIDAT | 1,1 | évalue | 0,n | ENTRETIEN |
| PROPOSITION_EMBAUCHE | 1,1 | est faite à partir de | 0,n | CANDIDATURE |
| EMPLOYE | 0,1 | est issu de | 0,1 | CANDIDATURE |
| CONTRAT | 1,1 | lie | 0,n | EMPLOYE |
| CONTRAT | 0,1 | succède à | 0,1 | CONTRAT |
| AFFECTATION | 1,1 | affecte | 0,n | EMPLOYE |
| AFFECTATION | 1,1 | occupe | 0,n | POSTE |
| AFFECTATION | 0,1 | s'appuie sur | 0,n | CONTRAT |
| AFFECTATION | 0,1 | est encadrée par | 0,n | EMPLOYE (manager) |
| ONBOARDING | 1,1 | concerne | 0,n | EMPLOYE |
| ONBOARDING | 1,1 | est déclenché par | 0,1 | CONTRAT |
| ONBOARDING_TACHE | 1,1 | fait partie de | 1,n | ONBOARDING |
| MODELE_TACHE | 1,1 | fait partie de | 1,n | MODELE_ONBOARDING |

### 5.6 Contraintes d'intégrité principales

| Contrainte | Objet | Règle |
|-----------|-------|-------|
| Unicité | Candidature | Un couple (candidat, offre) unique |
| Unicité | Candidat | Email unique |
| Unicité | Employé | Matricule unique et jamais réutilisé |
| Unicité | Validation | Un couple (demande, cycle, ordre) unique |
| Unicité | Entretien | Un couple (candidature, niveau) actif unique |
| Non-chevauchement | Contrat | Un seul contrat ACTIF à un instant donné pour un employé |
| Non-chevauchement | Affectation | Une affectation ne chevauche pas une autre sur le même poste pour le même employé |
| Plafond | Affectation | Le nombre d'affectations actives sur un poste ≤ `effectif_autorise` |
| Cohérence | Contrat | `date_fin_prevue` obligatoire pour ESSAI et CDD, vide pour CDI |
| Immuabilité | Évaluation | Non modifiable une fois `verrouillee = true` |

---

## 6. Modèle UML – diagramme de classes du domaine

Vue orientée objet (avec les opérations métier principales) : elle sert de base aux **services métier** de l'API.

```mermaid
classDiagram
  class Departement {
    +Long id
    +String code
    +String nom
  }
  class GrilleSalariale {
    +String niveau
    +BigDecimal salaireMin
    +BigDecimal salaireMax
  }
  class Poste {
    +Long id
    +String code
    +String intitule
    +int effectifAutorise
    +int occupationActuelle() int
    +int placesLibres() int
  }
  class DemandeRecrutement {
    +String reference
    +TypeDemande type
    +int effectif
    +String motif
    +BigDecimal budgetEstime
    +Date dateSouhaitee
    +StatutDemande statut
    +calculerBudget() BigDecimal
    +soumettre() void
    +annuler() void
  }
  class Validation {
    +int cycle
    +int ordre
    +RoleValidateur role
    +Decision decision
    +String commentaire
    +approuver() void
    +rejeter() void
    +retourner() void
  }
  class OffreEmploi {
    +String reference
    +int nombrePostes
    +StatutOffre statut
    +publier() void
    +cloturer() void
  }
  class Candidat {
    +String nom
    +String prenom
    +String email
  }
  class Candidature {
    +StatutCandidature statut
    +Date dateDepot
    +preselectionner() void
    +refuser(motif) void
    +retenir() void
  }
  class Entretien {
    +int niveau
    +TypeEntretien type
    +DateTime dateHeure
    +planifier() void
    +cloturer() void
  }
  class EvaluationCandidat {
    +BigDecimal note
    +Avis avis
    +String commentaire
    +verrouiller() void
  }
  class PropositionEmbauche {
    +BigDecimal salaire
    +String horaires
    +String lieu
    +Date dateLimite
    +StatutProposition statut
    +envoyer() void
    +accepter() Employe
    +refuser(motif) void
    +prolongerDelai() void
  }
  class Employe {
    +String matricule
    +StatutEmploye statut
    +genererMatricule() String
    +contratActif() Contrat
    +affectationsActives() List
  }
  class Contrat {
    +TypeContrat type
    +int numeroRenouvellement
    +Date dateDebut
    +Date dateFinPrevue
    +StatutContrat statut
    +valider() void
    +renouveler() Contrat
    +transformer(nouveauType) Contrat
    +terminer(motif) void
  }
  class Affectation {
    +Date dateDebut
    +Date dateFin
    +BigDecimal tauxOccupation
  }
  class Onboarding {
    +StatutOnboarding statut
    +genererTaches() void
    +estComplet() boolean
    +cloturer() void
  }
  class OnboardingTache {
    +String libelle
    +boolean obligatoire
    +StatutTache statut
    +marquerFait() void
  }
  class Document {
    +TypeDocument type
    +int version
    +String hash
  }

  Departement "1" --> "0..*" Poste : regroupe
  GrilleSalariale "1" --> "0..*" Poste : classe
  Poste "1" --> "0..*" DemandeRecrutement : concerne
  DemandeRecrutement "1" *-- "3..*" Validation : valide par
  DemandeRecrutement "1" --> "0..*" OffreEmploi : donne lieu à
  OffreEmploi "1" --> "0..*" Candidature : reçoit
  Candidat "1" --> "0..*" Candidature : dépose
  Candidature "1" *-- "0..3" Entretien : comporte
  Entretien "1" *-- "0..*" EvaluationCandidat : produit
  Candidature "1" --> "0..*" PropositionEmbauche : aboutit à
  Candidature "0..1" --> "0..1" Employe : donne naissance à
  Employe "1" --> "1..*" Contrat : lié par
  Contrat "0..1" --> "0..1" Contrat : succède à
  Employe "1" --> "0..*" Affectation : affecté
  Poste "1" --> "0..*" Affectation : occupé via
  Employe "1" --> "0..*" Onboarding : suit
  Onboarding "1" *-- "1..*" OnboardingTache : comprend
  Candidature --> Document : CV
  Contrat --> Document : contrat signé
```

---

## 7. Énumérations (valeurs des statuts et types)

| Énumération | Valeurs |
|-------------|---------|
| **TypeDemande** | `CREATION`, `REMPLACEMENT` |
| **MotifDemande** (création) | `CROISSANCE`, `NOUVEAU_PROJET`, `RESTRUCTURATION`, `RENFORT_TEMPORAIRE`, `AUTRE` |
| **MotifDemande** (remplacement) | `DEMISSION`, `LICENCIEMENT`, `RETRAITE`, `MUTATION`, `FIN_CONTRAT`, `DECES`, `AUTRE` |
| **StatutDemande** | `BROUILLON`, `EN_VALIDATION_RH`, `EN_VALIDATION_FINANCE`, `EN_VALIDATION_DG`, `RETOURNEE`, `REJETEE`, `VALIDEE`, `EN_RECRUTEMENT`, `POURVUE`, `ANNULEE` |
| **DecisionValidation** | `EN_ATTENTE`, `APPROUVEE`, `REJETEE`, `RETOURNEE` |
| **StatutOffre** | `BROUILLON`, `PUBLIEE`, `CLOTUREE`, `ANNULEE` |
| **StatutCandidature** | `RECUE`, `PRESELECTIONNEE`, `REFUSEE_DOSSIER`, `EN_ENTRETIEN`, `NON_RETENUE`, `LISTE_ATTENTE`, `RETENUE`, `PROPOSITION_ENVOYEE`, `EMBAUCHEE`, `DESISTEE` |
| **TypeEntretien** | `ENTRETIEN_RH` (niveau 1), `TEST_TECHNIQUE` (niveau 2), `ENTRETIEN_FINAL` (niveau 3) |
| **StatutEntretien** | `PLANIFIE`, `REPORTE`, `REALISE`, `ANNULE`, `CANDIDAT_ABSENT` |
| **Avis** | `FAVORABLE`, `RESERVE`, `DEFAVORABLE` |
| **StatutProposition** | `BROUILLON`, `ENVOYEE`, `DELAI_ACCORDE`, `ACCEPTEE`, `REFUSEE`, `EXPIREE`, `RETIREE` |
| **StatutEmploye** | `PRE_EMBAUCHE`, `ACTIF`, `SUSPENDU`, `SORTI` |
| **TypeContrat** | `ESSAI`, `CDD`, `CDI` |
| **StatutContrat** | `BROUILLON`, `A_SIGNER`, `ACTIF`, `TERMINE`, `ANNULE` |
| **MotifFinContrat** | `ECHEANCE`, `RENOUVELLEMENT`, `TRANSFORMATION`, `ESSAI_NON_CONCLUANT`, `DEMISSION`, `LICENCIEMENT`, `RETRAITE`, `RUPTURE_CONVENTIONNELLE`, `AUTRE` |
| **StatutOnboarding** | `A_DEMARRER`, `EN_COURS`, `TERMINE`, `ANNULE` |
| **StatutTache** | `A_FAIRE`, `EN_COURS`, `FAIT`, `NON_APPLICABLE` |
| **MotifAffectation** | `EMBAUCHE`, `MUTATION`, `PROMOTION`, `REORGANISATION` |

---

## 8. Machines à états (cycles de vie des objets)

### 8.1 DemandeRecrutement

```mermaid
stateDiagram-v2
  [*] --> BROUILLON
  BROUILLON --> EN_VALIDATION_RH : soumettre
  EN_VALIDATION_RH --> EN_VALIDATION_FINANCE : RH approuve
  EN_VALIDATION_RH --> RETOURNEE : RH retourne
  EN_VALIDATION_RH --> REJETEE : RH rejette
  EN_VALIDATION_FINANCE --> EN_VALIDATION_DG : Finance approuve
  EN_VALIDATION_FINANCE --> RETOURNEE : Finance retourne
  EN_VALIDATION_FINANCE --> REJETEE : Finance rejette
  EN_VALIDATION_DG --> VALIDEE : DG approuve
  EN_VALIDATION_DG --> RETOURNEE : DG retourne
  EN_VALIDATION_DG --> REJETEE : DG rejette
  RETOURNEE --> EN_VALIDATION_RH : re-soumettre, nouveau cycle
  RETOURNEE --> ANNULEE : abandonner
  BROUILLON --> ANNULEE : annuler
  VALIDEE --> EN_RECRUTEMENT : offre publiée
  EN_RECRUTEMENT --> POURVUE : effectif atteint
  EN_RECRUTEMENT --> ANNULEE : annuler
  REJETEE --> [*]
  POURVUE --> [*]
  ANNULEE --> [*]
```

### 8.2 Candidature

```mermaid
stateDiagram-v2
  [*] --> RECUE
  RECUE --> PRESELECTIONNEE : dossier retenu
  RECUE --> REFUSEE_DOSSIER : dossier refusé
  PRESELECTIONNEE --> EN_ENTRETIEN : niveau 1 planifié
  EN_ENTRETIEN --> EN_ENTRETIEN : niveau suivant
  EN_ENTRETIEN --> NON_RETENUE : avis défavorable
  EN_ENTRETIEN --> RETENUE : décision finale positive
  EN_ENTRETIEN --> LISTE_ATTENTE : bon profil, effectif atteint
  LISTE_ATTENTE --> RETENUE : place disponible
  RETENUE --> PROPOSITION_ENVOYEE : envoi de la proposition
  PROPOSITION_ENVOYEE --> EMBAUCHEE : acceptée
  PROPOSITION_ENVOYEE --> NON_RETENUE : refusée ou expirée
  RECUE --> DESISTEE : retrait du candidat
  PRESELECTIONNEE --> DESISTEE : retrait du candidat
  EN_ENTRETIEN --> DESISTEE : retrait du candidat
  REFUSEE_DOSSIER --> [*]
  NON_RETENUE --> [*]
  EMBAUCHEE --> [*]
  DESISTEE --> [*]
```

### 8.3 PropositionEmbauche

```mermaid
stateDiagram-v2
  [*] --> BROUILLON
  BROUILLON --> ENVOYEE : envoyer
  ENVOYEE --> ACCEPTEE : candidat accepte
  ENVOYEE --> REFUSEE : candidat refuse
  ENVOYEE --> DELAI_ACCORDE : candidat demande un délai
  DELAI_ACCORDE --> ACCEPTEE : candidat accepte
  DELAI_ACCORDE --> REFUSEE : candidat refuse
  DELAI_ACCORDE --> EXPIREE : date limite dépassée
  ENVOYEE --> EXPIREE : date limite dépassée
  ENVOYEE --> RETIREE : retrait par la RH
  DELAI_ACCORDE --> RETIREE : retrait par la RH
  ACCEPTEE --> [*]
  REFUSEE --> [*]
  EXPIREE --> [*]
  RETIREE --> [*]
```

### 8.4 Contrat

```mermaid
stateDiagram-v2
  [*] --> BROUILLON
  BROUILLON --> A_SIGNER : générer et envoyer
  A_SIGNER --> ACTIF : signature + date de début atteinte
  A_SIGNER --> ANNULE : signature impossible
  BROUILLON --> ANNULE : annuler
  ACTIF --> TERMINE : échéance, renouvellement, transformation ou rupture
  TERMINE --> [*]
  ANNULE --> [*]
```

Le renouvellement ou la transformation termine le contrat courant (`motif_fin`) **et crée un nouveau contrat en BROUILLON** chaîné au précédent.

### 8.5 Employé

```mermaid
stateDiagram-v2
  [*] --> PRE_EMBAUCHE : proposition acceptée
  PRE_EMBAUCHE --> ACTIF : date de début du contrat atteinte
  PRE_EMBAUCHE --> SORTI : désistement avant début
  ACTIF --> SUSPENDU : suspension
  SUSPENDU --> ACTIF : reprise
  ACTIF --> SORTI : rupture
  SORTI --> [*]
```

---

## 9. Workflows détaillés

### 9.1 Workflow de validation de la demande (Manager → RH → Finance → DG)

| Ordre | Validateur | Contrôle effectué | Issues possibles |
|-------|-----------|-------------------|------------------|
| 1 | **RH** | Poste existant, grille salariale, budget estimé cohérent, motif valide, effectif ≤ places libres (création) ou employé à remplacer valide | Approuver / Retourner / Rejeter |
| 2 | **Finance** | Enveloppe budgétaire disponible pour le département | Approuver / Retourner / Rejeter |
| 3 | **Direction Générale** | Opportunité stratégique | Approuver / Retourner / Rejeter |

**Mécanique :**
1. À la soumission, le système crée les 3 lignes `Validation` du cycle courant (ordre 1, 2, 3), la première en `EN_ATTENTE`.
2. Quand un validateur **approuve**, la ligne passe en `APPROUVEE` et la suivante passe en `EN_ATTENTE`.
3. Quand un validateur **retourne** : le statut devient `RETOURNEE`, le manager corrige et re-soumet. Un **nouveau cycle** (`cycle + 1`) est créé : les anciens cycles restent consultables.
4. Quand un validateur **rejette** : statut `REJETEE` (final), commentaire obligatoire.
5. Quand la dernière validation (DG) est approuvée : statut `VALIDEE`, notification à la RH qui peut ouvrir le recrutement.

### 9.2 Workflow de sélection des candidats

1. **Réception** : candidature `RECUE`, CV archivé et daté.
2. **Présélection sur dossier** par la RH : `PRESELECTIONNEE` ou `REFUSEE_DOSSIER` (motif).
3. **Niveau 1 : entretien RH** → évaluation → avis.
4. **Niveau 2 : test technique** (seulement si niveau 1 favorable) → évaluation → avis.
5. **Niveau 3 : entretien final** (seulement si niveau 2 favorable) → évaluation → avis.
6. **Décision finale** (RH + Manager) : `RETENU`, `LISTE_ATTENTE` ou `NON_RETENU`, avec date, auteur et motif.
7. **Notification** automatique au candidat à chaque issue.

### 9.3 Workflow de la proposition d'embauche

1. La RH prépare la proposition à partir de la candidature `RETENUE` (salaire dans la fourchette de la grille, horaires, lieu, date de début, type de contrat, date limite de réponse).
2. Envoi au candidat → `ENVOYEE`.
3. Le candidat dispose de **trois options** :
   - **Accepter** → création de l'employé + matricule + export PDF + contrat d'essai.
   - **Refuser** → motif enregistré, la RH peut proposer au candidat suivant de la liste d'attente.
   - **Attendre** (demande de délai) → `DELAI_ACCORDE`, nouvelle date limite (une seule prolongation).
4. Si la date limite est dépassée sans réponse : `EXPIREE`.

### 9.4 Workflow d'onboarding

1. À la création du contrat, génération des tâches depuis le `ModeleOnboarding`.
2. Tâches **obligatoires** : contrat signé, compte (accès SI), badge, inscription en paie.
3. Tâches **facultatives** : ouverture du compte bancaire, matériel nécessaire, formations.
4. Chaque tâche a un responsable (rôle), une échéance relative à la date de début et un statut.
5. L'onboarding passe `EN_COURS` à la première tâche démarrée, puis `TERMINE` quand **toutes les tâches obligatoires** sont `FAIT`.
6. Les tâches facultatives non traitées peuvent être marquées `NON_APPLICABLE`.

### 9.5 Modèle de checklist d'onboarding par défaut

| Ordre | Code | Tâche | Obligatoire | Responsable | Échéance |
|-------|------|-------|-------------|-------------|----------|
| 1 | CONTRAT_SIGNE | Contrat signé et archivé | Oui | RH | J-3 |
| 2 | PAIE | Inscription en paie | Oui | Paie | J-3 |
| 3 | COMPTE_SI | Création des comptes (email, applications) | Oui | IT | J-2 |
| 4 | BADGE | Remise du badge | Oui | Services généraux | J0 |
| 5 | MATERIEL | Matériel nécessaire (poste, téléphone…) | Non | IT / Services généraux | J-1 |
| 6 | COMPTE_BANCAIRE | Ouverture du compte bancaire | Non | RH | J+15 |
| 7 | FORMATION | Formations d'intégration | Non | Manager / RH | J+30 |

---

## 10. Règles de gestion (RG)

### Bloc 1 – Besoin et demande

| Code | Règle |
|------|-------|
| RG-01 | Un **Poste** est créé et maintenu par la RH, indépendamment de tout employé. Il rattache un département, une grille salariale et un `effectif_autorise`. |
| RG-02 | Un poste peut avoir **plusieurs occupants** simultanés. Le nombre d'affectations actives ne dépasse jamais `effectif_autorise`. |
| RG-03 | Une demande est de type `CREATION` ou `REMPLACEMENT`. En `REMPLACEMENT`, l'employé remplacé est obligatoire et l'effectif demandé ≤ nombre d'employés concernés. |
| RG-04 | Pour une `CREATION`, l'effectif demandé ≤ places libres du poste (`effectif_autorise` − occupation actuelle − demandes en cours). Sinon la RH doit d'abord relever l'effectif autorisé du poste. |
| RG-05 | Le **budget estimé** est **calculé** : salaire de référence de la grille × effectif × durée budgétée × (1 + coefficient de charges). Le manager ne le saisit pas librement. |
| RG-06 | La date souhaitée ≥ date du jour + délai minimal (paramètre `delai_min_embauche_jours`). |
| RG-07 | Un manager ne crée de demande que pour **son** département. |
| RG-08 | La chaîne de validation est **séquentielle et obligatoire** : RH → Finance → DG, sans saut d'étape. |
| RG-09 | Un validateur ne peut pas valider une demande qu'il a lui-même créée (séparation des rôles). |
| RG-10 | Un **rejet** ou un **retour** exige un commentaire. Un rejet est définitif. |
| RG-11 | Une demande soumise n'est plus modifiable. Seul l'état `RETOURNEE` permet la correction. |
| RG-12 | Un retour crée un **nouveau cycle** de validation ; l'historique des cycles précédents est conservé. |
| RG-13 | À l'approbation finale du DG, la demande passe `VALIDEE` et le recrutement peut être ouvert. |

### Bloc 2 – Recrutement

| Code | Règle |
|------|-------|
| RG-14 | Une **offre** ne peut être créée qu'à partir d'une demande `VALIDEE` ou `EN_RECRUTEMENT`. Son nombre de postes ≤ effectif restant à pourvoir. |
| RG-15 | La publication et la clôture d'une offre sont réservées à la RH. |
| RG-16 | Un **candidat** est unique (email). Il peut postuler à plusieurs offres, mais **une seule candidature par offre**. |
| RG-17 | Une candidature n'est acceptée que si l'offre est `PUBLIEE` et avant sa date de clôture. |
| RG-18 | Un refus au stade du dossier exige un motif. |
| RG-19 | La sélection comporte **3 niveaux séquentiels** (par défaut : entretien RH, test technique, entretien final). Le niveau n+1 n'est planifiable que si le niveau n a un avis `FAVORABLE`. |
| RG-20 | Chaque entretien a au moins un intervenant et reçoit au moins une évaluation avant de pouvoir être clôturé. |
| RG-21 | Une évaluation est **verrouillée** après validation. Toute correction passe par une nouvelle version tracée, jamais par une modification silencieuse. |
| RG-22 | La **décision finale** (RH + Manager) n'est possible qu'après tous les niveaux requis par l'offre. Elle est datée, signée et motivée. |
| RG-23 | Aucune donnée de recrutement n'est supprimée physiquement (CV, évaluations, décisions) : suppression logique et archivage uniquement. |
| RG-24 | Quand le nombre de propositions acceptées atteint l'effectif demandé, l'offre est **clôturée** automatiquement et la demande passe `POURVUE`. Les autres candidats bien classés restent en `LISTE_ATTENTE`. |
| RG-25 | Les données des candidats non retenus ne sont conservées que pour la durée paramétrée (`conservation_candidats_mois`), sauf consentement de maintien en vivier. |

### Bloc 3 – Proposition d'embauche

| Code | Règle |
|------|-------|
| RG-26 | Une proposition n'est créée que pour une candidature `RETENUE`. |
| RG-27 | La proposition mentionne obligatoirement : poste, type de contrat, **montant du salaire**, **horaires de travail**, **lieu de travail**, date de début, durée d'essai, date limite de réponse. Le salaire reste dans la fourchette de la grille ; au-delà, une dérogation DG est nécessaire. |
| RG-28 | Le candidat a **trois choix** : accepter, refuser, ou demander un délai. Un délai ne peut être accordé **qu'une seule fois**. |
| RG-29 | Sans réponse à la date limite, la proposition passe `EXPIREE` automatiquement. |
| RG-30 | En cas de refus ou d'expiration, la RH peut proposer au candidat suivant de la liste d'attente tant que l'effectif n'est pas atteint. |
| RG-31 | À l'**acceptation**, le système crée automatiquement l'**employé** (statut `PRE_EMBAUCHE`) avec un **matricule unique** (format `EMP-AAAA-NNNN`), jamais réutilisé, et génère la fiche salarié en **PDF**. |
| RG-32 | Le `Candidat` et la `Candidature` restent intacts après l'embauche. L'employé garde un lien vers la candidature d'origine (traçabilité). |

### Bloc 4 – Contrat et onboarding

| Code | Règle |
|------|-------|
| RG-33 | Le contrat créé par défaut après acceptation est le **contrat d'essai**. |
| RG-34 | **Contrat d'essai** : durée initiale ≤ **6 mois**, renouvelable **une seule fois**, durée cumulée ≤ **12 mois**. |
| RG-35 | **CDD** : durée ≤ **2 ans**, renouvelable **une seule fois**. Il est facultatif (non systématique). |
| RG-36 | **CDI** : pas de date de fin. |
| RG-37 | Un employé peut avoir **plusieurs contrats successifs** (essai, essai renouvelé, CDD, CDI…). Chacun est une ligne distincte chaînée au précédent. Aucune valeur n'est écrasée. |
| RG-38 | Un seul contrat `ACTIF` à la fois par employé. Le type de contrat « courant » est **dérivé** du contrat actif, jamais stocké sur l'employé. |
| RG-39 | Un contrat ne devient `ACTIF` que si le document signé est déposé et la date de début atteinte. |
| RG-40 | Alertes automatiques à la RH avant la fin d'un essai ou d'un CDD (paramètre `alerte_fin_essai_jours`, par défaut 30 jours). |
| RG-41 | À l'activation du contrat, une **Affectation** initiale est créée (poste, département, manager, date de début). Les changements ultérieurs ferment l'affectation précédente et en ouvrent une nouvelle. |
| RG-42 | L'**onboarding** est créé automatiquement avec le contrat, avec les tâches du modèle correspondant. |
| RG-43 | Tâches **obligatoires** : contrat signé, compte, badge, inscription en paie. Tâches **facultatives** : ouverture du compte bancaire, matériel, formations. |
| RG-44 | L'onboarding passe `TERMINE` quand toutes les tâches obligatoires sont `FAIT`. |
| RG-45 | L'employé passe `ACTIF` à la date de début du contrat actif. |

### Règles transverses

| Code | Règle |
|------|-------|
| RG-46 | Toute création, modification ou changement de statut est enregistré (qui, quand, avant, après). |
| RG-47 | Pas de suppression physique des données métier : suppression logique et archivage. |
| RG-48 | Chaque action est contrôlée par le **rôle** de l'utilisateur (section 12). |
| RG-49 | Notifications automatiques à chaque transition importante (demande à valider, décision, entretien planifié, proposition envoyée, tâche d'onboarding assignée, échéance proche). |
| RG-50 | Les documents (CV, proposition, contrat, fiche salarié) sont stockés avec version, empreinte (hash) et date de dépôt. |

---

## 11. Traçabilité et audit

| Exigence du sujet | Réponse dans le modèle |
|-------------------|------------------------|
| « CV, date, candidat » traçables | `Candidature.cv_document_id` → `Document` (version, date, hash) + `date_depot` + lien `candidat_id` |
| « Toutes les historiques doivent être traçables » | `HistoriqueStatut` (transitions) + `JournalAudit` (valeurs avant/après) sur tous les objets |
| Historique des validations | `Validation` conserve chaque décision de chaque cycle |
| Historique des contrats | Un contrat = une ligne, chaînée via `contrat_precedent_id` |
| Historique des postes occupés | `Affectation` avec `date_debut` / `date_fin` |
| Historique des évaluations | `EvaluationCandidat` verrouillée, versions tracées |
| Décision finale | `Candidature.decision_finale` + décideur + date + motif, et `HistoriqueStatut` |

---

## 12. Droits par rôle

Légende : **C** = créer, **L** = lire, **M** = modifier, **V** = valider / décider, **–** = aucun accès. *(own)* = limité à ses propres objets / son périmètre.

| Fonction | Manager | RH | Finance | DG | Intervenant | IT / SG | Paie | Salarié |
|----------|---------|----|---------|----|-------------|---------|------|---------|
| Départements, postes, grilles | L | C, M, L | L | L | – | – | – | – |
| Demande de recrutement | C, M (own, brouillon), L (own dép.) | L, V (étape 1) | L, V (étape 2) | L, V (étape 3) | – | – | – | – |
| Offre d'emploi | L | C, M, L | – | – | – | – | – | – |
| Candidatures | L (own dép.) | C, M, L | – | – | L (ses entretiens) | – | – | – |
| Entretiens | L | C, M, L | – | – | L (own) | – | – | – |
| Évaluations | L, C (si intervenant) | L | – | – | C, L (own) | – | – | – |
| Décision finale | V | V | – | – | – | – | – | – |
| Proposition d'embauche | L | C, M, L | – | V (dérogation salaire) | – | – | – | – |
| Dossier employé | L (own équipe) | C, M, L | – | – | – | – | L (limité) | L (own) |
| Contrat | L (own équipe) | C, M, L | – | – | – | – | L | L (own) |
| Onboarding / tâches | L, M (ses tâches) | C, M, L | – | – | – | M (ses tâches) | M (ses tâches) | L (own) |
| Journal d'audit | – | L | – | L | – | – | – | – |
| Paramètres / référentiels | – | L | – | – | – | – | – | – |

L'**Administrateur** gère comptes, rôles et paramètres, sans accès aux contenus RH sensibles par défaut.

---

## 13. Cas d'utilisation du MVP

| ID | Cas d'utilisation | Acteur principal | Bloc |
|----|-------------------|------------------|------|
| UC-01 | Gérer les départements, postes et grilles salariales | RH | 1 |
| UC-02 | Créer et soumettre une demande de recrutement | Manager | 1 |
| UC-03 | Valider, retourner ou rejeter une demande | RH, Finance, DG | 1 |
| UC-04 | Suivre l'état et l'historique d'une demande | Manager, RH | 1 |
| UC-05 | Créer et publier une offre d'emploi | RH | 2 |
| UC-06 | Enregistrer et présélectionner les candidatures | RH | 2 |
| UC-07 | Planifier un entretien ou un test (niveaux 1 à 3) | RH | 2 |
| UC-08 | Saisir l'évaluation d'un entretien | Intervenant | 2 |
| UC-09 | Prendre la décision finale | RH, Manager | 2 |
| UC-10 | Préparer et envoyer la proposition d'embauche | RH | 3 |
| UC-11 | Enregistrer la réponse du candidat (accepter, refuser, délai) | RH (ou Candidat via lien) | 3 |
| UC-12 | Créer automatiquement l'employé + matricule + fiche PDF | Système | 3 |
| UC-13 | Générer et activer le contrat d'essai | RH | 4 |
| UC-14 | Renouveler ou transformer un contrat (essai → CDI ou CDD…) | RH | 4 |
| UC-15 | Piloter la checklist d'onboarding | RH, IT, Paie, Manager | 4 |
| UC-16 | Consulter son onboarding et signer (mobile) | Salarié | 4 |
| UC-17 | Valider rapidement une demande depuis le mobile + notifications | Manager, DG | 1 |

---

## 14. Périmètre MVP / hors périmètre

| Inclus dans le MVP | Modélisé mais hors MVP (extensions) |
|--------------------|-------------------------------------|
| Blocs 1 à 4 de bout en bout | **Vie de salarié** : congés, absences, présence, dossier médical |
| Workflow de validation à 3 niveaux | **Évolution** : promotion, mutation, avenant, révision salariale (l'`Affectation` est déjà prête) |
| Offres, candidatures, 3 niveaux de sélection | **Rupture** : démission, licenciement, solde de tout compte, déclenchement d'une demande de remplacement |
| Proposition d'embauche avec 3 réponses | Portail candidat externe complet |
| Création automatique employé + matricule + PDF | Paie complète (seule l'inscription est suivie) |
| Contrats successifs avec règles de durée | Signature électronique légale |
| Checklist d'onboarding | Reporting / BI avancé |
| Audit, historique, rôles, notifications | Intégrations externes (job boards, comptabilité) |

Pistes futures dans le modèle : `Avenant`, `Absence`, `DemandeConge`, `Evolution`, `Rupture`, `SoldeToutCompte`.

---

## 15. Hypothèses et points à valider avec l'enseignant

Le sujet laisse certains points ouverts. Voici les choix retenus ici, à confirmer :

| # | Point | Hypothèse retenue |
|---|-------|-------------------|
| H1 | « CE par défaut » | **CE = contrat d'essai**. |
| H2 | « Essai : 6 mois, renouvelable 1 fois en 1 an » | Durée initiale ≤ 6 mois, 1 seul renouvellement, cumul ≤ 12 mois. |
| H3 | « CDD : 2 ans, renouvelable 1 fois » | Durée ≤ 2 ans, 1 seul renouvellement. **Ambigu** : le plafond de 2 ans s'applique-t-il au contrat initial seul, ou renouvellement inclus ? Modélisation prête pour les deux (paramètres). |
| H4 | « Validation Finance selon le budget » | Chaîne fixe RH → Finance → DG. Variante possible : seuil de budget en dessous duquel la Finance ou la DG est sautée (paramétrable, non retenue par défaut). |
| H5 | « 3 niveaux d'entretiens et tests » | Niveau 1 : entretien RH, niveau 2 : test technique, niveau 3 : entretien final (types paramétrables par offre). |
| H6 | « Accepter, refuser, attendre » | « Attendre » = demande d'un délai de réflexion, accordé une seule fois. |
| H7 | Matériel et formation | Listés à la fois dans les tâches d'onboarding et dans les éléments « facultatifs » : retenus comme **facultatifs**. Contrat signé, compte, badge et paie : **obligatoires**. |
| H8 | Mentions du contrat de travail | Salaire, horaires, lieu, date de début, durée d'essai (« à vérifier sur un modèle de contrat » : à comparer avec un modèle réel du droit du travail applicable, par ex. celui de Madagascar). |
| H9 | Retour d'un ancien salarié | Non traité dans le MVP (à décider : réactivation du matricule ou nouveau matricule). |
| H10 | Remplacement | L'employé remplacé reste dans la base (statut `SORTI` ou en cours de départ) pour la traçabilité. |

---

*Fin du document de conception. Étape suivante : architecture technique (modules Spring Boot, endpoints REST, sécurité), puis plan de développement du MVP.*
