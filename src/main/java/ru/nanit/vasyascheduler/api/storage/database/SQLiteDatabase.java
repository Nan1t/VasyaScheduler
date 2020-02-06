package ru.nanit.vasyascheduler.api.storage.database;

import ru.nanit.vasyascheduler.api.util.Logger;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.*;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

public class SQLiteDatabase implements Database {

    private String connString;
    private Connection connection;

    public SQLiteDatabase(Path dir, String dbName, String user, String password) throws SQLException {
        connString = "jdbc:sqlite:" + Paths.get(dir.toString(), dbName + ".db");
        connection = DriverManager.getConnection(connString, user, password);
    }

    public void closeConnection() throws SQLException {
        connection.close();
        Logger.info("Database connection closed");
    }

    @Override
    public String toString(){
        return connString;
    }

    @Override
    public Row getRow(String table, int id) {
        return getRow(table, "id", id);
    }

    @Override
    public Row getRow(String table, String key, Object value) {
        Row row = null;

        try  {
            PreparedStatement statement = connection.prepareStatement("SELECT * FROM " + table + " WHERE "+key+"=?");

            statement.setObject(1, value);

            ResultSet result = statement.executeQuery();
            ResultSetMetaData data = result.getMetaData();

            if(result.next()) {
                row = new Row();
                for(int i = 1; i <= data.getColumnCount(); i++) {
                    row.addField(data.getColumnName(i), result.getObject(i));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return row;
    }

    @Override
    public Row getRow(String table, String[] keys, Object[] values) {
        Row row = null;

        try {
            StringBuilder sql = new StringBuilder("SELECT * FROM " + table + " WHERE ");

            for (int i = 0; i < keys.length; i++){
                sql.append(keys[i]).append("=").append((i < keys.length - 1) ? "?," : "?");
            }

            PreparedStatement statement = connection.prepareStatement(sql.toString());

            for (int i = 0; i < keys.length; i++){
                statement.setObject(i+1, values[i]);
            }

            ResultSet result = statement.executeQuery();
            ResultSetMetaData data = result.getMetaData();

            if(result.next()) {
                row = new Row();
                for(int i = 1; i <= data.getColumnCount(); i++) {
                    row.addField(data.getColumnName(i), result.getObject(i));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return row;
    }

    @Override
    public Row getRow(String sql, Object... params) {
        Row row = null;

        try  {
            PreparedStatement statement = connection.prepareStatement(sql);

            for (int i = 0; i < params.length; i++){
                statement.setObject(i+1, params[i]);
            }

            ResultSet result = statement.executeQuery();
            ResultSetMetaData data = result.getMetaData();

            if(result.next()) {
                row = new Row();
                for(int i = 1; i <= data.getColumnCount(); i++) {
                    row.addField(data.getColumnName(i), result.getObject(i));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return row;
    }

    @Override
    public Row[] getRows(String table, String key, Object value) {
        List<Row> rows = new ArrayList<>();

        try  {
            PreparedStatement statement = connection.prepareStatement("SELECT * FROM " + table + " WHERE "+key+"=?");

            statement.setObject(1, value);

            ResultSet result = statement.executeQuery();
            ResultSetMetaData data = result.getMetaData();

            while(result.next()) {
                Row row = new Row();

                for(int i = 1; i <= data.getColumnCount(); i++) {
                    row.addField(data.getColumnName(i), result.getObject(i));
                }

                rows.add(row);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return rows.toArray(new Row[rows.size()]);
    }

    @Override
    public Row[] getRows(String table, String[] keys, String[] values) {
        List<Row> rows = new ArrayList<>();

        try {
            StringBuilder sql = new StringBuilder("SELECT * FROM " + table + " WHERE ");

            for (int i = 0; i < keys.length; i++){
                sql.append(keys[i]).append("=").append((i < keys.length - 1) ? "?," : "?");
            }

            PreparedStatement statement = connection.prepareStatement(sql.toString());

            for (int i = 0; i < keys.length; i++){
                statement.setObject(i+1, values[i]);
            }

            ResultSet result = statement.executeQuery();
            ResultSetMetaData data = result.getMetaData();

            while(result.next()) {
                Row row = new Row();
                for(int i = 1; i <= data.getColumnCount(); i++) {
                    row.addField(data.getColumnName(i), result.getObject(i));
                }
                rows.add(row);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return rows.toArray(new Row[rows.size()]);
    }

    @Override
    public Row[] getRows(String sql, Object... params) {
        List<Row> rows = new ArrayList<>();

        try  {
            PreparedStatement statement = connection.prepareStatement(sql);

            for (int i = 0; i < params.length; i++){
                statement.setObject(i+1, params[i]);
            }

            ResultSet result = statement.executeQuery();
            ResultSetMetaData data = result.getMetaData();

            while(result.next()) {
                Row row = new Row();

                for(int i = 1; i <= data.getColumnCount(); i++) {
                    row.addField(data.getColumnName(i), result.getObject(i));
                }

                rows.add(row);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return rows.toArray(new Row[rows.size()]);
    }

    @Override
    public int createRow(String table, Row row) {
        try {
            StringBuilder cols = new StringBuilder();
            StringBuilder vals = new StringBuilder();

            Collection<String> keys = row.getFields().keySet();
            Collection<Object> values = row.getFields().values();

            for(String key : keys){
                cols.append(key).append(",");
            }

            for (int i = 0; i < values.size(); i++){
                vals.append("?,");
            }

            cols = new StringBuilder(cols.substring(0, cols.length() - 1));
            vals = new StringBuilder(vals.substring(0, vals.length() - 1));

            String request = "INSERT INTO " + table + "(" + cols.toString() + ") VALUES (" + vals.toString()  + ");";

            PreparedStatement statement = connection.prepareStatement(request);

            int index = 1;
            for (Map.Entry<String, Object> entry : row.getFields().entrySet()){
                statement.setObject(index, entry.getValue());
                index++;
            }

            return statement.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
            return -1;
        }
    }

    @Override
    public void updateRow(String table, Row row, String key, Object value) {
        StringBuilder elements = new StringBuilder();

        String[] keys = row.getFields().keySet().toArray(new String[row.getFields().size()]);
        Object[] values = row.getFields().values().toArray(new Object[row.getFields().size()]);

        for (String k : keys) {
            elements.append("`").append(k).append("`=?,");
        }

        elements = new StringBuilder(elements.substring(0, elements.length() - 1));

        String request = "UPDATE " + table + " SET " + elements + " WHERE " + key + "=?";

        try  {
            PreparedStatement statement = connection.prepareStatement(request);

            statement.setObject(1, value);

            for(int i = 0; i < values.length; i++){
                int index = i+1;
                statement.setObject(index, values[i]);
            }

            statement.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void updateRow(String table, Row row, Row params) {
        StringBuilder elements = new StringBuilder();

        String[] keys = row.getFields().keySet().toArray(new String[row.getFields().size()]);
        Object[] values = row.getFields().values().toArray(new Object[row.getFields().size()]);

        for (String k : keys) {
            elements.append("`").append(k).append("`=?,");
        }

        StringBuilder cols = new StringBuilder();
        int size = params.getFields().keySet().size();
        int col = 1;

        for(String key : params.getFields().keySet()){
            cols.append(key).append("=?");
            if(col < size){
                cols.append(" AND ");
            }
            col++;
        }

        elements = new StringBuilder(elements.substring(0, elements.length() - 1));
        String request = "UPDATE " + table + " SET " + elements + " WHERE " + cols.toString();

        try  {
            PreparedStatement statement = connection.prepareStatement(request);

            int index = 1;
            for(int i = 0; i < values.length; i++){
                statement.setObject(i+1, values[i]);
                index++;
            }

            for (Map.Entry<String, Object> entry : params.getFields().entrySet()){
                statement.setObject(index, entry.getValue());
                index++;
            }

            statement.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public boolean deleteRow(String table, String key, Object value) {
        try  {
            PreparedStatement statement = connection.prepareStatement("DELETE FROM " + table + " WHERE " + key + "=?");
            statement.setObject(1, value);
            return statement.execute();
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public boolean deleteRow(String table, Row params) {
        try {
            StringBuilder cols = new StringBuilder();

            int size = params.getFields().keySet().size();
            int i = 1;

            for(String key : params.getFields().keySet()){
                cols.append(key).append("=?");
                if(i < size){
                    cols.append(" AND ");
                }
                i++;
            }

            PreparedStatement statement = connection.prepareStatement("DELETE FROM " + table + " WHERE " + cols.toString());

            i = 1;
            for (Map.Entry<String, Object> entry : params.getFields().entrySet()){
                statement.setObject(i, entry.getValue());
                i++;
            }

            return statement.execute();
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public boolean existsRow(String table, Row row){
        try {
            StringBuilder cols = new StringBuilder();

            int size = row.getFields().keySet().size();
            int i = 1;

            for(String key : row.getFields().keySet()){
                cols.append(key).append("=?");
                if(i < size){
                    cols.append(" AND ");
                }
                i++;
            }

            PreparedStatement statement = connection.prepareStatement("SELECT * FROM " + table + " WHERE " + cols.toString());

            i = 1;
            for (Map.Entry<String, Object> entry : row.getFields().entrySet()){
                statement.setObject(i, entry.getValue());
                i++;
            }

            ResultSet result = statement.executeQuery();

            return result.next();
        } catch(SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public void executeSQL(String sql, Object... params) {
        try {
            PreparedStatement statement = connection.prepareStatement(sql);

            for (int i = 0; i < params.length; i++){
                statement.setObject(i+1, params[i]);
            }

            statement.execute();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
