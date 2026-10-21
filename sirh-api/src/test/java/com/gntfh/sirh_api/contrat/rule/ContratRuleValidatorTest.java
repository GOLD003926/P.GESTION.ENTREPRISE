package com.gntfh.sirh_api.contrat.rule;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

@Disabled("TODO : implémenter les scénarios du validateur contrat")
class ContratRuleValidatorTest {

    @Test
    void verifierCreation_essaiDeSeptMois_refuseLaCreation() {
    }

    @Test
    void verifierRenouvellement_secondRenouvellement_refuseLaCreation() {
    }

    @Test
    void verifierRenouvellement_cumulEssaiSuperieurAUnAn_refuseLaCreation() {
    }

    @Test
    void verifierCreation_cddDeVingtCinqMois_refuseLaCreation() {
    }

    @Test
    void verifierCreation_cdiAvecDuree_refuseLaCreation() {
    }

    @Test
    void verifierTransformation_depuisCdi_refuseLaTransformation() {
    }
}
