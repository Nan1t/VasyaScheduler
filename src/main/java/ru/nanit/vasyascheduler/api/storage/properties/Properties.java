package ru.nanit.vasyascheduler.api.storage.properties;


public interface Properties {

    String getString(String key);

    String getString(String key, String def);

    int getInt(String key);

    int getInt(String key, int def);

    double getDouble(String key);

    double getDouble(String key, double def);

    void set(String key, Object value);

    void setIfAbsent(String key, Object value);

    void fill(Object value, String... keys);

    void fillIfAbsent(Object value, String... keys);

    void reload();

    void save();

}
