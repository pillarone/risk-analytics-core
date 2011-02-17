package org.pillarone.riskanalytics.core.persistence;

import org.hibernate.Hibernate;
import org.hibernate.HibernateException;
import org.hibernate.usertype.UserType;
import org.joda.time.DateTime;

import java.io.Serializable;
import java.math.BigInteger;
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
        return a == b || !(a == null || b == null) && a.equals(b);
    }

    public int hashCode(Object o) throws HibernateException {
        return o.hashCode();
    }

    public Object nullSafeGet(ResultSet resultSet, String[] strings, Object o) throws HibernateException, SQLException {
        BigInteger val = (BigInteger) Hibernate.BIG_INTEGER.nullSafeGet(resultSet, strings);
        return new DateTime(val.longValue());
    }

    public void nullSafeSet(PreparedStatement preparedStatement, Object o, int i) throws HibernateException, SQLException {
        Long millis = ((DateTime) o).getMillis();
        Hibernate.BIG_INTEGER.nullSafeSet(preparedStatement, new BigInteger(millis.toString()), i);
    }

    public Object deepCopy(Object o) throws HibernateException {
        return new DateTime(o);
    }

    public boolean isMutable() {
        return false;
    }

    public Serializable disassemble(Object o) throws HibernateException {
        return (Serializable) o;
    }

    public Object assemble(Serializable serializable, Object o) throws HibernateException {
        return serializable;
    }

    public Object replace(Object o, Object o1, Object o2) throws HibernateException {
        return o;
    }
}
