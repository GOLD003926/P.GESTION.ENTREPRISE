package com.gntfh.sirh_api.common.document.service;

import com.gntfh.sirh_api.common.exception.BusinessRuleException;
import com.gntfh.sirh_api.common.audit.enums.TypeEntite;
import com.gntfh.sirh_api.common.document.dto.ContenuDocument;
import com.gntfh.sirh_api.common.document.dto.DocumentResponse;
import com.gntfh.sirh_api.common.document.entity.Document;
import com.gntfh.sirh_api.common.document.enums.TypeDocument;
import com.gntfh.sirh_api.common.document.repository.DocumentRepository;
import com.gntfh.sirh_api.common.exception.ResourceNotFoundException;
import com.gntfh.sirh_api.common.security.CurrentUser;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Comparator;
import java.util.HexFormat;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class DocumentService {

    private final DocumentRepository documentRepository;
    private final FileStorageService fileStorageService;

    private static final List<String> MIME_TYPES_AUTORISES = List.of(
            "application/pdf",
            "application/msword",
            "application/vnd.openxmlformats-officedocument.wordprocessingml.document",
            "image/png",
            "image/jpeg"
    );

    @Transactional
    public Document enregistrer(MultipartFile fichier, TypeDocument type, TypeEntite entiteType, Long entiteId) throws IOException {
        verifierFichier(fichier);

        byte[] contenu = fichier.getBytes();
        String hashSha256 = calculerHashSha256(contenu);

        Integer derniereVersion = documentRepository
                .findMaxNumeroVersionByEntiteTypeAndEntiteIdAndTypeAndArchiveFalse(entiteType, entiteId, type)
                .orElse(0);

        Integer nouvelleVersion = derniereVersion + 1;

        if (derniereVersion > 0) {
            documentRepository
                    .findByEntiteTypeAndEntiteIdAndTypeAndNumeroVersion(entiteType, entiteId, type, derniereVersion)
                    .ifPresent(doc -> {
                        doc.setArchive(true);
                        documentRepository.save(doc);
                    });
        }

        String cheminStockage = fileStorageService.sauvegarder(contenu, entiteType, entiteId, fichier.getOriginalFilename());

        Document document = Document.builder()
                .type(type)
                .nomFichier(fichier.getOriginalFilename())
                .typeMime(fichier.getContentType())
                .tailleOctets((long) contenu.length)
                .cheminStockage(cheminStockage)
                .hashSha256(hashSha256)
                .numeroVersion(nouvelleVersion)
                .entiteType(entiteType)
                .entiteId(entiteId)
                .archive(false)
                .build();

        return documentRepository.save(document);
    }

    @Transactional
    public Document enregistrer(byte[] contenu, String nomFichier, String typeMime, TypeDocument type, TypeEntite entiteType, Long entiteId) throws IOException {
        if (contenu == null || contenu.length == 0) {
            throw new BusinessRuleException("Le contenu du fichier ne peut pas être vide");
        }

        String hashSha256 = calculerHashSha256(contenu);

        Integer derniereVersion = documentRepository
                .findMaxNumeroVersionByEntiteTypeAndEntiteIdAndTypeAndArchiveFalse(entiteType, entiteId, type)
                .orElse(0);

        Integer nouvelleVersion = derniereVersion + 1;

        if (derniereVersion > 0) {
            documentRepository
                    .findByEntiteTypeAndEntiteIdAndTypeAndNumeroVersion(entiteType, entiteId, type, derniereVersion)
                    .ifPresent(doc -> {
                        doc.setArchive(true);
                        documentRepository.save(doc);
                    });
        }

        String cheminStockage = fileStorageService.sauvegarder(contenu, entiteType, entiteId, nomFichier);

        Document document = Document.builder()
                .type(type)
                .nomFichier(nomFichier)
                .typeMime(typeMime)
                .tailleOctets((long) contenu.length)
                .cheminStockage(cheminStockage)
                .hashSha256(hashSha256)
                .numeroVersion(nouvelleVersion)
                .entiteType(entiteType)
                .entiteId(entiteId)
                .archive(false)
                .build();

        return documentRepository.save(document);
    }

    @Transactional
    public DocumentResponse televerser(MultipartFile fichier, TypeDocument type, TypeEntite entiteType, Long entiteId, CurrentUser currentUser) throws IOException {
        Document document = enregistrer(fichier, type, entiteType, entiteId);
        return toResponse(document);
    }

    @Transactional(readOnly = true)
    public DocumentResponse obtenir(Long id) {
        Document document = documentRepository.findById(id)
                .orElseThrow(() -> ResourceNotFoundException.of("Document", id));
        return toResponse(document);
    }

    @Transactional(readOnly = true)
    public List<DocumentResponse> lister(TypeEntite entiteType, Long entiteId, boolean inclureArchives) {
        List<Document> documents;
        if (inclureArchives) {
            documents = documentRepository.findByEntiteTypeAndEntiteId(entiteType, entiteId);
        } else {
            documents = documentRepository.findByEntiteTypeAndEntiteIdAndArchiveFalse(entiteType, entiteId);
        }

        return documents.stream()
                .sorted(Comparator.comparing(Document::getType).thenComparing(Document::getNumeroVersion).reversed())
                .map(this::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public ContenuDocument telecharger(Long id) throws IOException {
        Document document = documentRepository.findById(id)
                .orElseThrow(() -> ResourceNotFoundException.of("Document", id));

        Resource contenu = fileStorageService.charger(document.getCheminStockage());

        return new ContenuDocument(contenu, document.getNomFichier(), document.getTypeMime());
    }

    @Transactional(readOnly = true)
    public DocumentResponse toResponse(Document document) {
        return new DocumentResponse(
                document.getId(),
                document.getType(),
                document.getNomFichier(),
                document.getTypeMime(),
                document.getTailleOctets(),
                document.getNumeroVersion(),
                document.getEntiteType(),
                document.getEntiteId(),
                document.isArchive(),
                document.getCreatedAt(),
                document.getCreatedBy()
        );
    }

    private void verifierFichier(MultipartFile fichier) {
        if (fichier.isEmpty()) {
            throw new BusinessRuleException("Le fichier ne peut pas être vide");
        }

        String typeMime = fichier.getContentType();
        if (typeMime == null || !MIME_TYPES_AUTORISES.contains(typeMime)) {
            throw new BusinessRuleException("Type de fichier non autorisé : " + typeMime);
        }
    }

    private String calculerHashSha256(byte[] contenu) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(contenu);
            return HexFormat.of().formatHex(hash);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("Algorithme SHA-256 non disponible", e);
        }
    }
}
