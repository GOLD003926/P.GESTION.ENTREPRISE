package com.gntfh.sirh_api.onboarding.listener;

import com.gntfh.sirh_api.contrat.event.ContratAnnuleEvent;
import com.gntfh.sirh_api.contrat.event.ContratCreeEvent;
import com.gntfh.sirh_api.contrat.event.ContratSigneEvent;
import com.gntfh.sirh_api.onboarding.service.OnboardingService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ContratEventListener {

    private final OnboardingService onboardingService;

    @EventListener
    public void traiterContratCree(ContratCreeEvent event) {
        onboardingService.creerPourContrat(
                event.contratId(),
                event.employeId(),
                event.type().name(),
                event.dateDebut(),
                "system"
        );
    }

    @EventListener
    public void traiterContratSigne(ContratSigneEvent event) {
        onboardingService.marquerContratSigne(event.contratId(), "system");
    }

    @EventListener
    public void traiterContratAnnule(ContratAnnuleEvent event) {
        onboardingService.annulerPourContrat(event.contratId(), "system");
    }
}
