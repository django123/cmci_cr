package com.cmci.cr.infrastructure.config;

/**
 * Constantes pour les noms de topics Kafka
 */
public final class KafkaTopics {

    private KafkaTopics() {
        // Classe utilitaire, constructeur privé
    }

    /**
     * Topic pour les événements de Compte Rendu
     * (CRCreatedEvent, CRSubmittedEvent, CRValidatedEvent, etc.)
     */
    public static final String CR_EVENTS = "cr-events";

    /**
     * Topic pour les événements de Commentaire
     * (CommentaireAddedEvent, etc.)
     */
    public static final String COMMENTAIRE_EVENTS = "commentaire-events";

    /**
     * Topic pour les événements d'Utilisateur
     * (UtilisateurCreatedEvent, UtilisateurUpdatedEvent, etc.)
     */
    public static final String UTILISATEUR_EVENTS = "utilisateur-events";

    /**
     * Topic pour les notifications à envoyer aux utilisateurs
     */
    public static final String NOTIFICATIONS = "notifications";
}
