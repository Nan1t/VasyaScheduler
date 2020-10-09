package ru.nanit.vasyascheduler.services;

import com.aspose.cells.*;
import com.google.common.reflect.TypeToken;
import ru.nanit.vasyascheduler.api.storage.Configuration;
import ru.nanit.vasyascheduler.api.storage.Language;
import ru.nanit.vasyascheduler.api.storage.properties.Properties;
import ru.nanit.vasyascheduler.api.util.Logger;
import ru.nanit.vasyascheduler.data.datetime.Days;
import ru.nanit.vasyascheduler.data.schedule.ConsultationSchedule;
import ru.nanit.vasyascheduler.data.schedule.StudentSchedule;
import ru.nanit.vasyascheduler.data.schedule.TeacherSchedule;
import ru.nanit.vasyascheduler.services.conversion.XlsToImage;

import java.awt.image.BufferedImage;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public final class ScheduleManager {

    private final Configuration scheduleConf;
    private final Properties properties;
    private final Language lang;

    private TeacherSchedule teacherSchedule; // Contains parsed teacher schedule
    private ConsultationSchedule consultationSchedule; // Contains parsed consultation schedule
    private final Map<String, StudentSchedule> studentSchedule = new ConcurrentHashMap<>(); // Contains all parsed tables of students schedule
    private Map<String, Map<Integer, StudentSchedule.Day>> mergedAudDays;

    public ScheduleManager(Configuration scheduleConf, Properties properties, Language lang){
        this.properties = properties;
        this.scheduleConf = scheduleConf;
        this.lang = lang;
    }

    public TeacherSchedule getTeacherSchedule(){
        return this.teacherSchedule;
    }

    public ConsultationSchedule getConsultationSchedule(){
        return consultationSchedule;
    }

    public StudentSchedule getStudentSchedule(String key){
        return this.studentSchedule.get(key);
    }

    public Map<String, StudentSchedule> getStudentSchedule(){
        return this.studentSchedule;
    }

    public void updateAllSchedule() throws Exception {
        teacherSchedule.parse();
        Logger.info("Parsed teachers schedule");
        updateStudentsSchedule();
        Logger.info("Updated (re-parsed) all schedule files from website");
    }

    public void updateStudentsSchedule() throws Exception {
        Collection<StudentSchedule> students = studentSchedule.values();

        for (StudentSchedule schedule : students){
            try{
                Logger.info("Try to parse students schedule '" + schedule.getName() + "'...");
                schedule.parse();
                Logger.info("Parsed students schedule '" + schedule.getName() + "'");
            } catch (Exception e){
                Logger.error("Cannot load schedule " + schedule.getName() + ": ", e);
            }
        }

        mergeAudDays();
        Logger.info("Updated (re-parsed) students schedule");
    }

    public void loadTeacherSchedule() {
        try{
            Logger.info("Loading teachers schedule...");
            teacherSchedule = scheduleConf.get().getNode("teachers").getValue(TypeToken.of(TeacherSchedule.class));
            teacherSchedule.parse();
            Logger.info("Teachers schedule loaded!");
        } catch (Exception e){
            Logger.error("Cannot load teacher schedule: ", e);
        }
    }

    public void loadConsultationSchedule() {
        try{
            Logger.info("Loading consultations schedule...");
            consultationSchedule = scheduleConf.get().getNode("consultations").getValue(TypeToken.of(ConsultationSchedule.class));
            consultationSchedule.parse();
            Logger.info("Consultations schedule loaded!");
        } catch (Exception e){
            Logger.error("Cannot load consultation schedule: ", e);
        }
    }

    public void loadStudentsSchedule() throws Exception {
        Logger.info("Loading students schedule...");
        List<StudentSchedule> schedule = scheduleConf.get().getNode("students").getList(TypeToken.of(StudentSchedule.class));

        for (StudentSchedule sch : schedule){
            try{
                sch.parse();
                studentSchedule.put(sch.getName(), sch);
                Logger.info("Loaded student schedule '" + sch.getName() + "'");
            } catch (Exception e){
                Logger.error("Cannot load schedule " + sch.getName() + ": ", e);
            }
        }

        mergeAudDays();
        Logger.info("All students schedule successfully loaded!");
    }

    public void loadProperties(){
        for (String key : studentSchedule.keySet()){
            properties.setIfAbsent("hash." + key, "null");
        }

        properties.setIfAbsent("hash.teachers", "null");
        properties.setIfAbsent("hash.consultations", "null");
        properties.save();
    }

    private void mergeAudDays(){
        if(mergedAudDays != null) mergedAudDays.clear();

        mergedAudDays = new ConcurrentHashMap<>();
        Collection<String> auds = getAllAuds();

        for (String aud : auds){
            Map<Integer, StudentSchedule.Day> audDays = new ConcurrentHashMap<>();

            for (StudentSchedule schedule : studentSchedule.values()){
                Map<Integer, StudentSchedule.Day> map = schedule.getAudDays(aud);

                if(map != null){
                    for (Map.Entry<Integer, StudentSchedule.Day> dayEntry : map.entrySet()){
                        for (Map.Entry<Integer, Set<StudentSchedule.Class>> classEntry : dayEntry.getValue().getAudClases().entrySet()){
                            StudentSchedule.Day day = audDays.computeIfAbsent(dayEntry.getKey(), v -> dayEntry.getValue());
                            for (StudentSchedule.Class clazz : classEntry.getValue()){
                                day.addAudClass(classEntry.getKey(), clazz);
                            }
                        }
                    }
                }
            }

            mergedAudDays.put(aud, audDays);
            Logger.info("Merged audition days of " + aud);
        }

        Logger.info("All audition days has been merged");
    }

    public BufferedImage buildAudSchedule(String aud){
        return new Builder().buildAuds(aud, lang, mergedAudDays.get(aud));
    }

    public Collection<String> getAllAuds(){
        Set<String> auds = new HashSet<>();
        Collection<StudentSchedule> students = studentSchedule.values();

        for (StudentSchedule schedule : students){
            auds.addAll(schedule.getAuds());
        }

        return auds;
    }

    public static final class Builder {

        private Builder(){}

        public BufferedImage buildAuds(String aud, Language lang, Map<Integer, StudentSchedule.Day> audDays){
            if(audDays == null) return null;

            Workbook workbook = new Workbook();
            Worksheet sheet = workbook.getWorksheets().get(0);
            Cells cells = sheet.getCells();

            buildHeader(cells, lang, aud);
            buildClasses(cells, audDays);

            try{
                AutoFitterOptions options = new AutoFitterOptions();
                options.setAutoFitMergedCells(true);
                sheet.autoFitRows(options);
            } catch (Exception e){
                Logger.error("Error while build workbook: ", e);
            }

            return new XlsToImage(workbook)
                    .format(ImageFormat.getPng())
                    .resolution(80)
                    .generate();
        }

        private void buildHeader(Cells cells, Language lang, String aud){
            Cell teacherCell = cells.get(1, 0);
            Cell day = cells.get(2, 0);
            Cell classNum = cells.get(2, 1);
            Cell time = cells.get(2, 2);
            Cell classes = cells.get(2, 3);

            // Setup content
            teacherCell.setValue(String.format(lang.of("schedule.auds.header"), aud));
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

            cells.merge(1, 0, 1, 4);

            setOutlineBorder(teacherCell, true);
            setOutlineBorder(day, true);
            setOutlineBorder(classNum, true);
            setOutlineBorder(time, true);
            setOutlineBorder(classes, true);
        }

        private void buildClasses(Cells cells, Map<Integer, StudentSchedule.Day> audDays){
            int dayRow = 3;
            int classRow = 3;
            boolean addRow;

            // Iterate every day
            for (Map.Entry<Integer, StudentSchedule.Day> audDay : audDays.entrySet()){
                Cell dayCell = cells.get(dayRow, 0);
                int day = audDay.getKey();
                int rows = 0;

                // Iterate every class number in day
                for (int classNum : audDay.getValue().getAudClases().keySet()){
                    Cell classNumCell = cells.get(classRow, 1);
                    Cell timeCell = cells.get(classRow, 2);

                    classNumCell.setValue(classNum);
                    timeCell.setValue(audDay.getValue().getClassTime(classNum));

                    setCentered(classNumCell);
                    setDefaultFont(classNumCell);
                    setCentered(timeCell);
                    setDefaultFont(timeCell);

                    cells.merge(classRow, 1, 2, 1); // Merge class num cell
                    cells.merge(classRow, 2, 2, 1); // Merge time cell

                    setOutlineBorder(classNumCell, true);
                    setOutlineBorder(timeCell, true);

                    classRow += 2;
                    addRow = false;

                    Cell nameCell = cells.get(classNumCell.getRow(), 3);
                    Cell groupsCell = cells.get(classNumCell.getRow()+1, 3);

                    setCentered(nameCell);
                    setCentered(groupsCell);

                    setDefaultFont(nameCell);
                    setDefaultFont(groupsCell);

                    setBorder(nameCell, BorderType.RIGHT_BORDER, true);
                    setBorder(groupsCell, BorderType.RIGHT_BORDER, true);

                    cells.setRowHeightInch(classNumCell.getRow(), 0.26d);
                    cells.setRowHeightInch(classNumCell.getRow()+1, 0.26d);

                    Set<StudentSchedule.Class> classes = audDay.getValue().getAudClases().get(classNum);

                    if(classes != null){
                        for (StudentSchedule.Class clazz : classes){
                            String teacher = clazz.getName() + " (" + clazz.getTeacher() + ")";

                            if(nameCell.getType() == 3){
                                nameCell.setValue(teacher);
                                groupsCell.setValue(clazz.getGroups().toString());
                            } else {
                                nameCell.setValue(nameCell.getValue() + ", " + teacher);
                                groupsCell.setValue(groupsCell.getValue() + ", " + clazz.getGroups().toString());
                            }
                        }
                        addRow = true;
                    }

                    if(addRow){
                        setBorder(groupsCell, BorderType.BOTTOM_BORDER);
                        rows++;
                    }
                }

                if(rows > 0){
                    cells.merge(dayRow, 0, rows * 2, 1); // Merge day cell
                    dayRow += rows * 2;
                    dayCell.setValue(Days.getDayName(day));
                    setHeaderStyle(dayCell);
                    setOutlineBorder(dayCell, true);
                }
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

}
