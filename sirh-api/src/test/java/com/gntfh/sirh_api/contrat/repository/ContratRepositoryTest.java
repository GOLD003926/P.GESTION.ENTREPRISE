package com.gntfh.sirh_api.contrat.repository;

import com.gntfh.sirh_api.contrat.enums.StatutContrat;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
@Transactional
class ContratRepositoryTest {

    @Autowired
    private ContratRepository contratRepository;

    @Test
    void findByEmployeIdAndStatut_employeAbsent_retourneVide() {
        assertTrue(contratRepository.findByEmployeIdAndStatut(Long.MAX_VALUE, StatutContrat.ACTIF).isEmpty());
    }

    @Test
    void findContratsASignerActivables_executeLaRequeteSurLaBase() {
        assertNotNull(contratRepository.findContratsASignerActivables(LocalDate.now()));
    }

    @Test
    void findContratsActifsAvecFinAvantEtAlerteNonEnvoyee_executeLaRequeteSurLaBase() {
        assertNotNull(contratRepository.findContratsActifsAvecFinAvantEtAlerteNonEnvoyee(LocalDate.now()));
    }
}
