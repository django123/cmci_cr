package com.cmci.cr.infrastructure.persistence.converter;

import com.cmci.cr.infrastructure.persistence.entity.CompteRenduJpaEntity.StatutCREnum;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

/**
 * Convertisseur JPA pour StatutCREnum vers PostgreSQL enum
 */
@Converter(autoApply = false)
public class StatutCREnumConverter implements AttributeConverter<StatutCREnum, String> {

    @Override
    public String convertToDatabaseColumn(StatutCREnum attribute) {
        if (attribute == null) {
            return null;
        }
        return attribute.name();
    }

    @Override
    public StatutCREnum convertToEntityAttribute(String dbData) {
        if (dbData == null) {
            return null;
        }
        return StatutCREnum.valueOf(dbData);
    }
}
