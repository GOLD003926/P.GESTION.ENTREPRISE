package com.gntfh.sirh_api.employe.service;

import com.gntfh.sirh_api.demande.service.EmployeLookup;
import com.gntfh.sirh_api.employe.entity.Affectation;
import com.gntfh.sirh_api.employe.entity.Employe;
import com.gntfh.sirh_api.employe.repository.AffectationRepository;
import com.gntfh.sirh_api.employe.repository.EmployeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * Adaptateur pour le port EmployeLookup.
 */
@Component
@RequiredArgsConstructor
public class EmployeLookupAdapter implements EmployeLookup {

    private final EmployeRepository employeRepository;
    private final AffectationRepository affectationRepository;

    @Override
    public boolean existe(Long employeId) {
        return employeRepository.existsById(employeId);
    }

    @Override
    public Optional<Long> departementActuel(Long employeId) {
        List<Affectation> affectations = affectationRepository.findActivesByEmployeId(employeId, LocalDate.now());
        if (affectations.isEmpty()) {
            return Optional.empty();
        }
        // Prendre l'affectation active la plus récente
        Affectation derniere = affectations.stream()
                .max((a1, a2) -> a1.getDateDebut().compareTo(a2.getDateDebut()))
                .orElse(null);
        if (derniere != null && derniere.getPoste() != null && derniere.getPoste().getDepartement() != null) {
            return Optional.of(derniere.getPoste().getDepartement().getId());
        }
        return Optional.empty();
    }
}
