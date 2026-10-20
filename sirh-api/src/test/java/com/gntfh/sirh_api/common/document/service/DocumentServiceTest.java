package com.gntfh.sirh_api.common.document.service;

import com.gntfh.sirh_api.common.audit.enums.TypeEntite;
import com.gntfh.sirh_api.common.document.entity.Document;
import com.gntfh.sirh_api.common.document.enums.TypeDocument;
import com.gntfh.sirh_api.common.document.repository.DocumentRepository;
import com.gntfh.sirh_api.common.exception.ResourceNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DocumentServiceTest {

    @Mock
    private DocumentRepository documentRepository;

    @Mock
    private FileStorageService fileStorageService;

    @InjectMocks
    private DocumentService documentService;

    private MultipartFile fichierValide;

    @BeforeEach
    void setUp() {
        fichierValide = new MockMultipartFile(
                "fichier",
                "cv.pdf",
                "application/pdf",
                "contenu test".getBytes()
        );
    }

    @Test
    void enregistrer_fichierVide_exception() {
        MultipartFile fichierVide = new MockMultipartFile(
                "fichier",
                "vide.pdf",
                "application/pdf",
                new byte[0]
        );

        assertThrows(IllegalArgumentException.class, () ->
                documentService.enregistrer(fichierVide, TypeDocument.CV, TypeEntite.CANDIDAT, 1L)
        );
    }

    @Test
    void enregistrer_typeMimeNonAutorise_exception() {
        MultipartFile fichierInterdit = new MockMultipartFile(
                "fichier",
                "script.exe",
                "application/x-msdownload",
                "contenu".getBytes()
        );

        assertThrows(IllegalArgumentException.class, () ->
                documentService.enregistrer(fichierInterdit, TypeDocument.CV, TypeEntite.CANDIDAT, 1L)
        );
    }

    @Test
    void enregistrer_premiereVersion_archiveFalse() throws IOException {
        when(documentRepository.findMaxNumeroVersionByEntiteTypeAndEntiteIdAndTypeAndArchiveFalse(
                any(), any(), any()
        )).thenReturn(Optional.empty());
        when(fileStorageService.sauvegarder(any(), any(), any(), any())).thenReturn("chemin/relatif");
        when(documentRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        Document document = documentService.enregistrer(
                fichierValide, TypeDocument.CV, TypeEntite.CANDIDAT, 1L
        );

        assertEquals(1, document.getNumeroVersion());
        assertFalse(document.isArchive());
        verify(documentRepository, never()).save(any(Document.class));
    }

    @Test
    void enregistrer_versionExistante_archivePrecedente() throws IOException {
        Document documentExistant = Document.builder()
                .numeroVersion(1)
                .archive(false)
                .build();

        when(documentRepository.findMaxNumeroVersionByEntiteTypeAndEntiteIdAndTypeAndArchiveFalse(
                any(), any(), any()
        )).thenReturn(Optional.of(1));
        when(documentRepository.findByEntiteTypeAndEntiteIdAndTypeAndNumeroVersion(
                any(), any(), any(), any()
        )).thenReturn(Optional.of(documentExistant));
        when(fileStorageService.sauvegarder(any(), any(), any(), any())).thenReturn("chemin/relatif");
        when(documentRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        Document document = documentService.enregistrer(
                fichierValide, TypeDocument.CV, TypeEntite.CANDIDAT, 1L
        );

        assertEquals(2, document.getNumeroVersion());
        assertTrue(documentExistant.isArchive());
        verify(documentRepository, times(2)).save(any(Document.class));
    }

    @Test
    void obtenir_documentExiste_retourneResponse() {
        Document document = Document.builder()
                .type(TypeDocument.CV)
                .nomFichier("cv.pdf")
                .typeMime("application/pdf")
                .tailleOctets(1024L)
                .numeroVersion(1)
                .entiteType(TypeEntite.CANDIDAT)
                .entiteId(1L)
                .archive(false)
                .build();

        when(documentRepository.findById(1L)).thenReturn(Optional.of(document));

        var response = documentService.obtenir(1L);

        assertNotNull(response);
        assertEquals("cv.pdf", response.nomFichier());
    }

    @Test
    void obtenir_documentInexiste_exception() {
        when(documentRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> documentService.obtenir(1L));
    }

    @Test
    void telecharger_fichierInexistant_exception() throws IOException {
        Document document = Document.builder()
                .cheminStockage("chemin/inexistant.pdf")
                .nomFichier("fichier.pdf")
                .typeMime("application/pdf")
                .build();

        when(documentRepository.findById(1L)).thenReturn(Optional.of(document));
        when(fileStorageService.charger("chemin/inexistant.pdf"))
                .thenThrow(new IOException("Fichier introuvable"));

        assertThrows(IOException.class, () -> documentService.telecharger(1L));
    }
}
