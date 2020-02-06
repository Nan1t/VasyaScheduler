package ru.nanit.vasyascheduler.data.schedule;

import com.aspose.cells.*;
import com.google.common.reflect.TypeToken;
import ninja.leaping.configurate.ConfigurationNode;
import ninja.leaping.configurate.objectmapping.ObjectMappingException;
import ninja.leaping.configurate.objectmapping.serialize.TypeSerializer;
import ru.nanit.vasyascheduler.api.storage.Language;
import ru.nanit.vasyascheduler.api.util.Logger;
import ru.nanit.vasyascheduler.api.util.TimeUtil;
import ru.nanit.vasyascheduler.data.Person;
import ru.nanit.vasyascheduler.data.datetime.Days;
import ru.nanit.vasyascheduler.data.datetime.Timetable;
import ru.nanit.vasyascheduler.services.conversion.XlsToImage;

import java.awt.image.BufferedImage;
import java.io.InputStream;
import java.net.URL;
import java.util.*;

public class TeacherSchedule extends Schedule {

    private ParseData parseData;
    private Builder builder;
    private Map<String, String> matches;

    private String dateTime;
    private Map<Person, Week> weeks; // Work week for every teacher <Teacher name, Work week>

    private TeacherSchedule(){
        this(null, 0);
    }

    private TeacherSchedule(URL link, int sheet){
        super(link, sheet);
        this.builder = new Builder();
    }

    /**
     * @param teacher Teacher name in format "LastName" "FN"."PTR".
     *                For example: Petrovich S.V.
     * @return Teacher week object that contains all references to students schedule by a class number
     */
    public Week getWeek(Person teacher){
        return weeks.get(teacher);
    }

    public Map<Person, Week> getWeeks(){
        return weeks;
    }

    public Builder getBuilder(){
        return builder;
    }

    public boolean hasTeacher(Person teacher){
        return getWeek(teacher) != null;
    }

    @Override
    public void parse() throws Exception {
        if(weeks != null){
            weeks.clear();
        }

        weeks = new HashMap<>();
        int classesCount = Timetable.getClassCount();

        try (InputStream stream = getLink().openStream()) {
            Workbook workbook = new Workbook(stream);
            Worksheet worksheet = workbook.getWorksheets().get(getSheet());
            Cells cells = worksheet.getCells();

            int col = parseData.getSubjectPos().getColumn();
            int row = parseData.getSubjectPos().getRow();

            dateTime = cells.get(0, 0).getStringValue();

            while (cells.get(row, col).getType() != CELL_TYPE_NULL) {
                int classNum = 1;

                String teacher = cells.get(row, col).getDisplayStringValue();
                Week week = new Week();
                Day day = new Day();

                for (int i = 0; i < classesCount * Days.getDaysCount() + 1; i++) {
                    if (i != 0 && i % classesCount == 0) {
                        classNum = 1;
                        week.addDay(day);
                        day = new Day();
                    }

                    String classes = cells.get(row, col + i + 1).getDisplayStringValue();

                    if (classes != null) {
                        String[] arr = classes.split(",");

                        for (String str : arr) {
                            String ref = matches.get(str);
                            if (ref == null && str.length() > 0) {
                                ref = str;
                            }
                            day.addCourse(classNum, ref);
                        }
                    }

                    classNum++;
                }

                weeks.put(Person.parseFromString(teacher), week);
                row++;
            }
        }
    }

    public final class Week {

        private List<Day> days = new LinkedList<>();

        public List<Day> getDays(){
            return days;
        }

        public void addDay(Day day){
            this.days.add(day);
        }
    }

    public final class Day {

        private Map<Integer, List<String>> courses = new HashMap<>(); // Contains references to some student schedule

        public List<String> getCourses(int classNum){
            return courses.get(classNum);
        }

        public void addCourse(int classNum, String scheduleName){
            if(scheduleName != null){
                courses.putIfAbsent(classNum, new ArrayList<>());
                courses.get(classNum).add(scheduleName);
            }
        }
    }

    /**
     * Builder can assembly an unique schedule for each teacher
     */
    public final class Builder {

        private Builder(){}

        public BufferedImage build(Language lang, Map<String, StudentSchedule> students, Person teacher){
            TeacherSchedule.Week week = getWeek(teacher);

            if(week != null){
                Workbook workbook = new Workbook();
                Worksheet sheet = workbook.getWorksheets().get(0);
                Cells cells = sheet.getCells();

                buildHeader(cells, lang, teacher);
                setHeaderBorders(cells);
                buildClasses(cells, teacher, week, students);

                try{
                    sheet.autoFitRows();
                } catch (Exception e){
                    Logger.error("Error while build workbook: ", e);
                }

                return new XlsToImage(workbook).format(ImageFormat.getPng()).generate();
            }

            Logger.warn("Attempt to get schedule of unregistered teacher " + teacher + ". Attempt denied");
            return null;
        }

        private void setHeaderBorders(Cells cells){
            for (int col = 0; col <= cells.getMaxDataColumn(); col++){
                Cell cell = cells.get(1, col);
                Style style = cell.getStyle();
                style.setBorder(BorderType.LEFT_BORDER, CellBorderType.MEDIUM, Color.getBlack());
                style.setBorder(BorderType.TOP_BORDER, CellBorderType.MEDIUM, Color.getBlack());
                style.setBorder(BorderType.RIGHT_BORDER, CellBorderType.MEDIUM, Color.getBlack());
                style.setBorder(BorderType.BOTTOM_BORDER, CellBorderType.MEDIUM, Color.getBlack());
                cell.setStyle(style);
            }
        }

        private void buildHeader(Cells cells, Language lang, Person teacher){
            Cell dateCell = cells.get(0, 0);
            Cell teacherCell = cells.get(1, 0);
            Cell day = cells.get(2, 0);
            Cell classNum = cells.get(2, 1);
            Cell time = cells.get(2, 2);
            Cell classes = cells.get(2, 3);

            // Setup content
            dateCell.setValue(dateTime);
            teacherCell.setValue(teacher.toString());
            day.setValue(lang.of("schedule.built.header.day"));
            classNum.setValue(lang.of("schedule.built.header.class_num"));
            time.setValue(lang.of("schedule.built.header.time"));
            classes.setValue(lang.of("schedule.built.header.classes"));

            // Setup styles
            Style style = new Style();
            style.getFont().setSize(18);
            style.getFont().setBold(true);
            style.setHorizontalAlignment(TextAlignmentType.CENTER);
            style.setVerticalAlignment(TextAlignmentType.CENTER);

            dateCell.setStyle(style);
            teacherCell.setStyle(style);
            day.setStyle(style);
            classNum.setStyle(style);
            time.setStyle(style);
            classes.setStyle(style);

            // Setup sizes
            cells.setColumnWidthInch(0, 1.9d);
            cells.setColumnWidthInch(1, 0.72d);
            cells.setColumnWidthInch(2, 1.07d);
            cells.setColumnWidthInch(3, 7.87d);

            cells.setRowHeightInch(0, 0.38d);
            cells.setRowHeightInch(1, 0.38d);

            cells.merge(0, 0, 1, 4);
            cells.merge(1, 0, 1, 4);

            borderRow(cells, 2);
        }

        private void buildClasses(Cells cells, Person teacher, TeacherSchedule.Week week, Map<String, StudentSchedule> students){
            int day = 1;
            int dayRow = 3;
            int classRow = 3;
            boolean addRow;

            // Iterate every day
            for (TeacherSchedule.Day teacherDay : week.getDays()){
                Cell dayCell = cells.get(dayRow, 0);
                int rows = 0; // Count of rows with class data

                // Iterate every class number in day
                for (int classNum = 1; classNum <= Timetable.getClassCount(); classNum++){
                    List<String> courses = teacherDay.getCourses(classNum);

                    if(courses != null && !courses.isEmpty()){
                        Cell classNumCell = cells.get(classRow, 1);
                        Cell timeCell = cells.get(classRow, 2);

                        classNumCell.setValue(classNum);
                        timeCell.setValue(TimeUtil.timeFromMillis(Timetable.getInterval(classNum).getBegin()));

                        setDefStyle(classNumCell);
                        setDefStyle(timeCell);

                        cells.merge(classRow, 1, 4, 1); // Merge class num cell
                        cells.merge(classRow, 2, 4, 1); // Merge time cell

                        classRow += 4;
                        addRow = false;

                        Cell nameCell = cells.get(classNumCell.getRow(), 3);
                        Cell typeCell = cells.get(classNumCell.getRow()+1, 3);
                        Cell groupsCell = cells.get(classNumCell.getRow()+2, 3);
                        Cell audCell = cells.get(classNumCell.getRow()+3, 3);

                        setClassStyle(nameCell, true);
                        setClassStyle(typeCell);
                        setClassStyle(groupsCell);
                        setClassStyle(audCell);

                        cells.setRowHeightInch(classNumCell.getRow(), 0.26d);
                        cells.setRowHeightInch(classNumCell.getRow()+1, 0.26d);
                        cells.setRowHeightInch(classNumCell.getRow()+2, 0.26d);
                        cells.setRowHeightInch(classNumCell.getRow()+3, 0.26d);

                        for (String course : courses){
                            StudentSchedule schedule = students.get(course);
                            if(schedule != null){
                                StudentSchedule.Day studentDay = schedule.getDay(day);
                                if(studentDay != null){
                                    StudentSchedule.Class clazz = studentDay.getClazz(classNum, teacher);
                                    if(clazz != null){
                                        if(nameCell.getType() == 3 && audCell.getType() == 3){
                                            nameCell.setValue(clazz.getName());
                                            typeCell.setValue(clazz.getType());
                                            groupsCell.setValue(clazz.getGroups().toString());
                                            audCell.setValue(clazz.getAudience());

                                            addRow = true;
                                        } else {
                                            groupsCell.setValue(groupsCell.getStringValue() + ", " + clazz.getGroups().toString());
                                        }
                                    }
                                }
                            } else {
                                nameCell.setValue(course);
                                /*typeCell.setValue(course);
                                groupsCell.setValue(course);
                                audCell.setValue(course);*/
                                cells.merge(nameCell.getRow(), nameCell.getColumn(), 4, 1);
                                addRow = true;
                            }
                        }

                        if(addRow){
                            Style style = audCell.getStyle();
                            style.setBorder(BorderType.BOTTOM_BORDER, CellBorderType.THIN, Color.getBlack());
                            audCell.setStyle(style);
                            rows++;
                        }
                    }
                }

                if(rows > 0){
                    cells.merge(dayRow, 0, rows * 4, 1); // Merge day cell
                    dayRow += rows * 4;
                    dayCell.setValue(Days.getDayName(day));
                    borderRow(cells, dayRow-1);
                    setDefStyle(dayCell, true);
                }

                day++;
            }

            borderCol(cells, 0);
            borderCol(cells, 1);
            borderCol(cells, 2);
            borderCol(cells, 3);
            borderCol(cells, 4);
        }

        private void borderRow(Cells cells, int cellRow){
            borderRow(cells, cellRow, CellBorderType.MEDIUM);
        }

        private void borderRow(Cells cells, int cellRow, int type){
            int maxCol = cells.getMaxDataColumn();

            for (int i = 0; i <= maxCol; i++){
                Cell cell = cells.get(cellRow, i);
                Style style = cell.getStyle();
                style.setBorder(BorderType.BOTTOM_BORDER, type, Color.getBlack());
                cell.setStyle(style);
            }
        }

        private void borderCol(Cells cells, int cellCol){
            borderCol(cells, cellCol, CellBorderType.MEDIUM);
        }

        private void borderCol(Cells cells, int cellCol, int type){
            int maxRow = cells.getMaxDataRow();

            for (int i = 1; i <= maxRow; i++){
                Cell cell = cells.get(i, cellCol);
                Style style = cell.getStyle();
                style.setBorder(BorderType.LEFT_BORDER, type, Color.getBlack());
                cell.setStyle(style);
            }
        }

        private void setDefStyle(Cell cell){
            setDefStyle(cell, false);
        }

        private void setDefStyle(Cell cell, boolean bold){
            Style style = cell.getStyle();
            style.getFont().setSize(16);
            style.getFont().setBold(bold);
            style.setHorizontalAlignment(TextAlignmentType.CENTER);
            style.setVerticalAlignment(TextAlignmentType.CENTER);
            style.setBorder(BorderType.TOP_BORDER, CellBorderType.THIN, Color.getBlack());
            style.setBorder(BorderType.BOTTOM_BORDER, CellBorderType.THIN, Color.getBlack());
            cell.setStyle(style);
        }

        private void setClassStyle(Cell cell){
            setClassStyle(cell, false);
        }

        private void setClassStyle(Cell cell, boolean bold){
            Style style = cell.getStyle();
            style.getFont().setSize(14);
            style.getFont().setBold(bold);
            style.setTextWrapped(true);
            style.setHorizontalAlignment(TextAlignmentType.CENTER);
            style.setVerticalAlignment(TextAlignmentType.CENTER);
            cell.setStyle(style);
        }
    }

    private static final class ParseData {

        private SheetPoint subjectPos;

        public SheetPoint getSubjectPos() {
            return subjectPos;
        }
    }

    public static final class Serializer implements TypeSerializer<TeacherSchedule> {

        @Override
        public TeacherSchedule deserialize(TypeToken<?> type, ConfigurationNode node) throws ObjectMappingException {
            TeacherSchedule schedule = new TeacherSchedule();

            schedule.setLink(node.getNode("link").getString());
            schedule.setSheet(node.getNode("sheet").getInt());
            schedule.matches = getMatches(node);

            ParseData parseData = new ParseData();
            parseData.subjectPos = new SheetPoint(node.getNode("parseData", "subject", "col").getInt(),
                    node.getNode("parseData", "subject", "row").getInt());

            schedule.parseData = parseData;
            return schedule;
        }

        private Map<String, String> getMatches(ConfigurationNode node) throws ObjectMappingException {
            Map<String, String> map = new HashMap<>();
            List<? extends ConfigurationNode> nodes = node.getNode("matches").getChildrenList();

            for (ConfigurationNode n : nodes){
                String reference = n.getNode("id").getString();
                List<String> keys = n.getNode("values").getList(TypeToken.of(String.class));

                for (String key : keys){
                    map.put(key, reference);
                }
            }

            return map;
        }

        @Override
        public void serialize(TypeToken<?> type, TeacherSchedule obj, ConfigurationNode value) throws ObjectMappingException {

        }
    }
}
