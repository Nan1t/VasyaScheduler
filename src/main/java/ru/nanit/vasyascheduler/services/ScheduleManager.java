package ru.nanit.vasyascheduler.services;

import com.google.common.reflect.TypeToken;
import ru.nanit.vasyascheduler.api.storage.Configuration;
import ru.nanit.vasyascheduler.api.storage.properties.Properties;
import ru.nanit.vasyascheduler.api.util.Logger;
import ru.nanit.vasyascheduler.data.schedule.ConsultationSchedule;
import ru.nanit.vasyascheduler.data.schedule.StudentSchedule;
import ru.nanit.vasyascheduler.data.schedule.TeacherSchedule;

import java.util.*;

public final class ScheduleManager {

    private Configuration scheduleConf;
    private Properties properties;

    private TeacherSchedule teacherSchedule; // Contains parsed teacher schedule
    private ConsultationSchedule consultationSchedule; // Contains parsed consultation schedule
    private Map<String, StudentSchedule> studentSchedule = new HashMap<>(); // Contains all parsed tables of students schedule

    public ScheduleManager(Configuration scheduleConf, Properties properties){
        this.properties = properties;
        this.scheduleConf = scheduleConf;
    }

    public TeacherSchedule getTeacherSchedule(){
        return this.teacherSchedule;
    }

    public ConsultationSchedule getConsultationSchedule(){
        return consultationSchedule;
    }

    public StudentSchedule getStudentSchedule(String key){
        return this.studentSchedule.get(key);
    }

    public Map<String, StudentSchedule> getStudentSchedule(){
        return this.studentSchedule;
    }

    public void updateAllSchedule() throws Exception {
        teacherSchedule.parse();
        Collection<StudentSchedule> students = studentSchedule.values();
        for (StudentSchedule schedule : students){
            try{
                schedule.parse();
            } catch (Exception e){
                Logger.error("Cannot load schedule " + schedule.getName() + ": ", e);
            }
        }
        Logger.info("Updated (re-parsed) all schedule files from website");
    }

    public void updateStudentsSchedule() throws Exception {
        Collection<StudentSchedule> students = studentSchedule.values();
        for (StudentSchedule schedule : students){
            try{
                schedule.parse();
            } catch (Exception e){
                Logger.error("Cannot load schedule " + schedule.getName() + ": ", e);
            }
        }
        Logger.info("Updated (re-parsed) students schedule");
    }

    public void loadTeacherSchedule() {
        try{
            Logger.info("Loading teachers schedule...");
            teacherSchedule = scheduleConf.get().getNode("teachers").getValue(TypeToken.of(TeacherSchedule.class));
            teacherSchedule.parse();
            Logger.info("Teachers schedule loaded!");
        } catch (Exception e){
            Logger.error("Cannot load teacher schedule: ", e);
        }
    }

    public void loadConsultationSchedule() {
        try{
            Logger.info("Loading consultations schedule...");
            consultationSchedule = scheduleConf.get().getNode("consultations").getValue(TypeToken.of(ConsultationSchedule.class));
            consultationSchedule.parse();
            Logger.info("Consultations schedule loaded!");
        } catch (Exception e){
            Logger.error("Cannot load consultation schedule: ", e);
        }
    }

    public void loadStudentsSchedule() throws Exception {
        Logger.info("Loading students schedule...");
        List<StudentSchedule> schedules = scheduleConf.get().getNode("students").getList(TypeToken.of(StudentSchedule.class));

        for (StudentSchedule schedule : schedules){
            try{
                schedule.parse();
                studentSchedule.put(schedule.getName(), schedule);
                Logger.info("Loaded student schedule '" + schedule.getName() + "'");
            } catch (Exception e){
                Logger.error("Cannot load schedule " + schedule.getName() + ": ", e);
            }
        }

        Logger.info("All students schedule successfully loaded!");
    }

    public void loadProperties(){
        for (String key : studentSchedule.keySet()){
            properties.setIfAbsent("hash." + key, "null");
        }

        properties.setIfAbsent("hash.teachers", "null");
        properties.setIfAbsent("hash.consultations", "null");
        properties.save();
    }

}
