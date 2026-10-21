-- V6__employe
-- =====================================================================
-- Module EMPLOYE : employe, affectation
-- Conventions :
--   * noms de tables et colonnes en snake_case, tables au singulier
--   * chaque table a : id, version, created_at, updated_at, created_by, updated_by
--   * dates en TIMESTAMPTZ (UTC)
--   * énumérations en VARCHAR + CHECK
-- =====================================================================

-- Table employe
CREATE TABLE employe (
    id                         BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    version                    BIGINT       NOT NULL DEFAULT 0,

    matricule                  VARCHAR(30)  NOT NULL,
    nom                        VARCHAR(100) NOT NULL,
    prenom                     VARCHAR(100) NOT NULL,
    email_pro                  VARCHAR(150),
    email_perso                VARCHAR(150),
    telephone                  VARCHAR(30),
    date_naissance             DATE,
    adresse                    VARCHAR(255),
    numero_securite_sociale    VARCHAR(30),
    date_embauche              DATE,
    statut                     VARCHAR(20)  NOT NULL DEFAULT 'PRE_EMBAUCHE',
    candidature_origine_id     BIGINT,

    created_at                 TIMESTAMPTZ  NOT NULL,
    updated_at                 TIMESTAMPTZ  NOT NULL,
    created_by                 VARCHAR(150) NOT NULL,
    updated_by                 VARCHAR(150) NOT NULL,

    CONSTRAINT ck_employe_statut CHECK (
        statut IN ('PRE_EMBAUCHE', 'ACTIF', 'SUSPENDU', 'SORTI')
    )
);

CREATE UNIQUE INDEX uk_employe_matricule ON employe (matricule);
CREATE UNIQUE INDEX uk_employe_email_pro ON employe (LOWER(email_pro)) WHERE email_pro IS NOT NULL;
CREATE UNIQUE INDEX uk_employe_candidature_origine ON employe (candidature_origine_id) WHERE candidature_origine_id IS NOT NULL;

-- Table affectation
CREATE TABLE affectation (
    id                BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    version           BIGINT       NOT NULL DEFAULT 0,

    employe_id        BIGINT       NOT NULL,
    poste_id          BIGINT       NOT NULL,
    contrat_id        BIGINT,
    manager_id        BIGINT,
    date_debut        DATE         NOT NULL,
    date_fin          DATE,
    taux_occupation  NUMERIC(5,2) NOT NULL DEFAULT 100,
    motif             VARCHAR(20)  NOT NULL,

    created_at        TIMESTAMPTZ  NOT NULL,
    updated_at        TIMESTAMPTZ  NOT NULL,
    created_by        VARCHAR(150) NOT NULL,
    updated_by        VARCHAR(150) NOT NULL,

    CONSTRAINT ck_affectation_taux CHECK (taux_occupation > 0 AND taux_occupation <= 100),
    CONSTRAINT ck_affectation_dates CHECK (date_fin IS NULL OR date_fin >= date_debut),
    CONSTRAINT ck_affectation_motif CHECK (
        motif IN ('EMBAUCHE', 'MUTATION', 'PROMOTION', 'REORGANISATION')
    )
);

CREATE INDEX idx_affectation_employe ON affectation (employe_id);
CREATE INDEX idx_affectation_poste ON affectation (poste_id);
CREATE INDEX idx_affectation_manager ON affectation (manager_id);

-- Clés étrangères différées vers employe (ajoutées ici car employe est créé dans cette migration)
ALTER TABLE utilisateur
    ADD CONSTRAINT fk_utilisateur_employe
        FOREIGN KEY (employe_id) REFERENCES employe(id);

ALTER TABLE departement
    ADD CONSTRAINT fk_departement_responsable
        FOREIGN KEY (responsable_id) REFERENCES employe(id);

ALTER TABLE demande_recrutement
    ADD CONSTRAINT fk_demande_employe_remplace
        FOREIGN KEY (employe_remplace_id) REFERENCES employe(id);

-- Clés étrangères de affectation (contrat_id sans clé étrangère, ajoutée par V7)
ALTER TABLE affectation
    ADD CONSTRAINT fk_affectation_employe
        FOREIGN KEY (employe_id) REFERENCES employe(id),
    ADD CONSTRAINT fk_affectation_poste
        FOREIGN KEY (poste_id) REFERENCES poste(id),
    ADD CONSTRAINT fk_affectation_manager
        FOREIGN KEY (manager_id) REFERENCES employe(id);

ALTER TABLE employe
    ADD CONSTRAINT fk_employe_candidature_origine
        FOREIGN KEY (candidature_origine_id) REFERENCES candidature(id);
