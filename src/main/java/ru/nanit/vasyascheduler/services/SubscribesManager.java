package ru.nanit.vasyascheduler.services;

import ru.nanit.vasyascheduler.api.storage.Tables;
import ru.nanit.vasyascheduler.api.storage.database.Database;
import ru.nanit.vasyascheduler.api.storage.database.Row;
import ru.nanit.vasyascheduler.bot.Bot;
import ru.nanit.vasyascheduler.data.Person;
import ru.nanit.vasyascheduler.data.schedule.StudentSchedule;
import ru.nanit.vasyascheduler.data.user.BotUser;
import ru.nanit.vasyascheduler.data.user.SubscriberPoints;
import ru.nanit.vasyascheduler.data.user.SubscriberStudent;
import ru.nanit.vasyascheduler.data.user.SubscriberTeacher;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public final class SubscribesManager {

    private final Database database;
    private final ScheduleManager scheduleManager;

    public SubscribesManager(Database database, ScheduleManager scheduleManager){
        this.database = database;
        this.scheduleManager = scheduleManager;
    }

    public SubscriberTeacher getTeacherSubscriber(Bot.Type type, String id){
        Row row = getUserRow(Tables.SUBSCRIBES_TEACHERS, type, id);

        if (row != null){
            String firstName = row.getField("first_name");
            String lastName = row.getField("last_name");
            String patronymic = row.getField("patronymic");
            SubscriberTeacher subscriber = new SubscriberTeacher(new Person(firstName, lastName, patronymic));

            subscriber.setBotType(type);
            subscriber.setMessengerId(id);

            return subscriber;
        }

        return null;
    }

    public SubscriberStudent getStudentSubscriber(Bot.Type type, String id){
        Row row = getUserRow(Tables.SUBSCRIBES_STUDENTS, type, id);

        if (row != null){
            StudentSchedule schedule = scheduleManager.getStudentSchedule(row.getField("schedule_name"));

            if(schedule != null) {
                SubscriberStudent subscriber = new SubscriberStudent(schedule.getName());
                subscriber.setBotType(type);
                subscriber.setMessengerId(id);
                return subscriber;
            }
        }

        return null;
    }

    public SubscriberPoints getPointsSubscriber(Bot.Type type, String id){
        Row row = getUserRow(Tables.SUBSCRIBES_POINTS, type, id);

        if(row != null){
            String firstName = row.getField("first_name");
            String lastName = row.getField("last_name");
            String patronymic = row.getField("patronymic");
            String password = row.getField("password");
            SubscriberPoints subscriber = new SubscriberPoints(new Person(firstName, lastName, patronymic), password);
            subscriber.setBotType(type);
            subscriber.setMessengerId(id);
            return subscriber;
        }

        return null;
    }

    public Collection<SubscriberTeacher> getTeacherSubscribers(){
        Row[] rows = getUsersRows(Tables.SUBSCRIBES_TEACHERS);
        List<SubscriberTeacher> list = new LinkedList<>();

        for (Row row : rows){
            Bot.Type botType = Bot.Type.fromId(row.getField("messenger_type"));
            String id = row.getField("messenger_id");
            String firstName = row.getField("first_name");
            String lastName = row.getField("last_name");
            String patronymic = row.getField("patronymic");
            SubscriberTeacher subscriber = new SubscriberTeacher(new Person(firstName, lastName, patronymic));

            subscriber.setBotType(botType);
            subscriber.setMessengerId(id);

            list.add(subscriber);
        }

        return list;
    }

    public Collection<SubscriberStudent> getStudentSubscribers(){
        Row[] rows = getUsersRows(Tables.SUBSCRIBES_STUDENTS);
        List<SubscriberStudent> list = new LinkedList<>();

        for (Row row : rows){
            StudentSchedule schedule = scheduleManager.getStudentSchedule(row.getField("schedule_name"));

            if(schedule != null){
                Bot.Type botType = Bot.Type.fromId(row.getField("messenger_type"));
                String id = row.getField("messenger_id");
                SubscriberStudent subscriber = new SubscriberStudent(schedule.getName());

                subscriber.setBotType(botType);
                subscriber.setMessengerId(id);

                list.add(subscriber);
            }
        }

        return list;
    }

    public Collection<SubscriberPoints> getPointSubscribers(){
        Row[] rows = getUsersRows(Tables.SUBSCRIBES_POINTS);
        List<SubscriberPoints> list = new LinkedList<>();

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

            list.add(subscriber);
        }

        return list;
    }

    private Row[] getUsersRows(String table){
        return database.getRows("SELECT * FROM " + table);
    }

    private Row getUserRow(String table, Bot.Type type, String id){
        return database.getRow("SELECT * FROM "+table+" WHERE messenger_type=? AND messenger_id=?", type.getId(), id);
    }

    public void addTeacherSubscriber(SubscriberTeacher subscriber){
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
        removeDBUser(Tables.SUBSCRIBES_TEACHERS, subscriber);
    }

    public void removeStudentSubscriber(SubscriberStudent subscriber){
        removeDBUser(Tables.SUBSCRIBES_STUDENTS, subscriber);
    }

    public void removePointsSubscriber(SubscriberPoints subscriber){
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
}
