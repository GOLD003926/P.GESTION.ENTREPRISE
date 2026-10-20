package com.gntfh.sirh_api.common.numerotation;

import com.gntfh.sirh_api.common.config.SirhProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.time.Year;

@Slf4j
@Service
@RequiredArgsConstructor
public class NumeroService {

    private final JdbcTemplate jdbcTemplate;
    private final SirhProperties sirhProperties;

    public String prochain(SequenceNumero sequence) {
        Long numero = jdbcTemplate.queryForObject(
                "SELECT nextval(?)",
                Long.class,
                sequence.getNomSequence()
        );

        String prefixe = sequence.getPrefixe();
        if (sequence == SequenceNumero.EMPLOYE) {
            prefixe = sirhProperties.employe().matriculePrefixe();
        }

        int annee = Year.now().getValue();
        String numeroFormate = String.format("%04d", numero);

        return String.format("%s-%d-%s", prefixe, annee, numeroFormate);
    }
}
