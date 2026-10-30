package com.gntfh.sirh_api.organisation.port;

/**
 * Port vers le module employé pour connaître l'occupation actuelle d'un poste.
 */
@FunctionalInterface
public interface PosteOccupationProvider {

    int occupationActuelle(Long posteId);
}
