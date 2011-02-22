package org.pillarone.riskanalytics.core.persistence;

import org.apache.commons.lang.ObjectUtils;
import org.hibernate.HibernateException;
import org.hibernate.usertype.UserType;
import org.joda.time.DateTime;

import java.io.Serializable;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;

public class DateTimeMillisUserType implements UserType {

    public int[] sqlTypes() {
        return new int[]{Types.BIGINT};
    }

    public Class returnedClass() {
        return DateTime.class;
    }

    public boolean equals(Object a, Object b) throws HibernateException {
        return ObjectUtils.equals(a, b);
    }

    public int hashCode(Object o) throws HibernateException {
        return o.hashCode();
    }

    public Object nullSafeGet(ResultSet resultSet, String[] strings, Object o) throws HibernateException, SQLException {
        long millis = resultSet.getLong(strings[0]);
        if (resultSet.wasNull()) {
            return null;
        }
        return new DateTime(millis);
    }

    public void nullSafeSet(PreparedStatement preparedStatement, Object o, int i) throws HibernateException, SQLException {
        if (o == null) {
            preparedStatement.setNull(i, Types.BIGINT);
        } else {
            DateTime dateTime = (DateTime) o;
            preparedStatement.setLong(i, dateTime.getMillis());
        }
    }

    public Object deepCopy(Object o) throws HibernateException {
        if(o == null) {
            return null;
        }
        DateTime dateTime = (DateTime) o;
        return new DateTime(dateTime.getMillis());
    }

    public boolean isMutable() {
        return false; //DateTime is immutable
    }

    public Serializable disassemble(Object value) throws HibernateException {
        return (Serializable) value; //DateTime is serializable
    }

    public Object assemble(Serializable cached, Object owner) throws HibernateException {
        return cached; //DateTime is serializable
    }

    public Object replace(Object original, Object target, Object owner) throws HibernateException {
        return original;
    }
}
