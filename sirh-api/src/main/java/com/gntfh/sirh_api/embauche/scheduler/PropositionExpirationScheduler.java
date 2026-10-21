package com.gntfh.sirh_api.embauche.scheduler;

import com.gntfh.sirh_api.embauche.service.EmbaucheService;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;

@Component
@RequiredArgsConstructor
public class PropositionExpirationScheduler {

    private final EmbaucheService embaucheService;

    @Scheduled(cron = "0 10 0 * * *")
    public void expirerPropositionsEchues() {
        embaucheService.expirerPropositionsEchues(LocalDate.now());
    }
}
