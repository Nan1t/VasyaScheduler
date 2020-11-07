package ru.nanit.vasyascheduler.api.util;

import java.sql.Time;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

public final class TimeUtil {

    private static final SimpleDateFormat FORMAT_TIME = new SimpleDateFormat("HH:mm");
    private static final SimpleDateFormat FORMAT_DAY = new SimpleDateFormat("yyyy-MM-dd");
    private static final SimpleDateFormat FORMAT_FULL = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    static {
        FORMAT_TIME.setTimeZone(TimeZone.getTimeZone("GMT+3"));
    }

    /**
     * Get a time in milliseconds from string
     * @param time Time in format HH:mm
     * @return time in milliseconds
     */
    public static long millisFromString(String time){
        String[] arr = time.split(":");
        if(arr.length < 3){
            for (int i = arr.length; i < 3; i++){
                time += ":00";
            }
        }
        return Time.valueOf(time).getTime();
    }

    /**
     * Get a full Date with day from time string
     * @param time Time in format HH:mm
     * @return Time in milliseconds
     */
    public static Date dateFromTime(String time){
        try{
            String day = FORMAT_DAY.format(new Date());
            return FORMAT_FULL.parse(day + " " + time);
        } catch (ParseException e){
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Get a full Date with day from milliseconds
     * @param millis Time in milliseconds
     * @return Time in milliseconds
     */
    public static Date dateFromMillis(long millis){
        try{
            String day = FORMAT_DAY.format(new Date());
            Time time = new Time(millis);
            return FORMAT_FULL.parse(day + " " + time.toString());
        } catch (ParseException e){
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Get a time string in format HH:mm from milliseconds
     * @param millis Time in milliseconds
     * @return Parsed time in string format
     */
    public static String timeFromMillis(long millis){
        return FORMAT_TIME.format(new Date(millis));
    }
}
