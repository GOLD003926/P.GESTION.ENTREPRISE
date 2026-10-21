package com.gntfh.sirh_api.onboarding.repository;

import com.gntfh.sirh_api.onboarding.entity.Onboarding;
import com.gntfh.sirh_api.onboarding.enums.StatutOnboarding;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.Optional;

public interface OnboardingRepository extends JpaRepository<Onboarding, Long>,
        JpaSpecificationExecutor<Onboarding> {

    Optional<Onboarding> findByContratId(Long contratId);

    Optional<Onboarding> findFirstByEmployeIdOrderByCreatedAtDesc(Long employeId);

    boolean existsByEmployeIdAndStatut(Long employeId, StatutOnboarding statut);
}
