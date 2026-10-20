package com.gntfh.sirh_api.common.numerotation;

import com.gntfh.sirh_api.common.config.SirhProperties;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.jdbc.core.JdbcTemplate;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class NumeroServiceTest {

    @Mock
    private JdbcTemplate jdbcTemplate;

    @Mock
    private SirhProperties sirhProperties;

    @Mock
    private SirhProperties.Employe employeProperties;

    @InjectMocks
    private NumeroService numeroService;

    @BeforeEach
    void setUp() {
        when(sirhProperties.employe()).thenReturn(employeProperties);
        when(employeProperties.matriculePrefixe()).thenReturn("EMP");
    }

    @Test
    void prochain_demande_formatCorrect() {
        when(jdbcTemplate.queryForObject(any(String.class), eq(Long.class), any(String.class)))
                .thenReturn(1L);

        String numero = numeroService.prochain(SequenceNumero.DEMANDE);

        assertTrue(numero.matches("DR-\\d{4}-\\d{4}"));
        assertTrue(numero.startsWith("DR-"));
    }

    @Test
    void prochain_offre_formatCorrect() {
        when(jdbcTemplate.queryForObject(any(String.class), eq(Long.class), any(String.class)))
                .thenReturn(1L);

        String numero = numeroService.prochain(SequenceNumero.OFFRE);

        assertTrue(numero.matches("OF-\\d{4}-\\d{4}"));
        assertTrue(numero.startsWith("OF-"));
    }

    @Test
    void prochain_employe_utilisePrefixeConfiguration() {
        when(jdbcTemplate.queryForObject(any(String.class), eq(Long.class), any(String.class)))
                .thenReturn(1L);

        String numero = numeroService.prochain(SequenceNumero.EMPLOYE);

        assertTrue(numero.matches("EMP-\\d{4}-\\d{4}"));
        assertTrue(numero.startsWith("EMP-"));
    }

    @Test
    void prochain_contrat_formatCorrect() {
        when(jdbcTemplate.queryForObject(any(String.class), eq(Long.class), any(String.class)))
                .thenReturn(1L);

        String numero = numeroService.prochain(SequenceNumero.CONTRAT);

        assertTrue(numero.matches("CT-\\d{4}-\\d{4}"));
        assertTrue(numero.startsWith("CT-"));
    }

    @Test
    void prochain_numeroSur4Chiffres() {
        when(jdbcTemplate.queryForObject(any(String.class), eq(Long.class), any(String.class)))
                .thenReturn(42L);

        String numero = numeroService.prochain(SequenceNumero.DEMANDE);

        assertTrue(numero.endsWith("-0042"));
    }

    @Test
    void prochain_anneeCourante() {
        when(jdbcTemplate.queryForObject(any(String.class), eq(Long.class), any(String.class)))
                .thenReturn(1L);

        String numero = numeroService.prochain(SequenceNumero.DEMANDE);

        String[] parts = numero.split("-");
        int annee = Integer.parseInt(parts[1]);
        int anneeCourante = java.time.Year.now().getValue();
        assertEquals(anneeCourante, annee);
    }
}
