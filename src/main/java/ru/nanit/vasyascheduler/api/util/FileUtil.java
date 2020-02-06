package ru.nanit.vasyascheduler.api.util;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

public final class FileUtil {

    private static final String EVAL_PATTERN = "Evaluation Only. Created with Aspose.Cells for Java";
    private static Path root;

    public FileUtil(){}

    public static void setRootPath(Path path){
        root = path;
    }

    public static Path getFromURL(URL url){
        return getFromURL(url, "xls");
    }

    public static Path getFromURL(URL url, String extension){
        try {
            UUID uuid = UUID.randomUUID();
            Path temp = Paths.get(root + File.separator + "temp_" + uuid + "." + extension);
            Files.copy(url.openStream(), temp);
            return temp;
        } catch (IOException e){
            Logger.error("Error while creating temp file: " + e.getMessage());
            return null;
        }
    }

    public static String getExtension(Path file){
        String name = file.toFile().getName();
        String[] arr = name.split("\\.");
        return arr[arr.length-1];
    }
}
