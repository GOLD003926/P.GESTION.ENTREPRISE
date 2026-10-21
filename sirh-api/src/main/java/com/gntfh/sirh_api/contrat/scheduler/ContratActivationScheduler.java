package com.gntfh.sirh_api.contrat.scheduler;

import com.gntfh.sirh_api.contrat.service.ContratService;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;

@Component
@RequiredArgsConstructor
public class ContratActivationScheduler {

    private final ContratService contratService;

    @Scheduled(cron = "0 5 0 * * *")
    public void activerContratsDus() {
        contratService.activerContratsDus(LocalDate.now(), "system");
    }
}
