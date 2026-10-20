package com.gntfh.sirh_api.common.audit.service;

import com.gntfh.sirh_api.common.audit.entity.JournalAudit;
import com.gntfh.sirh_api.common.audit.enums.ActionAudit;
import com.gntfh.sirh_api.common.audit.enums.TypeEntite;
import com.gntfh.sirh_api.common.audit.repository.JournalAuditRepository;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

/**
 * Tests avec un VRAI JsonMapper Jackson 3 : ils prouvent que dates et décimaux sont bien
 * enregistrés en JSON exploitable. Aucune vérification n'est affaiblie pour « faire passer ».
 */
class AuditServiceTest {

    public record Exemple(Long id, BigDecimal montant, LocalDate date, Instant creeLe) {
    }

    /** Objet dont la sérialisation échoue à coup sûr. */
    public static class NonSerialisable {
        public String getValeur() {
            throw new IllegalStateException("échec volontaire du test");
        }
    }

    private final JournalAuditRepository repository = mock(JournalAuditRepository.class);
    private final JsonMapper jsonMapper = JsonMapper.builder().build();
    private final AuditService service = new AuditService(repository, jsonMapper);

    private static Exemple exemple() {
        return new Exemple(7L, new BigDecimal("1000.50"),
                LocalDate.of(2026, 10, 20), Instant.parse("2026-10-20T10:15:30Z"));
    }

    private JournalAudit journalEnregistre() {
        ArgumentCaptor<JournalAudit> captor = ArgumentCaptor.forClass(JournalAudit.class);
        verify(repository).save(captor.capture());
        return captor.getValue();
    }

    @Test
    void journaliser_recordAvecDatesEtDecimal_produitUnJsonExact() {
        service.journaliser(ActionAudit.CREATION, TypeEntite.POSTE, 7L, null, exemple(), "rh@sirh.local");

        JsonNode json = jsonMapper.readTree(journalEnregistre().getValeursApres());

        assertEquals(7L, json.get("id").longValue());
        // Comparaison numérique : 1000.50 et 1000.5 sont la même valeur.
        assertEquals(0, new BigDecimal("1000.50").compareTo(json.get("montant").decimalValue()));
        // Dates en chaînes ISO-8601 (toString d'un nœud texte = la chaîne entre guillemets).
        assertEquals("\"2026-10-20\"", json.get("date").toString());
        assertEquals("\"2026-10-20T10:15:30Z\"", json.get("creeLe").toString());
    }

    @Test
    void journaliser_valeurNulle_enregistreNullEtNonLaChaineNull() {
        service.journaliser(ActionAudit.CREATION, TypeEntite.POSTE, 7L, null, exemple(), "rh@sirh.local");

        assertNull(journalEnregistre().getValeursAvant());
    }

    @Test
    void journaliser_enregistreLesChampsDuJournal() {
        service.journaliser(ActionAudit.MODIFICATION, TypeEntite.CONTRAT, 42L,
                exemple(), exemple(), "admin@sirh.local");

        JournalAudit journal = journalEnregistre();

        assertEquals("CONTRAT", journal.getEntiteType());
        assertEquals(42L, journal.getEntiteId());
        assertEquals("MODIFICATION", journal.getAction());
        assertEquals("admin@sirh.local", journal.getAuteur());
        assertNotNull(journal.getDateAction());
    }

    @Test
    void journaliser_objetNonSerialisable_enregistreLeMarqueurSansLeverd_exception() {
        assertDoesNotThrow(() -> service.journaliser(ActionAudit.MODIFICATION, TypeEntite.POSTE, 7L,
                new NonSerialisable(), exemple(), "rh@sirh.local"));

        JournalAudit journal = journalEnregistre();

        assertEquals("{\"erreur\":\"serialisation\"}", journal.getValeursAvant());
        // Le champ « apres » reste valide : une erreur d'un côté n'abîme pas l'autre.
        assertNotNull(journal.getValeursApres());
        assertNotEquals("{\"erreur\":\"serialisation\"}", journal.getValeursApres());
    }
}
