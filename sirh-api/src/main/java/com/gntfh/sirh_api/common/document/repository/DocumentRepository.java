package com.gntfh.sirh_api.common.document.repository;

import com.gntfh.sirh_api.common.audit.enums.TypeEntite;
import com.gntfh.sirh_api.common.document.entity.Document;
import com.gntfh.sirh_api.common.document.enums.TypeDocument;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface DocumentRepository extends JpaRepository<Document, Long>, JpaSpecificationExecutor<Document> {

    /**
     * Numéro de la version courante (non archivée). Spring Data ne sait pas dériver un
     * max() d'un nom de méthode : la requête est donc écrite explicitement.
     */
    @Query("""
            select max(d.numeroVersion) from Document d
            where d.entiteType = :entiteType
              and d.entiteId = :entiteId
              and d.type = :type
              and d.archive = false
            """)
    Optional<Integer> findMaxNumeroVersionByEntiteTypeAndEntiteIdAndTypeAndArchiveFalse(
            @Param("entiteType") TypeEntite entiteType,
            @Param("entiteId") Long entiteId,
            @Param("type") TypeDocument type
    );

    Optional<Document> findByEntiteTypeAndEntiteIdAndTypeAndNumeroVersion(
            TypeEntite entiteType, Long entiteId, TypeDocument type, Integer numeroVersion
    );

    List<Document> findByEntiteTypeAndEntiteId(TypeEntite entiteType, Long entiteId);

    List<Document> findByEntiteTypeAndEntiteIdAndArchiveFalse(TypeEntite entiteType, Long entiteId);
}
