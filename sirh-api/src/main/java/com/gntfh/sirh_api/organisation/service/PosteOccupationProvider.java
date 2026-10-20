package com.gntfh.sirh_api.organisation.port;

/**
 * Port pour obtenir l'occupation actuelle d'un poste.
 * L'implémentation sera fournie par le module employe.
 */
public interface PosteOccupationProvider {

    /**
     * Retourne le nombre d'occupants actifs d'un poste.
     *
     * @param posteId identifiant du poste
     * @return nombre d'occupants actifs
     */
    int occupationActuelle(Long posteId);
}
