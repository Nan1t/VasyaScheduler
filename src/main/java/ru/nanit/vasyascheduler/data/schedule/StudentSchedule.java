package ru.nanit.vasyascheduler.data.schedule;

import com.aspose.cells.*;
import com.google.common.reflect.TypeToken;
import ninja.leaping.configurate.ConfigurationNode;
import ninja.leaping.configurate.objectmapping.serialize.TypeSerializer;
import ru.nanit.vasyascheduler.api.util.Logger;
import ru.nanit.vasyascheduler.api.util.Patterns;
import ru.nanit.vasyascheduler.data.Person;
import ru.nanit.vasyascheduler.data.datetime.Days;

import java.io.InputStream;
import java.net.URL;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Matcher;

public class StudentSchedule extends Schedule {

    private final String name;
    private String displayName;
    private ParseData parseData;
    private Map<Integer, Day> days;
    private Map<String, Map<Integer, Day>> auds;

    private StudentSchedule(String name){
        this.name = name;
    }

    private StudentSchedule(String name, URL link, int sheet){
        super(link, sheet);
        this.name = name;
    }

    public String getName(){
        return name;
    }

    public String getDisplayName(){
        return displayName;
    }

    public void setDisplayName(String name){
        this.displayName = name;
    }

    public Day getDay(int num){
        return days.getOrDefault(num, null);
    }

    public Map<Integer, Day> getAudDays(String aud){
        return auds.get(aud);
    }

    public Collection<String> getAuds(){
        return auds.keySet();
    }

    @Override
    public void parse() throws Exception {
        if(days != null){
            days.clear();
        }

        if(auds != null){
            auds.clear();
        }

        days = new ConcurrentHashMap<>();
        auds = new ConcurrentHashMap<>();

        InputStream stream = getLink().openStream();

        Workbook workbook = new Workbook(stream);
        Worksheet worksheet = workbook.getWorksheets().get(getSheet());

        Cells cells = worksheet.getCells();
        Cell dayCell = cells.get(parseData.getDayPos().getRow(), parseData.getDayPos().getColumn());

        Range dayCellRange = dayCell.getMergedRange();
        int maxDataCol = cells.getMaxDataColumn();
        int classTimeCellCol = parseData.getClassNumPos().getColumn() + 1;

        // Iterate every day in table
        while (dayCell.getType() != CELL_TYPE_NULL) {
            String dayName = dayCell.getStringValue().split("\n")[0].trim();
            int dayNum = Days.getDayNumber(dayName);

            if (dayNum == -1) {
                Logger.error("Error. Day " + dayName + " is not defined in configuration. Check schedule.conf to fix it");
                dayCell = getNextDayCell(cells, dayCell, dayCellRange);
                dayCellRange = dayCell.getMergedRange();
                continue;
            }

            Day day = new Day();
            Cell classNumCell = cells.get(dayCellRange.getFirstRow(), parseData.getClassNumPos().getColumn());
            Cell classTimeCell = cells.get(dayCellRange.getFirstRow(), classTimeCellCol);
            int rowSum = 0;

            // Iterate every classes row in day
            while (classNumCell.getRow() < (dayCell.getRow() + dayCellRange.getRowCount())){
                int classCol = parseData.getGroupPos().getColumn();
                int classNum = classNumCell.getIntValue();
                Cell classCell = cells.get(classNumCell.getRow(), classCol);

                day.setClassTime(classNum, classTimeCell.getStringValue());

                // Iterate every class in classes row
                while (classCol <= maxDataCol){
                    Cell classType = cells.get(classCell.getRow()+1, classCell.getColumn());
                    Cell teacher = cells.get(classCell.getRow()+2, classCell.getColumn());
                    Cell audience = cells.get(classCell.getRow()+3, classCell.getColumn());

                    if(classCell.getType() == CELL_TYPE_NULL && audience.getType() == CELL_TYPE_NULL){
                        classCol += getColRange(classCell);
                        classCell = cells.get(classNumCell.getRow(), classCol);
                        continue;
                    }

                    parseGroups(day, classNum, cells, classCell, classType, teacher, audience);
                    classCol += getColRange(classCell);
                    classCell = cells.get(classNumCell.getRow(), classCol);
                }

                rowSum += classNumCell.getMergedRange().getRowCount();
                classNumCell = cells.get(dayCellRange.getFirstRow() + rowSum, parseData.getClassNumPos().getColumn());
                classTimeCell = cells.get(dayCellRange.getFirstRow() + rowSum, classTimeCellCol);

                if(classNumCell.getType() == CELL_TYPE_NULL){
                    break;
                }
            }

            days.put(dayNum, day);
            dayCell = getNextDayCell(cells, dayCell, dayCellRange);
            dayCellRange = dayCell.getMergedRange();
        }

        stream.close();
        parseAuds();

        CompletableFuture.runAsync(this::parseImage);
    }

    private void parseGroups(Day day, int classNum, Cells cells, Cell classCell, Cell classType, Cell teacher, Cell audience){
        List<Class> classes = splitInlineClasses(teacher, classCell.getStringValue(), classType.getStringValue());
        List<String> groups = getClassGroups(cells, classCell);

        if(!classes.isEmpty()){
            // Parse classes in inline format (3 or 3,4 lines with several teachers)
            for (Class c : classes){
                c.addGroup(groups);
                day.addClass(classNum, c);
            }

            if(audience.getType() != CELL_TYPE_NULL){
                classes = splitInlineClasses(audience, classCell.getStringValue(), classType.getStringValue());

                if(!classes.isEmpty()){
                    for (Class c : classes){
                        c.addGroup(groups);
                        day.addClass(classNum, c);
                    }
                }
            }
        } else{
            // Parse class in default format (3 line - one teacher, 4 line - audience)
            Matcher matcher = Patterns.TEACHER_DEFAULT.matcher(teacher.getStringValue());

            if(matcher.find()){
                Class c = new Class(classCell.getStringValue(),
                        classType.getStringValue(),
                        audience.getStringValue(),
                        Person.parseFromString(matcher.group(0)));

                c.addGroup(groups);
                day.addClass(classNum, c);
            }
        }
    }

    private void parseAuds(){
        for(Map.Entry<Integer, Day> entry : days.entrySet()){
            for (Map.Entry<Integer, Map<Person, Class>> personEntry : entry.getValue().getClasses().entrySet()){
                for (Map.Entry<Person, Class> classEntry : personEntry.getValue().entrySet()){
                    String aud = classEntry.getValue().getAudience();
                    Map<Integer, Day> days = auds.computeIfAbsent(aud, v -> new ConcurrentHashMap<>());
                    Day day = days.computeIfAbsent(entry.getKey(), v -> {
                        Day newDay = new Day();
                        newDay.classTime = entry.getValue().classTime;
                        return newDay;
                    });

                    day.addAudClass(personEntry.getKey(), classEntry.getValue());
                }
            }
        }
    }

    private List<String> getClassGroups(Cells cells, Cell classCell){
        List<String> list = new ArrayList<>();
        int groupsCount = getColRange(classCell);

        for (int i = 0; i < groupsCount; i++){
            Cell groupCell = cells.get(parseData.getGroupPos().getRow(), classCell.getColumn() + i);
            list.add(groupCell.getStringValue());
        }

        return list;
    }

    private List<Class> splitInlineClasses(Cell cell, String className, String classType){
        List<Class> list = new ArrayList<>();
        Matcher matcher = Patterns.TEACHER_INLINE.matcher(cell.getStringValue());

        if(matcher.find()){
            matcher.reset();

            while(matcher.find()){
                Person teacher = Person.parseFromString(matcher.group(1));
                String aud = matcher.group(2);
                list.add(new Class(className, classType, aud, teacher));
            }
        }

        return list;
    }

    private int getColRange(Cell cell){
        Range range = cell.getMergedRange();
        if(range != null){
            return range.getColumnCount();
        }
        return 1;
    }

    private Cell getNextDayCell(Cells cells, Cell curr, Range dayCellRange){
        int dayRowHeight = dayCellRange.getRowCount();
        return cells.get(curr.getRow() + dayRowHeight, parseData.getDayPos().getColumn());
    }

    public class Day {

        private Map<Integer, String> classTime = new HashMap<>();
        private Map<Integer, Map<Person, Class>> classes = new HashMap<>(); // <ClassNum, <Teacher, ClassObj>>
        private Map<Integer, Set<Class>> audClass = new HashMap<>();

        public Class getClazz(int num, Person teacher){
            Map<Person, Class> map = classes.get(num);
            if(map != null){
                return map.get(teacher);
            }
            return null;
        }

        public Map<Integer, Map<Person, Class>> getClasses(){
            return classes;
        }

        public String getClassTime(int num){
            return classTime.get(num);
        }

        public void setClassTime(int num, String time){
            classTime.put(num, time);
        }

        public Map<Integer, Set<Class>> getAudClases(){
            return audClass;
        }

        public void addAudClass(int num, Class clazz){
            Set<Class> classes = audClass.computeIfAbsent(num, k -> new HashSet<>());
            classes.add(clazz);
            audClass.put(num, classes);
        }

        public void addClass(int num, Class clazz){
            Map<Person, Class> map = classes.get(num);
            if(map != null){
                Class c = map.get(clazz.getTeacher());
                if(c != null){
                    c.addGroup(clazz.getGroups());
                    return;
                }

                map.put(clazz.getTeacher(), clazz);
                return;
            }

            map = new HashMap<>();
            map.put(clazz.getTeacher(), clazz);
            classes.put(num, map);
        }
    }

    public class Class {

        private String name, type, audience;
        private Person teacher;
        private List<String> groups = new ArrayList<>();

        public Class(String name, String type, String audience, Person teacher){
            this.name = name;
            this.type = type;
            this.audience = audience;
            this.teacher = teacher;
        }

        public List<String> getGroups(){
            return groups;
        }

        public void setGroups(List<String> groups){
            this.groups = groups;
        }

        public void addGroup(String group){
            groups.add(group);
        }

        public void addGroup(Collection<String> group){
            groups.addAll(group);
        }

        public String getName() {
            return name;
        }

        public String getType() {
            return type;
        }

        public Person getTeacher() {
            return teacher;
        }

        public String getAudience() {
            return audience;
        }

        public void print(){
            System.out.println("    Name: " + getName());
            System.out.println("    Type: " + getType());
            System.out.println("    Teacher: " + getTeacher());
            System.out.println("    Aud: " + getAudience());
            System.out.println("    Groups: " + getGroups());
            System.out.println();
        }
    }

    private static class ParseData {

        private SheetPoint dayPos;
        private SheetPoint classNumPos;
        private SheetPoint groupPos;

        public SheetPoint getDayPos() {
            return dayPos;
        }

        public SheetPoint getClassNumPos() {
            return classNumPos;
        }

        public SheetPoint getGroupPos() {
            return groupPos;
        }
    }

    public static class Serializer implements TypeSerializer<StudentSchedule>{

        @Override
        public StudentSchedule deserialize(TypeToken<?> type, ConfigurationNode node) {
            StudentSchedule schedule = new StudentSchedule(node.getNode("id").getString());
            schedule.setLink(node.getNode("link").getString());
            schedule.setSheet(node.getNode("sheet").getInt());
            schedule.setDisplayName(node.getNode("name").getString());

            ParseData parseData = new ParseData();
            parseData.dayPos = new SheetPoint(node.getNode("parseData", "day", "col").getInt(),
                    node.getNode("parseData", "day", "row").getInt());
            parseData.classNumPos = new SheetPoint(node.getNode("parseData", "classNum", "col").getInt(),
                    node.getNode("parseData", "classNum", "row").getInt());
            parseData.groupPos = new SheetPoint(node.getNode("parseData", "groups", "col").getInt(),
                    node.getNode("parseData", "groups", "row").getInt());

            schedule.parseData = parseData;
            return schedule;
        }

        @Override
        public void serialize(TypeToken<?> type, StudentSchedule obj, ConfigurationNode node) {

        }
    }
}
