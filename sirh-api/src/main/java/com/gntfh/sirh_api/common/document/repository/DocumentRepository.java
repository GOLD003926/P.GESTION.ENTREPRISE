package com.gntfh.sirh_api.common.document.repository;

import com.gntfh.sirh_api.common.audit.enums.TypeEntite;
import com.gntfh.sirh_api.common.document.entity.Document;
import com.gntfh.sirh_api.common.document.enums.TypeDocument;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;
import java.util.Optional;

public interface DocumentRepository extends JpaRepository<Document, Long>, JpaSpecificationExecutor<Document> {

    Optional<Integer> findMaxNumeroVersionByEntiteTypeAndEntiteIdAndTypeAndArchiveFalse(
            TypeEntite entiteType, Long entiteId, TypeDocument type
    );

    Optional<Document> findByEntiteTypeAndEntiteIdAndTypeAndNumeroVersion(
            TypeEntite entiteType, Long entiteId, TypeDocument type, Integer numeroVersion
    );

    List<Document> findByEntiteTypeAndEntiteId(TypeEntite entiteType, Long entiteId);

    List<Document> findByEntiteTypeAndEntiteIdAndArchiveFalse(TypeEntite entiteType, Long entiteId);
}
