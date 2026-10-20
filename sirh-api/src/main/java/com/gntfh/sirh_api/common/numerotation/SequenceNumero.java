package com.gntfh.sirh_api.common.numerotation;

public enum SequenceNumero {
    DEMANDE("seq_demande_recrutement", "DR"),
    OFFRE("seq_offre_emploi", "OF"),
    EMPLOYE("seq_employe_matricule", null),
    CONTRAT("seq_contrat", "CT");

    private final String nomSequence;
    private final String prefixe;

    SequenceNumero(String nomSequence, String prefixe) {
        this.nomSequence = nomSequence;
        this.prefixe = prefixe;
    }

    public String getNomSequence() {
        return nomSequence;
    }

    public String getPrefixe() {
        return prefixe;
    }
}
