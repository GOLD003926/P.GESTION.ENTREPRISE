package com.gntfh.sirh_api.onboarding.repository;

import com.gntfh.sirh_api.onboarding.listener.ContratEventListener;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@SpringBootTest
@Transactional
class OnboardingMigrationTest {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private ContratEventListener contratEventListener;

    @Test
    void modeleParDefaut_contientSeptTaches() {
        Integer nombre = jdbcTemplate.queryForObject("""
                SELECT COUNT(mt.id)
                FROM modele_onboarding mo
                JOIN modele_tache mt ON mt.modele_id = mo.id
                WHERE mo.nom = 'Intégration standard'
                  AND mo.actif = TRUE
                  AND mo.type_contrat IS NULL
                """, Integer.class);

        assertEquals(7, nombre);
    }

    @Test
    void unSeulModeleParDefautEstActif() {
        Integer nombre = jdbcTemplate.queryForObject("""
                SELECT COUNT(*)
                FROM modele_onboarding
                WHERE actif = TRUE AND type_contrat IS NULL
                """, Integer.class);

        assertEquals(1, nombre);
    }

    @Test
    void listenerContrat_estEnregistreCommeBean() {
        assertNotNull(contratEventListener);
    }
}
