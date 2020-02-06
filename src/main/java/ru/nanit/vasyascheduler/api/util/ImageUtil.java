package ru.nanit.vasyascheduler.api.util;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

public final class ImageUtil {

    public ImageUtil(){}

    public static InputStream createInputStream(BufferedImage image, String format) throws IOException {
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        ImageIO.write(image, format, os);
        return new ByteArrayInputStream(os.toByteArray());
    }

}
