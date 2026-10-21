package com.gntfh.sirh_api.recrutement.repository;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

/**
 * Tests d'intégration pour CandidatureRepository sur la vraie base.
 */
@SpringBootTest
@ActiveProfiles("test")
@Transactional
class CandidatureRepositoryTest {

    @Autowired
    private CandidatureRepository candidatureRepository;

    @Test
    void maxRangListeAttenteByOffreId_retourne_maximum_correct() {
        // TODO : insérer des candidatures en liste d'attente et vérifier le rang max
    }
}
