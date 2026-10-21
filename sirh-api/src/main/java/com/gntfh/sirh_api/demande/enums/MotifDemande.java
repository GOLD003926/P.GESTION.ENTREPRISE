package com.gntfh.sirh_api.demande.enums;

public enum MotifDemande {
    CROISSANCE,
    NOUVEAU_PROJET,
    RESTRUCTURATION,
    RENFORT_TEMPORAIRE,
    DEMISSION,
    LICENCIEMENT,
    RETRAITE,
    MUTATION,
    FIN_CONTRAT,
    DECES,
    AUTRE;

    public boolean estValidePour(TypeDemande typeDemande) {
        return switch (typeDemande) {
            case CREATION -> this == CROISSANCE || this == NOUVEAU_PROJET || this == RESTRUCTURATION || this == RENFORT_TEMPORAIRE;
            case REMPLACEMENT -> this == DEMISSION || this == LICENCIEMENT || this == RETRAITE || this == MUTATION || this == FIN_CONTRAT || this == DECES;
        };
    }
}
