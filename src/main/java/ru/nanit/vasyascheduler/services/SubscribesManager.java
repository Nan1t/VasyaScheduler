package ru.nanit.vasyascheduler.services;

import ru.nanit.vasyascheduler.api.storage.Tables;
import ru.nanit.vasyascheduler.api.storage.database.Database;
import ru.nanit.vasyascheduler.api.storage.database.Row;
import ru.nanit.vasyascheduler.bot.Bot;
import ru.nanit.vasyascheduler.data.Person;
import ru.nanit.vasyascheduler.data.schedule.Schedule;
import ru.nanit.vasyascheduler.data.schedule.StudentSchedule;
import ru.nanit.vasyascheduler.data.user.BotUser;
import ru.nanit.vasyascheduler.data.user.SubscriberPoints;
import ru.nanit.vasyascheduler.data.user.SubscriberStudent;
import ru.nanit.vasyascheduler.data.user.SubscriberTeacher;

import java.util.Collection;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.CompletableFuture;

public final class SubscribesManager {

    private Database database;
    private ScheduleManager scheduleManager;

    private Map<String, SubscriberTeacher> teachers = new TreeMap<>();
    private Map<String, SubscriberStudent> students = new TreeMap<>();
    private Map<String, SubscriberPoints> points = new TreeMap<>();

    public SubscribesManager(Database database, ScheduleManager scheduleManager){
        this.database = database;
        this.scheduleManager = scheduleManager;
    }

    public String generateKey(Bot.Type type, String id){
        return type.getId() + "::" + id;
    }

    public String generateKey(BotUser user){
        return user.getBotType().getId() + "::" + user.getMessengerId();
    }

    public SubscriberTeacher getTeacherSubscriber(Bot.Type type, String id){
        return teachers.get(generateKey(type, id));
    }

    public SubscriberStudent getStudentSubscriber(Bot.Type type, String id){
        return students.get(generateKey(type, id));
    }

    public SubscriberPoints getPointsSubscriber(Bot.Type type, String id){
        return points.get(generateKey(type, id));
    }

    public Collection<SubscriberTeacher> getTeacherSubscribers(){
        return teachers.values();
    }

    public Collection<SubscriberStudent> getStudentSubscribers(){
        return students.values();
    }

    public Collection<SubscriberPoints> getPointSubscribers(){
        return points.values();
    }

    public void addTeacherSubscriber(SubscriberTeacher subscriber){
        teachers.put(generateKey(subscriber), subscriber);
        CompletableFuture.runAsync(()->{
            Row row = new Row();
            row.addField("messenger_id", subscriber.getMessengerId());
            row.addField("messenger_type", subscriber.getBotType().getId());

            boolean exist = database.existsRow(Tables.SUBSCRIBES_TEACHERS, row);

            row.addField("first_name", subscriber.getTeacher().getFirstName());
            row.addField("last_name", subscriber.getTeacher().getLastName());
            row.addField("patronymic", subscriber.getTeacher().getPatronymic());

            if(exist){
                updateRow(Tables.SUBSCRIBES_TEACHERS, row, subscriber);
            } else {
                database.createRow(Tables.SUBSCRIBES_TEACHERS, row);
            }
        });
    }

    public void addStudentSubscriber(SubscriberStudent subscriber){
        students.put(generateKey(subscriber), subscriber);
        CompletableFuture.runAsync(()->{
            Row row = new Row();
            row.addField("messenger_id", subscriber.getMessengerId());
            row.addField("messenger_type", subscriber.getBotType().getId());

            boolean exist = database.existsRow(Tables.SUBSCRIBES_STUDENTS, row);

            row.addField("schedule_name", subscriber.getSchedule());

            if(exist){
                updateRow(Tables.SUBSCRIBES_STUDENTS, row, subscriber);
            } else {
                database.createRow(Tables.SUBSCRIBES_STUDENTS, row);
            }
        });
    }

    public void addPointsSubscriber(SubscriberPoints subscriber){
        points.put(generateKey(subscriber), subscriber);
        CompletableFuture.runAsync(()->{
            Row row = new Row();
            row.addField("messenger_id", subscriber.getMessengerId());
            row.addField("messenger_type", subscriber.getBotType().getId());

            boolean exist = database.existsRow(Tables.SUBSCRIBES_POINTS, row);

            row.addField("first_name", subscriber.getStudent().getFirstName());
            row.addField("last_name", subscriber.getStudent().getLastName());
            row.addField("patronymic", subscriber.getStudent().getPatronymic());
            row.addField("password", subscriber.getPassword());

            if(exist){
                updateRow(Tables.SUBSCRIBES_POINTS, row, subscriber);
            } else {
                database.createRow(Tables.SUBSCRIBES_POINTS, row);
            }
        });
    }

    private void updateRow(String table, Row row, BotUser user){
        Row params = new Row();
        params.addField("messenger_id", user.getMessengerId());
        params.addField("messenger_type", user.getBotType().getId());
        database.updateRow(table, row, params);
    }

    public void removeTeacherSubscriber(SubscriberTeacher subscriber){
        teachers.remove(generateKey(subscriber));
        removeDBUser(Tables.SUBSCRIBES_TEACHERS, subscriber);
    }

    public void removeStudentSubscriber(SubscriberStudent subscriber){
        students.remove(generateKey(subscriber));
        removeDBUser(Tables.SUBSCRIBES_STUDENTS, subscriber);
    }

    public void removePointsSubscriber(SubscriberPoints subscriber){
        points.remove(generateKey(subscriber));
        removeDBUser(Tables.SUBSCRIBES_POINTS, subscriber);
    }

    private void removeDBUser(String table, BotUser user){
        CompletableFuture.runAsync(()->{
            Row row = new Row();
            row.addField("messenger_id", user.getMessengerId());
            row.addField("messenger_type", user.getBotType().getId());
            database.deleteRow(table, row);
        });
    }

    public void loadAll(){
        loadTeachers();
        loadStudents();
        loadPoints();
    }

    private void loadTeachers(){
        Row[] rows = database.getRows("SELECT * FROM " + Tables.SUBSCRIBES_TEACHERS);

        for (Row row : rows){
            Bot.Type botType = Bot.Type.fromId(row.getField("messenger_type"));
            String id = row.getField("messenger_id");
            String firstName = row.getField("first_name");
            String lastName = row.getField("last_name");
            String patronymic = row.getField("patronymic");
            SubscriberTeacher subscriber = new SubscriberTeacher(new Person(firstName, lastName, patronymic));

            subscriber.setBotType(botType);
            subscriber.setMessengerId(id);

            teachers.put(generateKey(subscriber), subscriber);
        }
    }

    private void loadStudents(){
        Row[] rows = database.getRows("SELECT * FROM " + Tables.SUBSCRIBES_STUDENTS);

        for (Row row : rows){
            StudentSchedule schedule = scheduleManager.getStudentSchedule(row.getField("schedule_name"));

            if(schedule != null){
                Bot.Type botType = Bot.Type.fromId(row.getField("messenger_type"));
                String id = row.getField("messenger_id");
                SubscriberStudent subscriber = new SubscriberStudent(schedule.getName());

                subscriber.setBotType(botType);
                subscriber.setMessengerId(id);

                students.put(generateKey(subscriber), subscriber);
            }
        }
    }

    private void loadPoints(){
        Row[] rows = database.getRows("SELECT * FROM " + Tables.SUBSCRIBES_POINTS);

        for (Row row : rows){
            Bot.Type botType = Bot.Type.fromId(row.getField("messenger_type"));
            String id = row.getField("messenger_id");
            String firstName = row.getField("first_name");
            String lastName = row.getField("last_name");
            String patronymic = row.getField("patronymic");
            String password = row.getField("password");
            SubscriberPoints subscriber = new SubscriberPoints(new Person(firstName, lastName, patronymic), password);

            subscriber.setBotType(botType);
            subscriber.setMessengerId(id);

            points.put(generateKey(subscriber), subscriber);
        }
    }
}
