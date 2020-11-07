package ru.nanit.vasyascheduler.api.storage.database;

import java.sql.SQLException;

public interface Database {

    void closeConnection() throws SQLException;

    Row getRow(String table, int id);

    Row getRow(String table, String key, Object value);

    Row getRow(String table, String[] keys, Object[] values);

    Row getRow(String sql, Object... params);

    Row[] getRows(String table, String key, Object value);

    Row[] getRows(String table, String[] keys, String[] values);

    Row[] getRows(String sql, Object... params);

    int createRow(String table, Row row);

    void updateRow(String table, Row row, String key, Object value);

    void updateRow(String table, Row row, Row params);

    boolean deleteRow(String table, String key, Object value);

    boolean deleteRow(String table, Row params);

    boolean existsRow(String table, Row row);

    void executeSQL(String sql, Object... params);

}
