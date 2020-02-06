package ru.nanit.vasyascheduler.data.user;

import ru.nanit.vasyascheduler.data.Person;

public class SubscriberTeacher extends BotUser {

    private Person teacher;

    public SubscriberTeacher(Person teacher){
        this.teacher = teacher;
    }

    public SubscriberTeacher(String firstName, String lastName, String patronymic){
        this(new Person(firstName, lastName, patronymic));
    }

    public Person getTeacher(){
        return teacher;
    }
}
