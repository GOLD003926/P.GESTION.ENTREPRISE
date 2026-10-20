package com.gntfh.sirh_api.common.audit.service;

import com.fasterxml.jackson.core.JsonProcessingException;
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

import java.time.Instant;

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
        String avant = "Test";
        String apres = "Test Modifié";

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

        // Vérifier que les valeurs sont du JSON valide (String sérialisé en JSON)
        assertNotNull(journal.getValeursAvant());
        assertNotNull(journal.getValeursApres());
        assertEquals("\"Test\"", journal.getValeursAvant());
        assertEquals("\"Test Modifié\"", journal.getValeursApres());
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

        // When
        auditService.journaliser(ActionAudit.CREATION, TypeEntite.EMPLOYE, 1L, null, instant, "admin@sirh.local");

        // Then
        ArgumentCaptor<JournalAudit> captor = ArgumentCaptor.forClass(JournalAudit.class);
        verify(journalAuditRepository).save(captor.capture());

        JournalAudit journal = captor.getValue();
        assertNotNull(journal.getValeursApres());
        assertNull(journal.getValeursAvant());
    }

    @Test
    void journaliser_erreurSerialisation_logueErreurEtEnregistre() throws JsonProcessingException {
        // Given
        JsonMapper failingMapper = mock(JsonMapper.class);
        when(failingMapper.writeValueAsString(any())).thenThrow(new JsonProcessingException("Test error") {});
        AuditService auditServiceWithFailingMapper = new AuditService(journalAuditRepository, failingMapper);

        // When
        auditServiceWithFailingMapper.journaliser(ActionAudit.MODIFICATION, TypeEntite.UTILISATEUR, 1L, "test", null, "admin@sirh.local");

        // Then
        ArgumentCaptor<JournalAudit> captor = ArgumentCaptor.forClass(JournalAudit.class);
        verify(journalAuditRepository).save(captor.capture());

        JournalAudit journal = captor.getValue();
        // En cas d'erreur de sérialisation, on enregistre le marqueur d'erreur
        assertEquals("{\"erreur\":\"serialisation\"}", journal.getValeursAvant());
        // Apres est null, donc pas d'erreur de sérialisation
        assertNull(journal.getValeursApres());
    }
}
