package com.gntfh.sirh_api.common.audit.repository;

import com.gntfh.sirh_api.common.audit.entity.HistoriqueStatut;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface HistoriqueStatutRepository extends JpaRepository<HistoriqueStatut, Long> {

    List<HistoriqueStatut> findByEntiteTypeAndEntiteIdOrderByDateChangementAsc(
            String entiteType, Long entiteId
    );
}
