package com.cmci.cr.domain.valueobject;

import lombok.Value;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Value Object représentant le Rencontre Dynamique Quotidien avec Dieu (RDQD)
 * Format: "accompli/attendu" (ex: "1/1", "0/1")
 */
@Value
public class RDQD {
    private static final Pattern RDQD_PATTERN = Pattern.compile("^(\\d+)/(\\d+)$");

    int accompli;
    int attendu;

    private RDQD(int accompli, int attendu) {
        if (attendu <= 0) {
            throw new IllegalArgumentException("La valeur attendue doit être supérieure à 0");
        }
        if (accompli < 0) {
            throw new IllegalArgumentException("La valeur accomplie ne peut pas être négative");
        }
        if (accompli > attendu) {
            throw new IllegalArgumentException("La valeur accomplie ne peut pas dépasser la valeur attendue");
        }
        this.accompli = accompli;
        this.attendu = attendu;
    }

    /**
     * Crée un RDQD à partir d'une chaîne au format "accompli/attendu"
     */
    public static RDQD fromString(String rdqdString) {
        if (rdqdString == null || rdqdString.trim().isEmpty()) {
            throw new IllegalArgumentException("Le RDQD ne peut pas être vide");
        }

        Matcher matcher = RDQD_PATTERN.matcher(rdqdString.trim());
        if (!matcher.matches()) {
            throw new IllegalArgumentException("Format RDQD invalide. Format attendu: 'accompli/attendu' (ex: '1/1')");
        }

        int accompli = Integer.parseInt(matcher.group(1));
        int attendu = Integer.parseInt(matcher.group(2));

        return new RDQD(accompli, attendu);
    }

    /**
     * Crée un RDQD directement à partir des valeurs
     */
    public static RDQD of(int accompli, int attendu) {
        return new RDQD(accompli, attendu);
    }

    /**
     * Vérifie si le rendez-vous a été complètement accompli
     */
    public boolean isComplete() {
        return accompli == attendu;
    }

    /**
     * Calcule le pourcentage d'accomplissement
     */
    public double getCompletionPercentage() {
        return (double) accompli / attendu * 100;
    }

    @Override
    public String toString() {
        return accompli + "/" + attendu;
    }
}
