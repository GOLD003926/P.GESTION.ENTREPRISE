package com.gntfh.sirh_api.common.document.repository;

import com.gntfh.sirh_api.common.audit.enums.TypeEntite;
import com.gntfh.sirh_api.common.document.entity.Document;
import com.gntfh.sirh_api.common.document.enums.TypeDocument;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Test sur la VRAIE base (PostgreSQL de développement). Chaque test s'exécute dans une
 * transaction annulée à la fin : aucune ligne ne reste en base. Les repositories simulés
 * par Mockito ne peuvent pas détecter une requête mal écrite : ce test le peut.
 */
@SpringBootTest
@Transactional
class DocumentRepositoryTest {

    private static final Long ENTITE_ID = 987_654L;   // identifiant inexistant en base réelle

    @Autowired
    private DocumentRepository repository;

    private void enregistrer(int version, boolean archive) {
        repository.saveAndFlush(Document.builder()
                .type(TypeDocument.CV)
                .nomFichier("cv.pdf")
                .typeMime("application/pdf")
                .tailleOctets(10L)
                .cheminStockage("test/cv-" + version + ".pdf")
                .hashSha256("a".repeat(64))
                .numeroVersion(version)
                .entiteType(TypeEntite.CANDIDATURE)
                .entiteId(ENTITE_ID)
                .archive(archive)
                .build());
    }

    private Optional<Integer> versionCourante() {
        return repository.findMaxNumeroVersionByEntiteTypeAndEntiteIdAndTypeAndArchiveFalse(
                TypeEntite.CANDIDATURE, ENTITE_ID, TypeDocument.CV);
    }

    @Test
    void versionCourante_aucunDocument_estVide() {
        assertEquals(Optional.empty(), versionCourante());
    }

    @Test
    void versionCourante_uneVersionNonArchivee_retourneCetteVersion() {
        enregistrer(1, false);

        assertEquals(Optional.of(1), versionCourante());
    }

    @Test
    void versionCourante_ancienneArchivee_retourneLaVersionCourante() {
        enregistrer(1, true);
        enregistrer(2, false);

        assertEquals(Optional.of(2), versionCourante());
    }
}
