package ru.nanit.vasyascheduler.services;

import ru.nanit.vasyascheduler.api.storage.Configuration;
import ru.nanit.vasyascheduler.api.storage.Language;
import ru.nanit.vasyascheduler.api.storage.properties.FileProperties;
import ru.nanit.vasyascheduler.api.storage.properties.Properties;
import ru.nanit.vasyascheduler.api.util.HashUtil;
import ru.nanit.vasyascheduler.api.util.ImageUtil;
import ru.nanit.vasyascheduler.api.util.Logger;
import ru.nanit.vasyascheduler.data.chat.Media;
import ru.nanit.vasyascheduler.data.chat.Message;
import ru.nanit.vasyascheduler.data.schedule.StudentSchedule;
import ru.nanit.vasyascheduler.data.user.SubscriberStudent;
import ru.nanit.vasyascheduler.data.user.SubscriberTeacher;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.URL;
import java.util.*;

public final class ScheduleTimer {

    private Language lang;
    private Properties properties;

    private ScheduleManager scheduleManager;
    private SubscribesManager subscribesManager;

    private Timer timer;
    private Task task;

    private long period;

    public ScheduleTimer(Configuration conf,
                         Language lang,
                         Properties properties,
                         ScheduleManager scheduleManager,
                         SubscribesManager subscribesManager){
        this.lang = lang;
        this.properties = properties;
        this.scheduleManager = scheduleManager;
        this.subscribesManager = subscribesManager;

        this.timer = new Timer();
        this.task = new Task();
        this.period = conf.get().getNode("checkRate").getLong();
    }

    public void start(){
        timer.schedule(task, 0, period);
        Logger.info("Timer has been started");
    }

    public void stop(){
        timer.cancel();
        Logger.info("Timer has been stopped");
    }

    private class Task extends TimerTask {

        @Override
        public void run(){
            boolean updated = false;

            if(checkTeachers()){
                Logger.info("Teachers schedule updated on website. Start sending...");
                try{
                    scheduleManager.updateAllSchedule();
                    updated = true;
                    sendTeachers();
                    Logger.info("Teachers schedule sent");
                } catch (Exception e){
                    Logger.error("Error while updating and sending teachers schedule: ", e);
                }
            }

            Collection<String> updatedStudents = checkStudents();

            if(!updatedStudents.isEmpty()){
                Logger.info("Students schedule updated on website. Start sending...");

                try{
                    if(!updated) scheduleManager.updateStudentsSchedule();
                    sendStudents(updatedStudents);
                    Logger.info("Students schedule sent");
                } catch (Exception e){
                    Logger.error("Sending student schedule interrupted by exception: ", e);
                }
            }

            if(checkConsultations()){
                Logger.info("Consultations schedule updated on website. Start sending...");
                try{
                    scheduleManager.getConsultationSchedule().parse();
                    sendConsultations();
                    Logger.info("Consultation schedule sent");
                } catch (Exception e){
                    Logger.error("Error while updating and sending consultations schedule: ", e);
                }
            }
        }

        private void sendTeachers(){
            for (SubscriberTeacher teacher : subscribesManager.getTeacherSubscribers()){
                try{
                    BufferedImage built = scheduleManager.getTeacherSchedule()
                            .getBuilder().build(lang, scheduleManager.getStudentSchedule(), teacher.getTeacher());

                    if(built != null){
                        Message message = new Message(lang.of("timer.new_schedule"));
                        Media media = new Media(Media.Type.DOCUMENT);

                        media.setStream(ImageUtil.createInputStream(built, "png"));
                        media.setFileName("schedule.png");
                        message.addMedia(media);
                        message.setChatId(teacher.getMessengerId());
                        message.setRemoveLastId(true);

                        BotManager.getBot(teacher.getBotType()).sendMessage(message);
                        Logger.info("Sent schedule to " + teacher.getTeacher() + " (user "+teacher+")");
                    }
                } catch (IOException e){
                    Logger.error("Cannot send schedule to teacher " + teacher + ": ", e);
                }
            }
        }

        private void sendConsultations(){
            for (SubscriberTeacher teacher : subscribesManager.getTeacherSubscribers()){
                try{
                    BufferedImage built = scheduleManager.getConsultationSchedule()
                            .getBuilder().build(lang, teacher.getTeacher());

                    if(built != null){
                        Message message = new Message(lang.of("timer.new_schedule.consultations"));
                        Media media = new Media(Media.Type.DOCUMENT);

                        media.setStream(ImageUtil.createInputStream(built, "png"));
                        media.setFileName("consultations.png");
                        message.addMedia(media);
                        message.setChatId(teacher.getMessengerId());
                        message.setRemoveLastId(true);

                        BotManager.getBot(teacher.getBotType()).sendMessage(message);
                        Logger.info("Sent consultation schedule to " + teacher.getTeacher() + " (user "+teacher+")");
                    }
                } catch (IOException e){
                    Logger.error("Cannot send schedule to teacher " + teacher + ": ", e);
                }
            }
        }

        private void sendStudents(Collection<String> updated){
            for (SubscriberStudent student : subscribesManager.getStudentSubscribers()){
                if(updated.contains(student.getSchedule())){
                    try{
                        BufferedImage built = scheduleManager.getStudentSchedule(student.getSchedule()).toImage();

                        if(built != null){
                            Message message = new Message(lang.of("timer.new_schedule"));
                            Media media = new Media(Media.Type.DOCUMENT);

                            media.setStream(ImageUtil.createInputStream(built, "png"));
                            media.setFileName("students.png");
                            message.addMedia(media);
                            message.setChatId(student.getMessengerId());
                            message.setRemoveLastId(true);

                            BotManager.getBot(student.getBotType()).sendMessage(message);
                            Logger.info("Sent students schedule to user " + student);
                        }
                    } catch (Exception e){
                        Logger.error("Cannot send schedule to student " + student + ": ", e);
                    }
                }
            }
        }

        private boolean checkConsultations(){
            URL consultations = scheduleManager.getConsultationSchedule().getLink();
            String lastHash = properties.getString("hash.consultations");
            String hash = HashUtil.getSHA1(consultations);

            if(hash == null){
                Logger.error("Hash of consultations schedule is null. Maybe ZIEIT website is temporarily unavailable");
                return false;
            }

            if(lastHash.equals(hash)){
                return false;
            }

            properties.set("hash.consultations", hash);
            properties.save();
            return true;
        }

        private boolean checkTeachers(){
            URL teachers = scheduleManager.getTeacherSchedule().getLink();
            String lastHash = properties.getString("hash.teachers");
            String hash = HashUtil.getSHA1(teachers);

            if(hash != null && !lastHash.equals(hash)){
                properties.set("hash.teachers", hash);
                properties.save();
                return true;
            }

            return false;
        }

        private Collection<String> checkStudents(){
            Map<String, String> tempMap = new HashMap<>();
            Map<String, StudentSchedule> students = scheduleManager.getStudentSchedule();
            List<String> updated = new ArrayList<>();

            String lastHash;
            String hash;

            for (Map.Entry<String, StudentSchedule> entry : students.entrySet()){
                lastHash = properties.getString("hash." + entry.getKey());
                hash = HashUtil.getSHA1(entry.getValue().getLink());

                if(hash == null || lastHash.equals(hash)){
                    continue;
                }

                tempMap.put("hash." + entry.getKey(), hash);
                updated.add(entry.getKey());
            }

            // Save all temporary hashes in properties file
            for (Map.Entry<String, String> entry : tempMap.entrySet()){
                properties.set(entry.getKey(), entry.getValue());
            }

            properties.save();
            return updated;
        }

    }

}
