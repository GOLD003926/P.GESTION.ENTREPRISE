package com.gntfh.sirh_api.auth.enums;

/**
 * Rôles applicatifs (miroir de la section 2 et de la matrice de droits du document de conception).
 * Un utilisateur a UN seul rôle. Les valeurs sont stockées en texte en base : ne jamais renommer
 * une valeur sans migration, et ne jamais réordonner dans l'espoir de changer des numéros.
 *
 * "Intervenant" n'est volontairement pas un rôle : c'est une situation (être affecté à un entretien).
 */
public enum Role {
    /** Gère les comptes, rôles et paramètres. */
    ADMIN,
    /** Pilote tout le processus de recrutement et de gestion des employés. */
    RH,
    /** Exprime les besoins, participe aux entretiens, encadre les arrivants. */
    MANAGER,
    /** Valide la disponibilité budgétaire des demandes. */
    FINANCE,
    /** Validation finale des demandes, dérogations de salaire. */
    DG,
    /** Comptes, badges, matériel (onboarding). */
    IT_SERVICES,
    /** Inscription du salarié en paie (onboarding). */
    PAIE,
    /** Salarié : consulte son dossier et son onboarding. */
    SALARIE
}
