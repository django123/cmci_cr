-- Migration pour corriger le type de la colonne priere_seule
-- Change de INTERVAL vers VARCHAR pour correspondre à l'entité JPA

-- Convertir la colonne priere_seule de INTERVAL vers VARCHAR
ALTER TABLE compte_rendu
ALTER COLUMN priere_seule TYPE VARCHAR(20)
USING CASE
    WHEN priere_seule IS NOT NULL THEN
        LPAD(EXTRACT(HOUR FROM priere_seule)::text, 2, '0') || ':' ||
        LPAD(EXTRACT(MINUTE FROM priere_seule)::text, 2, '0') || ':' ||
        LPAD(EXTRACT(SECOND FROM priere_seule)::text, 2, '0')
    ELSE '00:00:00'
END;
