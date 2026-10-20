package com.gntfh.sirh_api.common.audit.controller;

import com.gntfh.sirh_api.common.audit.dto.HistoriqueStatutResponse;
import com.gntfh.sirh_api.common.audit.dto.JournalAuditResponse;
import com.gntfh.sirh_api.common.audit.enums.TypeEntite;
import com.gntfh.sirh_api.common.audit.service.AuditService;
import com.gntfh.sirh_api.common.audit.service.HistoriqueStatutService;
import com.gntfh.sirh_api.common.dto.PageResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/audit")
@RequiredArgsConstructor
@Tag(name = "Audit", description = "Historique de statuts et journal d'audit")
public class AuditController {

    private final HistoriqueStatutService historiqueStatutService;
    private final AuditService auditService;

    @GetMapping("/historique-statuts")
    @PreAuthorize("hasAnyRole('RH', 'DG')")
    @Operation(summary = "Historique des changements de statut",
            description = "Historique chronologique des transitions de statut d'une entité.")
    public List<HistoriqueStatutResponse> historiqueStatuts(
            @RequestParam("entiteType") TypeEntite entiteType,
            @RequestParam("entiteId") Long entiteId) {
        return historiqueStatutService.lister(entiteType, entiteId);
    }

    @GetMapping("/journal")
    @PreAuthorize("hasAnyRole('RH', 'DG')")
    @Operation(summary = "Journal d'audit",
            description = "Journal des actions (création, modification, suppression) avec filtres facultatifs.")
    public PageResponse<JournalAuditResponse> journal(
            @RequestParam(value = "entiteType", required = false) TypeEntite entiteType,
            @RequestParam(value = "entiteId", required = false) Long entiteId,
            @RequestParam(value = "auteur", required = false) String auteur,
            @ParameterObject @PageableDefault(size = 20, sort = "dateAction") Pageable pageable) {
        return auditService.rechercher(entiteType, entiteId, auteur, pageable);
    }
}
