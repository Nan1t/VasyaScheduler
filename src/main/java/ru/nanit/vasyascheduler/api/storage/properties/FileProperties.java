package ru.nanit.vasyascheduler.api.storage.properties;

import ru.nanit.vasyascheduler.api.util.Logger;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Set;

public class FileProperties implements Properties {

    private final Path file;
    private java.util.Properties properties;

    public FileProperties(Path directory, String file){
        this.file = Paths.get(directory.toString(), file);
        reload();
    }

    public FileProperties(Path file){
        this.file = file;
        reload();
    }

    public String getString(String key){
        return getString(key, null);
    }

    public String getString(String key, String def){
        return properties.getProperty(key, def);
    }

    public int getInt(String key){
        return getInt(key, -1);
    }

    public int getInt(String key, int def){
        try{
            return Integer.parseInt(properties.getProperty(key));
        } catch (NumberFormatException e){
            e.printStackTrace();
            return def;
        }
    }

    public double getDouble(String key){
        return getDouble(key, -1.0);
    }

    public double getDouble(String key, double def){
        try{
            return Double.parseDouble(properties.getProperty(key));
        } catch (NumberFormatException e){
            e.printStackTrace();
            return def;
        }
    }

    public void set(String key, Object value){
        properties.setProperty(key, value.toString());
    }

    public void setIfAbsent(String key, Object value){
        if(!properties.containsKey(key)){
            set(key, value);
        }
    }

    public void fill(Object value, String... keys){
        for (String key : keys){
            set(key, value);
        }
    }

    public void fillIfAbsent(Object value, String... keys){
        for (String key : keys){
            setIfAbsent(key, value);
        }
    }

    public void reload(){
        try{
            if(!Files.exists(file)){
                Files.createFile(file);
            }

            InputStream stream = Files.newInputStream(file);
            properties = new java.util.Properties();
            properties.load(stream);
            stream.close();
        } catch (IOException e){
            Logger.error("Error while load properties in " + file.toString() + ": " + e.getMessage());
        }
    }

    @Override
    public void invalidate() {
        Set<Object> keys = properties.keySet();

        for (Object key : keys){
            properties.setProperty(key.toString(), "invalidated");
        }
    }

    public void save(){
        try {
            OutputStream stream = Files.newOutputStream(file);
            properties.store(stream, null);
            stream.close();
        } catch (IOException e){
            Logger.error("Error while saving properties file " + file.toString() + ": " + e.getMessage());
        }
    }
}
