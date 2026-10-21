package com.gntfh.sirh_api.employe.service;

import com.gntfh.sirh_api.common.numerotation.SequenceNumero;
import com.gntfh.sirh_api.common.numerotation.NumeroService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * Service pour la génération des matricules.
 */
@Service
@RequiredArgsConstructor
public class MatriculeService {

    private final NumeroService numeroService;

    /**
     * Génère un nouveau matricule.
     */
    public String generer() {
        return numeroService.prochain(SequenceNumero.EMPLOYE);
    }
}
