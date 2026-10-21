package com.gntfh.sirh_api.demande.service;

import com.gntfh.sirh_api.common.config.SirhProperties;
import com.gntfh.sirh_api.organisation.entity.GrilleSalariale;
import com.gntfh.sirh_api.organisation.entity.Poste;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

/**
 * Test de BudgetService.
 * Formule : ((salaireMin + salaireMax) / 2) × effectif × dureeBudgetMois × (1 + coefCharges)
 */
@ExtendWith(MockitoExtension.class)
class BudgetServiceTest {

    @Mock
    private SirhProperties sirhProperties;

    @Mock
    private SirhProperties.Demande demandeConfig;

    @InjectMocks
    private BudgetService budgetService;

    @BeforeEach
    void setUp() {
        when(sirhProperties.demande()).thenReturn(demandeConfig);
        when(demandeConfig.dureeBudgetMois()).thenReturn(12);
        when(demandeConfig.coefCharges()).thenReturn(new BigDecimal("0.43"));
    }

    @Test
    @SuppressWarnings("java:S2637")
    void calculer_formuleCorrecte() {
        // Given
        GrilleSalariale grille = GrilleSalariale.builder()
                .code("GR-001")
                .intitule("Grille test")
                .niveau("A")
                .salaireMin(new BigDecimal("3000.00"))
                .salaireMax(new BigDecimal("4000.00"))
                .build();
        Poste poste = Poste.builder()
                .code("P-001")
                .intitule("Poste test")
                .grilleSalariale(grille)
                .effectifAutorise(10)
                .build();
        int effectif = 2;

        // When
        BigDecimal budget = budgetService.calculer(poste, effectif);

        // Then
        // ((3000 + 4000) / 2) × 2 × 12 × (1 + 0.43)
        // = 3500 × 2 × 12 × 1.43
        // = 7000 × 12 × 1.43
        // = 84000 × 1.43
        // = 120120.00
        assertEquals(new BigDecimal("120120.00"), budget);
    }
}
