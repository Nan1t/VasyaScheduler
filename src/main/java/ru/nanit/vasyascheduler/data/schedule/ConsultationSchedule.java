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
import java.util.HashMap;
import java.util.Map;

public class ConsultationSchedule extends Schedule {

    private ParseData parseData;
    private Builder builder;
    private Map<Person, Week> weeks;

    private ConsultationSchedule(){
        this(null, 0);
    }

    private ConsultationSchedule(URL link, int sheet){
        super(link, sheet);
        this.builder = new Builder();
    }

    public Week getTeacherWeek(Person teacher){
        return weeks.get(teacher);
    }

    public Builder getBuilder(){
        return builder;
    }

    @Override
    public void parse() throws Exception {
        if(weeks != null){
            weeks.clear();
        }

        weeks = new HashMap<>();

        parseImage();

        try (InputStream stream = getLink().openStream()) {
            Workbook workbook = new Workbook(stream);
            Worksheet worksheet = workbook.getWorksheets().get(getSheet());
            Cells cells = worksheet.getCells();

            int maxRow = cells.getMaxDataRow();

            for (int i = parseData.getSubjectPoint().getRow(); i <= maxRow; i++){
                Cell teacherCell = cells.get(i, parseData.getSubjectPoint().getColumn());
                Person teacher = Person.parseFromString(teacherCell.getStringValue());

                if(teacher != null){
                    Week week = new Week();
                    Cell dayCell = cells.get(parseData.getDayPoint().getRow(), parseData.getDayPoint().getColumn());
                    int col = parseData.getDayPoint().getColumn();

                    while (dayCell.getType() != CELL_TYPE_NULL){
                        Cell timeCell = cells.get(i, dayCell.getColumn());

                        if(timeCell.getType() != CELL_TYPE_NULL){
                            int day = Days.getDayNumber(dayCell.getStringValue());
                            week.addConsultTime(day, timeCell.getStringValue());
                        }

                        dayCell = cells.get(parseData.getDayPoint().getRow(), ++col);
                    }

                    weeks.put(teacher, week);
                }
            }
        }
    }

    private final class Week {

        private Map<Integer, String> days = new HashMap<>();

        public String getConsultTime(int day){
            return days.get(day);
        }

        public void addConsultTime(int day, String time){
            days.put(day, time);
        }
    }

    /**
     * Builder can assembly an unique consultations schedule for each teacher
     */
    public final class Builder {

        private Builder(){}

        public BufferedImage build(Language lang, Person teacher){
            Week week = getTeacherWeek(teacher);

            if(week != null){
                Workbook workbook = new Workbook();
                Worksheet sheet = workbook.getWorksheets().get(0);
                Cells cells = sheet.getCells();

                buildHeader(cells, lang, teacher);

                int row = 2;
                for (int day = 1; day <= Days.getDaysCount(); day++){
                    String time = week.getConsultTime(day);

                    if(time != null){
                        Cell dayCell = cells.get(row, 0);
                        Cell timeCell = cells.get(row, 1);
                        Style style = new Style();

                        style.getFont().setSize(16);
                        style.setHorizontalAlignment(TextAlignmentType.CENTER);
                        style.setBorder(BorderType.LEFT_BORDER, CellBorderType.THIN, Color.getBlack());
                        style.setBorder(BorderType.TOP_BORDER, CellBorderType.THIN, Color.getBlack());
                        style.setBorder(BorderType.RIGHT_BORDER, CellBorderType.THIN, Color.getBlack());
                        style.setBorder(BorderType.BOTTOM_BORDER, CellBorderType.THIN, Color.getBlack());

                        dayCell.setStyle(style);
                        timeCell.setStyle(style);

                        dayCell.putValue(Days.getDayName(day));
                        timeCell.putValue(time);

                        row++;
                    }
                }

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

        private void buildHeader(Cells cells, Language lang, Person teacher){
            Cell teacherCell = cells.get(0, 0);
            Cell day = cells.get(1, 0);
            Cell time = cells.get(1, 1);

            // Setup content
            teacherCell.setValue(teacher.toString());
            day.setValue(lang.of("schedule.built.header.day"));
            time.setValue(lang.of("schedule.built.header.time"));

            // Setup styles
            Style style = new Style();
            style.getFont().setSize(18);
            style.getFont().setBold(true);
            style.setBorder(BorderType.LEFT_BORDER, CellBorderType.THIN, Color.getBlack());
            style.setBorder(BorderType.TOP_BORDER, CellBorderType.THIN, Color.getBlack());
            style.setBorder(BorderType.RIGHT_BORDER, CellBorderType.THIN, Color.getBlack());
            style.setBorder(BorderType.BOTTOM_BORDER, CellBorderType.THIN, Color.getBlack());
            style.setHorizontalAlignment(TextAlignmentType.CENTER);
            style.setVerticalAlignment(TextAlignmentType.CENTER);

            teacherCell.setStyle(style);
            cells.get(0, 1).setStyle(style); // Set border to merged cell
            day.setStyle(style);
            time.setStyle(style);

            // Setup sizes
            cells.setColumnWidthInch(0, 1.9d);
            cells.setColumnWidthInch(1, 1.9d);

            cells.setRowHeightInch(0, 0.38d);
            cells.setRowHeightInch(1, 0.38d);

            cells.merge(0, 0, 1, 2);
        }
    }

    private static final class ParseData {

        private SheetPoint dayPoint, subjectPoint;

        SheetPoint getDayPoint() {
            return dayPoint;
        }

        SheetPoint getSubjectPoint() {
            return subjectPoint;
        }
    }

    public static final class Serializer implements TypeSerializer<ConsultationSchedule>{

        @Override
        public ConsultationSchedule deserialize(TypeToken<?> type, ConfigurationNode node) throws ObjectMappingException {
            ConsultationSchedule schedule = new ConsultationSchedule();
            schedule.setLink(node.getNode("link").getString());
            schedule.setSheet(node.getNode("sheet").getInt());

            ParseData parseData = new ParseData();
            parseData.dayPoint = new SheetPoint(node.getNode("parseData", "day", "col").getInt(),
                    node.getNode("parseData", "day", "row").getInt());
            parseData.subjectPoint = new SheetPoint(node.getNode("parseData", "subject", "col").getInt(),
                    node.getNode("parseData", "subject", "row").getInt());

            schedule.parseData = parseData;

            return schedule;
        }

        @Override
        public void serialize(TypeToken<?> type, ConsultationSchedule obj, ConfigurationNode value) throws ObjectMappingException {

        }
    }
}

