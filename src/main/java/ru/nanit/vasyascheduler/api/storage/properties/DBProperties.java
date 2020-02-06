package ru.nanit.vasyascheduler.api.storage.properties;

import ru.nanit.vasyascheduler.api.storage.Tables;
import ru.nanit.vasyascheduler.api.storage.database.Database;
import ru.nanit.vasyascheduler.api.storage.database.Row;
import ru.nanit.vasyascheduler.api.util.Logger;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

public class DBProperties implements Properties {

    private Database database;
    private Map<String, String> properties = new HashMap<>();

    public DBProperties(Database database){
        this.database = database;
        loadAll();
    }

    @Override
    public String getString(String key) {
        return properties.get(key);
    }

    @Override
    public String getString(String key, String def) {
        return properties.getOrDefault(key, def);
    }

    @Override
    public int getInt(String key) {
        return Integer.parseInt(properties.get(key));
    }

    @Override
    public int getInt(String key, int def) {
        return Integer.parseInt(properties.getOrDefault(key, String.valueOf(def)));
    }

    @Override
    public double getDouble(String key) {
        return Double.parseDouble((properties.get(key)));
    }

    @Override
    public double getDouble(String key, double def) {
        return Double.parseDouble(properties.getOrDefault(key, String.valueOf(def)));
    }

    @Override
    public void set(String key, Object value) {
        properties.put(key, value.toString());
        CompletableFuture.runAsync(()->{
            Row row = new Row();
            row.addField("key", key);

            if(database.existsRow(Tables.HASH, row)){
                row.addField("value", value.toString());
                database.updateRow(Tables.HASH, row, "key", key);
            } else {
                row.addField("value", value.toString());
                database.createRow(Tables.HASH, row);
            }
        });
    }

    @Override
    public void setIfAbsent(String key, Object value) {
        properties.putIfAbsent(key, value.toString());

        CompletableFuture.runAsync(()->{
            Row row = new Row();
            row.addField("key", key);

            if(!database.existsRow(Tables.HASH, row)){
                row.addField("value", value.toString());
                database.createRow(Tables.HASH, row);
            }
        });
    }

    @Override
    public void fill(Object value, String... keys) {

    }

    @Override
    public void fillIfAbsent(Object value, String... keys) {

    }

    @Override
    public void reload() {

    }

    @Override
    public void save() {

    }

    private void loadAll(){
        Row[] rows = database.getRows("SELECT * FROM " + Tables.HASH);

        for (Row row : rows){
            properties.put(row.getField("key"), row.getField("value"));
        }

        Logger.info("Loaded " + rows.length + " properties");
    }
}
