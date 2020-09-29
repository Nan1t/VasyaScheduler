package ru.nanit.vasyascheduler.services.conversion;

import com.aspose.cells.*;
import ru.nanit.vasyascheduler.api.util.Logger;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.List;

public final class XlsToImage {

    private final ImageOrPrintOptions options;
    private final Workbook workbook;
    private final int sheetIndex;

    public XlsToImage(Workbook workbook){
        this(workbook, 0);
    }

    public XlsToImage(Workbook workbook, int sheetIndex){
        this.workbook = workbook;
        this.sheetIndex = sheetIndex;

        options = new ImageOrPrintOptions();
        options.setHorizontalResolution(80);
        options.setVerticalResolution(80);
        options.setQuality(100);
        options.setOnePagePerSheet(true);
        options.setOutputBlankPageWhenNothingToPrint(true);
        options.setImageFormat(ImageFormat.getJpeg());
    }

    public XlsToImage resolution(int value){
        options.setHorizontalResolution(value);
        options.setVerticalResolution(value);
        return this;
    }

    public XlsToImage format(ImageFormat format){
        options.setImageFormat(format);
        return this;
    }

    /**
     * Generate image from document
     * @return BufferedImage with generated sheet
     */
    public BufferedImage generate(){
        try {
            Worksheet sheet = workbook.getWorksheets().get(sheetIndex);

            if(sheet != null && sheet.getCells().getCount() > 0){
                SheetRender sr = new SheetRender(sheet, options);
                ByteArrayOutputStream output = new ByteArrayOutputStream();
                sr.toImage(0, output);
                workbook.dispose();
                return ImageIO.read(new ByteArrayInputStream(output.toByteArray()));
            }

            Logger.error("Cannot convert table to image. Table is empty");
        } catch (Exception e) {
            Logger.error("Cannot convert workbook to image: ", e);
        }

        return null;
    }

    /**
     * Generate images for every sheet of document
     * @return List with generated images
     */
    public List<BufferedImage> generateAll() {
        try {
            List<BufferedImage> images = new ArrayList<>();
            WorksheetCollection sheets = workbook.getWorksheets();

            for (int i = 0; i < sheets.getCount(); i++) {
                Worksheet sheet = sheets.get(i);

                if(sheet != null){
                    if (sheet.getCells().getCount() > 0 || sheet.getCharts().getCount() > 0 || sheet.getPictures().getCount() > 0) {
                        SheetRender sr = new SheetRender(sheet, options);
                        ByteArrayOutputStream output = new ByteArrayOutputStream();
                        sr.toImage(i, output);
                        ByteArrayInputStream input = new ByteArrayInputStream(output.toByteArray());
                        images.add(ImageIO.read(input));
                    }
                }
            }

            return images;
        } catch (Exception e) {
            Logger.error("Cannot convert workbooks to images collection: ", e);
        }

        return null;
    }

    private static BufferedImage removeWatermark(BufferedImage image){
        for(int x = 0; x < image.getWidth(); x++){
            for(int y = 0; y < 50; y++){
                image.setRGB(x, y, 0xFFFFFF);
            }
        }
        return image;
    }
}
