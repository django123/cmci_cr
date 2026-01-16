package com.cmci.cr.domain.valueobject;

/**
 * Value Object représentant les rôles dans la hiérarchie de responsabilité spirituelle CMCI
 *
 * IMPORTANT: Tous les rôles représentent des disciples de Jésus-Christ.
 * La hiérarchie reflète les niveaux de responsabilité dans l'accompagnement spirituel,
 * pas une différence de statut spirituel. Un FD est d'abord un disciple qui amène
 * d'autres disciples à Christ. De même pour Leader, Pasteur, etc.
 */
public enum Role {
    /**
     * Fidèle - Disciple membre de la communauté
     */
    FIDELE("Fidèle", 1),

    /**
     * FD - Faiseur de Disciples
     * Disciple qui accompagne spirituellement 5-15 autres disciples (fidèles)
     */
    FD("Faiseur de Disciples", 2),

    /**
     * Leader - Disciple qui supervise plusieurs FD au niveau d'une église de maison
     */
    LEADER("Leader", 3),

    /**
     * Pasteur - Disciple qui dirige une église locale (rassemblement de plusieurs églises de maison)
     */
    PASTEUR("Pasteur", 4),

    /**
     * Admin - Administrateur système avec accès complet
     */
    ADMIN("Administrateur", 5);

    private final String displayName;
    private final int hierarchyLevel;

    Role(String displayName, int hierarchyLevel) {
        this.displayName = displayName;
        this.hierarchyLevel = hierarchyLevel;
    }

    public String getDisplayName() {
        return displayName;
    }

    public int getHierarchyLevel() {
        return hierarchyLevel;
    }

    /**
     * Vérifie si ce rôle peut superviser un autre rôle
     */
    public boolean canSupervise(Role otherRole) {
        return this.hierarchyLevel > otherRole.hierarchyLevel;
    }

    /**
     * Vérifie si ce rôle peut consulter les CR d'un autre rôle
     */
    public boolean canViewCROf(Role otherRole) {
        return switch (this) {
            case FIDELE -> this == otherRole;
            case FD -> otherRole == FIDELE || this == otherRole;
            case LEADER, PASTEUR -> otherRole.hierarchyLevel <= this.hierarchyLevel;
            case ADMIN -> true;
        };
    }
}
