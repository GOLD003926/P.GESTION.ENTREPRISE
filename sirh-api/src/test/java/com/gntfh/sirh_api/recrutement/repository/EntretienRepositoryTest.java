package com.gntfh.sirh_api.recrutement.repository;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

/**
 * Tests d'intégration pour EntretienRepository sur la vraie base.
 */
@SpringBootTest
@ActiveProfiles("test")
@Transactional
class EntretienRepositoryTest {

    @Autowired
    private EntretienRepository entretienRepository;

    @Test
    void findActifByCandidatureIdAndNiveau_retourne_entretien_non_annule() {
        // TODO : insérer un entretien et vérifier la recherche par niveau
    }

    @Test
    void countActifByCandidatureIdAndNiveau_retourne_nombre_correct() {
        // TODO : insérer des entretiens et vérifier le compte
    }
}
