package ru.nanit.vasyascheduler.data.schedule;

import com.aspose.cells.*;
import com.google.common.reflect.TypeToken;
import ninja.leaping.configurate.ConfigurationNode;
import ninja.leaping.configurate.objectmapping.ObjectMappingException;
import ninja.leaping.configurate.objectmapping.serialize.TypeSerializer;
import ru.nanit.vasyascheduler.api.storage.Language;
import ru.nanit.vasyascheduler.api.util.Logger;
import ru.nanit.vasyascheduler.data.Person;
import ru.nanit.vasyascheduler.data.datetime.Days;
import ru.nanit.vasyascheduler.services.conversion.XlsToImage;

import java.awt.image.BufferedImage;
import java.io.InputStream;
import java.net.URL;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

public class TeacherSchedule extends Schedule {

    private ParseData parseData;
    private Builder builder;
    private Map<String, String> matches;

    private String dateTime;
    private Map<Person, Week> weeks; // Work week for every teacher <Teacher name, Work week>
    private List<Person> teachers;
    private int classesCount;

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

    public List<Person> getTeachers(){
        return teachers;
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

        weeks = new ConcurrentHashMap<>();

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

            teachers = weeks.keySet().stream().sorted().collect(Collectors.toList());
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
                buildClasses(lang, cells, teacher, week, students);

                try{
                    AutoFitterOptions options = new AutoFitterOptions();
                    options.setAutoFitMergedCells(true);
                    sheet.autoFitRows(options);
                } catch (Exception e){
                    Logger.error("Error while build workbook: ", e);
                }

                return new XlsToImage(workbook)
                        .resolution(80)
                        .generate();
            }

            Logger.warn("Attempt to get schedule of unregistered teacher " + teacher + ". Attempt denied");
            return null;
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

            setOutlineBorder(teacherCell, true);
            setOutlineBorder(day, true);
            setOutlineBorder(classNum, true);
            setOutlineBorder(time, true);
            setOutlineBorder(classes, true);
        }

        private void buildClasses(Language lang, Cells cells, Person teacher, TeacherSchedule.Week week, Map<String, StudentSchedule> students){
            int day = 1;
            int dayRow = 3;
            int classRow = 3;
            boolean addRow;

            // Iterate every day
            for (TeacherSchedule.Day teacherDay : week.getDays()){
                Cell dayCell = cells.get(dayRow, 0);
                int rows = 0; // Count of rows with class data

                // Iterate every class number in day
                for (int classNum = 1; classNum <= classesCount; classNum++){
                    List<String> courses = teacherDay.getCourses(classNum);

                    if(courses != null && !courses.isEmpty()){
                        Cell classNumCell = cells.get(classRow, 1);
                        Cell timeCell = cells.get(classRow, 2);

                        classNumCell.setValue(classNum);

                        setCentered(classNumCell);
                        setDefaultFont(classNumCell);
                        setCentered(timeCell);
                        setDefaultFont(timeCell);

                        cells.merge(classRow, 1, 4, 1); // Merge class num cell
                        cells.merge(classRow, 2, 4, 1); // Merge time cell

                        setOutlineBorder(classNumCell, true);
                        setOutlineBorder(timeCell, true);

                        classRow += 4;
                        addRow = false;

                        Cell nameCell = cells.get(classNumCell.getRow(), 3);
                        Cell typeCell = cells.get(classNumCell.getRow()+1, 3);
                        Cell groupsCell = cells.get(classNumCell.getRow()+2, 3);
                        Cell audCell = cells.get(classNumCell.getRow()+3, 3);

                        setCentered(nameCell);
                        setCentered(typeCell);
                        setCentered(groupsCell);
                        setCentered(audCell);

                        setDefaultFont(nameCell);
                        setDefaultFont(typeCell);
                        setDefaultFont(groupsCell);
                        setDefaultFont(audCell);

                        setBorder(nameCell, BorderType.RIGHT_BORDER, true);
                        setBorder(typeCell, BorderType.RIGHT_BORDER, true);
                        setBorder(groupsCell, BorderType.RIGHT_BORDER, true);
                        setBorder(audCell, BorderType.RIGHT_BORDER, true);

                        cells.setRowHeightInch(classNumCell.getRow(), 0.26d);
                        cells.setRowHeightInch(classNumCell.getRow()+1, 0.26d);
                        cells.setRowHeightInch(classNumCell.getRow()+2, 0.26d);
                        cells.setRowHeightInch(classNumCell.getRow()+3, 0.26d);

                        boolean setTime = false;

                        for (String course : courses){
                            StudentSchedule schedule = students.get(course);
                            if(schedule != null){
                                StudentSchedule.Day studentDay = schedule.getDay(day);
                                if(studentDay != null){
                                    StudentSchedule.Class clazz = studentDay.getClazz(classNum, teacher);
                                    if(!setTime){
                                        timeCell.setValue(studentDay.getClassTime(classNum));
                                        setTime = true;
                                    }
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
                                    } else {
                                        nameCell.setValue(String.format(lang.of("schedule.built.class.missing"), schedule.getDisplayName()));
                                        cells.merge(nameCell.getRow(), nameCell.getColumn(), 4, 1);
                                        addRow = true;
                                    }
                                }
                            } else {
                                nameCell.setValue(course);
                                cells.merge(nameCell.getRow(), nameCell.getColumn(), 4, 1);
                                addRow = true;
                            }
                        }

                        if(addRow){
                            setBorder(audCell, BorderType.BOTTOM_BORDER);
                            rows++;
                        }
                    }
                }

                if(rows > 0){
                    cells.merge(dayRow, 0, rows * 4, 1); // Merge day cell
                    dayRow += rows * 4;
                    dayCell.setValue(Days.getDayName(day));
                    setHeaderStyle(dayCell);
                    setOutlineBorder(dayCell, true);
                }

                day++;
            }
        }

        private void setHeaderStyle(Cell cell){
            Style style = cell.getStyle();
            style.getFont().setBold(true);
            style.getFont().setSize(18);
            style.setHorizontalAlignment(TextAlignmentType.CENTER);
            style.setVerticalAlignment(TextAlignmentType.CENTER);
            cell.setStyle(style);
        }

        private void setCentered(Cell cell){
            Style style = cell.getStyle();
            style.setHorizontalAlignment(TextAlignmentType.CENTER);
            style.setVerticalAlignment(TextAlignmentType.CENTER);
            style.setTextWrapped(true);
            cell.setStyle(style);
        }

        private void setDefaultFont(Cell cell){
            Style style = cell.getStyle();
            style.getFont().setSize(16);
            cell.setStyle(style);
        }

        private void setBorder(Cell cell, int type){
            setBorder(cell, type, false);
        }

        private void setBorder(Cell cell, int type, boolean bold){
            Style style = cell.getStyle();
            Range range = cell.getMergedRange();

            style.setBorder(type, bold ? CellBorderType.MEDIUM : CellBorderType.THIN, Color.getBlack());

            if(range != null){
                range.setStyle(style);
            } else {
                cell.setStyle(style);
            }
        }

        private void setOutlineBorder(Cell cell, boolean bold){
            Range range = cell.getMergedRange();

            if(range == null){
                Style style = cell.getStyle();
                style.setBorder(BorderType.LEFT_BORDER,
                        bold ? CellBorderType.MEDIUM : CellBorderType.THIN,
                        Color.getBlack());
                style.setBorder(BorderType.TOP_BORDER,
                        bold ? CellBorderType.MEDIUM : CellBorderType.THIN,
                        Color.getBlack());
                style.setBorder(BorderType.RIGHT_BORDER,
                        bold ? CellBorderType.MEDIUM : CellBorderType.THIN,
                        Color.getBlack());
                style.setBorder(BorderType.BOTTOM_BORDER,
                        bold ? CellBorderType.MEDIUM : CellBorderType.THIN,
                        Color.getBlack());
                cell.setStyle(style);
                return;
            }

            range.setOutlineBorder(BorderType.LEFT_BORDER,
                    bold ? CellBorderType.MEDIUM : CellBorderType.THIN,
                    Color.getBlack());
            range.setOutlineBorder(BorderType.TOP_BORDER,
                    bold ? CellBorderType.MEDIUM : CellBorderType.THIN,
                    Color.getBlack());
            range.setOutlineBorder(BorderType.RIGHT_BORDER,
                    bold ? CellBorderType.MEDIUM : CellBorderType.THIN,
                    Color.getBlack());
            range.setOutlineBorder(BorderType.BOTTOM_BORDER,
                    bold ? CellBorderType.MEDIUM : CellBorderType.THIN,
                    Color.getBlack());
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
            schedule.classesCount = node.getNode("classes").getInt();

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
