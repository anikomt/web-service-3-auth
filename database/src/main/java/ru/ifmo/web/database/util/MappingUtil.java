package ru.ifmo.web.database.util;

import java.sql.Types;
import java.util.Date;

public class MappingUtil {

    public static int getSqlType(Class<?> clazz) {
        if (clazz == Long.class) {
            return Types.BIGINT;
        } else if (clazz == String.class) {
            return Types.VARCHAR;
        } else if (clazz == Date.class) {
            return Types.TIMESTAMP;
        } else if (clazz == Boolean.class) {
            return Types.BOOLEAN;
        }
        throw new IllegalArgumentException(clazz.getName());
    }
}
