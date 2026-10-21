package com.gntfh.sirh_api.contrat.scheduler;

import com.gntfh.sirh_api.contrat.service.ContratService;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;

@Component
@RequiredArgsConstructor
public class ContratAlerteScheduler {

    private final ContratService contratService;

    @Scheduled(cron = "0 0 7 * * *")
    public void alerterFinsProches() {
        contratService.alerterFinsProches(LocalDate.now());
    }
}
