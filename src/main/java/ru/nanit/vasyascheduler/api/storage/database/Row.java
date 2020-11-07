package ru.nanit.vasyascheduler.api.storage.database;

import java.util.HashMap;

public class Row {

    private HashMap<String, Object> fields = new HashMap<>();

    public <T> T getField(String key){
        return (fields.containsKey(key)) ? (T) fields.get(key) : null;
    }

    public HashMap<String, Object> getFields(){
        return fields;
    }

    public void addField(String key, Object value){
        fields.put(key.toLowerCase(), value);
    }

}
