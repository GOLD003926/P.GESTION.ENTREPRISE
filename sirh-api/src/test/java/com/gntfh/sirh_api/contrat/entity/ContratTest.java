package com.gntfh.sirh_api.contrat.entity;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

class ContratTest {

    @Test
    void builder_sansValeurAlerteFinEnvoyee_defautFaux() {
        Contrat contrat = Contrat.builder()
                .alerteFinEnvoyee(null)
                .numeroRenouvellement(null)
                .build();

        assertFalse(contrat.getAlerteFinEnvoyee());
        assertEquals(0, contrat.getNumeroRenouvellement());
    }
}
