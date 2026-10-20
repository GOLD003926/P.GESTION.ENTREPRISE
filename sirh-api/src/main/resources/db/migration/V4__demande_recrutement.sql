-- V4__demande_recrutement.sql
-- Module demande : tables demande_recrutement et validation

-- Table demande_recrutement
CREATE TABLE demande_recrutement (
    id BIGSERIAL PRIMARY KEY,
    version BIGINT NOT NULL DEFAULT 0,
    reference VARCHAR(30) NOT NULL UNIQUE,
    poste_id BIGINT NOT NULL,
    departement_id BIGINT NOT NULL,
    demandeur_id BIGINT NOT NULL,
    type_demande VARCHAR(20) NOT NULL CHECK (type_demande IN ('CREATION', 'REMPLACEMENT')),
    employe_remplace_id BIGINT,
    effectif_demande INT NOT NULL CHECK (effectif_demande BETWEEN 1 AND 50),
    effectif_pourvu INT NOT NULL DEFAULT 0,
    motif VARCHAR(30) NOT NULL CHECK (motif IN ('CROISSANCE', 'NOUVEAU_PROJET', 'RESTRUCTURATION', 'RENFORT_TEMPORAIRE', 'DEMISSION', 'LICENCIEMENT', 'RETRAITE', 'MUTATION', 'FIN_CONTRAT', 'DECES', 'AUTRE')),
    justification TEXT,
    budget_estime NUMERIC(14,2) NOT NULL,
    date_souhaitee DATE NOT NULL,
    statut VARCHAR(30) NOT NULL DEFAULT 'BROUILLON' CHECK (statut IN ('BROUILLON', 'EN_VALIDATION_RH', 'EN_VALIDATION_FINANCE', 'EN_VALIDATION_DG', 'RETOURNEE', 'REJETEE', 'VALIDEE', 'EN_RECRUTEMENT', 'POURVUE', 'ANNULEE')),
    cycle_courant INT NOT NULL DEFAULT 0,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    created_by VARCHAR(150) NOT NULL,
    updated_by VARCHAR(150) NOT NULL
);

-- Indexes sur demande_recrutement
CREATE INDEX idx_demande_statut ON demande_recrutement(statut);
CREATE INDEX idx_demande_poste ON demande_recrutement(poste_id);
CREATE INDEX idx_demande_demandeur ON demande_recrutement(demandeur_id);

-- Clés étrangères sur demande_recrutement
ALTER TABLE demande_recrutement
    ADD CONSTRAINT fk_demande_poste
    FOREIGN KEY (poste_id) REFERENCES poste(id);

ALTER TABLE demande_recrutement
    ADD CONSTRAINT fk_demande_departement
    FOREIGN KEY (departement_id) REFERENCES departement(id);

ALTER TABLE demande_recrutement
    ADD CONSTRAINT fk_demande_demandeur
    FOREIGN KEY (demandeur_id) REFERENCES utilisateur(id);

-- Note : employe_remplace_id n'a PAS de clé étrangère (ajoutée par V6__employe.sql)

-- Table validation
CREATE TABLE validation (
    id BIGSERIAL PRIMARY KEY,
    version BIGINT NOT NULL DEFAULT 0,
    demande_id BIGINT NOT NULL,
    cycle INT NOT NULL,
    ordre INT NOT NULL CHECK (ordre BETWEEN 1 AND 3),
    role_validateur VARCHAR(20) NOT NULL CHECK (role_validateur IN ('RH', 'FINANCE', 'DG')),
    validateur_id BIGINT,
    decision VARCHAR(20) NOT NULL DEFAULT 'EN_ATTENTE' CHECK (decision IN ('EN_ATTENTE', 'APPROUVEE', 'REJETEE', 'RETOURNEE')),
    commentaire TEXT,
    date_decision TIMESTAMPTZ,
    CONSTRAINT uk_validation_demande_cycle_ordre UNIQUE (demande_id, cycle, ordre)
);

-- Clé étrangère sur validation
ALTER TABLE validation
    ADD CONSTRAINT fk_validation_demande
    FOREIGN KEY (demande_id) REFERENCES demande_recrutement(id);

ALTER TABLE validation
    ADD CONSTRAINT fk_validation_validateur
    FOREIGN KEY (validateur_id) REFERENCES utilisateur(id);
