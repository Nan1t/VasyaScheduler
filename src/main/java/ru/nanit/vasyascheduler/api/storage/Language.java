package ru.nanit.vasyascheduler.api.storage;

import com.google.common.reflect.TypeToken;
import ninja.leaping.configurate.objectmapping.ObjectMappingException;
import ru.nanit.vasyascheduler.api.util.Logger;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Collections;
import java.util.List;

public class Language extends Configuration {

    public Language(String name, Path directory, Object main) throws IOException {
        super(name, directory, main);
    }

    public Language(String name, Path directory) throws IOException {
        super(name, directory);
    }

    public String of(String key){
        return String.join("\n", ofList(key));
    }

    public List<String> ofList(String key){
        try{
            return get().getNode(key).getList(TypeToken.of(String.class), Collections.singletonList(key));
        } catch (ObjectMappingException e){
            Logger.warn(e.getMessage());
            return Collections.singletonList(key);
        }
    }
}
