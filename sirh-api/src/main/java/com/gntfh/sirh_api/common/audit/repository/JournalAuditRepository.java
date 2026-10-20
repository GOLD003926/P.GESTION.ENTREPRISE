package com.gntfh.sirh_api.common.audit.repository;

import com.gntfh.sirh_api.common.audit.entity.JournalAudit;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface JournalAuditRepository extends JpaRepository<JournalAudit, Long>, JpaSpecificationExecutor<JournalAudit> {
}
