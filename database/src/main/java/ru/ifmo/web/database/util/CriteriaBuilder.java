package ru.ifmo.web.database.util;

import lombok.NoArgsConstructor;

import java.util.Map;


public class CriteriaBuilder {
    StringBuilder builder;

    public CriteriaBuilder select() {
        builder = new StringBuilder("SELECT ");
        return this;
    }

    public CriteriaBuilder delete() {
        builder = new StringBuilder("DELETE ");
        return this;
    }

    public CriteriaBuilder insert(String tableName) {
        builder = new StringBuilder("INSERT INTO ");
        append(tableName);
        return this;
    }


    public CriteriaBuilder update(String tableName) {
        builder = new StringBuilder("UPDATE ");
        append(tableName);
        append("SET");

        return this;
    }

    public CriteriaBuilder selectors(String... fields) {
        for (String field : fields) {
            append(field, ", ");
        }
        builder.setLength(builder.length() - 2);
        return this;
    }

    public void columns(String... columns) {
        builder.append("(");
        for (String column : columns) {
            append(column, ",");
        }
        builder.setLength(builder.length() - 1);
        builder.append(") ");
    }

    public void setColumns(Map.Entry<String, String>... columns) {
        for (Map.Entry<String, String> column : columns) {
            append(column.getKey(), "= '");
            builder.append(column.getValue());
            builder.append("',");

        }
        builder.setLength(builder.length() - 1);
    }

    public CriteriaBuilder from(String tableName) {
        append("FROM");
        append(tableName);
        return this;
    }

    public CriteriaBuilder where(Predicate predicate) {
        append("WHERE").append(predicate);
        return this;
    }

    public CriteriaBuilder values(Predicate predicate) {
        append("VALUES(").append(predicate).append(")");
        return this;
    }

    @Override
    public String toString() {
        for (StackTraceElement stackTraceElement : Thread.currentThread().getStackTrace()) {
            System.out.println(stackTraceElement.toString());
        }
        return builder.toString().concat(";");
    }

    private StringBuilder append(String str) {
        return append(str, " ");
    }

    private StringBuilder append(String str, String delimeter) {
        return builder.append(" ").append(str).append(delimeter);
    }


}
