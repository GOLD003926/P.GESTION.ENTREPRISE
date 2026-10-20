package com.gntfh.sirh_api.common.audit.service;

import com.fasterxml.jackson.databind.json.JsonMapper;
import com.gntfh.sirh_api.common.audit.entity.JournalAudit;
import com.gntfh.sirh_api.common.audit.enums.ActionAudit;
import com.gntfh.sirh_api.common.audit.enums.TypeEntite;
import com.gntfh.sirh_api.common.audit.repository.JournalAuditRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuditServiceTest {

    @Mock
    private JournalAuditRepository journalAuditRepository;

    @InjectMocks
    private AuditService auditService;

    private JsonMapper jsonMapper;

    @BeforeEach
    void setUp() {
        jsonMapper = JsonMapper.builder().build();
        auditService = new AuditService(journalAuditRepository, jsonMapper);
    }

    @Test
    void journaliser_objetsNonNuls_seraliseJSON() {
        // Given
        TestResponse avant = new TestResponse(1L, "Test", BigDecimal.valueOf(1000.50), LocalDate.of(2026, 1, 15), Instant.now());
        TestResponse apres = new TestResponse(2L, "Test Modifié", BigDecimal.valueOf(1200.00), LocalDate.of(2026, 2, 1), Instant.now());

        // When
        auditService.journaliser(ActionAudit.MODIFICATION, TypeEntite.UTILISATEUR, 1L, avant, apres, "admin@sirh.local");

        // Then
        ArgumentCaptor<JournalAudit> captor = ArgumentCaptor.forClass(JournalAudit.class);
        verify(journalAuditRepository).save(captor.capture());

        JournalAudit journal = captor.getValue();
        assertEquals(ActionAudit.MODIFICATION.name(), journal.getAction());
        assertEquals(TypeEntite.UTILISATEUR.name(), journal.getEntiteType());
        assertEquals(1L, journal.getEntiteId());
        assertEquals("admin@sirh.local", journal.getAuteur());

        // Vérifier que les valeurs sont du JSON valide
        assertNotNull(journal.getValeursAvant());
        assertNotNull(journal.getValeursApres());
        assertTrue(journal.getValeursAvant().contains("\"id\":1"));
        assertTrue(journal.getValeursAvant().contains("\"nom\":\"Test\""));
        assertTrue(journal.getValeursApres().contains("\"id\":2"));
        assertTrue(journal.getValeursApres().contains("\"nom\":\"Test Modifié\""));
    }

    @Test
    void journaliser_valeursNulles_nullEnBase() {
        // When
        auditService.journaliser(ActionAudit.CREATION, TypeEntite.DEPARTEMENT, 1L, null, null, "admin@sirh.local");

        // Then
        ArgumentCaptor<JournalAudit> captor = ArgumentCaptor.forClass(JournalAudit.class);
        verify(journalAuditRepository).save(captor.capture());

        JournalAudit journal = captor.getValue();
        assertNull(journal.getValeursAvant());
        assertNull(journal.getValeursApres());
    }

    @Test
    void journaliser_recordAvecInstant_seraliseCorrectement() {
        // Given
        Instant instant = Instant.parse("2026-01-15T10:30:00Z");
        TestResponse record = new TestResponse(1L, "Test", BigDecimal.valueOf(1000.50), LocalDate.of(2026, 1, 15), instant);

        // When
        auditService.journaliser(ActionAudit.CREATION, TypeEntite.EMPLOYE, 1L, null, record, "admin@sirh.local");

        // Then
        ArgumentCaptor<JournalAudit> captor = ArgumentCaptor.forClass(JournalAudit.class);
        verify(journalAuditRepository).save(captor.capture());

        JournalAudit journal = captor.getValue();
        assertNotNull(journal.getValeursApres());
        assertTrue(journal.getValeursApres().contains("\"instantAtest\":"));
        assertNull(journal.getValeursAvant());
    }

    @Test
    void journaliser_erreurSerialisation_logueErreurEtEnregistre() {
        // Given
        Object objetNonSeralisable = new Object() {
            @Override
            public String toString() {
                throw new RuntimeException("Test exception");
            }
        };

        // When
        auditService.journaliser(ActionAudit.MODIFICATION, TypeEntite.UTILISATEUR, 1L, objetNonSeralisable, null, "admin@sirh.local");

        // Then
        ArgumentCaptor<JournalAudit> captor = ArgumentCaptor.forClass(JournalAudit.class);
        verify(journalAuditRepository).save(captor.capture());

        JournalAudit journal = captor.getValue();
        assertEquals("{\"erreur\":\"serialisation\"}", journal.getValeursAvant());
        assertNull(journal.getValeursApres());
    }

    // Record de test pour simuler un Response DTO
    record TestResponse(Long id, String nom, BigDecimal montant, LocalDate date, Instant instantAtest) {}
}
