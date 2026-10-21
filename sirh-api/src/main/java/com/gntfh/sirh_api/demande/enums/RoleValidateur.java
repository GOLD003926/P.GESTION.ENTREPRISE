package com.gntfh.sirh_api.demande.enums;

import com.gntfh.sirh_api.auth.enums.Role;

public enum RoleValidateur {
    RH(1),
    FINANCE(2),
    DG(3);

    private final int ordre;

    RoleValidateur(int ordre) {
        this.ordre = ordre;
    }

    public int ordre() {
        return ordre;
    }

    public Role versRole() {
        return switch (this) {
            case RH -> Role.RH;
            case FINANCE -> Role.FINANCE;
            case DG -> Role.DG;
        };
    }

    public StatutDemande statutEnValidation() {
        return switch (this) {
            case RH -> StatutDemande.EN_VALIDATION_RH;
            case FINANCE -> StatutDemande.EN_VALIDATION_FINANCE;
            case DG -> StatutDemande.EN_VALIDATION_DG;
        };
    }
}
