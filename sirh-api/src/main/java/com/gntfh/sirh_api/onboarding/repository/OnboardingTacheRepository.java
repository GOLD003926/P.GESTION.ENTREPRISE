package com.gntfh.sirh_api.onboarding.repository;

import com.gntfh.sirh_api.auth.enums.Role;
import com.gntfh.sirh_api.onboarding.entity.OnboardingTache;
import com.gntfh.sirh_api.onboarding.enums.StatutTache;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;

public interface OnboardingTacheRepository extends JpaRepository<OnboardingTache, Long> {

    List<OnboardingTache> findByOnboardingIdOrderByEcheanceAscIdAsc(Long onboardingId);

    Page<OnboardingTache> findByRoleResponsableInAndStatutIn(
            Collection<Role> roles,
            Collection<StatutTache> statuts,
            Pageable pageable
    );

    Page<OnboardingTache> findByRoleResponsableAndStatutIn(
            Role roleResponsable,
            Collection<StatutTache> statuts,
            Pageable pageable
    );

    Page<OnboardingTache> findByRoleResponsableInAndStatut(
            Collection<Role> roles,
            StatutTache statut,
            Pageable pageable
    );

    Page<OnboardingTache> findByRoleResponsableAndStatut(
            Role roleResponsable,
            StatutTache statut,
            Pageable pageable
    );

    List<OnboardingTache> findByOnboardingIdAndCode(Long onboardingId, String code);

    boolean existsByOnboardingIdAndObligatoireTrueAndStatutNot(Long onboardingId, StatutTache statut);
}
