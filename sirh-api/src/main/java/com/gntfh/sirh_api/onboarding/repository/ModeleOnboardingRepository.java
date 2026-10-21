package com.gntfh.sirh_api.onboarding.repository;

import com.gntfh.sirh_api.onboarding.entity.ModeleOnboarding;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.Optional;

public interface ModeleOnboardingRepository extends JpaRepository<ModeleOnboarding, Long>,
        JpaSpecificationExecutor<ModeleOnboarding> {

    boolean existsByNomIgnoreCase(String nom);

    boolean existsByNomIgnoreCaseAndIdNot(String nom, Long id);

    Optional<ModeleOnboarding> findFirstByTypeContratAndActifTrue(String typeContrat);

    Optional<ModeleOnboarding> findFirstByTypeContratIsNullAndActifTrue();
}
