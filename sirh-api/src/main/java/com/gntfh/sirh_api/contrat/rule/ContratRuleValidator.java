package com.gntfh.sirh_api.contrat.rule;

import com.gntfh.sirh_api.common.config.SirhProperties;
import com.gntfh.sirh_api.contrat.entity.Contrat;
import com.gntfh.sirh_api.contrat.enums.TypeContrat;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.LocalDate;

@Component
@RequiredArgsConstructor
public class ContratRuleValidator {

    private final SirhProperties sirhProperties;

    /**
     * Vérifie les règles de création d'un contrat.
     *
     * CON-01 / RG-34 : ESSAI de durée strictement positive et inférieure ou égale à
     * sirh.contrat.essai.duree-max-mois.
     * CON-02 / RG-35 : CDD de durée strictement positive et inférieure ou égale à
     * sirh.contrat.cdd.duree-max-mois. H3 centralise également ici le plafond cumulé du CDD.
     * CON-03 / RG-36 : CDI sans durée ni date de fin ; ESSAI et CDD avec une durée obligatoire
     * et une date de fin calculée par calculerFinPrevue.
     */
    public void verifierCreation(TypeContrat type, LocalDate debut, Integer dureeMois) {
        throw new UnsupportedOperationException("TODO CON-01");
    }

    /**
     * Vérifie les règles de renouvellement.
     *
     * CON-04 / RG-34 : ESSAI renouvelable une seule fois ; le cumul des contrats ESSAI de la chaîne
     * ne dépasse pas sirh.contrat.essai.cumul-max-mois.
     * CON-05 / RG-38 : un seul contrat ACTIF par employé, garanti aussi par l'index unique partiel.
     * CON-06 : renouvellement depuis un contrat ACTIF, de même type, avec une date de début comprise
     * entre le lendemain du début et le lendemain de la fin prévue du précédent.
     */
    public void verifierRenouvellement(Contrat precedent, int dureeMois, LocalDate debut) {
        throw new UnsupportedOperationException("TODO CON-04");
    }

    /**
     * Vérifie les règles de transformation de contrat.
     *
     * CON-07 : seules les transformations ESSAI → CDD/CDI et CDD → CDI sont autorisées ; un CDI
     * ne peut pas être transformé. Pour un CDD cible, la durée est obligatoire et bornée par
     * sirh.contrat.cdd.duree-max-mois.
     */
    public void verifierTransformation(Contrat precedent, TypeContrat cible, Integer dureeMois, LocalDate debut) {
        throw new UnsupportedOperationException("TODO CON-07");
    }

    /**
     * Calcule la durée cumulée (en mois) de la chaîne de contrats de même type.
     * Le parcours remonte contratPrecedent tant que le type reste identique.
     *
     * CON-04 : supporte le plafond de cumul des ESSAI.
     * H3/CON-02 : concentre l'interprétation du plafond cumulé des CDD.
     */
    public int dureeCumuleeMois(Contrat contrat) {
        throw new UnsupportedOperationException("TODO CON-04");
    }

    /**
     * Calcule la date de fin prévue : dateDebut + dureeMois mois - 1 jour.
     */
    public LocalDate calculerFinPrevue(LocalDate dateDebut, int dureeMois) {
        throw new UnsupportedOperationException("TODO CON-01");
    }
}
