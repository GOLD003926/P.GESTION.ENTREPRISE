package com.gntfh.sirh_api.demande.service;

import com.gntfh.sirh_api.common.config.SirhProperties;
import com.gntfh.sirh_api.organisation.entity.Poste;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * Service de calcul du budget estimé pour une demande de recrutement.
 * Formule (RG-05) : ((salaireMin + salaireMax) / 2) × effectif × dureeBudgetMois × (1 + coefCharges)
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class BudgetService {

    private final SirhProperties sirhProperties;

    /**
     * Calcule le budget estimé pour un poste et un effectif donnés.
     *
     * @param poste le poste concerné
     * @param effectif l'effectif demandé
     * @return le budget estimé arrondi à 2 décimales
     */
    public BigDecimal calculer(Poste poste, int effectif) {
        BigDecimal salaireMoyen = poste.getGrilleSalariale().getSalaireMin().add(poste.getGrilleSalariale().getSalaireMax())
                .divide(BigDecimal.valueOf(2), RoundingMode.HALF_UP);
        BigDecimal salaireTotal = salaireMoyen.multiply(BigDecimal.valueOf(effectif));
        int dureeBudgetMois = sirhProperties.demande().dureeBudgetMois();
        BigDecimal coefCharges = sirhProperties.demande().coefCharges();
        BigDecimal budget = salaireTotal.multiply(BigDecimal.valueOf(dureeBudgetMois))
                .multiply(BigDecimal.ONE.add(coefCharges));
        return budget.setScale(2, RoundingMode.HALF_UP);
    }
}
