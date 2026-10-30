-- Données de démonstration SIRH.
-- Exécution :
--   psql -h localhost -U postgres -d sirh_db -f scripts/seed-test-data.sql
--
-- Tous les comptes de test utilisent le mot de passe : Admin@12345
-- Le hash BCrypt ci-dessous est volontairement réservé au développement local.

BEGIN;

CREATE TEMP TABLE seed_users (
    email VARCHAR(150),
    nom VARCHAR(100),
    prenom VARCHAR(100),
    role VARCHAR(30)
) ON COMMIT DROP;

INSERT INTO seed_users (email, nom, prenom, role) VALUES
    ('manager@sirh.local', 'Rakoto', 'Miora', 'MANAGER'),
    ('rh@sirh.local', 'Rasoanaivo', 'Hery', 'RH'),
    ('finance@sirh.local', 'Andriamihaja', 'Fara', 'FINANCE'),
    ('dg@sirh.local', 'Ramanantsoa', 'Aina', 'DG');

INSERT INTO utilisateur (
    email, mot_de_passe, nom, prenom, role, actif,
    created_at, updated_at, created_by, updated_by
)
SELECT
    s.email,
    '$2a$10$Z76uwgjE0ym1/3fsTBo7QOOcj3qGfllj2otx8Lto3PTZW6hPdZRQ2',
    s.nom, s.prenom, s.role, TRUE,
    NOW(), NOW(), 'seed@sirh.local', 'seed@sirh.local'
FROM seed_users s
WHERE NOT EXISTS (
    SELECT 1 FROM utilisateur u
    WHERE LOWER(u.email) = LOWER(s.email)
);

INSERT INTO grille_salariale (
    code, intitule, niveau, salaire_min, salaire_max, devise, actif,
    created_at, updated_at, created_by, updated_by
)
SELECT
    'DEV_JUNIOR', 'Développeur junior', 'Technicien', 800000, 1200000, 'MGA', TRUE,
    NOW(), NOW(), 'seed@sirh.local', 'seed@sirh.local'
WHERE NOT EXISTS (
    SELECT 1 FROM grille_salariale WHERE LOWER(code) = LOWER('DEV_JUNIOR')
);

INSERT INTO grille_salariale (
    code, intitule, niveau, salaire_min, salaire_max, devise, actif,
    created_at, updated_at, created_by, updated_by
)
SELECT
    'RH_GENERALISTE', 'Responsable ressources humaines', 'Cadre', 1500000, 2500000, 'MGA', TRUE,
    NOW(), NOW(), 'seed@sirh.local', 'seed@sirh.local'
WHERE NOT EXISTS (
    SELECT 1 FROM grille_salariale WHERE LOWER(code) = LOWER('RH_GENERALISTE')
);

INSERT INTO departement (
    code, nom, actif, created_at, updated_at, created_by, updated_by
)
SELECT
    'IT', 'Direction des systèmes d''information', TRUE,
    NOW(), NOW(), 'seed@sirh.local', 'seed@sirh.local'
WHERE NOT EXISTS (
    SELECT 1 FROM departement WHERE LOWER(code) = LOWER('IT')
);

INSERT INTO departement (
    code, nom, actif, created_at, updated_at, created_by, updated_by
)
SELECT
    'RH', 'Ressources humaines', TRUE,
    NOW(), NOW(), 'seed@sirh.local', 'seed@sirh.local'
WHERE NOT EXISTS (
    SELECT 1 FROM departement WHERE LOWER(code) = LOWER('RH')
);

INSERT INTO poste (
    code, intitule, description, competences_requises,
    departement_id, grille_salariale_id, effectif_autorise, actif,
    created_at, updated_at, created_by, updated_by
)
SELECT
    'DEV_JUNIOR',
    'Développeur Java junior',
    'Développe et maintient les applications internes.',
    'Java, Spring Boot, SQL, Git',
    d.id, g.id, 3, TRUE,
    NOW(), NOW(), 'seed@sirh.local', 'seed@sirh.local'
FROM departement d
JOIN grille_salariale g ON LOWER(g.code) = LOWER('DEV_JUNIOR')
WHERE LOWER(d.code) = LOWER('IT')
  AND NOT EXISTS (
      SELECT 1 FROM poste WHERE LOWER(code) = LOWER('DEV_JUNIOR')
  );

INSERT INTO poste (
    code, intitule, description, competences_requises,
    departement_id, grille_salariale_id, effectif_autorise, actif,
    created_at, updated_at, created_by, updated_by
)
SELECT
    'RH_GENERALISTE',
    'Responsable RH généraliste',
    'Pilote les processus RH et le suivi des collaborateurs.',
    'Droit social, recrutement, gestion administrative',
    d.id, g.id, 2, TRUE,
    NOW(), NOW(), 'seed@sirh.local', 'seed@sirh.local'
FROM departement d
JOIN grille_salariale g ON LOWER(g.code) = LOWER('RH_GENERALISTE')
WHERE LOWER(d.code) = LOWER('RH')
  AND NOT EXISTS (
      SELECT 1 FROM poste WHERE LOWER(code) = LOWER('RH_GENERALISTE')
  );

INSERT INTO demande_recrutement (
    reference, poste_id, departement_id, demandeur_id, type_demande,
    effectif_demande, effectif_pourvu, motif, justification, budget_estime,
    date_souhaitee, statut, cycle_courant,
    created_at, updated_at, created_by, updated_by
)
SELECT
    'DR-SEED-0001',
    p.id,
    p.departement_id,
    u.id,
    'CREATION',
    1,
    0,
    'CROISSANCE',
    'Renforcement de l''équipe de développement.',
    14400000,
    CURRENT_DATE + INTERVAL '30 days',
    'EN_RECRUTEMENT',
    1,
    NOW(), NOW(), u.email, u.email
FROM poste p
JOIN utilisateur u ON LOWER(u.email) = LOWER('manager@sirh.local')
WHERE LOWER(p.code) = LOWER('DEV_JUNIOR')
  AND NOT EXISTS (
      SELECT 1 FROM demande_recrutement WHERE reference = 'DR-SEED-0001'
  );

INSERT INTO offre_emploi (
    reference, demande_id, titre, description, profil_recherche,
    nombre_postes, nombre_niveaux_selection, canal_publication,
    date_publication, date_cloture, statut,
    created_at, updated_at, created_by, updated_by
)
SELECT
    'OF-SEED-0001',
    d.id,
    'Développeur Java junior',
    'Nous recherchons un développeur Java junior pour rejoindre notre équipe.',
    'Formation informatique, Java, Spring Boot et SQL.',
    1,
    3,
    'SITE_WEB',
    CURRENT_DATE,
    CURRENT_DATE + INTERVAL '30 days',
    'PUBLIEE',
    NOW(), NOW(), 'rh@sirh.local', 'rh@sirh.local'
FROM demande_recrutement d
WHERE d.reference = 'DR-SEED-0001'
  AND NOT EXISTS (
      SELECT 1 FROM offre_emploi WHERE reference = 'OF-SEED-0001'
  );

INSERT INTO candidat (
    nom, prenom, email, telephone, date_naissance, adresse,
    nationalite, consentement_donnees,
    created_at, updated_at, created_by, updated_by
)
SELECT
    'Rakoto', 'Tiana', 'tiana.rakoto@example.com', '+261340000001',
    DATE '1998-05-12', 'Antananarivo', 'Malgache', TRUE,
    NOW(), NOW(), 'seed@sirh.local', 'seed@sirh.local'
WHERE NOT EXISTS (
    SELECT 1 FROM candidat
    WHERE LOWER(email) = LOWER('tiana.rakoto@example.com')
);

INSERT INTO candidature (
    candidat_id, offre_id, lettre_motivation, date_depot, statut,
    created_at, updated_at, created_by, updated_by
)
SELECT
    c.id, o.id,
    'Je souhaite rejoindre votre équipe de développement.',
    NOW(), 'RECUE',
    NOW(), NOW(), 'seed@sirh.local', 'seed@sirh.local'
FROM candidat c
JOIN offre_emploi o ON o.reference = 'OF-SEED-0001'
WHERE LOWER(c.email) = LOWER('tiana.rakoto@example.com')
  AND NOT EXISTS (
      SELECT 1
      FROM candidature ca
      WHERE ca.candidat_id = c.id
        AND ca.offre_id = o.id
  );

COMMIT;

-- Contrôles rapides après import :
-- SELECT email, role FROM utilisateur ORDER BY role;
-- SELECT code, intitule FROM grille_salariale ORDER BY code;
-- SELECT code, intitule FROM poste ORDER BY code;
-- SELECT reference, statut FROM demande_recrutement;
-- SELECT reference, statut FROM offre_emploi;
-- SELECT id, statut FROM candidature;
