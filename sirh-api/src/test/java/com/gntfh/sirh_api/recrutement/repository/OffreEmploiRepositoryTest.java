package com.gntfh.sirh_api.recrutement.repository;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests d'intégration pour OffreEmploiRepository sur la vraie base.
 */
@SpringBootTest
@ActiveProfiles("test")
@Transactional
class OffreEmploiRepositoryTest {

    @Autowired
    private OffreEmploiRepository offreEmploiRepository;

    @Test
    void sumPostesOffresActivesByDemandeId_retourne_somme_correcte() {
        // TODO : insérer des données de test et vérifier la somme
    }

    @Test
    void countCandidaturesByOffreId_retourne_nombre_correct() {
        // TODO : insérer des candidatures et vérifier le compte
    }
}
