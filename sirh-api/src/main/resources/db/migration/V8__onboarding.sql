-- Module ONBOARDING : modèles, onboardings et tâches d'intégration

CREATE TABLE modele_onboarding (
    id          BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    version     BIGINT       NOT NULL DEFAULT 0,
    nom         VARCHAR(100) NOT NULL,
    type_contrat VARCHAR(10),
    actif       BOOLEAN      NOT NULL DEFAULT TRUE,
    created_at  TIMESTAMPTZ  NOT NULL,
    updated_at  TIMESTAMPTZ  NOT NULL,
    created_by  VARCHAR(150) NOT NULL,
    updated_by  VARCHAR(150) NOT NULL,
    CONSTRAINT ck_modele_onboarding_type_contrat
        CHECK (type_contrat IS NULL OR type_contrat IN ('ESSAI', 'CDD', 'CDI'))
);

CREATE UNIQUE INDEX uk_modele_onboarding_nom ON modele_onboarding (LOWER(nom));
CREATE UNIQUE INDEX uk_modele_onboarding_defaut_actif
    ON modele_onboarding ((type_contrat IS NULL))
    WHERE actif = TRUE AND type_contrat IS NULL;

CREATE TABLE modele_tache (
    id                BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    version           BIGINT       NOT NULL DEFAULT 0,
    modele_id         BIGINT       NOT NULL,
    ordre             INT          NOT NULL,
    code              VARCHAR(40)  NOT NULL,
    libelle           VARCHAR(200) NOT NULL,
    obligatoire       BOOLEAN      NOT NULL,
    role_responsable  VARCHAR(30)  NOT NULL,
    echeance_jours    INT          NOT NULL,
    created_at        TIMESTAMPTZ  NOT NULL,
    updated_at        TIMESTAMPTZ  NOT NULL,
    created_by        VARCHAR(150) NOT NULL,
    updated_by        VARCHAR(150) NOT NULL,
    CONSTRAINT fk_modele_tache_modele FOREIGN KEY (modele_id) REFERENCES modele_onboarding(id),
    CONSTRAINT uk_modele_tache_ordre UNIQUE (modele_id, ordre),
    CONSTRAINT uk_modele_tache_code UNIQUE (modele_id, code),
    CONSTRAINT ck_modele_tache_ordre CHECK (ordre >= 1),
    CONSTRAINT ck_modele_tache_code CHECK (code ~ '^[A-Z0-9_]+$'),
    CONSTRAINT ck_modele_tache_role_responsable
        CHECK (role_responsable IN ('RH', 'IT_SERVICES', 'PAIE', 'MANAGER')),
    CONSTRAINT ck_modele_tache_echeance_jours CHECK (echeance_jours BETWEEN -60 AND 365)
);

CREATE TABLE onboarding (
    id                BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    version           BIGINT       NOT NULL DEFAULT 0,
    employe_id        BIGINT       NOT NULL,
    contrat_id        BIGINT       NOT NULL,
    modele_id         BIGINT       NOT NULL,
    responsable_rh_id BIGINT,
    date_debut        DATE         NOT NULL,
    date_cible        DATE         NOT NULL,
    statut            VARCHAR(20)  NOT NULL DEFAULT 'A_DEMARRER',
    date_cloture      TIMESTAMPTZ,
    created_at        TIMESTAMPTZ  NOT NULL,
    updated_at        TIMESTAMPTZ  NOT NULL,
    created_by        VARCHAR(150) NOT NULL,
    updated_by        VARCHAR(150) NOT NULL,
    CONSTRAINT fk_onboarding_employe FOREIGN KEY (employe_id) REFERENCES employe(id),
    CONSTRAINT fk_onboarding_contrat FOREIGN KEY (contrat_id) REFERENCES contrat(id),
    CONSTRAINT fk_onboarding_modele FOREIGN KEY (modele_id) REFERENCES modele_onboarding(id),
    CONSTRAINT fk_onboarding_responsable_rh FOREIGN KEY (responsable_rh_id) REFERENCES utilisateur(id),
    CONSTRAINT uk_onboarding_contrat UNIQUE (contrat_id),
    CONSTRAINT ck_onboarding_statut CHECK (statut IN ('A_DEMARRER', 'EN_COURS', 'TERMINE', 'ANNULE'))
);

CREATE TABLE onboarding_tache (
    id                BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    version           BIGINT       NOT NULL DEFAULT 0,
    onboarding_id     BIGINT       NOT NULL,
    modele_tache_id   BIGINT,
    code              VARCHAR(40)  NOT NULL,
    libelle           VARCHAR(200) NOT NULL,
    obligatoire       BOOLEAN      NOT NULL,
    role_responsable  VARCHAR(30)  NOT NULL,
    assigne_a_id      BIGINT,
    echeance          DATE         NOT NULL,
    statut            VARCHAR(20)  NOT NULL DEFAULT 'A_FAIRE',
    date_realisation  TIMESTAMPTZ,
    preuve_document_id BIGINT,
    commentaire       TEXT,
    created_at        TIMESTAMPTZ  NOT NULL,
    updated_at        TIMESTAMPTZ  NOT NULL,
    created_by        VARCHAR(150) NOT NULL,
    updated_by        VARCHAR(150) NOT NULL,
    CONSTRAINT fk_onboarding_tache_onboarding FOREIGN KEY (onboarding_id) REFERENCES onboarding(id),
    CONSTRAINT fk_onboarding_tache_modele_tache FOREIGN KEY (modele_tache_id) REFERENCES modele_tache(id),
    CONSTRAINT fk_onboarding_tache_assigne_a FOREIGN KEY (assigne_a_id) REFERENCES utilisateur(id),
    CONSTRAINT fk_onboarding_tache_preuve_document FOREIGN KEY (preuve_document_id) REFERENCES document(id),
    CONSTRAINT ck_onboarding_tache_statut CHECK (statut IN ('A_FAIRE', 'EN_COURS', 'FAIT', 'NON_APPLICABLE')),
    CONSTRAINT ck_onboarding_tache_role_responsable
        CHECK (role_responsable IN ('RH', 'IT_SERVICES', 'PAIE', 'MANAGER'))
);

CREATE INDEX idx_onboarding_tache_onboarding ON onboarding_tache (onboarding_id);
CREATE INDEX idx_onboarding_tache_role_statut ON onboarding_tache (role_responsable, statut);

INSERT INTO modele_onboarding (nom, type_contrat, actif, created_at, updated_at, created_by, updated_by)
VALUES ('Intégration standard', NULL, TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 'system', 'system');

INSERT INTO modele_tache (
    modele_id, ordre, code, libelle, obligatoire, role_responsable, echeance_jours,
    created_at, updated_at, created_by, updated_by
)
SELECT modele.id, t.ordre, t.code, t.libelle, t.obligatoire, t.role_responsable, t.echeance_jours,
       CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 'system', 'system'
FROM modele_onboarding modele
CROSS JOIN (VALUES
    (1, 'CONTRAT_SIGNE', 'Contrat signé et archivé', TRUE, 'RH', -3),
    (2, 'PAIE', 'Inscription en paie', TRUE, 'PAIE', -3),
    (3, 'COMPTE_SI', 'Création des comptes (email, applications)', TRUE, 'IT_SERVICES', -2),
    (4, 'BADGE', 'Remise du badge', TRUE, 'IT_SERVICES', 0),
    (5, 'MATERIEL', 'Matériel nécessaire', FALSE, 'IT_SERVICES', -1),
    (6, 'COMPTE_BANCAIRE', 'Ouverture du compte bancaire', FALSE, 'RH', 15),
    (7, 'FORMATION', 'Formations d''intégration', FALSE, 'MANAGER', 30)
) AS t(ordre, code, libelle, obligatoire, role_responsable, echeance_jours)
WHERE modele.nom = 'Intégration standard';
