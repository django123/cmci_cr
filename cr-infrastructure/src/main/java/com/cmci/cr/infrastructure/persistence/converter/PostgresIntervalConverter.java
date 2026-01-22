package com.cmci.cr.infrastructure.persistence.converter;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

import java.sql.SQLException;

/**
 * Convertisseur JPA pour le type INTERVAL de PostgreSQL
 * Convertit entre le type INTERVAL de PostgreSQL et String Java
 */
@Converter(autoApply = false)
public class PostgresIntervalConverter implements AttributeConverter<String, Object> {

    @Override
    public Object convertToDatabaseColumn(String attribute) {
        if (attribute == null || attribute.isEmpty()) {
            return "00:00:00";
        }
        return attribute;
    }

    @Override
    public String convertToEntityAttribute(Object dbData) {
        if (dbData == null) {
            return "00:00:00";
        }

        // Si c'est déjà une String, la retourner
        if (dbData instanceof String) {
            return (String) dbData;
        }

        // Gérer PGInterval via reflection pour éviter les dépendances directes
        String className = dbData.getClass().getName();
        if (className.contains("PGInterval") || className.contains("Interval")) {
            try {
                // Essayer d'appeler getValue() ou toString()
                java.lang.reflect.Method getHours = dbData.getClass().getMethod("getHours");
                java.lang.reflect.Method getMinutes = dbData.getClass().getMethod("getMinutes");
                java.lang.reflect.Method getSeconds = dbData.getClass().getMethod("getSeconds");

                int hours = (Integer) getHours.invoke(dbData);
                int minutes = (Integer) getMinutes.invoke(dbData);
                double seconds = (Double) getSeconds.invoke(dbData);

                return String.format("%02d:%02d:%02d", hours, minutes, (int) seconds);
            } catch (Exception e) {
                // Fallback: utiliser toString et parser
                return parseIntervalString(dbData.toString());
            }
        }

        // Fallback: parser la représentation string
        return parseIntervalString(dbData.toString());
    }

    /**
     * Parse une chaîne d'interval PostgreSQL (ex: "01:30:00" ou "1 hour 30 mins")
     */
    private String parseIntervalString(String intervalStr) {
        if (intervalStr == null || intervalStr.isEmpty()) {
            return "00:00:00";
        }

        // Si déjà au format HH:mm:ss ou HH:mm
        if (intervalStr.matches("\\d{2}:\\d{2}(:\\d{2})?")) {
            return intervalStr.length() == 5 ? intervalStr + ":00" : intervalStr;
        }

        // Parser les formats comme "1 hour 30 mins" ou "01:30:00"
        int hours = 0, minutes = 0, seconds = 0;

        // Format avec mots
        if (intervalStr.contains("hour")) {
            String[] parts = intervalStr.split("\\s+");
            for (int i = 0; i < parts.length - 1; i++) {
                if (parts[i + 1].startsWith("hour")) {
                    hours = Integer.parseInt(parts[i]);
                } else if (parts[i + 1].startsWith("min")) {
                    minutes = Integer.parseInt(parts[i]);
                } else if (parts[i + 1].startsWith("sec")) {
                    seconds = Integer.parseInt(parts[i]);
                }
            }
        }

        return String.format("%02d:%02d:%02d", hours, minutes, seconds);
    }
}
