package ru.nanit.vasyascheduler.api.util;

import java.util.regex.Pattern;

public final class Patterns {

    /**
     * Pattern returns 1 group - string in format '%LastName% %FirstName%.%Patronymic%.'
     */
    public static Pattern TEACHER_DEFAULT = Pattern.compile("[А-ЯЁЇІЄҐ][а-яёїієґ']{1,32}\\s*[А-ЯЁЇІЄҐ]\\.[А-ЯЁЇІЄҐ]\\.*");

    /**
     * Pattern like TEACHER_DEFAULT, but returns 3 groups instead of 1: [1] - last name, [2] - first name, [3] - patronymic
     */
    public static Pattern TEACHER_DEFAULT_SEPARATE = Pattern.compile("([А-ЯЁЇІЄҐ][а-яёїієґ']{1,32})\\s*([А-ЯЁЇІЄҐ])\\.([А-ЯЁЇІЄҐ])\\.*");

    /**
     * Pattern returns 2 groups: [1] - TEACHER_DEFAULT name format, [2] - class audience
     */
    public static Pattern TEACHER_INLINE = Pattern.compile("([А-ЯЁЇІЄҐ][а-яёїієґ']{1,32}\\s*[А-ЯЁЇІЄҐ]\\.[А-ЯЁЇІЄҐ]\\.*)\\s*(ауд\\..{1,3})");

    public static void setTeacherDefault(String expression){
        TEACHER_DEFAULT = Pattern.compile(expression);
    }

    public static void setTeacherDefaultSeparate(String expression){
        TEACHER_DEFAULT_SEPARATE = Pattern.compile(expression);
    }

    public static void setTeacherInline(String expression){
        TEACHER_INLINE = Pattern.compile(expression);
    }
}
