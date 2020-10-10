package ru.nanit.vasyascheduler.data.schedule;

import com.aspose.cells.Workbook;
import ru.nanit.vasyascheduler.api.util.Logger;
import ru.nanit.vasyascheduler.services.conversion.XlsToImage;

import java.awt.image.BufferedImage;
import java.net.MalformedURLException;
import java.net.URL;

public abstract class Schedule {

    protected static final int CELL_TYPE_NULL = 3;

    private URL link;
    private int sheet;
    private BufferedImage image;

    public Schedule(){ }

    public Schedule(URL link, int sheet){
        this.link = link;
        this.sheet = sheet;
    }

    public URL getLink(){
        return link;
    }

    public int getSheet(){
        return sheet;
    }

    public void setLink(String link){
        try {
            this.link = new URL(link);
        } catch (MalformedURLException e){
            Logger.error("Schedule URL parse error: ", e);
        }
    }

    public void setSheet(int sheet){
        this.sheet = sheet;
    }

    public abstract void parse() throws Exception;

    public void parseImage(Workbook workbook){
        image = new XlsToImage(workbook, getSheet())
                .resolution(180)
                .generate();
    }

    public BufferedImage toImage(){
        return image;
    }
}
