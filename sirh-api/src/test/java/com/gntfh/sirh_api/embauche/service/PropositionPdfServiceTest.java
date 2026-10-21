package com.gntfh.sirh_api.embauche.service;

import com.gntfh.sirh_api.contrat.enums.TypeContrat;
import com.gntfh.sirh_api.embauche.entity.PropositionEmbauche;
import com.gntfh.sirh_api.organisation.entity.Poste;
import com.gntfh.sirh_api.recrutement.entity.Candidat;
import com.gntfh.sirh_api.recrutement.entity.Candidature;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertTrue;

class PropositionPdfServiceTest {

    private final PropositionPdfService propositionPdfService = new PropositionPdfService();

    @Test
    void generer_propositionComplete_retourneUnPdfNonVide() {
        Candidat candidat = Candidat.builder()
                .nom("Rakoto")
                .prenom("Miora")
                .email("miora@example.test")
                .build();
        Candidature candidature = Candidature.builder()
                .candidat(candidat)
                .build();
        Poste poste = Poste.builder()
                .intitule("Analyste")
                .build();
        PropositionEmbauche proposition = PropositionEmbauche.builder()
                .candidature(candidature)
                .poste(poste)
                .typeContratPropose(TypeContrat.ESSAI)
                .salairePropose(new BigDecimal("1500000.00"))
                .devise("MGA")
                .horairesTravail("08:00-17:00")
                .lieuTravail("Antananarivo")
                .dateDebutPrevue(LocalDate.of(2026, 11, 1))
                .dureeMois(3)
                .dateLimiteReponse(LocalDate.of(2026, 10, 30))
                .build();

        byte[] contenu = propositionPdfService.generer(proposition);

        assertTrue(contenu.length > 0);
        assertTrue(new String(contenu, 0, 5, java.nio.charset.StandardCharsets.US_ASCII).startsWith("%PDF-"));
    }
}
