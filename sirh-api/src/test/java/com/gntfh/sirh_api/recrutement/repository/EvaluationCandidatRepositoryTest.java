package com.gntfh.sirh_api.recrutement.repository;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

/**
 * Tests d'intégration pour EvaluationCandidatRepository sur la vraie base.
 */
@SpringBootTest
@ActiveProfiles("test")
@Transactional
class EvaluationCandidatRepositoryTest {

    @Autowired
    private EvaluationCandidatRepository evaluationCandidatRepository;

    @Test
    void findActiveByEntretienIdAndEvaluateurId_retourne_evaluation_active() {
        // TODO : insérer une évaluation et vérifier la recherche active
    }

    @Test
    void countActiveAndVerrouilleeByEntretienId_retourne_nombre_correct() {
        // TODO : insérer des évaluations et vérifier le compte verrouillé
    }
}
