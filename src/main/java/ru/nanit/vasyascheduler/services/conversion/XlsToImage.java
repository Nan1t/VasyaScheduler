package ru.nanit.vasyascheduler.services.conversion;

import com.aspose.cells.*;
import com.sun.imageio.plugins.png.PNGImageReader;
import ru.nanit.vasyascheduler.api.util.Logger;
import sun.awt.image.PNGImageDecoder;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.awt.image.Raster;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.List;

public final class XlsToImage {

    private static final ImageOrPrintOptions IMG_OPTIONS;

    static{
        IMG_OPTIONS = new ImageOrPrintOptions();
        IMG_OPTIONS.setHorizontalResolution(150);
        IMG_OPTIONS.setVerticalResolution(150);
        IMG_OPTIONS.setQuality(100);
        IMG_OPTIONS.setOnePagePerSheet(true);
        IMG_OPTIONS.setOutputBlankPageWhenNothingToPrint(true);
        IMG_OPTIONS.setImageFormat(ImageFormat.getJpeg());

        ImageIO.setUseCache(false);
    }

    private Workbook workbook;
    private int sheetIndex;

    public XlsToImage(Workbook workbook){
        this(workbook, 0);
    }

    public XlsToImage(Workbook workbook, int sheetIndex){
        this.workbook = workbook;
        this.sheetIndex = sheetIndex;
    }

    public XlsToImage format(ImageFormat format){
        IMG_OPTIONS.setImageFormat(format);
        return this;
    }

    /**
     * Generate image from document
     * @return BufferedImage with generated sheet
     */
    public BufferedImage generate(){
        try {
            Worksheet sheet = workbook.getWorksheets().get(sheetIndex);

            if(sheet != null){
                if (sheet.getCells().getCount() > 0) {
                    SheetRender sr = new SheetRender(sheet, IMG_OPTIONS);
                    ByteArrayOutputStream output = new ByteArrayOutputStream();
                    sr.toImage(0, output);

                    BufferedImage image = ImageIO.read(new ByteArrayInputStream(output.toByteArray()));

                    return image;
                }
            }

            System.out.println("[Image] Error");
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
                        SheetRender sr = new SheetRender(sheet, IMG_OPTIONS);
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
