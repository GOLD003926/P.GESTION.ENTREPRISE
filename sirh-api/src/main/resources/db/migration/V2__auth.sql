-- V2__auth
-- =====================================================================
-- Module AUTH : table utilisateur
-- Conventions pour toutes les migrations :
--   * noms de tables et colonnes en snake_case, tables au singulier
--   * chaque table a : id, version, created_at, updated_at, created_by, updated_by
--   * dates en TIMESTAMPTZ (UTC)
--   * une migration déjà fusionnée ne se modifie JAMAIS : on en ajoute une nouvelle
-- =====================================================================

CREATE TABLE utilisateur (
    id            BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    version       BIGINT       NOT NULL DEFAULT 0,

    email         VARCHAR(150) NOT NULL,
    mot_de_passe  VARCHAR(100) NOT NULL,
    nom           VARCHAR(100) NOT NULL,
    prenom        VARCHAR(100) NOT NULL,
    role          VARCHAR(30)  NOT NULL,
    actif         BOOLEAN      NOT NULL DEFAULT TRUE,

    -- Lien facultatif vers l'employé. Volontairement SANS clé étrangère :
    -- la table employe n'existe pas encore (migration V5 ajoutera la contrainte).
    employe_id    BIGINT,

    created_at    TIMESTAMPTZ  NOT NULL,
    updated_at    TIMESTAMPTZ  NOT NULL,
    created_by    VARCHAR(150) NOT NULL,
    updated_by    VARCHAR(150) NOT NULL,

    CONSTRAINT ck_utilisateur_role CHECK (
        role IN ('ADMIN', 'RH', 'MANAGER', 'FINANCE', 'DG', 'IT_SERVICES', 'PAIE', 'SALARIE')
    )
);

-- Email unique sans tenir compte de la casse (Jean@x.com = jean@x.com)
CREATE UNIQUE INDEX uk_utilisateur_email_lower ON utilisateur (LOWER(email));

CREATE INDEX idx_utilisateur_role ON utilisateur (role);
