package ru.nanit.vasyascheduler.services;

import ru.nanit.vasyascheduler.api.storage.Configuration;
import ru.nanit.vasyascheduler.api.storage.Language;
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
import java.util.concurrent.*;

public final class ScheduleTimer {

    private static boolean updating;

    private final Language lang;
    private final Properties properties;

    private final ScheduleManager scheduleManager;
    private final SubscribesManager subscribesManager;

    private final long period;
    private final ScheduledExecutorService timer;
    private final Executor executor;

    private ScheduledFuture<?> task;

    public ScheduleTimer(Configuration conf,
                         Language lang,
                         Properties properties,
                         ScheduleManager scheduleManager,
                         SubscribesManager subscribesManager){
        this.lang = lang;
        this.properties = properties;
        this.scheduleManager = scheduleManager;
        this.subscribesManager = subscribesManager;

        this.executor = Executors.newCachedThreadPool();
        this.timer = Executors.newScheduledThreadPool(1);
        this.period = conf.get().getNode("checkRate").getLong();
    }

    public void start(){
        task = timer.scheduleAtFixedRate(new TimerTask(), 0, period, TimeUnit.MILLISECONDS);
        Logger.info("Timer has been started");
    }

    public void stop(){
        task.cancel(true);
        Logger.info("Timer has been stopped");
    }

    public static boolean isUpdating(){
        return updating;
    }

    private class TimerTask implements Runnable {

        @Override
        public void run(){
            boolean updated = false;

            if(checkTeachers()){
                Logger.info("Teachers schedule updated on website. Downloading...");
                try{
                    updating = true;
                    scheduleManager.updateAllSchedule();
                    updated = true;
                    updating = false;
                    sendTeachers();
                } catch (Exception e){
                    Logger.error("Error while updating and sending teachers schedule: ", e);
                }
            }

            Collection<String> updatedStudents = checkStudents();

            if(!updatedStudents.isEmpty()){
                try{
                    sendStudents(updated, updatedStudents);
                } catch (Exception e){
                    Logger.error("Sending student schedule interrupted by exception: ", e);
                }
            }

            if(checkConsultations()){
                try{
                    sendConsultations();
                } catch (Exception e){
                    Logger.error("Error while updating and sending consultations schedule: ", e);
                }
            }

            updating = false;
        }

        private void sendTeachers(){
            CompletableFuture.runAsync(()->{
                Logger.info("Start sending teachers schedule...");

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

                Logger.info("Teachers schedule sent");
            }, executor);
        }

        private void sendConsultations(){
            Logger.info("Consultations schedule updated on website. Start sending...");

            try{
                updating = true;
                scheduleManager.getConsultationSchedule().parse();
                updating = false;
            } catch (Exception e){
                Logger.error("Error while parsing consultations schedule: ", e);
            }

            CompletableFuture.runAsync(()->{
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
                        Logger.error("Cannot send consultation schedule to teacher " + teacher + ": ", e);
                    }
                }

                Logger.info("Consultations schedule sent");
            }, executor);
        }

        private void sendStudents(boolean isUpdate, Collection<String> updated){
            try{
                if(!isUpdate) {
                    Logger.info("Students schedule updated on website. Downloading...");
                    updating = true;
                    scheduleManager.updateStudentsSchedule();
                    updating = false;
                }
            } catch (Exception e){
                Logger.error("Error while updating students schedule: ", e);
            }

            CompletableFuture.runAsync(()->{
                Logger.info("Sending updated students schedule...");

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

                Logger.info("Students schedule sent");
            }, executor);
        }

        private boolean checkConsultations(){
            try{
                URL consultations = scheduleManager.getConsultationSchedule().getLink();
                String lastHash = properties.getString("hash.consultations");
                String hash = HashUtil.getSHA1(consultations);

                if(hash == null){
                    Logger.error("Hash of consultations schedule is null. Maybe website is temporarily unavailable");
                    return false;
                }

                if(lastHash.equals(hash)){
                    return false;
                }

                properties.set("hash.consultations", hash);
                properties.save();

                return true;
            } catch (Exception e){
                Logger.error("Something wrong while get hash of consultations schedule: ", e);
            }

            return false;
        }

        private boolean checkTeachers(){
            try{
                URL teachers = scheduleManager.getTeacherSchedule().getLink();
                String lastHash = properties.getString("hash.teachers");
                String hash = HashUtil.getSHA1(teachers);

                if(hash != null && !lastHash.equals(hash)){
                    properties.set("hash.teachers", hash);
                    properties.save();
                    return true;
                }
            } catch(Exception e){
                Logger.error("Error while get hash of teachers schedule:", e);
            }

            return false;
        }

        private Collection<String> checkStudents(){
            try{
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
            } catch (Exception e){
                Logger.error("Something wrong while checking for updates of students schedule: ", e);
            }

            return Collections.emptyList();
        }

    }

}
