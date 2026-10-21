package com.gntfh.sirh_api.embauche.repository;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
@Transactional
class PropositionEmbaucheRepositoryTest {

    @Autowired
    private PropositionEmbaucheRepository propositionRepository;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Test
    void findActiveByCandidatureId_candidatureInexistante_retourneVide() {
        assertTrue(propositionRepository.findActiveByCandidatureId(-1L).isEmpty());
    }

    @Test
    void findEchues_dateDeReferenceAnterieure_auxPropositions_retourneVide() {
        assertTrue(propositionRepository.findEchues(LocalDate.of(1900, 1, 1)).isEmpty());
    }

    @Test
    void migration_indexUniquePartielDesPropositionsActives_estPresent() {
        Integer nombre = jdbcTemplate.queryForObject("""
                SELECT COUNT(*)
                FROM pg_indexes
                WHERE schemaname = 'public'
                  AND indexname = 'uk_proposition_active_candidature'
                  AND indexdef LIKE '% WHERE %'
                """, Integer.class);

        assertEquals(1, nombre);
    }
}
