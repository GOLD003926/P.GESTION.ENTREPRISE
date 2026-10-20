-- V3__transverse
-- =====================================================================
-- Module COMMON : tables transverses (document, historique_statut, journal_audit, notification)
-- Séquences de numérotation
-- =====================================================================

-- Table document
CREATE TABLE document (
    id              BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    version         BIGINT       NOT NULL DEFAULT 0,

    type            VARCHAR(30)  NOT NULL,
    nom_fichier     VARCHAR(255) NOT NULL,
    type_mime       VARCHAR(100) NOT NULL,
    taille_octets   BIGINT       NOT NULL,
    chemin_stockage VARCHAR(500) NOT NULL,
    hash_sha256     VARCHAR(64)  NOT NULL,
    numero_version  INT          NOT NULL DEFAULT 1,

    entite_type     VARCHAR(50)  NOT NULL,
    entite_id       BIGINT       NOT NULL,

    archive         BOOLEAN      NOT NULL DEFAULT FALSE,

    created_at      TIMESTAMPTZ  NOT NULL,
    updated_at      TIMESTAMPTZ  NOT NULL,
    created_by      VARCHAR(150) NOT NULL,
    updated_by      VARCHAR(150) NOT NULL,

    CONSTRAINT ck_document_type CHECK (
        type IN ('CV', 'LETTRE_MOTIVATION', 'PROPOSITION_EMBAUCHE', 'CONTRAT', 'CONTRAT_SIGNE', 'FICHE_SALARIE', 'PREUVE_ONBOARDING', 'AUTRE')
    ),
    CONSTRAINT ck_document_taille CHECK (taille_octets > 0),
    CONSTRAINT ck_document_entite_type CHECK (
        entite_type IN ('UTILISATEUR', 'DEPARTEMENT', 'GRILLE_SALARIALE', 'POSTE', 'DEMANDE_RECRUTEMENT', 'VALIDATION', 'OFFRE_EMPLOI', 'CANDIDAT', 'CANDIDATURE', 'ENTRETIEN', 'EVALUATION_CANDIDAT', 'PROPOSITION_EMBAUCHE', 'EMPLOYE', 'AFFECTATION', 'CONTRAT', 'MODELE_ONBOARDING', 'ONBOARDING', 'ONBOARDING_TACHE')
    )
);

CREATE INDEX idx_document_entite ON document (entite_type, entite_id, type);

-- Table historique_statut (insertion seule)
CREATE TABLE historique_statut (
    id              BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    version         BIGINT       NOT NULL DEFAULT 0,

    entite_type     VARCHAR(50)  NOT NULL,
    entite_id       BIGINT       NOT NULL,
    ancien_statut   VARCHAR(40),
    nouveau_statut  VARCHAR(40)  NOT NULL,
    auteur          VARCHAR(150) NOT NULL,
    date_changement TIMESTAMPTZ  NOT NULL,
    commentaire     TEXT,

    CONSTRAINT ck_historique_statut_entite_type CHECK (
        entite_type IN ('UTILISATEUR', 'DEPARTEMENT', 'GRILLE_SALARIALE', 'POSTE', 'DEMANDE_RECRUTEMENT', 'VALIDATION', 'OFFRE_EMPLOI', 'CANDIDAT', 'CANDIDATURE', 'ENTRETIEN', 'EVALUATION_CANDIDAT', 'PROPOSITION_EMBAUCHE', 'EMPLOYE', 'AFFECTATION', 'CONTRAT', 'MODELE_ONBOARDING', 'ONBOARDING', 'ONBOARDING_TACHE')
    )
);

CREATE INDEX idx_historique_statut_entite ON historique_statut (entite_type, entite_id);

-- Table journal_audit (insertion seule)
CREATE TABLE journal_audit (
    id              BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    version         BIGINT       NOT NULL DEFAULT 0,

    entite_type     VARCHAR(50)  NOT NULL,
    entite_id       BIGINT       NOT NULL,
    action          VARCHAR(30)  NOT NULL,
    valeurs_avant   TEXT,
    valeurs_apres   TEXT,
    auteur          VARCHAR(150) NOT NULL,
    date_action     TIMESTAMPTZ  NOT NULL,

    CONSTRAINT ck_journal_audit_action CHECK (action IN ('CREATION', 'MODIFICATION', 'SUPPRESSION_LOGIQUE')),
    CONSTRAINT ck_journal_audit_entite_type CHECK (
        entite_type IN ('UTILISATEUR', 'DEPARTEMENT', 'GRILLE_SALARIALE', 'POSTE', 'DEMANDE_RECRUTEMENT', 'VALIDATION', 'OFFRE_EMPLOI', 'CANDIDAT', 'CANDIDATURE', 'ENTRETIEN', 'EVALUATION_CANDIDAT', 'PROPOSITION_EMBAUCHE', 'EMPLOYE', 'AFFECTATION', 'CONTRAT', 'MODELE_ONBOARDING', 'ONBOARDING', 'ONBOARDING_TACHE')
    )
);

CREATE INDEX idx_journal_audit_entite ON journal_audit (entite_type, entite_id);

-- Table notification
CREATE TABLE notification (
    id              BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    version         BIGINT       NOT NULL DEFAULT 0,

    destinataire_id BIGINT       NOT NULL,
    type            VARCHAR(40)  NOT NULL,
    canal           VARCHAR(20)  NOT NULL DEFAULT 'INTERNE',
    titre           VARCHAR(150) NOT NULL,
    message         VARCHAR(500) NOT NULL,
    entite_type     VARCHAR(50),
    entite_id       BIGINT,
    lue             BOOLEAN      NOT NULL DEFAULT FALSE,
    date_lecture    TIMESTAMPTZ,

    created_at      TIMESTAMPTZ  NOT NULL,
    updated_at      TIMESTAMPTZ  NOT NULL,
    created_by      VARCHAR(150) NOT NULL,
    updated_by      VARCHAR(150) NOT NULL,

    CONSTRAINT fk_notification_destinataire FOREIGN KEY (destinataire_id) REFERENCES utilisateur(id),
    CONSTRAINT ck_notification_type CHECK (
        type IN ('DEMANDE_A_VALIDER', 'DEMANDE_DECISION', 'ENTRETIEN_PLANIFIE', 'EVALUATION_DEMANDEE', 'CANDIDATURE_DECISION', 'PROPOSITION_ENVOYEE', 'PROPOSITION_REPONDUE', 'PROPOSITION_EXPIREE', 'ONBOARDING_TACHE_ASSIGNEE', 'CONTRAT_ECHEANCE', 'AUTRE')
    ),
    CONSTRAINT ck_notification_canal CHECK (canal IN ('INTERNE', 'EMAIL', 'PUSH')),
    CONSTRAINT ck_notification_entite_type CHECK (
        entite_type IN ('UTILISATEUR', 'DEPARTEMENT', 'GRILLE_SALARIALE', 'POSTE', 'DEMANDE_RECRUTEMENT', 'VALIDATION', 'OFFRE_EMPLOI', 'CANDIDAT', 'CANDIDATURE', 'ENTRETIEN', 'EVALUATION_CANDIDAT', 'PROPOSITION_EMBAUCHE', 'EMPLOYE', 'AFFECTATION', 'CONTRAT', 'MODELE_ONBOARDING', 'ONBOARDING', 'ONBOARDING_TACHE')
    )
);

CREATE INDEX idx_notification_destinataire ON notification (destinataire_id, lue);

-- Séquences de numérotation
CREATE SEQUENCE seq_demande_recrutement START 1 INCREMENT 1;
CREATE SEQUENCE seq_offre_emploi START 1 INCREMENT 1;
CREATE SEQUENCE seq_employe_matricule START 1 INCREMENT 1;
CREATE SEQUENCE seq_contrat START 1 INCREMENT 1;
