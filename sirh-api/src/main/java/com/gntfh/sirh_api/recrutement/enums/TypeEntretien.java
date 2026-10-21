package com.gntfh.sirh_api.recrutement.enums;

public enum TypeEntretien {
    ENTRETIEN_RH,
    TEST_TECHNIQUE,
    ENTRETIEN_FINAL;

    public static TypeEntretien pourNiveau(int niveau) {
        return switch (niveau) {
            case 1 -> ENTRETIEN_RH;
            case 2 -> TEST_TECHNIQUE;
            case 3 -> ENTRETIEN_FINAL;
            default -> throw new IllegalArgumentException("Niveau doit être entre 1 et 3");
        };
    }
}
