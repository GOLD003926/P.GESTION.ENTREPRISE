package com.gntfh.sirh_api.employe.service;

import com.gntfh.sirh_api.organisation.port.PosteOccupationProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * Adaptateur pour le port PosteOccupationProvider.
 */
@Component
@RequiredArgsConstructor
public class AffectationOccupationAdapter implements PosteOccupationProvider {

    private final AffectationService affectationService;

    @Override
    public int occupationActuelle(Long posteId) {
        return affectationService.occupationActuelle(posteId);
    }
}
