package com.cmci.cr.infrastructure.persistence.converter;

import org.hibernate.engine.spi.SharedSessionContractImplementor;
import org.hibernate.usertype.UserType;

import java.io.Serializable;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;

/**
 * Type Hibernate personnalisé pour les enums PostgreSQL
 */
public class PostgreSQLEnumType implements UserType<Enum> {

    @Override
    public int getSqlType() {
        return Types.OTHER;
    }

    @Override
    public Class<Enum> returnedClass() {
        return Enum.class;
    }

    @Override
    public boolean equals(Enum x, Enum y) {
        return x == y || (x != null && x.equals(y));
    }

    @Override
    public int hashCode(Enum x) {
        return x == null ? 0 : x.hashCode();
    }

    @Override
    public Enum nullSafeGet(ResultSet rs, int position, SharedSessionContractImplementor session, Object owner) throws SQLException {
        String value = rs.getString(position);
        if (rs.wasNull() || value == null) {
            return null;
        }
        // This will be overridden by subclasses
        return null;
    }

    @Override
    public void nullSafeSet(PreparedStatement st, Enum value, int index, SharedSessionContractImplementor session) throws SQLException {
        if (value == null) {
            st.setNull(index, Types.OTHER);
        } else {
            st.setObject(index, value.name(), Types.OTHER);
        }
    }

    @Override
    public Enum deepCopy(Enum value) {
        return value;
    }

    @Override
    public boolean isMutable() {
        return false;
    }

    @Override
    public Serializable disassemble(Enum value) {
        return value;
    }

    @Override
    public Enum assemble(Serializable cached, Object owner) {
        return (Enum) cached;
    }
}
