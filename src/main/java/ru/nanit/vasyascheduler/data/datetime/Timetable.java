package ru.nanit.vasyascheduler.data.datetime;

import ninja.leaping.configurate.ConfigurationNode;
import ru.nanit.vasyascheduler.api.storage.Configuration;
import ru.nanit.vasyascheduler.api.util.Logger;
import ru.nanit.vasyascheduler.api.util.TimeUtil;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class Timetable {

    private static Map<Integer, TimeInterval> intervals = new HashMap<>();

    public static void addInterval(int classIndex, TimeInterval interval){
        intervals.put(classIndex, interval);
    }

    public static TimeInterval getInterval(int classIndex){
        return intervals.get(classIndex);
    }

    public static int getClassCount(){
        return intervals.size();
    }

    public static void parse(Configuration conf){
        Map<Object, ? extends ConfigurationNode> map = conf.get().getNode("timetable").getChildrenMap();

        for (Map.Entry<Object, ? extends ConfigurationNode> entry : map.entrySet()){
            int index = 0;

            try{
                index = Integer.parseInt(entry.getKey().toString());
            } catch (NumberFormatException e){
                Logger.error("Error while parsing timetable. Wrong class count format '" + entry.getKey().toString() + "'");
            }

            String begin = entry.getValue().getNode("begin").getString();
            String end = entry.getValue().getNode("end").getString();

            addInterval(index, new TimeInterval(begin, end));
        }
    }

    public static class TimeInterval {

        private long begin, end;

        public TimeInterval(String begin, String end){
            this.begin = TimeUtil.millisFromString(begin);
            this.end = TimeUtil.millisFromString(end);
        }

        public long getBegin(){
            return begin;
        }

        public long getEnd(){
            return end;
        }

        public long getTime(){
            return end - begin;
        }

        public Date getBeginDate(){
            return TimeUtil.dateFromMillis(begin);
        }

        public Date getEndDate(){
            return TimeUtil.dateFromMillis(end);
        }
    }
}
