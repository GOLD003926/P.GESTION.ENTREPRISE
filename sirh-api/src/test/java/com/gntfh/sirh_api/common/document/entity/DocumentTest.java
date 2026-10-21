package com.gntfh.sirh_api.common.document.entity;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** Un document est créé NON ARCHIVÉ par défaut. */
class DocumentTest {

    @Test
    void builder_sansValeur_estNonArchive() {
        assertFalse(Document.builder().build().isArchive());
    }

    @Test
    void builder_archiveFaux_estNonArchive() {
        assertFalse(Document.builder().archive(false).build().isArchive());
    }

    @Test
    void builder_archiveVrai_estArchive() {
        assertTrue(Document.builder().archive(true).build().isArchive());
    }
}
