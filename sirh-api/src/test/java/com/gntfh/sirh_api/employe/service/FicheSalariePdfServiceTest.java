package com.gntfh.sirh_api.employe.service;

import com.gntfh.sirh_api.employe.entity.Employe;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertTrue;

class FicheSalariePdfServiceTest {

    @Test
    void generer_produitUnPdfNonVide() {
        Employe employe = Employe.builder()
                .matricule("EMP-2026-0001")
                .nom("Rakoto")
                .prenom("Jean")
                .build();

        byte[] pdf = new FicheSalariePdfService().generer(employe, Optional.empty());

        assertTrue(pdf.length > 0);
        assertTrue(new String(pdf, 0, 4, StandardCharsets.US_ASCII).equals("%PDF"));
    }
}
