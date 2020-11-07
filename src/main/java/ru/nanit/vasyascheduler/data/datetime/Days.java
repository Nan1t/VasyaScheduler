package ru.nanit.vasyascheduler.data.datetime;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import ninja.leaping.configurate.ConfigurationNode;
import ru.nanit.vasyascheduler.api.storage.Configuration;
import ru.nanit.vasyascheduler.api.util.StringUtil;

import java.util.Map;

public class Days {

    private static BiMap<String, Integer> names = HashBiMap.create();

    public static int getDayNumber(String name){
        for (Map.Entry<String, Integer> entry : names.entrySet()){
            if(StringUtil.levenshtein(name, entry.getKey()) < 2){
                return entry.getValue();
            }
        }
        return -1;
    }

    public static String getDayName(int number){
        return names.inverse().getOrDefault(number, String.valueOf(number));
    }

    public static int getDaysCount(){
        return names.size();
    }

    public static void parse(Configuration scheduleConf){
        names.clear();
        names = HashBiMap.create();

        Map<Object, ? extends ConfigurationNode> child = scheduleConf.get().getNode("days").getChildrenMap();

        for (ConfigurationNode node : child.values()){
            String name = node.getString();
            int day = Integer.parseInt(node.getKey().toString());
            names.put(name, day);
        }
    }

}
