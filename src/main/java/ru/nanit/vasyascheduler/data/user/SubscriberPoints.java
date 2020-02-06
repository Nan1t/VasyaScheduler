package ru.nanit.vasyascheduler.data.user;

import ru.nanit.vasyascheduler.data.Person;

public class SubscriberPoints extends BotUser {

    private Person student;
    private String password;

    public SubscriberPoints(Person student, String password){
        this.student = student;
        this.password = password;
    }

    public SubscriberPoints(String firstName, String lastName, String patronymic, String password){
        this(new Person(firstName, lastName, patronymic), password);
    }

    public Person getStudent(){
        return student;
    }

    public String getPassword(){
        return password;
    }
}
