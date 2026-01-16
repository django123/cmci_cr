package com.cmci.cr.domain.valueobject;

/**
 * Value Object représentant le statut d'un Compte Rendu
 */
public enum StatutCR {
    /**
     * CR en cours de rédaction, non encore soumis
     */
    BROUILLON("Brouillon"),

    /**
     * CR soumis par le fidèle
     */
    SOUMIS("Soumis"),

    /**
     * CR validé par le FD
     */
    VALIDE("Validé");

    private final String displayName;

    StatutCR(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }

    /**
     * Vérifie si le CR est modifiable
     */
    public boolean isModifiable() {
        return this == BROUILLON;
    }

    /**
     * Vérifie si le CR peut être validé
     */
    public boolean canBeValidated() {
        return this == SOUMIS;
    }
}
