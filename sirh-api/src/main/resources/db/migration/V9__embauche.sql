-- V9 : embauche
-- Module EMBAUCHE : propositions d'embauche et suivi des réponses

CREATE TABLE proposition_embauche (
    id                       BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    version                  BIGINT       NOT NULL DEFAULT 0,
    candidature_id           BIGINT       NOT NULL,
    poste_id                 BIGINT       NOT NULL,
    type_contrat_propose     VARCHAR(10)  NOT NULL DEFAULT 'ESSAI',
    salaire_propose          NUMERIC(14,2) NOT NULL,
    devise                   VARCHAR(3)   NOT NULL DEFAULT 'MGA',
    horaires_travail         VARCHAR(255) NOT NULL,
    lieu_travail             VARCHAR(255) NOT NULL,
    date_debut_prevue        DATE         NOT NULL,
    duree_mois               INT,
    date_limite_reponse       DATE         NOT NULL,
    nb_prolongations_delai   INT          NOT NULL DEFAULT 0,
    statut                   VARCHAR(20)  NOT NULL DEFAULT 'BROUILLON',
    date_envoi               TIMESTAMPTZ,
    date_reponse             TIMESTAMPTZ,
    motif_refus              VARCHAR(500),
    derogation_requise       BOOLEAN      NOT NULL DEFAULT FALSE,
    derogation_validee_par_id BIGINT,
    date_derogation          TIMESTAMPTZ,
    document_id              BIGINT,
    employe_id               BIGINT,
    created_at               TIMESTAMPTZ  NOT NULL,
    updated_at               TIMESTAMPTZ  NOT NULL,
    created_by               VARCHAR(150) NOT NULL,
    updated_by               VARCHAR(150) NOT NULL,
    CONSTRAINT fk_proposition_candidature
        FOREIGN KEY (candidature_id) REFERENCES candidature(id),
    CONSTRAINT fk_proposition_poste
        FOREIGN KEY (poste_id) REFERENCES poste(id),
    CONSTRAINT fk_proposition_derogation
        FOREIGN KEY (derogation_validee_par_id) REFERENCES utilisateur(id),
    CONSTRAINT fk_proposition_document
        FOREIGN KEY (document_id) REFERENCES document(id),
    CONSTRAINT fk_proposition_employe
        FOREIGN KEY (employe_id) REFERENCES employe(id),
    CONSTRAINT ck_proposition_type_contrat
        CHECK (type_contrat_propose IN ('ESSAI', 'CDD', 'CDI')),
    CONSTRAINT ck_proposition_salaire
        CHECK (salaire_propose > 0),
    CONSTRAINT ck_proposition_duree
        CHECK (
            (type_contrat_propose = 'CDI' AND duree_mois IS NULL)
            OR (
                type_contrat_propose IN ('ESSAI', 'CDD')
                AND duree_mois IS NOT NULL
                AND duree_mois BETWEEN 1 AND 60
            )
        ),
    CONSTRAINT ck_proposition_nb_prolongations
        CHECK (nb_prolongations_delai BETWEEN 0 AND 1),
    CONSTRAINT ck_proposition_statut
        CHECK (statut IN ('BROUILLON', 'ENVOYEE', 'DELAI_ACCORDE', 'ACCEPTEE',
                          'REFUSEE', 'EXPIREE', 'RETIREE'))
);

CREATE INDEX idx_proposition_candidature ON proposition_embauche (candidature_id);
CREATE INDEX idx_proposition_statut ON proposition_embauche (statut);
CREATE INDEX idx_proposition_limite ON proposition_embauche (date_limite_reponse);
CREATE UNIQUE INDEX uk_proposition_active_candidature
    ON proposition_embauche (candidature_id)
    WHERE statut IN ('BROUILLON', 'ENVOYEE', 'DELAI_ACCORDE');
