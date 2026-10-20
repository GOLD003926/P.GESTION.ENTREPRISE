-- V1__organisation
-- =====================================================================
-- Module ORGANISATION : tables grille_salariale, departement, poste
-- Conventions pour toutes les migrations :
--   * noms de tables et colonnes en snake_case, tables au singulier
--   * chaque table a : id, version, created_at, updated_at, created_by, updated_by
--   * dates en TIMESTAMPTZ (UTC)
--   * une migration déjà fusionnée ne se modifie JAMAIS : on en ajoute une nouvelle
-- =====================================================================

-- Table grille_salariale
CREATE TABLE grille_salariale (
    id            BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    version       BIGINT       NOT NULL DEFAULT 0,

    code          VARCHAR(30)  NOT NULL,
    intitule      VARCHAR(100) NOT NULL,
    niveau        VARCHAR(30)  NOT NULL,
    salaire_min   NUMERIC(14,2) NOT NULL,
    salaire_max   NUMERIC(14,2) NOT NULL,
    devise        VARCHAR(3)   NOT NULL DEFAULT 'MGA',
    actif         BOOLEAN      NOT NULL DEFAULT TRUE,

    created_at    TIMESTAMPTZ  NOT NULL,
    updated_at    TIMESTAMPTZ  NOT NULL,
    created_by    VARCHAR(150) NOT NULL,
    updated_by    VARCHAR(150) NOT NULL,

    CONSTRAINT ck_grille_salariale_bornes CHECK (salaire_max >= salaire_min)
);

-- Table departement
CREATE TABLE departement (
    id            BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    version       BIGINT       NOT NULL DEFAULT 0,

    code          VARCHAR(30)  NOT NULL,
    nom           VARCHAR(150) NOT NULL,
    departement_parent_id BIGINT,
    responsable_id BIGINT,
    actif         BOOLEAN      NOT NULL DEFAULT TRUE,

    created_at    TIMESTAMPTZ  NOT NULL,
    updated_at    TIMESTAMPTZ  NOT NULL,
    created_by    VARCHAR(150) NOT NULL,
    updated_by    VARCHAR(150) NOT NULL,

    CONSTRAINT fk_departement_parent
        FOREIGN KEY (departement_parent_id)
        REFERENCES departement(id)
);

-- Table poste
CREATE TABLE poste (
    id            BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    version       BIGINT       NOT NULL DEFAULT 0,

    code          VARCHAR(30)  NOT NULL,
    intitule      VARCHAR(150) NOT NULL,
    description   TEXT,
    competences_requises TEXT,
    departement_id BIGINT      NOT NULL,
    grille_salariale_id BIGINT NOT NULL,
    effectif_autorise INT      NOT NULL,
    actif         BOOLEAN      NOT NULL DEFAULT TRUE,

    created_at    TIMESTAMPTZ  NOT NULL,
    updated_at    TIMESTAMPTZ  NOT NULL,
    created_by    VARCHAR(150) NOT NULL,
    updated_by    VARCHAR(150) NOT NULL,

    CONSTRAINT fk_poste_departement
        FOREIGN KEY (departement_id)
        REFERENCES departement(id),

    CONSTRAINT fk_poste_grille
        FOREIGN KEY (grille_salariale_id)
        REFERENCES grille_salariale(id),

    CONSTRAINT ck_poste_effectif CHECK (effectif_autorise >= 1)
);

-- Index d'unicité insensibles à la casse (LOWER(code))
CREATE UNIQUE INDEX uk_grille_salariale_code ON grille_salariale (LOWER(code));
CREATE UNIQUE INDEX uk_departement_code ON departement (LOWER(code));
CREATE UNIQUE INDEX uk_poste_code ON poste (LOWER(code));

-- Index de performance
CREATE INDEX idx_poste_departement ON poste (departement_id);
CREATE INDEX idx_poste_grille ON poste (grille_salariale_id);
