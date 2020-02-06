package ru.nanit.vasyascheduler.data;

import ru.nanit.vasyascheduler.api.util.Patterns;

import java.util.Objects;
import java.util.regex.Matcher;

/**
 * Represents person full name as unique object able to be compared
 */
public class Person {

    private String firstName, lastName, patronymic;

    public Person(String firstName, String lastName, String patronymic){
        this.firstName = firstName;
        this.lastName = lastName;
        this.patronymic = patronymic;
    }

    public String getFirstName(){
        return firstName.toUpperCase();
    }

    public String getLastName() {
        return lastName;
    }

    public String getPatronymic() {
        return patronymic.toUpperCase();
    }

    public char getFirstNameLetter(){
        return Character.toUpperCase(firstName.charAt(0));
    }

    public char getLastNameLetter(){
        return Character.toUpperCase(lastName.charAt(0));
    }

    public char getPatronymicLetter(){
        return Character.toUpperCase(patronymic.charAt(0));
    }

    /**
     * Parse new person object from string in specific format
     * @param str string that must contains person name in format %last_name% %first_name%.%patronymic%.
     * @return Parsed Person object
     */
    public static Person parseFromString(String str){
        Matcher matcher = Patterns.TEACHER_DEFAULT_SEPARATE.matcher(str);
        return (matcher.find()) ? new Person(matcher.group(2), matcher.group(1), matcher.group(3)) : null;
    }

    @Override
    public String toString(){
        return String.format("%s %s.%s.", lastName, getFirstNameLetter(), getPatronymicLetter());
    }

    @Override
    public boolean equals(Object teacher){
        if(teacher instanceof Person){
            Person t = (Person) teacher;
            return t.getFirstName().equalsIgnoreCase(firstName)
                    && t.getLastName().equalsIgnoreCase(lastName)
                    && t.getPatronymic().equalsIgnoreCase(patronymic);
        }

        return false;
    }

    @Override
    public int hashCode(){
        return Objects.hash(firstName, lastName, patronymic);
    }
}
