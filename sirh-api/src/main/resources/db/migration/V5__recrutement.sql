-- V5__recrutement
-- =====================================================================
-- Module RECRUTEMENT : offre_emploi, candidat, candidature, entretien, entretien_intervenant, evaluation_candidat
-- Conventions :
--   * noms de tables et colonnes en snake_case, tables au singulier
--   * chaque table a : id, version, created_at, updated_at, created_by, updated_by
--   * dates en TIMESTAMPTZ (UTC)
--   * énumérations en VARCHAR + CHECK
--   * index uniques partiels pour entretien et evaluation_candidat
-- =====================================================================

-- Table offre_emploi
CREATE TABLE offre_emploi (
    id                         BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    version                    BIGINT       NOT NULL DEFAULT 0,

    reference                  VARCHAR(30)  NOT NULL,
    demande_id                 BIGINT       NOT NULL,
    titre                      VARCHAR(150) NOT NULL,
    description                TEXT         NOT NULL,
    profil_recherche           TEXT,
    nombre_postes              INT          NOT NULL,
    nombre_niveaux_selection   INT          NOT NULL DEFAULT 3,
    canal_publication          VARCHAR(100),
    date_publication           DATE,
    date_cloture               DATE,
    statut                     VARCHAR(20)  NOT NULL DEFAULT 'BROUILLON',

    created_at                 TIMESTAMPTZ  NOT NULL,
    updated_at                 TIMESTAMPTZ  NOT NULL,
    created_by                 VARCHAR(150) NOT NULL,
    updated_by                 VARCHAR(150) NOT NULL,

    CONSTRAINT ck_offre_emploi_statut CHECK (
        statut IN ('BROUILLON', 'PUBLIEE', 'CLOTUREE', 'ANNULEE')
    ),
    CONSTRAINT ck_offre_emploi_nombre_postes CHECK (nombre_postes >= 1),
    CONSTRAINT ck_offre_emploi_niveaux_selection CHECK (nombre_niveaux_selection BETWEEN 1 AND 3)
);

CREATE UNIQUE INDEX uk_offre_emploi_reference ON offre_emploi (reference);
CREATE INDEX idx_offre_emploi_demande ON offre_emploi (demande_id);
CREATE INDEX idx_offre_emploi_statut ON offre_emploi (statut);

ALTER TABLE offre_emploi
    ADD CONSTRAINT fk_offre_emploi_demande
        FOREIGN KEY (demande_id) REFERENCES demande_recrutement(id);

-- Table candidat
CREATE TABLE candidat (
    id                     BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    version                BIGINT       NOT NULL DEFAULT 0,

    nom                    VARCHAR(100) NOT NULL,
    prenom                 VARCHAR(100) NOT NULL,
    email                  VARCHAR(150) NOT NULL,
    telephone              VARCHAR(30),
    date_naissance         DATE,
    adresse                VARCHAR(255),
    nationalite            VARCHAR(60),
    consentement_donnees   BOOLEAN      NOT NULL DEFAULT FALSE,

    created_at             TIMESTAMPTZ  NOT NULL,
    updated_at             TIMESTAMPTZ  NOT NULL,
    created_by             VARCHAR(150) NOT NULL,
    updated_by             VARCHAR(150) NOT NULL
);

-- Email unique sans tenir compte de la casse
CREATE UNIQUE INDEX uk_candidat_email_lower ON candidat (LOWER(email));

-- Table candidature
CREATE TABLE candidature (
    id                       BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    version                  BIGINT       NOT NULL DEFAULT 0,

    candidat_id              BIGINT       NOT NULL,
    offre_id                 BIGINT       NOT NULL,
    cv_document_id           BIGINT,
    lettre_motivation        TEXT,
    date_depot               TIMESTAMPTZ  NOT NULL,
    statut                   VARCHAR(30)  NOT NULL DEFAULT 'RECUE',
    score_preselection       INT,
    motif_refus              VARCHAR(500),
    decision_finale          VARCHAR(20),
    rang_liste_attente       INT,
    decideur_id              BIGINT,
    date_decision            TIMESTAMPTZ,
    motif_decision           TEXT,

    created_at               TIMESTAMPTZ  NOT NULL,
    updated_at               TIMESTAMPTZ  NOT NULL,
    created_by               VARCHAR(150) NOT NULL,
    updated_by               VARCHAR(150) NOT NULL,

    CONSTRAINT ck_candidature_statut CHECK (
        statut IN ('RECUE', 'PRESELECTIONNEE', 'REFUSEE_DOSSIER', 'EN_ENTRETIEN', 'NON_RETENUE',
                   'LISTE_ATTENTE', 'RETENUE', 'PROPOSITION_ENVOYEE', 'EMBAUCHEE', 'DESISTEE')
    ),
    CONSTRAINT ck_candidature_score_preselection CHECK (score_preselection IS NULL OR (score_preselection BETWEEN 0 AND 100)),
    CONSTRAINT ck_candidature_decision_finale CHECK (
        decision_finale IS NULL OR decision_finale IN ('RETENU', 'NON_RETENU', 'LISTE_ATTENTE')
    ),
    CONSTRAINT ck_candidature_rang_liste_attente CHECK (rang_liste_attente IS NULL OR rang_liste_attente >= 1)
);

CREATE UNIQUE INDEX uk_candidature_candidat_offre ON candidature (candidat_id, offre_id);
CREATE INDEX idx_candidature_offre ON candidature (offre_id);
CREATE INDEX idx_candidature_statut ON candidature (statut);

ALTER TABLE candidature
    ADD CONSTRAINT fk_candidature_candidat
        FOREIGN KEY (candidat_id) REFERENCES candidat(id),
    ADD CONSTRAINT fk_candidature_offre
        FOREIGN KEY (offre_id) REFERENCES offre_emploi(id),
    ADD CONSTRAINT fk_candidature_cv_document
        FOREIGN KEY (cv_document_id) REFERENCES document(id),
    ADD CONSTRAINT fk_candidature_decideur
        FOREIGN KEY (decideur_id) REFERENCES utilisateur(id);

-- Table entretien
CREATE TABLE entretien (
    id                BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    version           BIGINT       NOT NULL DEFAULT 0,

    candidature_id    BIGINT       NOT NULL,
    niveau            INT          NOT NULL,
    type              VARCHAR(30)  NOT NULL,
    date_heure        TIMESTAMPTZ  NOT NULL,
    duree_minutes     INT          NOT NULL DEFAULT 60,
    lieu_ou_lien      VARCHAR(255),
    statut            VARCHAR(20)  NOT NULL DEFAULT 'PLANIFIE',
    motif_annulation  VARCHAR(500),

    created_at        TIMESTAMPTZ  NOT NULL,
    updated_at        TIMESTAMPTZ  NOT NULL,
    created_by        VARCHAR(150) NOT NULL,
    updated_by        VARCHAR(150) NOT NULL,

    CONSTRAINT ck_entretien_niveau CHECK (niveau BETWEEN 1 AND 3),
    CONSTRAINT ck_entretien_type CHECK (
        type IN ('ENTRETIEN_RH', 'TEST_TECHNIQUE', 'ENTRETIEN_FINAL')
    ),
    CONSTRAINT ck_entretien_statut CHECK (
        statut IN ('PLANIFIE', 'REPORTE', 'REALISE', 'ANNULE', 'CANDIDAT_ABSENT')
    ),
    CONSTRAINT ck_entretien_duree_minutes CHECK (duree_minutes BETWEEN 15 AND 480)
);

-- Index unique partiel : un seul entretien actif par candidature et par niveau
CREATE UNIQUE INDEX uk_entretien_candidature_niveau ON entretien (candidature_id, niveau)
    WHERE statut <> 'ANNULE';

CREATE INDEX idx_entretien_candidature ON entretien (candidature_id);
CREATE INDEX idx_entretien_statut ON entretien (statut);
CREATE INDEX idx_entretien_date_heure ON entretien (date_heure);

ALTER TABLE entretien
    ADD CONSTRAINT fk_entretien_candidature
        FOREIGN KEY (candidature_id) REFERENCES candidature(id);

-- Table entretien_intervenant
CREATE TABLE entretien_intervenant (
    id                BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    version           BIGINT       NOT NULL DEFAULT 0,

    entretien_id      BIGINT       NOT NULL,
    utilisateur_id    BIGINT       NOT NULL,
    role_intervenant  VARCHAR(50),

    created_at        TIMESTAMPTZ  NOT NULL,
    updated_at        TIMESTAMPTZ  NOT NULL,
    created_by        VARCHAR(150) NOT NULL,
    updated_by        VARCHAR(150) NOT NULL
);

CREATE UNIQUE INDEX uk_entretien_intervenant ON entretien_intervenant (entretien_id, utilisateur_id);
CREATE INDEX idx_entretien_intervenant_utilisateur ON entretien_intervenant (utilisateur_id);

ALTER TABLE entretien_intervenant
    ADD CONSTRAINT fk_entretien_intervenant_entretien
        FOREIGN KEY (entretien_id) REFERENCES entretien(id),
    ADD CONSTRAINT fk_entretien_intervenant_utilisateur
        FOREIGN KEY (utilisateur_id) REFERENCES utilisateur(id);

-- Table evaluation_candidat
CREATE TABLE evaluation_candidat (
    id                     BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    version                BIGINT       NOT NULL DEFAULT 0,

    entretien_id           BIGINT       NOT NULL,
    evaluateur_id          BIGINT       NOT NULL,
    note_globale           NUMERIC(4,2) NOT NULL,
    grille_criteres        TEXT,
    avis                   VARCHAR(20)  NOT NULL,
    commentaire            TEXT,
    date_evaluation        TIMESTAMPTZ  NOT NULL,
    verrouillee            BOOLEAN      NOT NULL DEFAULT FALSE,
    active                 BOOLEAN      NOT NULL DEFAULT TRUE,
    evaluation_precedente_id BIGINT,

    created_at             TIMESTAMPTZ  NOT NULL,
    updated_at             TIMESTAMPTZ  NOT NULL,
    created_by             VARCHAR(150) NOT NULL,
    updated_by             VARCHAR(150) NOT NULL,

    CONSTRAINT ck_evaluation_note_globale CHECK (note_globale BETWEEN 0 AND 20),
    CONSTRAINT ck_evaluation_avis CHECK (avis IN ('FAVORABLE', 'RESERVE', 'DEFAVORABLE'))
);

-- Index unique partiel : une seule évaluation active par entretien et par évaluateur
CREATE UNIQUE INDEX uk_evaluation_active ON evaluation_candidat (entretien_id, evaluateur_id)
    WHERE active;

CREATE INDEX idx_evaluation_entretien ON evaluation_candidat (entretien_id);
CREATE INDEX idx_evaluation_evaluateur ON evaluation_candidat (evaluateur_id);
CREATE INDEX idx_evaluation_precedente ON evaluation_candidat (evaluation_precedente_id);

ALTER TABLE evaluation_candidat
    ADD CONSTRAINT fk_evaluation_entretien
        FOREIGN KEY (entretien_id) REFERENCES entretien(id),
    ADD CONSTRAINT fk_evaluation_evaluateur
        FOREIGN KEY (evaluateur_id) REFERENCES utilisateur(id),
    ADD CONSTRAINT fk_evaluation_precedente
        FOREIGN KEY (evaluation_precedente_id) REFERENCES evaluation_candidat(id);
