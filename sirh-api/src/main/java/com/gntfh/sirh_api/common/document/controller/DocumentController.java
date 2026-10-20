package com.gntfh.sirh_api.common.document.controller;

import com.gntfh.sirh_api.common.audit.enums.TypeEntite;
import com.gntfh.sirh_api.common.document.dto.ContenuDocument;
import com.gntfh.sirh_api.common.document.dto.DocumentResponse;
import com.gntfh.sirh_api.common.document.enums.TypeDocument;
import com.gntfh.sirh_api.common.document.service.DocumentService;
import com.gntfh.sirh_api.common.security.CurrentUser;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/api/documents")
@RequiredArgsConstructor
@Tag(name = "Documents", description = "Gestion des documents (CV, contrats, etc.)")
public class DocumentController {

    private final DocumentService documentService;

    @PostMapping
    @ResponseStatus(org.springframework.http.HttpStatus.CREATED)
    @PreAuthorize("hasAnyRole('RH', 'IT_SERVICES', 'PAIE')")
    @Operation(summary = "Déposer un document",
            description = "Dépose un fichier lié à une entité (ex. CV d'un candidat). Versionnement automatique.")
    public DocumentResponse televerser(
            @RequestPart("fichier") @NotNull MultipartFile fichier,
            @RequestParam("type") TypeDocument type,
            @RequestParam("entiteType") TypeEntite entiteType,
            @RequestParam("entiteId") Long entiteId,
            @AuthenticationPrincipal Jwt jwt) throws IOException {
        CurrentUser currentUser = CurrentUser.from(jwt);
        return documentService.televerser(fichier, type, entiteType, entiteId, currentUser);
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('RH', 'MANAGER', 'DG', 'IT_SERVICES', 'PAIE')")
    @Operation(summary = "Lister les documents",
            description = "Liste les documents d'une entité, avec ou sans les archives.")
    public List<DocumentResponse> lister(
            @RequestParam("entiteType") TypeEntite entiteType,
            @RequestParam("entiteId") Long entiteId,
            @RequestParam(value = "inclureArchives", defaultValue = "false") boolean inclureArchives) {
        return documentService.lister(entiteType, entiteId, inclureArchives);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('RH', 'MANAGER', 'DG', 'IT_SERVICES', 'PAIE')")
    @Operation(summary = "Obtenir les métadonnées d'un document")
    public DocumentResponse obtenir(@PathVariable Long id) {
        return documentService.obtenir(id);
    }

    @GetMapping("/{id}/contenu")
    @PreAuthorize("hasAnyRole('RH', 'MANAGER', 'DG', 'IT_SERVICES', 'PAIE')")
    @Operation(summary = "Télécharger le contenu d'un document")
    public ResponseEntity<Resource> telecharger(@PathVariable Long id) throws IOException {
        ContenuDocument contenu = documentService.telecharger(id);

        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(contenu.typeMime()))
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=\"" + contenu.nomFichier() + "\"")
                .body(contenu.contenu());
    }
}
