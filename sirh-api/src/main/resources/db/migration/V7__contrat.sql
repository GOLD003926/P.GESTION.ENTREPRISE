CREATE TABLE contrat (
    id                    BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    version               BIGINT       NOT NULL DEFAULT 0,
    numero                VARCHAR(30)  NOT NULL,
    employe_id            BIGINT       NOT NULL,
    type                  VARCHAR(10)  NOT NULL,
    numero_renouvellement INT          NOT NULL DEFAULT 0,
    contrat_precedent_id  BIGINT,
    poste_id              BIGINT       NOT NULL,
    manager_id            BIGINT,
    date_debut            DATE         NOT NULL,
    date_fin_prevue       DATE,
    date_fin_reelle       DATE,
    salaire_base          NUMERIC(14,2) NOT NULL,
    devise                VARCHAR(3)   NOT NULL DEFAULT 'MGA',
    horaires_travail      VARCHAR(255) NOT NULL,
    lieu_travail          VARCHAR(255) NOT NULL,
    statut                VARCHAR(20)  NOT NULL DEFAULT 'BROUILLON',
    motif_fin             VARCHAR(30),
    document_signe_id     BIGINT,
    date_signature        DATE,
    alerte_fin_envoyee    BOOLEAN      NOT NULL DEFAULT FALSE,
    created_at            TIMESTAMPTZ  NOT NULL,
    updated_at            TIMESTAMPTZ  NOT NULL,
    created_by            VARCHAR(150) NOT NULL,
    updated_by            VARCHAR(150) NOT NULL,

    CONSTRAINT uk_contrat_numero UNIQUE (numero),
    CONSTRAINT uk_contrat_precedent UNIQUE (contrat_precedent_id),
    CONSTRAINT fk_contrat_employe FOREIGN KEY (employe_id) REFERENCES employe(id),
    CONSTRAINT fk_contrat_precedent FOREIGN KEY (contrat_precedent_id) REFERENCES contrat(id),
    CONSTRAINT fk_contrat_poste FOREIGN KEY (poste_id) REFERENCES poste(id),
    CONSTRAINT fk_contrat_manager FOREIGN KEY (manager_id) REFERENCES employe(id),
    CONSTRAINT fk_contrat_document FOREIGN KEY (document_signe_id) REFERENCES document(id),
    CONSTRAINT ck_contrat_type CHECK (type IN ('ESSAI', 'CDD', 'CDI')),
    CONSTRAINT ck_contrat_statut CHECK (statut IN ('BROUILLON', 'A_SIGNER', 'ACTIF', 'TERMINE', 'ANNULE')),
    CONSTRAINT ck_contrat_motif CHECK (
        motif_fin IS NULL OR motif_fin IN (
            'ECHEANCE', 'RENOUVELLEMENT', 'TRANSFORMATION', 'ESSAI_NON_CONCLUANT',
            'DEMISSION', 'LICENCIEMENT', 'RETRAITE', 'RUPTURE_CONVENTIONNELLE', 'AUTRE'
        )
    ),
    CONSTRAINT ck_contrat_renouvellement CHECK (numero_renouvellement BETWEEN 0 AND 1),
    CONSTRAINT ck_contrat_fin_type CHECK (
        (type = 'CDI' AND date_fin_prevue IS NULL)
        OR (type IN ('ESSAI', 'CDD') AND date_fin_prevue IS NOT NULL)
    ),
    CONSTRAINT ck_contrat_salaire CHECK (salaire_base > 0)
);

CREATE INDEX idx_contrat_employe ON contrat (employe_id);
CREATE INDEX idx_contrat_statut ON contrat (statut);
CREATE INDEX idx_contrat_fin ON contrat (date_fin_prevue);
CREATE UNIQUE INDEX uk_contrat_actif_employe ON contrat (employe_id) WHERE statut = 'ACTIF';

ALTER TABLE affectation ADD CONSTRAINT fk_affectation_contrat FOREIGN KEY (contrat_id) REFERENCES contrat(id);
